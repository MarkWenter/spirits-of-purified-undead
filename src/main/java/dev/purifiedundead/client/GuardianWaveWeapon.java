package dev.purifiedundead.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.purifiedundead.client.WeaponMesh.Face;
import java.util.List;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

/** Baked once per renderer reload. Ferin's mesh and atlas are also used by his existing apparition. */
final class GuardianWaveWeapon {
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
        faces = WeaponMesh.load(resources, id(mesh));
        strikingFaces = faces.stream().filter(f -> {
            float[][] v = f.vertices();
            return (v[0][1] + v[1][1] + v[2][1] + v[3][1]) * .25F >= form.headStart;
        }).toList();
        tileU = (form.ordinal() % 4) * .25F;
        tileV = (form.ordinal() / 4) * .5F;
    }

    static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("purified_undead", path);
    }

    void draw(PoseStack.Pose pose, MultiBufferSource buffers, float strength) {
        var out = buffers.getBuffer(material);
        int alpha = Math.round(222 * strength);
        for (Face face : faces) {
            var n = face.normal();
            for (float[] v : face.vertices()) {
                out.vertex(pose.pose(), v[0], v[1], v[2]).color(246, 225, 255, alpha)
                        .uv(v[3], v[4]).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                        .normal(pose.normal(), n.x, n.y, n.z).endVertex();
            }
        }
        var glow = buffers.getBuffer(LIGHT);
        for (Face face : strikingFaces) {
            var n = face.normal();
            for (float[] v : face.vertices()) {
                glow.vertex(pose.pose(), v[0] + .012F*n.x, v[1] + .012F*n.y, v[2] + .012F*n.z)
                        .color(255, 255, 255, alpha).uv(tileU + v[3]*.25F, tileV + v[4]*.5F)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                        .normal(pose.normal(), n.x, n.y, n.z).endVertex();
            }
        }
    }
}
