package dev.purifiedundead.client;

import dev.purifiedundead.PurifiedUndead;
import dev.purifiedundead.foundry.PurificationFoundryBlock;
import dev.purifiedundead.foundry.PurificationFoundryEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;

public final class PurificationFoundryModel extends DefaultedBlockGeoModel<PurificationFoundryEntity> {
    private static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(PurifiedUndead.MOD_ID, "purification_foundry");
    private static final ResourceLocation LIT =
            ResourceLocation.fromNamespaceAndPath(PurifiedUndead.MOD_ID, "textures/block/purification_foundry_lit.png");

    public PurificationFoundryModel() {
        super(ID);
    }

    @Override
    public ResourceLocation getTextureResource(PurificationFoundryEntity foundry) {
        return foundry.getBlockState().getValue(PurificationFoundryBlock.LIT)
                ? LIT : super.getTextureResource(foundry);
    }
}
