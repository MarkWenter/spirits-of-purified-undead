package dev.purifiedundead.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.purifiedundead.PurifiedUndead;
import dev.purifiedundead.purification.BlightedGuardianItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.RenderUtils;

public final class BlightedGuardianRenderer extends GeoItemRenderer<BlightedGuardianItem> {
    private static final ResourceLocation SWORD =
            ResourceLocation.fromNamespaceAndPath(PurifiedUndead.MOD_ID, "blighted_guardian");
    private static final String VORTEX = "vortex";

    public BlightedGuardianRenderer() {
        super(new DefaultedItemGeoModel<BlightedGuardianItem>(SWORD).withAltTexture(SWORD.withSuffix("_hand")));
    }

    @Override
    public long getInstanceId(BlightedGuardianItem sword) {
        // The decorative orbit uses one clock for every stack, including stacks with external animation IDs.
        return 0L;
    }

    @Override
    public void renderRecursively(
            PoseStack pose, BlightedGuardianItem sword, GeoBone bone, RenderType type,
            MultiBufferSource buffers, VertexConsumer vertices, boolean reRender, float partialTick,
            int light, int overlay, float red, float green, float blue, float alpha) {
        if (!VORTEX.equals(bone.getName()))
            super.renderRecursively(pose, sword, bone, type, buffers, vertices, reRender,
                    partialTick, light, overlay, red, green, blue, alpha);
    }

    @Override
    public void postRender(
            PoseStack pose, BlightedGuardianItem sword, BakedGeoModel model, MultiBufferSource buffers,
            VertexConsumer vertices, boolean reRender, float partialTick, int light, int overlay,
            float red, float green, float blue, float alpha) {
        super.postRender(pose, sword, model, buffers, vertices, reRender, partialTick, light, overlay, red, green, blue, alpha);
        if (reRender) return;
        GeoBone vortex = model.getBone(VORTEX).orElse(null);
        if (vortex == null) return;

        // Submit the translucent subtree only after the steel and its foil have finished using their buffers.
        pose.pushPose();
        try {
            if (applyVisibleParents(pose, vortex.getParent())) {
                RenderType translucent = RenderType.entityTranslucentCull(getTextureLocation(sword));
                super.renderRecursively(pose, sword, vortex, translucent, buffers, buffers.getBuffer(translucent),
                        true, partialTick, light, overlay, red, green, blue, alpha);
            }
        } finally {
            pose.popPose();
        }
    }

    private static boolean applyVisibleParents(PoseStack pose, GeoBone parent) {
        if (parent == null) return true;
        if (parent.isHidingChildren() || !applyVisibleParents(pose, parent.getParent())) return false;
        RenderUtils.prepMatrixForBone(pose, parent);
        return true;
    }
}
