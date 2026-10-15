package dev.purifiedundead.content;

/** Dedicated catalogue; the icon is not a gameplay item. */
public final class ModCreativeTabs {
    private static final net.minecraftforge.registries.DeferredRegister<net.minecraft.world.item.CreativeModeTab> TABS =
        net.minecraftforge.registries.DeferredRegister.create(net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB, "purified_undead");
    public static final net.minecraftforge.registries.RegistryObject<net.minecraft.world.item.CreativeModeTab> MAIN = TABS.register("main", () ->
        net.minecraft.world.item.CreativeModeTab.builder()
            .title(net.minecraft.network.chat.Component.translatable("itemGroup.purified_undead"))
            .icon(() -> new net.minecraft.world.item.ItemStack(BlightedSpawnEggs.TAB_ICON.get()))
            .build());
    public static void register(net.minecraftforge.eventbus.api.IEventBus bus) { TABS.register(bus); BlightedSpawnEggs.register(bus); }
    private ModCreativeTabs() {}
}
