package dev.purifiedundead.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.purifiedundead.entity.GuardianWaveEntity;
import java.util.EnumMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;

public final class GuardianWaveRenderer extends EntityRenderer<GuardianWaveEntity> {
    private static final double HAND_SIDE = .36, HAND_DROP = .16;
    private static final float INWARD_YAW = (float) Math.toDegrees(Math.atan2(HAND_SIDE, GuardianWaveEntity.RANGE));
    private static final float UP_PITCH = (float) -Math.toDegrees(
            Math.atan2(HAND_DROP, Math.hypot(GuardianWaveEntity.RANGE, HAND_SIDE)));

    private final EnumMap<GuardianWaveForm, GuardianWaveWeapon> weapons = new EnumMap<>(GuardianWaveForm.class);
    private final GuardianWaveWake wake = new GuardianWaveWake();

    public GuardianWaveRenderer(EntityRendererProvider.Context c) {
        super(c);
        shadowRadius = 0;
        for (var form : GuardianWaveForm.values())
            weapons.put(form, new GuardianWaveWeapon(c.getResourceManager(), form));
    }

    @Override
    public Vec3 getRenderOffset(GuardianWaveEntity wave, float partialTick) {
        return handOffset(wave.launchArm(), wave.getYRot(), wave.getXRot(), wave.visualTravel(partialTick));
    }

    static Vec3 handOffset(HumanoidArm arm, float yaw, float pitch, double travel) {
        double remaining = 1 - Mth.clamp(travel / GuardianWaveEntity.RANGE, 0, 1);
        double side = arm == HumanoidArm.RIGHT ? -HAND_SIDE : HAND_SIDE;
        // At yaw zero the player faces +Z, so their right hand is on the -X side.
        return new Vec3(side * remaining, -HAND_DROP * remaining, 0)
                .xRot(-pitch * Mth.DEG_TO_RAD).yRot(-yaw * Mth.DEG_TO_RAD);
    }

    @Override
    public void render(
            GuardianWaveEntity e,
            float yaw,
            float partial,
            PoseStack poses,
            MultiBufferSource buffers,
            int light) {
        if (GuardianWavePrediction.suppress(e) || e.ownerId() < 0) return;
        poses.pushPose();
        try {
            float scale = GuardianWaveWake.DISPLAY_SCALE;
            poses.scale(scale, scale, scale);
            poses.mulPose(Axis.YP.rotationDegrees(-e.getYRot()));
            poses.mulPose(Axis.XP.rotationDegrees(e.getXRot()));
            poses.mulPose(Axis.YP.rotationDegrees(e.launchArm() == HumanoidArm.RIGHT ? INWARD_YAW : -INWARD_YAW));
            poses.mulPose(Axis.XP.rotationDegrees(UP_PITCH));
            poses.mulPose(Axis.ZP.rotationDegrees(e.roll()));
            drawWeapon(e, partial, poses, buffers);
        } finally {
            poses.popPose();
        }
    }

    private void drawWeapon(GuardianWaveEntity wave, float partial, PoseStack poses, MultiBufferSource buffers) {
        var form = GuardianWaveForm.select(wave.ownerId(), wave.shotId(), wave.getId());
        float age = Math.min(GuardianWaveEntity.LIFETIME, wave.tickCount + partial);
        poses.pushPose();
        try {
            if (form.turns != 0) {
                poses.mulPose(Axis.XP.rotationDegrees(GuardianWaveWake.SPIN_CANT));
                poses.mulPose(Axis.YP.rotationDegrees(form.angle(age, GuardianWaveEntity.LIFETIME)));
            }
            poses.mulPose(Axis.XP.rotationDegrees(90));
            poses.mulPose(Axis.YP.rotationDegrees(GuardianWaveWake.WEAPON_TWIST));
            poses.scale(form.scale, form.scale, form.scale);
            poses.translate(0, -form.pivot, 0);
            weapons.get(form).draw(poses.last(), buffers, GuardianWaveWake.strength(age));
        } finally {
            poses.popPose();
        }
        wake.draw(poses.last().pose(), buffers, form, age);
    }

    @Override
    public boolean shouldRender(
            GuardianWaveEntity e,
            net.minecraft.client.renderer.culling.Frustum f,
            double x,
            double y,
            double z) {
        return f.isVisible(e.getBoundingBox().inflate(3));
    }

    @Override
    public ResourceLocation getTextureLocation(GuardianWaveEntity e) {
        return weapons.get(GuardianWaveForm.select(e.ownerId(), e.shotId(), e.getId())).texture;
    }
}
