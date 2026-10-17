package dev.purifiedundead.purification;

import net.minecraft.world.item.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.*;
import net.minecraft.world.effect.*;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public final class LotusItem extends BlockItem {
    public enum Kind {
        WHITE,
        SCARLET
    }

    private final Kind kind;

    public LotusItem(Kind kind) {
        super(
                kind == Kind.WHITE ? LotusBlocks.WHITE.get() : LotusBlocks.SCARLET.get(),
                new Properties().stacksTo(16).rarity(Rarity.RARE));
        this.kind = kind;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 30;
    }

    @Override
    public String getDescriptionId() {
        return "item.purified_undead." + (kind == Kind.WHITE ? "white_lotus" : "scarlet_lotus");
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player p, InteractionHand hand) {
        ItemStack s = p.getItemInHand(hand);
        p.startUsingItem(hand);
        return InteractionResultHolder.consume(s);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack s, Level level, LivingEntity entity) {
        if (!level.isClientSide && !s.isEmpty() && entity instanceof Player p && p.isAlive()) {
            if (kind == Kind.WHITE) p.heal(p.getMaxHealth() * .5F);
            else if (kind == Kind.SCARLET)
                p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1000, 3));
            if (!p.getAbilities().instabuild) s.shrink(1);
            p.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));
        }
        return s;
    }

    @Override
    public void appendHoverText(
            ItemStack s, Level level, java.util.List<Component> lines, TooltipFlag flag) {
        lines.add(
                Component.translatable(getDescriptionId() + ".desc")
                        .withStyle(ChatFormatting.GRAY));
    }
}
