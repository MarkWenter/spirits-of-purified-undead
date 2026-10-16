package dev.purifiedundead.client;

public final class CliffYouthRenderer
        extends net.minecraft.client.renderer.entity.EntityRenderer<
                dev.purifiedundead.blightedrain.content.CliffYouthProjectile> {
    private final net.minecraft.client.renderer.block.BlockRenderDispatcher blocks;

    public CliffYouthRenderer(
            net.minecraft.client.renderer.entity.EntityRendererProvider.Context context) {
        super(context);
        blocks = context.getBlockRenderDispatcher();
    }

    @Override
    public net.minecraft.resources.ResourceLocation getTextureLocation(
            dev.purifiedundead.blightedrain.content.CliffYouthProjectile entity) {
        return net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS;
    }

    @Override
    public void render(
            dev.purifiedundead.blightedrain.content.CliffYouthProjectile entity,
            float yaw,
            float partial,
            com.mojang.blaze3d.vertex.PoseStack pose,
            net.minecraft.client.renderer.MultiBufferSource buffers,
            int light) {
        pose.pushPose();
        pose.scale(.5F, .5F, .5F);
        pose.translate(-.5, 0, -.5);
        blocks.renderSingleBlock(
                net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),
                pose,
                buffers,
                light,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        pose.popPose();
        super.render(entity, yaw, partial, pose, buffers, light);
    }
}
