package dev.purifiedundead.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.purifiedundead.PurifiedUndead;
import dev.purifiedundead.purification.BlightedGuardianItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public final class BlightedGuardianRenderer extends GeoItemRenderer<BlightedGuardianItem> {
    private static final ResourceLocation SWORD =
            ResourceLocation.fromNamespaceAndPath(PurifiedUndead.MOD_ID, "blighted_guardian");

    public BlightedGuardianRenderer() {
        super(new DefaultedItemGeoModel<BlightedGuardianItem>(SWORD).withAltTexture(SWORD.withSuffix("_hand")));
    }

    @Override
    public void renderRecursively(
            PoseStack pose, BlightedGuardianItem sword, GeoBone bone, RenderType type,
            MultiBufferSource buffers, VertexConsumer vertices, boolean reRender, float partialTick,
            int light, int overlay, float red, float green, float blue, float alpha) {
        // The vortex is the last bone. The steel has already written depth and received its foil pass.
        if ("vortex".equals(bone.getName())) {
            type = RenderType.entityTranslucentCull(getTextureLocation(sword));
            vertices = buffers.getBuffer(type);
        }
        super.renderRecursively(pose, sword, bone, type, buffers, vertices, reRender,
                partialTick, light, overlay, red, green, blue, alpha);
    }
}
