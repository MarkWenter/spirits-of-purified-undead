package dev.purifiedundead.validation;

import dev.purifiedundead.blightedrain.*;
import dev.purifiedundead.compat.TetraMaterialTraits;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.impl.ModularBladedItem;
import se.mickelus.tetra.module.SchematicRegistry;

/** Isolated Tetra-equipped validation world only. */
public final class TetraMaterialsSmoke {
    private static void check(boolean ok, String detail) {
        if (!ok) throw new IllegalStateException(detail);
    }

    private static ItemStack blade(ServerPlayer player, String ingredient) {
        var base = ModularBladedItem.createItemStack("basic_blade", "iron", "stick");
        var schematic = SchematicRegistry.getSchematic("sword/basic_blade");
        var id = ResourceLocation.fromNamespaceAndPath("purified_undead", ingredient);
        var input = new ItemStack(BuiltInRegistries.ITEM.get(id), 8);
        check(schematic.acceptsMaterial(base, "sword/blade", 0, input), "workbench rejected " + ingredient);
        var made = schematic.applyUpgrade(base, new ItemStack[]{input}, true, "sword/blade", player);
        ((IModularItem) made.getItem()).assemble(made, player.level(), 0);
        return made;
    }

    public static void run(ServerPlayer player) throws ReflectiveOperationException {
        var level = player.serverLevel();
        var breath = blade(player, "breathgold_ingot");
        var steel = blade(player, "purified_arcsteel");
        check(!TetraMaterialTraits.immaculate(breath) && TetraMaterialTraits.immaculate(steel), "wrong trait");
        var cow = EntityType.COW.create(level);
        cow.moveTo(player.getX(), player.getY(), player.getZ());
        level.addFreshEntity(cow);
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        player.setItemSlot(EquipmentSlot.MAINHAND, steel);
        cow.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300));
        check(cow.hurt(level.damageSources().playerAttack(player), 4), "attack rejected");
        check(TetraMaterialTraits.immaculate(player.getMainHandItem()), "mainhand trait");
        check(!cow.hasEffect(MobEffects.MOVEMENT_SPEED), "buff retained " + cow.getPersistentData());
        check(!cow.hasEffect(MobEffects.MOVEMENT_SPEED)
                && !cow.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300)), "seal");
        player.teleportTo(5.5, 280, 5.5);
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        level.setWeatherParameters(0, 2400, true, false);
        level.setRainLevel(1);
        var field = BlightedRainData.class.getDeclaredField("model");
        field.setAccessible(true);
        var state = (BlightedRainModel) field.get(BlightedRainData.get(level));
        state.reset();
        state.start(0, 1200);
        var slots = new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND, EquipmentSlot.CHEST};
        breath.setDamageValue(7);
        for (var slot : slots) player.setItemSlot(slot, breath.copy());
        player.getInventory().setItem(10, breath.copy());
        player.tickCount = 100;
        RainEffects.tick(player);
        check(player.getMainHandItem().getDamageValue() == 7, "warning repaired");
        state.advance(300);
        var roof = player.blockPosition().above(3);
        level.setBlockAndUpdate(roof, Blocks.STONE.defaultBlockState());
        RainEffects.tick(player);
        check(player.getMainHandItem().getDamageValue() == 7, "roof repaired");
        level.removeBlock(roof, false);
        check(RainExposure.exposed(player), "test location lacks precipitation");
        RainEffects.tick(player);
        for (var slot : slots) check(player.getItemBySlot(slot).getDamageValue()
                == Math.max(0, 7 - Math.max(1, breath.getMaxDamage() / 100)), "repair " + slot);
        check(player.getInventory().getItem(10).getDamageValue() == 7, "backpack repaired");
        System.out.println("TETRA_MATERIALS_OK workbench, melee seal, rain phases, shelter, equipment and backpack");
    }

    private TetraMaterialsSmoke() {}
}
