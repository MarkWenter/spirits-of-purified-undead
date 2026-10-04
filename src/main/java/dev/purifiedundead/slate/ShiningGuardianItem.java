package dev.purifiedundead.slate;

public final class ShiningGuardianItem extends dev.purifiedundead.content.relic.WhiteWitchRelicItem {
    public ShiningGuardianItem(){super(new Properties().rarity(net.minecraft.world.item.Rarity.EPIC));}
    @Override public net.minecraft.resources.ResourceLocation relicId(){return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("purified_undead","shining_guardian_treasure");}
    @Override public void appendHoverText(net.minecraft.world.item.ItemStack stack, net.minecraft.world.level.Level context, java.util.List<net.minecraft.network.chat.Component> lines, net.minecraft.world.item.TooltipFlag flag){
        lines.add(net.minecraft.network.chat.Component.translatable("item.purified_undead.shining_guardian_treasure.desc").withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
