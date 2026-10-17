package dev.purifiedundead.purification;

public final class LotusBlocks {
    private static final net.minecraftforge.registries.DeferredRegister<
                    net.minecraft.world.level.block.Block>
            BLOCKS =
                    net.minecraftforge.registries.DeferredRegister.create(
                            net.minecraft.core.registries.Registries.BLOCK, "purified_undead");
    public static final net.minecraftforge.registries.RegistryObject<
                    net.minecraft.world.level.block.Block>
            WHITE = BLOCKS.register("white_lotus", () -> new LotusBlock(false));
    public static final net.minecraftforge.registries.RegistryObject<
                    net.minecraft.world.level.block.Block>
            SCARLET = BLOCKS.register("scarlet_lotus", () -> new LotusBlock(true));

    public static void register(net.minecraftforge.eventbus.api.IEventBus bus) {
        BLOCKS.register(bus);
    }

    private LotusBlocks() {}
}
