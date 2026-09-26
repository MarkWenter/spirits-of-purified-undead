package dev.purifiedundead.client;

import net.minecraft.client.renderer.entity.IronGolemRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.IronGolem;

public final class BlightedGolemRenderer extends IronGolemRenderer {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "purified_undead", "textures/entity/blighted_golem.png");
    public BlightedGolemRenderer(EntityRendererProvider.Context context) { super(context); }
    @Override public ResourceLocation getTextureLocation(IronGolem entity) { return TEXTURE; }
}
