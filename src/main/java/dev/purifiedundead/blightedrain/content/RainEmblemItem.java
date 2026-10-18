package dev.purifiedundead.blightedrain.content;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

public final class RainEmblemItem extends Item {
    public RainEmblemItem() {
        super(new Properties().durability(20));
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 40;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level, Player player, InteractionHand hand) {
        return net.minecraft.world.item.ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (level instanceof net.minecraft.server.level.ServerLevel server
                && entity instanceof Player player) {
            if (dev.purifiedundead.blightedrain.RainController.forceStart(server)) {
                if (!player.getAbilities().instabuild) {
                    stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(p.getUsedItemHand()));
                }
            } else
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable(
                                "message.purified_undead.emblem.unavailable"),
                        true);
        }
        return stack;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Level context,
            java.util.List<net.minecraft.network.chat.Component> lines,
            TooltipFlag flag) {
        lines.add(
                net.minecraft.network.chat.Component.translatable(
                                "item.purified_undead.blighted_rain_emblem.desc")
                        .withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
