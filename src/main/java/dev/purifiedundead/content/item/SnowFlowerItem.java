package dev.purifiedundead.content.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class SnowFlowerItem extends Item {
    public SnowFlowerItem() {
        super(new Item.Properties().stacksTo(16));
    }

    @Override
    public void appendHoverText(
            ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        lines.add(
                Component.translatable("item.purified_undead.snow_flower.summary")
                        .withStyle(ChatFormatting.GRAY));
    }
}
