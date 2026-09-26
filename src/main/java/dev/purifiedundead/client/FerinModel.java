package dev.purifiedundead.client;

import dev.purifiedundead.PurifiedUndead;
import dev.purifiedundead.entity.FerinEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class FerinModel extends GeoModel<FerinEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(
            PurifiedUndead.MOD_ID, "geo/ferin.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            PurifiedUndead.MOD_ID, "textures/entity/ferin.png");
    private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath(
            PurifiedUndead.MOD_ID, "animations/ferin.animation.json");

    @Override
    public ResourceLocation getModelResource(FerinEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(FerinEntity entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(FerinEntity entity) {
        return ANIMATION;
    }
}
