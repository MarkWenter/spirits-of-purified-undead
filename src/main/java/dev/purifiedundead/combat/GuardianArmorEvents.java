package dev.purifiedundead.combat;

import dev.purifiedundead.content.ModItems;
import dev.purifiedundead.config.PurifiedUndeadConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.UUID;

/** Expresses the Guardians' exact add-then-multiply armor formulas as transient attributes. */
public final class GuardianArmorEvents {
    /** Shift the fall-distance calculation, preserving other safe-fall bonuses and jump effects. */
    @SubscribeEvent
    public void onFallDistance(net.minecraftforge.event.entity.living.LivingFallEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && CuriosApi.getCuriosInventory(player)
                        .map(handler -> handler.isEquipped(ModItems.GUARDIAN_WARRIORS.get()))
                        .orElse(false)) {
            event.setDistance(Math.max(0.0F, event.getDistance() - 2.0F));
        }
    }

    @SubscribeEvent
    public void onFallDamage(net.minecraftforge.event.entity.living.LivingHurtEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FALL)
                && CuriosApi.getCuriosInventory(player)
                        .map(handler -> handler.isEquipped(ModItems.GUARDIAN_WARRIORS.get()))
                        .orElse(false)) {
            event.setAmount(event.getAmount() * 0.5F);
        }
    }

    private static final UUID ARMOR_ADD_ID =
            UUID.fromString("5aa846c9-70fd-46b5-8a2c-080a844a7b84");
    private static final UUID ARMOR_SCALE_ID =
            UUID.fromString("dc3e2fc2-2761-4d28-8c45-5d80e12365b2");
    private static final UUID TOUGHNESS_SCALE_ID =
            UUID.fromString("22c625bb-c107-4613-b82c-9862b9132188");

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
                                                handler.isEquipped(
                                                        ModItems.GUARDIAN_WARRIORS.get())))
                        .orElse(Equipment.NONE);
        double flat = PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.guardianArmorFlatChange);
        double configuredScale =
                PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.guardianArmorScale);
        double addition = equipment.contract ? (equipment.guardians ? flat : -flat) : 0.0D;
        double scale =
                equipment.contract
                        ? (equipment.guardians ? configuredScale : -configuredScale)
                        : 0.0D;
        replace(
                player.getAttribute(Attributes.ARMOR),
                ARMOR_ADD_ID,
                "Guardian armor addition",
                addition,
                AttributeModifier.Operation.ADDITION);
        replace(
                player.getAttribute(Attributes.ARMOR),
                ARMOR_SCALE_ID,
                "Guardian armor scale",
                scale,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
        replace(
                player.getAttribute(Attributes.ARMOR_TOUGHNESS),
                TOUGHNESS_SCALE_ID,
                "Guardian toughness scale",
                scale,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    private static void replace(
            AttributeInstance attribute,
            UUID id,
            String name,
            double amount,
            AttributeModifier.Operation operation) {
        if (attribute == null) {
            return;
        }
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

    private record Equipment(boolean contract, boolean guardians) {
        private static final Equipment NONE = new Equipment(false, false);
    }
}
