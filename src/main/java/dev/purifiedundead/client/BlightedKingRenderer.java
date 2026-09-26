package dev.purifiedundead.client;

import dev.purifiedundead.entity.BlightedKingEntity;
import net.minecraft.client.renderer.entity.EvokerRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public final class BlightedKingRenderer extends EvokerRenderer<BlightedKingEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "purified_undead", "textures/entity/blighted_king.png");
    public BlightedKingRenderer(EntityRendererProvider.Context context) { super(context); }
    @Override public ResourceLocation getTextureLocation(BlightedKingEntity entity) { return TEXTURE; }
}
