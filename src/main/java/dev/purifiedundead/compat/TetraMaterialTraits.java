package dev.purifiedundead.compat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.IModularItem;

/** Call only when the optional Tetra mod is loaded. */
public final class TetraMaterialTraits {
    private static final ItemEffect BREATH = ItemEffect.get("purified_undead:blighted_breath");
    private static final ItemEffect SEAL = ItemEffect.get("purified_undead:immaculate");

    public static boolean immaculate(ItemStack stack) {
        return stack.getItem() instanceof IModularItem item
                && !item.isBroken(stack)
                && hasTrait(item, stack, SEAL);
    }

    public static void repairEquipment(ServerPlayer player) {
        for (ItemStack stack : player.getAllSlots()) {
            if (stack.isDamaged() && stack.getItem() instanceof IModularItem item
                    && hasTrait(item, stack, BREATH)) {
                int repair = Math.max(1, stack.getMaxDamage() / 100);
                stack.setDamageValue(Math.max(0, stack.getDamageValue() - repair));
            }
        }
    }

    private static boolean hasTrait(IModularItem item, ItemStack stack, ItemEffect effect) {
        for (var module : item.getAllModules(stack)) {
            if (module.getEffectLevel(stack, effect) > 0) return true;
        }
        return false;
    }

    private TetraMaterialTraits() {}
}
