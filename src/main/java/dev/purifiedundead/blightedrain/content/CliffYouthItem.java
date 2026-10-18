package dev.purifiedundead.blightedrain.content;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.Level;

public final class CliffYouthItem extends Item {
    public CliffYouthItem() {
        super(new Properties().durability(10));
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment != Enchantments.INFINITY_ARROWS
                && enchantment.canEnchant(new ItemStack(Items.BOW));
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return EnchantmentHelper.getEnchantments(book).keySet().stream()
                .allMatch(e -> canApplyAtEnchantingTable(stack, e));
    }

    public static int enchantment(
            ItemStack stack,
            Level level,
            net.minecraft.world.item.enchantment.Enchantment enchantment) {
        return stack.getEnchantmentLevel(enchantment);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (stack.getDamageValue() >= stack.getMaxDamage())
            return InteractionResultHolder.fail(stack);
        if (level instanceof net.minecraft.server.level.ServerLevel server) {
            var projectile = new CliffYouthProjectile(RainItems.YOUTH_PROJECTILE.get(), server);
            projectile.configure(player, stack);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, 1.5F, 1);
            if (!server.addFreshEntity(projectile)) return InteractionResultHolder.fail(stack);
            if (!player.getAbilities().instabuild) {
                stack.hurt(
                        1,
                        player.getRandom(),
                        player instanceof net.minecraft.server.level.ServerPlayer sp ? sp : null);
            }
            player.getCooldowns().addCooldown(this, 10);
            server.playSound(
                    null,
                    player.blockPosition(),
                    net.minecraft.sounds.SoundEvents.SNOWBALL_THROW,
                    net.minecraft.sounds.SoundSource.PLAYERS,
                    .5F,
                    .8F);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void inventoryTick(
            ItemStack stack, Level level, Entity owner, int slot, boolean selected) {
        // Batch carried-time accounting to once per second, avoiding per-tick stack sync.
        if (level.isClientSide
                || !(owner instanceof Player)
                || !stack.isDamaged()
                || Math.floorMod(owner.tickCount + slot, 20) != 0) return;
        var tag = stack.getOrCreateTag();
        int ticks = Math.max(0, Math.min(599, tag.getInt("YouthRecoveryTicks"))) + 20;
        if (ticks >= 600) {
            stack.setDamageValue(Math.max(0, stack.getDamageValue() - 1));
            ticks = 0;
        }
        tag.putInt("YouthRecoveryTicks", ticks);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Level context,
            java.util.List<net.minecraft.network.chat.Component> lines,
            TooltipFlag flag) {
        lines.add(
                net.minecraft.network.chat.Component.translatable(
                                "item.purified_undead.cliff_village_youth.desc")
                        .withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
