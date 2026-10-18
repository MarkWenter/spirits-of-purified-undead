package dev.purifiedundead.client;

/** The vanilla donkey renderer keys its texture map by exact vanilla entity type. */
public final class BlightedDonkeyRenderer
        extends net.minecraft.client.renderer.entity.ChestedHorseRenderer<
                dev.purifiedundead.blightedrain.mob.BlightedDonkey> {
    public BlightedDonkeyRenderer(
            net.minecraft.client.renderer.entity.EntityRendererProvider.Context context) {
        super(context, .87F, net.minecraft.client.model.geom.ModelLayers.DONKEY);
    }

    @Override
    public net.minecraft.resources.ResourceLocation getTextureLocation(
            dev.purifiedundead.blightedrain.mob.BlightedDonkey donkey) {
        return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                "purified_undead", "textures/entity/blighted/horse/donkey.png");
    }
}
