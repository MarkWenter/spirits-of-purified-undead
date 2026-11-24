package dev.purifiedundead.client;

import com.google.gson.Gson;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Vector3f;

/** Baked once per renderer reload. Ferin's mesh and atlas are also used by his existing apparition. */
final class GuardianWaveWeapon {
    private record Face(float[][] vertices, Vector3f normal) {}

    private final List<Face> faces;
    private final RenderType material;
    final ResourceLocation texture;
    private static final RenderType LIGHT = RenderType.entityTranslucentEmissive(id("textures/effect/guardian_wave/surface_light.png"));
    private final List<Face> strikingFaces;
    private final float tileU, tileV;

    GuardianWaveWeapon(ResourceManager resources, GuardianWaveForm form) {
        boolean ferin = form == GuardianWaveForm.FERIN;
        String mesh = (ferin ? "echo/" : "guardian_wave/") + form.asset + ".json";
        texture = id("textures/effect/" + (ferin ? "" : "guardian_wave/") + form.asset + ".png");
        material = RenderType.entityTranslucentCull(texture);
        faces = load(resources, id(mesh));
        strikingFaces = faces.stream().filter(f -> {
            float[][] v = f.vertices;
            return (v[0][1] + v[1][1] + v[2][1] + v[3][1]) * .25F >= form.headStart;
        }).toList();
        tileU = (form.ordinal() % 4) * .25F;
        tileV = (form.ordinal() / 4) * .5F;
    }

    static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("purified_undead", path);
    }

    private static List<Face> load(ResourceManager resources, ResourceLocation mesh) {
        try (var reader = resources.openAsReader(mesh)) {
            float[][][] data = new Gson().fromJson(reader, float[][][].class);
            var result = new ArrayList<Face>(data.length);
            for (float[][] quad : data) {
                if (quad.length != 4) throw new IllegalArgumentException("Expected four vertices");
                for (float[] v : quad) {
                    if (v.length != 5) throw new IllegalArgumentException("Expected XYZ and UV");
                    for (float f : v) if (!Float.isFinite(f)) throw new IllegalArgumentException("Non-finite vertex");
                }
                var a = new Vector3f(quad[0]);
                var normal = new Vector3f(quad[1]).sub(a).cross(new Vector3f(quad[2]).sub(a)).normalize();
                if (!normal.isFinite()) throw new IllegalArgumentException("Degenerate face");
                result.add(new Face(quad, normal));
            }
            return List.copyOf(result);
        } catch (IOException | RuntimeException ex) {
            LogUtils.getLogger().warn("Could not load Guardian wave weapon {}", mesh, ex);
            return List.of();
        }
    }

    void draw(PoseStack.Pose pose, MultiBufferSource buffers, float strength) {
        var out = buffers.getBuffer(material);
        int alpha = Math.round(222 * strength);
        for (Face face : faces) {
            var n = face.normal;
            for (float[] v : face.vertices) {
                out.vertex(pose.pose(), v[0], v[1], v[2]).color(246, 225, 255, alpha)
                        .uv(v[3], v[4]).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                        .normal(pose.normal(), n.x, n.y, n.z).endVertex();
            }
        }
        var glow = buffers.getBuffer(LIGHT);
        for (Face face : strikingFaces) {
            var n = face.normal;
            for (float[] v : face.vertices) {
                glow.vertex(pose.pose(), v[0] + .012F*n.x, v[1] + .012F*n.y, v[2] + .012F*n.z)
                        .color(255, 255, 255, alpha).uv(tileU + v[3]*.25F, tileV + v[4]*.5F)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                        .normal(pose.normal(), n.x, n.y, n.z).endVertex();
            }
        }
    }
}
