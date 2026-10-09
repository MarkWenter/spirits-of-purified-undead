package dev.purifiedundead.foundry;

import dev.purifiedundead.PurifiedUndead;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class FoundryContent {
    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, PurifiedUndead.MOD_ID);
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, PurifiedUndead.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, PurifiedUndead.MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, PurifiedUndead.MOD_ID);
    public static final RegistryObject<Block> BLOCK =
            BLOCKS.register("purification_foundry", PurificationFoundryBlock::new);
    public static final RegistryObject<Item> ITEM =
            ITEMS.register(
                    "purification_foundry",
                    () -> new BlockItem(BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<PurificationFoundryEntity>> ENTITY =
            ENTITIES.register(
                    "purification_foundry",
                    () ->
                            BlockEntityType.Builder.of(PurificationFoundryEntity::new, BLOCK.get())
                                    .build(null));
    public static final RegistryObject<MenuType<PurificationFoundryMenu>> MENU =
            MENUS.register(
                    "purification_foundry",
                    () -> new MenuType<>(PurificationFoundryMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        ENTITIES.register(bus);
        MENUS.register(bus);
    }

    private FoundryContent() {}
}
