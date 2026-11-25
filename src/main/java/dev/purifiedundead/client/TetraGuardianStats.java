package dev.purifiedundead.client;

import dev.purifiedundead.compat.TetraGuardianSocket;
import dev.purifiedundead.content.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.gui.stats.data.StatRegistry;
import se.mickelus.tetra.gui.stats.getter.StatGetterEffectLevel;

/** Tetra asks separately about the whole weapon, the selected slot and the selected improvement. */
public final class TetraGuardianStats extends StatGetterEffectLevel {
    private TetraGuardianStats() {
        super(TetraGuardianSocket.EFFECT);
    }

    public static void register() {
        StatRegistry.registerStatGetter(TetraGuardianSocket.KEY, json -> new TetraGuardianStats());
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(TetraGuardianStats::tooltip);
    }

    private static void tooltip(net.minecraftforge.event.entity.player.ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.is(ModItems.PURE_TOUCH.get())) {
            event.getToolTip().add(Component.translatable("purified_undead.tetra.pure_touch_socket").withStyle(ChatFormatting.GRAY));
        } else if (TetraGuardianSocket.active(stack)) {
            event.getToolTip().add(Component.translatable("purified_undead.effect.blighted_guardian.name").withStyle(ChatFormatting.DARK_AQUA));
        }
    }

    @Override
    public double getValue(Player player, ItemStack stack) {
        return TetraGuardianSocket.active(stack) ? 1 : 0;
    }

    @Override
    public double getValue(Player player, ItemStack stack, String slot) {
        return TetraGuardianSocket.active(stack) && super.getValue(player, stack, slot) > 0 ? 1 : 0;
    }

    @Override
    public double getValue(Player player, ItemStack stack, String slot, String improvement) {
        return TetraGuardianSocket.active(stack) && super.getValue(player, stack, slot, improvement) > 0 ? 1 : 0;
    }
}
