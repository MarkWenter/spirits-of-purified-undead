package dev.purifiedundead.content.item;

import dev.purifiedundead.progress.ContractProgressService;
import dev.purifiedundead.progress.WhiteWitchTalisman;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Four spirits are consumed after a completed use to raise the built-in talisman by one level. */
public final class BlightedSpiritItem extends Item {
    public static final int USE_TICKS = 40;

    public BlightedSpiritItem() {
        super(new Item.Properties().stacksTo(64));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return ItemUtils.startUsingInstantly(level, player, hand);
        }
        if (!ContractProgressService.hasContract(serverPlayer)) {
            player.displayClientMessage(Component.translatable("message.purified_undead.talisman.no_contract"), true);
            return InteractionResultHolder.fail(stack);
        }
        if (ContractProgressService.talismanLevel(serverPlayer) >= WhiteWitchTalisman.maxLevel()) {
            player.displayClientMessage(Component.translatable("message.purified_undead.talisman.max"), true);
            return InteractionResultHolder.fail(stack);
        }
        if (!player.getAbilities().instabuild && stack.getCount() < WhiteWitchTalisman.spiritsPerLevel()) {
            player.displayClientMessage(Component.translatable("message.purified_undead.talisman.insufficient",
                    WhiteWitchTalisman.spiritsPerLevel()), true);
            return InteractionResultHolder.fail(stack);
        }
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide() && entity instanceof ServerPlayer player
                && (player.getAbilities().instabuild || stack.getCount() >= WhiteWitchTalisman.spiritsPerLevel())
                && ContractProgressService.upgradeTalisman(player)) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(WhiteWitchTalisman.spiritsPerLevel());
            }
            int currentLevel = ContractProgressService.talismanLevel(player);
            player.displayClientMessage(Component.translatable("message.purified_undead.talisman.upgraded",
                    currentLevel, WhiteWitchTalisman.maxLevel(),
                    String.format(java.util.Locale.ROOT, "%.0f%%",
                            WhiteWitchTalisman.incomingDamageMultiplier(currentLevel) * 100.0F)), false);
        }
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.purified_undead.blighted_spirit.summary")
                .withStyle(ChatFormatting.DARK_PURPLE));
        lines.add(Component.translatable("item.purified_undead.blighted_spirit.use",
                WhiteWitchTalisman.spiritsPerLevel()).withStyle(ChatFormatting.GRAY));
    }
}
