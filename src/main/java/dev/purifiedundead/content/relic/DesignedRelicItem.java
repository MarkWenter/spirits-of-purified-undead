package dev.purifiedundead.content.relic;

import dev.purifiedundead.PurifiedUndead;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import java.util.List;

public final class DesignedRelicItem extends WhiteWitchRelicItem {
    public final RelicKind kind;
    public DesignedRelicItem(RelicKind kind) { super(new Properties()); this.kind = kind; }
    @Override public ResourceLocation relicId() {
        return ResourceLocation.fromNamespaceAndPath(PurifiedUndead.MOD_ID, kind.id);
    }
    @Override public void appendHoverText(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.purified_undead." + kind.id + ".summary").withStyle(ChatFormatting.GRAY));
    }
}
