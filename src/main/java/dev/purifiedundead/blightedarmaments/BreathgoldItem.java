package dev.purifiedundead.blightedarmaments;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

import java.util.List;

public final class BreathgoldItem extends Item {
    public BreathgoldItem() {
        super(new Properties().stacksTo(BreathgoldSettings.MATERIAL_STACK));
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            net.minecraft.world.level.Level level,
            List<Component> lines,
            TooltipFlag flag) {
        lines.add(
                Component.translatable("tooltip.purified_undead.breathgold")
                        .withStyle(ChatFormatting.GRAY));
    }
}
