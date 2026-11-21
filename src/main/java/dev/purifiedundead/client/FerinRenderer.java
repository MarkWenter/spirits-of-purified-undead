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

    // Capture once per tracked visual. Weak keys retire with the client entity.
    private final java.util.Map<FerinEntity, net.minecraft.world.phys.Vec3> offsets =
            new java.util.WeakHashMap<>();

    private net.minecraft.world.phys.Vec3 launchOffset(FerinEntity entity, double age, float partial) {
        if (age > 7) return net.minecraft.world.phys.Vec3.ZERO;
        return offsets.computeIfAbsent(entity, key -> {
            var owner = key.getOwnerUuid().map(key.level()::getPlayerByUUID).orElse(null);
            var light = owner == null ? null : ContractWispClient.findFor(owner);
            if (light == null || light.isRemoved() || age > 3) return net.minecraft.world.phys.Vec3.ZERO;
            var offset = light.getPosition(partial).subtract(key.releasePosition());
            return offset.lengthSqr() <= 64 ? offset : net.minecraft.world.phys.Vec3.ZERO;
        });
    }

    private final EntityRendererProvider.Context context;
    private dev.purifiedundead.client.echo.MaidEchoRenderer echoes;

    public FerinRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.context = context;
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
        var basis = dev.purifiedundead.combat.FerinFlightMotion.basis(entity.visualHeading(partialTick));
        double x = net.minecraft.util.Mth.lerp(partialTick, entity.xOld, entity.getX());
        double y = net.minecraft.util.Mth.lerp(partialTick, entity.yOld, entity.getY());
        double z = net.minecraft.util.Mth.lerp(partialTick, entity.zOld, entity.getZ());
        boolean flight = entity.flightLifetime() > 0;
        var offset = flight ? launchOffset(entity, age, partialTick) : net.minecraft.world.phys.Vec3.ZERO;
        boolean overheadCut = false;
        if (flight && age >= 0 && age < dev.purifiedundead.combat.FerinBladeTrajectory.sweepEnd(entity.getStage()) + 2
                && !entity.echoAppearance().isEmpty() && dev.purifiedundead.content.ModItems.KNIGHT_NOCTURNE != null
                && dev.purifiedundead.config.PurifiedUndeadConfig.SHOW_ECHO.get()) {
            var origin = entity.releasePosition().add(offset);
            poses.pushPose();
            try {
                poses.translate(origin.x - x, origin.y - y, origin.z - z);
                if (echoes == null) echoes = new dev.purifiedundead.client.echo.MaidEchoRenderer(context);
                overheadCut = echoes.render(entity, age, partialTick, origin, poses, buffers)
                        && entity.getStage() == 3;
            } finally {
                poses.popPose();
            }
        }
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));
        if (flight
                && age >= 0
                && age
                        < dev.purifiedundead.combat.FerinBladeTrajectory.sweepEnd(entity.getStage())
                                + 2) {
            var origin = entity.releasePosition().add(offset);
            poses.pushPose();
            try {
                poses.translate(origin.x - x, origin.y - y, origin.z - z);
                draw(
                        vertices,
                        poses.last().pose(),
                        overheadCut ? FerinSlashMesh.overheadLaunch(age) : FerinSlashMesh.launch(entity.getStage(), age),
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
                double distance = predicted.distanceTo(entity.releasePosition());
                double blend = 1 - Math.min(1, Math.max(distance / 5, age / 6));
                predicted = predicted.add(offset.scale(Math.max(0, blend)));
                // Change heading around the forward cutting tip. Its position is
                // independent of the instantaneous rotation of the blade plane.
                double reach = dev.purifiedundead.combat.FerinBladeTrajectory.reach(entity.getStage());
                var pivotOffset = direction.subtract(basis.forward()).scale(reach);
                predicted = predicted.add(pivotOffset);
                poses.translate(predicted.x - x, predicted.y - y, predicted.z - z);
                if (entity.getStage() >= 4) {
                    // Spin within the sweep plane: the silhouette and forward reach stay fixed.
                    var axis = entity.getStage() == 5 ? basis.side()
                            : basis.side().scale(Math.sin(Math.toRadians(20)))
                                    .add(basis.vertical().scale(Math.cos(Math.toRadians(20))));
                    poses.mulPose(new org.joml.Quaternionf().rotationAxis(
                            (float)(age * (entity.getStage() == 5 ? .40 : -.48)),
                            (float)axis.x, (float)axis.y, (float)axis.z));
                }
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
            if (flight && flightAge > 0 && flightAge < entity.flightLifetime() + 1) {
                draw(vertices, poses.last().pose(),
                        FerinCrystalModel.sample(entity.getStage(), flightAge, entity.flightLifetime()),
                        basis, true, age);
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
                        .inflate(26));
    }

    @Override
    public ResourceLocation getTextureLocation(FerinEntity entity) {
        return TEXTURE;
    }
}
