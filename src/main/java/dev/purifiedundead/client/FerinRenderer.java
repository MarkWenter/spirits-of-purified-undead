package dev.purifiedundead.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.purifiedundead.entity.FerinEntity;
import dev.purifiedundead.entity.FerinSlashMesh;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import org.joml.Matrix4f;

/** Initial release: only the independent slash mesh is visible; the knight rig is archived. */
public final class FerinRenderer extends EntityRenderer<FerinEntity> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("purified_undead", "textures/entity/ferin_slash_dynamic.png");

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
        double x = net.minecraft.util.Mth.lerp(partialTick, entity.xOld, entity.getX());
        double y = net.minecraft.util.Mth.lerp(partialTick, entity.yOld, entity.getY());
        double z = net.minecraft.util.Mth.lerp(partialTick, entity.zOld, entity.getZ());
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));
        boolean flight = entity.flightLifetime() > 0;
        if (flight
                && age >= 0
                && age
                        < dev.purifiedundead.combat.FerinBladeTrajectory.sweepEnd(entity.getStage())
                                + 2) {
            var origin = entity.releasePosition();
            poses.pushPose();
            try {
                poses.translate(origin.x - x, origin.y - y, origin.z - z);
                draw(
                        vertices,
                        poses.last().pose(),
                        FerinSlashMesh.launch(entity.getStage(), age),
                        dev.purifiedundead.combat.FerinFlightMotion.basis(
                                entity.releaseDirection()),
                        true,
                        age);
            } finally {
                poses.popPose();
            }
        }
        poses.pushPose();
        try {
            if (flight) {
                var predicted = entity.predictedFlightPosition(partialTick);
                poses.translate(predicted.x - x, predicted.y - y, predicted.z - z);
            }
            double flightAge = Math.min(entity.flightAge() + 2 + partialTick, age);
            if (flight) {
                draw(
                        vertices,
                        poses.last().pose(),
                        FerinSlashEffects.trail(
                                entity.getStage(), flightAge, entity.flightLifetime()),
                        basis,
                        true,
                        age);
            }
            draw(
                    vertices,
                    poses.last().pose(),
                    flight
                            ? FerinSlashMesh.flight(
                                    entity.getStage(), flightAge, entity.flightLifetime())
                            : FerinSlashMesh.sample(entity.getStage(), age),
                    basis,
                    flight,
                    age);
        } finally {
            poses.popPose();
        }
    }

    private static void draw(
            VertexConsumer vertices,
            Matrix4f matrix,
            java.util.List<FerinSlashMesh.Quad> mesh,
            dev.purifiedundead.combat.FerinFlightMotion.Basis basis,
            boolean flight,
            double age) {
        for (var quad : mesh) {
            vertex(vertices, matrix, quad.a(), basis, flight, age);
            vertex(vertices, matrix, quad.b(), basis, flight, age);
            vertex(vertices, matrix, quad.c(), basis, flight, age);
            vertex(vertices, matrix, quad.d(), basis, flight, age);
        }
    }

    private static void vertex(
            VertexConsumer out,
            Matrix4f matrix,
            FerinSlashMesh.Vertex v,
            dev.purifiedundead.combat.FerinFlightMotion.Basis basis,
            boolean flight,
            double age) {
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
                .color(
                        v.texturedFace() ? 255 : v.red(),
                        v.texturedFace() ? 255 : v.green(),
                        v.texturedFace() ? 255 : v.blue(),
                        v.alpha())
                .uv(v.texU(), FerinSlashEffects.atlasV(v.texV(), age))
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(0, 1, 0)
                .endVertex();
    }

    @Override
    public boolean shouldRender(FerinEntity entity, Frustum frustum, double x, double y, double z) {
        if (!entity.shouldRender(x, y, z)) return false;
        if (entity.flightLifetime() > 0
                && entity.level().getGameTime() - entity.getStageStartedAt()
                        < dev.purifiedundead.combat.FerinBladeTrajectory.sweepEnd(entity.getStage())
                                + 2
                && frustum.isVisible(
                        new net.minecraft.world.phys.AABB(
                                        entity.releasePosition(), entity.releasePosition())
                                .inflate(12))) return true;
        return frustum.isVisible(
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
