package dev.purifiedundead.content;

import dev.purifiedundead.PurifiedUndead;
import dev.purifiedundead.content.enchantment.BlightCurseEnchantment;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public final class ModEnchantments {
    private static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, PurifiedUndead.MOD_ID);

    public static final List<String> BLIGHT_IDS = ContractRoster.BLIGHT_IDS;

    public static final List<RegistryObject<BlightCurseEnchantment>> BLIGHT_CURSES = BLIGHT_IDS.stream()
            .map(id -> ENCHANTMENTS.register(id, BlightCurseEnchantment::new)).toList();

    private ModEnchantments() {
    }

    public static void register(IEventBus modBus) {
        ENCHANTMENTS.register(modBus);
    }

    public static void applyAllBlightCurses(ItemStack stack) {
        for (RegistryObject<BlightCurseEnchantment> curse : BLIGHT_CURSES) {
            if (curse.isPresent() && stack.getEnchantmentLevel(curse.get()) == 0) {
                stack.enchant(curse.get(), 1);
            }
        }
        // The contract supplies complete red/gray lore for every curse; suppress the
        // vanilla name-only list so the first blight is not displayed twice.
        stack.hideTooltipPart(ItemStack.TooltipPart.ENCHANTMENTS);
    }
}
