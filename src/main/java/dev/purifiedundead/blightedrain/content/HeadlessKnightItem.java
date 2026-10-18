package dev.purifiedundead.blightedrain.content;

import net.minecraft.world.item.*;

public final class HeadlessKnightItem extends ShieldItem {
    public HeadlessKnightItem() {
        super(new Properties().durability(336));
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack ingredient) {
        return ingredient.is(net.minecraft.tags.ItemTags.PLANKS)
                || super.isValidRepairItem(stack, ingredient);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            net.minecraft.world.level.Level context,
            java.util.List<net.minecraft.network.chat.Component> lines,
            TooltipFlag flag) {
        lines.add(
                net.minecraft.network.chat.Component.translatable(
                                "item.purified_undead.headless_knight.desc")
                        .withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
