package dev.purifiedundead.combat;

import dev.purifiedundead.content.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.UUID;

/** Maintains reach, sprint speed, and Hero of the Village from the Knight Captain's blight. */
public final class JuliusCombatEvents {
    private static final UUID REACH_MODIFIER_ID =
            UUID.fromString("6410e679-751b-4ed3-aa8c-63cfb3438724");
    private static final UUID SPRINT_MODIFIER_ID =
            UUID.fromString("6d14ed4a-8672-4143-9ec4-c1c2d349aa79");

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || event.player.level().isClientSide()
                || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        Equipment equipment =
                CuriosApi.getCuriosInventory(player)
                        .map(
                                handler ->
                                        new Equipment(
                                                handler.isEquipped(ModItems.ANCIENT_CONTRACT.get()),
                                                handler.isEquipped(ModItems.JULIUS_WARRIOR.get())))
                        .orElse(Equipment.NONE);

        updateReach(player, equipment);
        updateSprintSpeed(player, equipment);
        if (equipment.reversed() && player.tickCount % 20 == 0) {
            player.addEffect(
                    new MobEffectInstance(
                            MobEffects.HERO_OF_THE_VILLAGE, 40, 2, true, false, true));
        }
    }

    private static void updateReach(ServerPlayer player, Equipment equipment) {
        AttributeInstance reach = player.getAttribute(ForgeMod.ENTITY_REACH.get());
        if (reach == null) {
            return;
        }
        double amount = JuliusModel.attackReachDelta(equipment.contract, equipment.julius);
        replaceModifier(
                reach,
                REACH_MODIFIER_ID,
                "Knight Captain attack reach",
                amount,
                AttributeModifier.Operation.ADDITION);
    }

    private static void updateSprintSpeed(ServerPlayer player, Equipment equipment) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        double amount = 0.0D;
        if (equipment.contract && player.isSprinting()) {
            double multiplier = JuliusModel.sprintSpeedMultiplier(true, equipment.julius);
            amount = multiplier - 1.0D;
        }
        replaceModifier(
                speed,
                SPRINT_MODIFIER_ID,
                "Knight Captain sprint speed",
                amount,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    private static void replaceModifier(
            AttributeInstance attribute,
            UUID id,
            String name,
            double amount,
            AttributeModifier.Operation operation) {
        AttributeModifier existing = attribute.getModifier(id);
        if (existing != null
                && existing.getAmount() == amount
                && existing.getOperation() == operation) {
            return;
        }
        if (existing != null) {
            attribute.removeModifier(id);
        }
        if (amount != 0.0D) {
            attribute.addTransientModifier(new AttributeModifier(id, name, amount, operation));
        }
    }

    private record Equipment(boolean contract, boolean julius) {
        private static final Equipment NONE = new Equipment(false, false);

        boolean reversed() {
            return contract && julius;
        }
    }
}
