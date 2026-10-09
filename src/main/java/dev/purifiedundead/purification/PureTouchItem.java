package dev.purifiedundead.purification;

import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;

public final class PureTouchItem extends Item {
    public PureTouchItem() {
        super(new Properties().rarity(Rarity.EPIC).fireResistant());
    }

    @Override
    public void appendHoverText(
            ItemStack s, Level level, java.util.List<Component> lines, TooltipFlag flag) {
        lines.add(
                Component.literal("Amidst a Collapsed world, her words echo out")
                        .withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
