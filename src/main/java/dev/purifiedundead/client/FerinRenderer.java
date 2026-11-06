package dev.purifiedundead.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.purifiedundead.entity.FerinEntity;
import dev.purifiedundead.entity.FerinSlashMesh;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

/** Initial release: only the independent slash mesh is visible; the knight rig is archived. */
public final class FerinRenderer extends EntityRenderer<FerinEntity> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("purified_undead", "textures/entity/ferin_slash_white.png");

    public FerinRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(
            FerinEntity entity,
            float yaw,
            float partialTick,
            PoseStack poses,
            MultiBufferSource buffers,
            int packedLight) {
        double age = entity.level().getGameTime() - entity.getStageStartedAt() + partialTick;
        var direction = entity.flightDirection();
        var basis = dev.purifiedundead.combat.FerinFlightMotion.basis(direction);
        poses.pushPose();
        try {
            if (entity.flightLifetime() > 0) {
                var predicted = entity.predictedFlightPosition(partialTick);
                double x = net.minecraft.util.Mth.lerp(partialTick, entity.xOld, entity.getX());
                double y = net.minecraft.util.Mth.lerp(partialTick, entity.yOld, entity.getY());
                double z = net.minecraft.util.Mth.lerp(partialTick, entity.zOld, entity.getZ());
                poses.translate(predicted.x - x, predicted.y - y, predicted.z - z);
            }
            VertexConsumer vertices =
                    buffers.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));
            Matrix4f matrix = poses.last().pose();
            for (FerinSlashMesh.Quad quad :
                    entity.flightLifetime() > 0
                            ? FerinSlashMesh.flight(
                                    entity.getStage(),
                                    Math.min(entity.flightAge() + 2 + partialTick, age),
                                    entity.flightLifetime())
                            : FerinSlashMesh.sample(entity.getStage(), age)) {
                vertex(vertices, matrix, quad.a(), basis, entity.flightLifetime() > 0);
                vertex(vertices, matrix, quad.b(), basis, entity.flightLifetime() > 0);
                vertex(vertices, matrix, quad.c(), basis, entity.flightLifetime() > 0);
                vertex(vertices, matrix, quad.d(), basis, entity.flightLifetime() > 0);
            }
        } finally {
            poses.popPose();
        }
    }

    private static void vertex(
            VertexConsumer out,
            Matrix4f matrix,
            FerinSlashMesh.Vertex v,
            dev.purifiedundead.combat.FerinFlightMotion.Basis basis,
            boolean flight) {
        double up = v.up() - (flight ? 1.05 : 0);
        double x =
                basis.side().x * v.right()
                        + basis.vertical().x * up
                        + basis.forward().x * v.forward();
        double y =
                basis.side().y * v.right()
                        + basis.vertical().y * up
                        + basis.forward().y * v.forward();
        double z =
                basis.side().z * v.right()
                        + basis.vertical().z * up
                        + basis.forward().z * v.forward();
        out.vertex(matrix, (float) x, (float) y, (float) z)
                .color(v.red(), v.green(), v.blue(), v.alpha())
                .uv(0.5F, 0.5F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(0, 1, 0)
                .endVertex();
    }

    @Override
    public boolean shouldRender(FerinEntity entity, Frustum frustum, double x, double y, double z) {
        return entity.shouldRender(x, y, z)
                && frustum.isVisible(
                        entity.getBoundingBox()
                                .minmax(
                                        new net.minecraft.world.phys.AABB(
                                                entity.predictedFlightPosition(0),
                                                entity.predictedFlightPosition(1)))
                                .inflate(12));
    }

    @Override
    public ResourceLocation getTextureLocation(FerinEntity entity) {
        return TEXTURE;
    }
}
