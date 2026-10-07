package dev.purifiedundead.compat;

import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;

/** Read the optional attribute only. Never forge a projectile damage source or replay combat events. */
public final class GuardianProjectileBonus {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("attributeslib", "arrow_damage");
    public static float multiplier(Player player) {
        var attribute = net.minecraftforge.registries.ForgeRegistries.ATTRIBUTES.getValue(ID);
        if (attribute == null || player.getAttribute(attribute) == null) return 1F;
        double value = player.getAttributeValue(attribute);
        // Respect the attribute's own range; reject non-finite values from malformed modifiers.
        return Double.isFinite(value) && value >= 0 && value <= Float.MAX_VALUE ? (float) value : 1F;
    }
    private GuardianProjectileBonus() {}
}
