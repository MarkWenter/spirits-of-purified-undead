package dev.purifiedundead.validation;

import dev.purifiedundead.content.*;
import dev.purifiedundead.progress.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.resources.ResourceLocation;

public final class AdvancementSmoke {
    private static void check(boolean b, String m) {
        if (!b) throw new IllegalStateException("ADVANCEMENT_FAILED: " + m);
    }

    private static ServerPlayer player(net.minecraft.server.MinecraftServer s, String name) {
        var profile = new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), name);
        var p = new ServerPlayer(s, s.overworld(), profile);
        p.connection =
                new net.minecraftforge.common.util.FakePlayer(
                                s.overworld(),
                                new com.mojang.authlib.GameProfile(
                                        java.util.UUID.randomUUID(), "NetworkStub"))
                        .connection;
        return p;
    }

    private static boolean done(ServerPlayer p, String id) {
        var a =
                p.server
                        .getAdvancements()
                        .getAdvancement(
                                ResourceLocation.fromNamespaceAndPath(
                                        "purified_undead", "journey/" + id));
        check(a != null, "loaded " + id);
        return p.getAdvancements().getOrStartProgress(a).isDone();
    }

    public static void run(net.minecraft.server.MinecraftServer server) {
        var empty = player(server, "AdvEmpty");
        ModAdvancements.migrate(empty);
        for (String id : ModAdvancements.IDS)
            check(!done(empty, id), "empty player not awarded " + id);
        var original = player(server, "AdvRevive");
        original.addEffect(
                new net.minecraft.world.effect.MobEffectInstance(
                        ModEffects.BLIGHTED_TRANSFORMATION.get(), 100));
        var events = new ContractProgressEvents();
        events.onPlayerDeath(
                new net.minecraftforge.event.entity.living.LivingDeathEvent(
                        original, original.damageSources().generic()));
        check(!done(original, "hidden_power"), "death alone does not award");
        var revived = player(server, "AdvReborn");
        events.onPlayerClone(
                new net.minecraftforge.event.entity.player.PlayerEvent.Clone(
                        revived, original, true));
        check(done(revived, "hidden_power"), "actual death/clone reward grants root");
        var old = player(server, "AdvOld");
        var state = new WarriorProgress();
        state.claimContract(true);
        state.claimGroth(true);
        state.claimEleine(true);
        state.claimHoenir(true);
        state.claimUlv(true);
        state.claimJulius(true);
        state.claimFaden(true);
        state.claimGuardians(true);
        while (state.upgradeTalisman()) {}
        old.getPersistentData().put("purified_undead:warrior_progress", state.save());
        old.getEnderChestInventory()
                .setItem(0, new ItemStack(ModItems.WHITE_PRIESTESS_STATUE.get()));
        old.awardStat(net.minecraft.stats.Stats.ITEM_CRAFTED.get(ModItems.PURE_TOUCH.get()));
        dev.purifiedundead.purification.PurificationProgress.unlock(old);
        top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(old)
                .orElseThrow(() -> new IllegalStateException("curios"))
                .getCurios()
                .get("wanderer_log")
                .getStacks()
                .setStackInSlot(0, new ItemStack(ModItems.LILY_DIARY.get()));
        ModAdvancements.migrate(old);
        for (String id : ModAdvancements.IDS) check(done(old, id), "legacy recovery " + id);
        check(old.getPersistentData().getBoolean(ModAdvancements.MIGRATION), "migration marked");
        ModAdvancements.migrate(old);
        var clone = player(server, "AdvClone");
        WarriorRewardService.copyProgress(old, clone);
        check(
                clone.getPersistentData().getBoolean(ModAdvancements.MIGRATION),
                "migration flag survives death");
        var pending = player(server, "AdvPending");
        var ps = new WarriorProgress();
        ps.claimGroth(false);
        ModAdvancements.progression(pending, ps);
        check(!done(pending, "anger"), "pending not delivered");
        ps.resolvePendingGroth(true);
        ModAdvancements.progression(pending, ps);
        check(done(pending, "anger"), "delivered award");
        var fresh = player(server, "AdvNewItem");
        Item[] items = {
            ModItems.GROTH_WARRIOR.get(),
            ModItems.ELEINE_WARRIOR.get(),
            ModItems.HOENIR_WARRIOR.get(),
            ModItems.ULV_WARRIOR.get(),
            ModItems.JULIUS_WARRIOR.get(),
            ModItems.FADEN_WARRIOR.get(),
            ModItems.GUARDIAN_WARRIORS.get(),
            ModItems.BLOODSTAINED_RIBBON.get(),
            ModItems.PURE_TOUCH.get(),
            ModItems.LILY_DIARY.get()
        };
        String[] ids = {
            "anger",
            "lament",
            "determination",
            "falling_petals",
            "blighted_throne",
            "separation",
            "purification",
            "witch_relic",
            "frontier_witch",
            "rebirth"
        };
        for (int i = 0; i < items.length; i++) {
            var stack = new ItemStack(items[i]);
            fresh.getInventory().setItem(0, stack);
            net.minecraft.advancements.CriteriaTriggers.INVENTORY_CHANGED.trigger(
                    fresh, fresh.getInventory(), stack);
            check(done(fresh, ids[i]), "inventory trigger " + ids[i]);
        }
        check(!done(fresh, "hidden_power"), "items alone do not fake resurrection");
        System.out.println(
                "ADVANCEMENTS_OK: all 12 loaded; real revival, inventory triggers, pending guards, legacy state/ender chest/stat/Curios backfill, empty-player guard, repeat and death persistence");
    }
}
