package dev.purifiedundead.client;

import dev.purifiedundead.content.ModItems;
import dev.purifiedundead.purification.LilyDiaryProgress;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(
        modid = "purified_undead",
        value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class LilyDiaryTooltip {
    private LilyDiaryTooltip() {}

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void tooltip(net.minecraftforge.event.entity.player.ItemTooltipEvent event) {
        if (!event.getItemStack().is(ModItems.LILY_DIARY.get())) return;
        var lines = event.getToolTip();
        int insertion = lines.size();
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).getContents() instanceof TranslatableContents contents) {
                if (contents.getKey().equals("item.purified_undead.lily_diary.current")) return;
                if (contents.getKey().equals("item.purified_undead.lily_diary.lore")) insertion = i;
            }
        }
        var player = event.getEntity();
        lines.add(
                insertion,
                Component.translatable(
                                "item.purified_undead.lily_diary.current",
                                number(LilyDiaryProgress.currentCriticalBonus(player) * 100),
                                number(LilyDiaryProgress.currentLuckBonus(player)))
                        .withStyle(ChatFormatting.GRAY));
    }

    private static String number(double value) {
        return java.math.BigDecimal.valueOf(value)
                .setScale(2, java.math.RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }
}
