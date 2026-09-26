package dev.purifiedundead.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.purifiedundead.entity.EleineMagicOrbEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/** Pixel ember surrounded by soot, with curved ribbons following actual flight history. */
public final class EleineMagicOrbRenderer extends EntityRenderer<EleineMagicOrbEntity> {
    private static final ResourceLocation WHITE = ResourceLocation.fromNamespaceAndPath(
            "purified_undead", "textures/entity/ferin_slash_white.png");

    public EleineMagicOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0;
    }

    @Override
    public void render(EleineMagicOrbEntity orb, float yaw, float partialTick, PoseStack poses,
                       MultiBufferSource buffers, int light) {
        VertexConsumer out = buffers.getBuffer(RenderType.entityTranslucentEmissive(WHITE));
        Vec3 origin = new Vec3(orb.xOld, orb.yOld, orb.zOld).lerp(orb.position(), partialTick);
        var history = orb.trailPositions();
        Matrix4f matrix = poses.last().pose();
        // Three separate tapering black strips, with a narrower crimson seam.
        for (int strip = -1; strip <= 1; strip++) {
            Vec3 previous = Vec3.ZERO;
            for (int i = 0; i < history.size(); i++) {
                double t = (i + 1.0) / (history.size() + 1.0);
                Vec3 next = history.get(i).subtract(origin);
                Vec3 tangent = next.subtract(previous);
                if (tangent.lengthSqr() < 0.00001) continue;
                Vec3 side = tangent.cross(entityRenderDispatcher.camera.getPosition().subtract(origin)).normalize();
                if (side.lengthSqr() < 0.01) side = new Vec3(0, 1, 0);
                double wave = Math.sin((orb.tickCount + partialTick) * 0.65 - i * 0.8 + strip) * 0.07 * t;
                Vec3 shift = side.scale(strip * 0.14 * t + wave);
                Vec3 a = previous.add(shift), b = next.add(shift);
                double width = 0.095 * (1 - t) + 0.008;
                ribbon(out, matrix, a, b, side, width, 18, 5, 12, (int)(230 * (1-t)));
                ribbon(out, matrix, a, b, side, width * 0.2, 115, 8, 22, (int)(150 * (1-t)));
                previous = next;
            }
        }
        poses.pushPose();
        poses.mulPose(entityRenderDispatcher.cameraOrientation());
        matrix = poses.last().pose();
        double pixel = 0.065;
        for (int x = -3; x <= 3; x++) {
            for (int y = -3; y <= 3; y++) {
                int radius = x*x + y*y;
                if (radius > 12) continue;
                int seed = Math.floorMod(x*17+y*31, 7);
                int r, g, b;
                if (radius >= 8) { r=25+seed*5; g=4; b=12; }
                else if (radius >= 3) { r=130+seed*15; g=12+seed*3; b=24; }
                else { r=245; g=60+seed*13; b=35; }
                double p = pixel * (1 + 0.035 * Math.sin((orb.tickCount+partialTick)*1.4));
                quad(out, matrix, new Vec3(x*p,y*p,0), new Vec3((x+1)*p,y*p,0),
                        new Vec3((x+1)*p,(y+1)*p,0), new Vec3(x*p,(y+1)*p,0),r,g,b,255);
            }
        }
        poses.popPose();
    }

    private static void ribbon(VertexConsumer out, Matrix4f matrix, Vec3 a, Vec3 b, Vec3 side,
                               double width, int r, int g, int blue, int alpha) {
        Vec3 w = side.scale(width);
        quad(out, matrix, a.add(w), a.subtract(w), b.subtract(w.scale(0.85)), b.add(w.scale(0.85)),
                r,g,blue,alpha);
    }

    private static void quad(VertexConsumer out, Matrix4f matrix, Vec3 a, Vec3 b, Vec3 c, Vec3 d,
                             int r, int g, int blue, int alpha) {
        vertex(out,matrix,a,r,g,blue,alpha);vertex(out,matrix,b,r,g,blue,alpha);
        vertex(out,matrix,c,r,g,blue,alpha);vertex(out,matrix,d,r,g,blue,alpha);
    }

    private static void vertex(VertexConsumer out, Matrix4f matrix, Vec3 v, int r, int g, int b, int alpha) {
        out.vertex(matrix,(float)v.x,(float)v.y,(float)v.z).color(r,g,b,alpha)
                .uv(0.5F,0.5F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880)
                .normal(0,1,0).endVertex();
    }

    @Override
    public boolean shouldRender(EleineMagicOrbEntity orb, Frustum frustum, double x, double y, double z) {
        return orb.shouldRender(x,y,z) && frustum.isVisible(orb.getBoundingBox().inflate(6));
    }

    @Override
    public ResourceLocation getTextureLocation(EleineMagicOrbEntity orb) { return WHITE; }
}
