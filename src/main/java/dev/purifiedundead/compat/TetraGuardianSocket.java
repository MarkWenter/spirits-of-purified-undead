package dev.purifiedundead.compat;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.impl.ModularBladedItem;

import java.util.Set;

/** Loaded only when Tetra is installed. Tetra owns the material, active effects and broken-item rules. */
public final class TetraGuardianSocket {
    public static final String KEY = "purified_undead:blighted_guardian";
    public static final ItemEffect EFFECT = ItemEffect.get(KEY);
    private static final TagKey<Item> MELEE = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("purified_undead", "tetra_melee_weapons"));
    private static final TagKey<Item> MIXED = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("purified_undead", "tetra_mixed_tools"));
    private static final Set<String> MELEE_HEADS = Set.of(
            "double/basic_axe", "double/basic_hammer", "double/sickle",
            "single/spearhead", "single/trident",
            "single/more_mod_tetra/mmt_flanged_mace", "single/more_mod_tetra/naginata");

    public static boolean active(ItemStack stack) {
        if (!(stack.getItem() instanceof IModularItem item) || !melee(stack, item) || item.isBroken(stack)) return false;
        // A workbench preview may still share the input's Tetra cache key. Read the installed modules.
        for (var module : item.getAllModules(stack)) {
            if (module.getEffectLevel(stack, EFFECT) > 0) return true;
        }
        return false;
    }

    private static boolean melee(ItemStack stack, IModularItem item) {
        if (stack.is(MIXED)) {
            for (var module : item.getMajorModules(stack)) {
                if (module != null && MELEE_HEADS.contains(module.getUnlocalizedName())) return true;
            }
            return false;
        }
        if (stack.is(MELEE)) {
            var blade = item.getModuleFromSlot(stack, "sword/blade");
            if (blade == null) return !stack.is(ModularBladedItem.instance); // Add-ons can use their own blade slot.
            String key = blade.getUnlocalizedName();
            return !key.equals("sword/stonecutter") && !key.equals("sword/unbound_stonecutter");
        }
        return false;
    }

    private TetraGuardianSocket() {}
}
