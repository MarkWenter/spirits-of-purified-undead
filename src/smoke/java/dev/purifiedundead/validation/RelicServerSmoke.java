package dev.purifiedundead.validation;

import dev.purifiedundead.content.ModItems;
import dev.purifiedundead.content.relic.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.TickEvent;
import top.theillusivec4.curios.api.CuriosApi;
import java.util.UUID;

/** Opt-in development integration checks, excluded from the production JAR. */
@Mod.EventBusSubscriber(modid = "purified_undead")
public final class RelicServerSmoke {
    private static void check(boolean ok, String message) {
        if (!ok) throw new IllegalStateException("RELIC_SMOKE_FAILED: " + message);
    }
    @SubscribeEvent public static void started(ServerStartedEvent event) {
        if (!Boolean.getBoolean("purified_undead.relicSmoke")) return;
        var server = event.getServer();
        var level = server.overworld();
        for (var kind : RelicKind.values()) {
            if (kind == RelicKind.WHITE_PRIESTESS_STATUE || kind == RelicKind.WHITE_PRIESTESS_EARRINGS) continue;
            check(server.getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath("purified_undead", kind.id)).isPresent(), "recipe " + kind.id);
        }
        var lootParams = new net.minecraft.world.level.storage.loot.LootParams.Builder(level)
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, net.minecraft.world.phys.Vec3.ZERO)
                .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.CHEST);
        for (String tableName : new String[]{"abandoned_mineshaft", "stronghold_corridor", "stronghold_crossing", "stronghold_library", "ancient_city", "ancient_city_ice_box", "simple_dungeon"}) {
            var table = server.getLootData().getLootTable(ResourceLocation.fromNamespaceAndPath("minecraft", "chests/" + tableName));
            int statues = 0, earrings = 0, vanilla = 0;
            for (int seed = 1; seed <= 512; seed++) {
                for (var stack : table.getRandomItems(lootParams, seed * 104729L)) {
                    if (stack.is(ModItems.WHITE_PRIESTESS_STATUE.get())) statues++;
                    else if (stack.is(ModItems.WHITE_PRIESTESS_EARRINGS.get())) earrings++;
                    else vanilla++;
                }
            }
            check(vanilla > 0, "vanilla loot preserved " + tableName);
            check(tableName.equals("simple_dungeon") ? statues == 0 && earrings == 0 : statues > 0 && earrings > 0, "loot injection " + tableName);
            System.out.println("RELIC_LOOT " + tableName + " statue=" + statues + " earrings=" + earrings);
        }
        var necklace = server.getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath("purified_undead", "weathered_warrior_necklace")).orElseThrow();
        var potion = necklace.getIngredients().get(3);
        var strong = net.minecraft.world.item.alchemy.PotionUtils.setPotion(new ItemStack(Items.POTION), net.minecraft.world.item.alchemy.Potions.STRONG_HEALING);
        var weak = net.minecraft.world.item.alchemy.PotionUtils.setPotion(new ItemStack(Items.POTION), net.minecraft.world.item.alchemy.Potions.HEALING);
        check(potion.test(strong) && !potion.test(weak) && !potion.test(new ItemStack(Items.POTION)), "Healing II ingredient");
        var player = FakePlayerFactory.get(level, new com.mojang.authlib.GameProfile(UUID.fromString("2474d251-e742-4be6-94ea-025f398db314"), "RelicSmoke"));
        var inventory = CuriosApi.getCuriosInventory(player).orElseThrow(() -> new IllegalStateException("Curios missing"));
        var slots = inventory.getCurios().get("white_witch_relic").getStacks();
        if (slots.getSlots() < 3) slots.grow(3 - slots.getSlots());
        slots.setStackInSlot(0, new ItemStack(ModItems.BLOODSTAINED_RIBBON.get()));
        int initialXp = player.totalExperience;
        for (int i = 0; i < 4; i++) player.giveExperiencePoints(1);
        check(player.totalExperience - initialXp == 5, "XP event / fractional accumulation");
        slots.setStackInSlot(0, new ItemStack(ModItems.SOILED_SILVER_ROSARY.get()));
        slots.setStackInSlot(1, new ItemStack(ModItems.KINGS_SHIELD_BADGE.get()));
        var effects = new RelicEvents();
        effects.onTick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END, player));
        check(Math.abs(player.getMaxHealth() - 26) < .01, "health 20 -> 26");
        slots.setStackInSlot(0, new ItemStack(ModItems.BLIGHTED_FINGER.get()));
        slots.setStackInSlot(1, new ItemStack(ModItems.ANCIENT_DRAGON_CLAW.get()));
        var zombie = new net.minecraft.world.entity.monster.Zombie(level);
        var attack = new LivingHurtEvent(zombie, level.damageSources().playerAttack(player), 10);
        effects.onDamage(attack);
        check(Math.abs(attack.getAmount() - 12.5) < .01, "melee boost");
        var arrow = new net.minecraft.world.entity.projectile.Snowball(level, player);
        var ranged = new LivingHurtEvent(zombie, level.damageSources().thrown(arrow, player), 10);
        effects.onDamage(ranged);
        check(Math.abs(ranged.getAmount() - 29.375) < .01, "ranged boost");
        slots.setStackInSlot(0, new ItemStack(ModItems.WEATHERED_WARRIOR_NECKLACE.get()));
        slots.setStackInSlot(1, ItemStack.EMPTY);
        effects.onTick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END, player));
        player.setHealth(10);
        effects.onKill(new LivingDeathEvent(zombie, level.damageSources().playerAttack(player)));
        check(Math.abs(player.getHealth() - 13.5) < .01, "kill healing");
        slots.setStackInSlot(0, new ItemStack(ModItems.WHITE_PRIESTESS_STATUE.get()));
        slots.setStackInSlot(1, new ItemStack(ModItems.WHITE_PRIESTESS_EARRINGS.get()));
        var first = new LivingDeathEvent(player, level.damageSources().generic()); effects.onDeathProtection(first);
        check(first.isCanceled() && player.getHealth() == 1 && player.hasEffect(net.minecraft.world.effect.MobEffects.REGENERATION), "statue resurrection");
        var second = new LivingDeathEvent(player, level.damageSources().generic()); effects.onDeathProtection(second);
        check(second.isCanceled(), "independent earring cooldown");
        var third = new LivingDeathEvent(player, level.damageSources().generic()); effects.onDeathProtection(third);
        check(!third.isCanceled(), "cooldowns block repeated resurrection");
        var state = player.getPersistentData().getCompound("purified_undead:white_witch_relics");
        check(state.getLong("white_priestess_statue") - level.getGameTime() == 2400, "120 seconds");
        check(state.getLong("white_priestess_earrings") - level.getGameTime() == 12000, "600 seconds");
        state.putLong("white_priestess_statue", 0);
        var bypass = new LivingDeathEvent(player, level.damageSources().fellOutOfWorld()); effects.onDeathProtection(bypass);
        check(!bypass.isCanceled(), "void bypass preserved");
        for (int i = 0; i < slots.getSlots(); i++) slots.setStackInSlot(i, ItemStack.EMPTY);
        System.out.println("PURIFIED_UNDEAD_RELIC_SMOKE_OK: six recipes, strict potion, XP, health, damage, healing, independent resurrection and bypass");
    }
}
