package dev.purifiedundead.nocturne;

import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;

public final class NocturneItem extends Item {
    public NocturneItem() {
        super(new Properties().rarity(Rarity.EPIC).stacksTo(1));
    }

    @Override
    public void appendHoverText(
            ItemStack s, Level level, java.util.List<Component> lines, TooltipFlag flag) {
        lines.add(
                Component.translatable("tooltip.purified_undead.knight_nocturne")
                        .withStyle(net.minecraft.ChatFormatting.GRAY));
    }

    public static final String KEY = "purified_undead_knight_nocturne";

    public static boolean unbind(net.minecraft.world.entity.player.Player player) {
        return NocturneLedger.get(player.getServer()).release(player.getUUID());
    }

    @Override
    public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level level,
            net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) return net.minecraft.world.InteractionResultHolder.pass(stack);
        if (!level.isClientSide) {
            var data = stack.getOrCreateTag();
            var ledger = NocturneLedger.get(player.getServer());
            var active = ledger.record(player.getUUID());
            var charm = data.getCompound(KEY);
            String message;
            if (active.hasUUID("epoch")) {
                if (active.hasUUID("id") && charm.hasUUID("id") && charm.hasUUID("epoch")
                        && active.getUUID("id").equals(charm.getUUID("id"))
                        && active.getUUID("epoch").equals(charm.getUUID("epoch"))) {
                    unbind(player);
                    charm.remove("epoch");
                    charm.remove("owner");
                    message = "unbound";
                } else {
                    message = "occupied";
                }
            } else if (charm.hasUUID("owner") && !player.getUUID().equals(charm.getUUID("owner"))
                    && ledger.record(charm.getUUID("owner")).hasUUID("epoch")
                    && charm.hasUUID("epoch") && charm.getUUID("epoch").equals(ledger.record(charm.getUUID("owner")).getUUID("epoch"))) {
                message = "foreign";
            } else {
                if (!charm.hasUUID("id")) charm.putUUID("id", java.util.UUID.randomUUID());
                charm.putUUID("owner", player.getUUID());
                charm.putUUID("epoch", java.util.UUID.randomUUID());
                ledger.bind(player.getUUID(), charm.copy());
                message = "bound";
            }
            data.put(KEY, charm);

            player.displayClientMessage(Component.translatable("item.purified_undead.knight_nocturne." + message), true);
        }
        return net.minecraft.world.InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
