package dev.purifiedundead.compat;

import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.IModularItem;

/** Loaded only when Tetra is installed. Tetra owns the material, active effects and broken-item rules. */
public final class TetraGuardianSocket {
    public static final String KEY = "purified_undead:blighted_guardian";
    public static final ItemEffect EFFECT = ItemEffect.get(KEY);

    public static boolean active(ItemStack stack) {
        if (!(stack.getItem() instanceof IModularItem item) || item.isBroken(stack)) return false;
        // A workbench preview may still share the input's Tetra cache key. Read the installed modules.
        for (var module : item.getAllModules(stack)) {
            if (module.getEffectLevel(stack, EFFECT) > 0) return true;
        }
        return false;
    }

    private TetraGuardianSocket() {}
}
