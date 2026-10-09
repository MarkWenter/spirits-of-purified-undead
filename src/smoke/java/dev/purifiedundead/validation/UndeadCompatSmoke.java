package dev.purifiedundead.validation;

import dev.purifiedundead.compat.UndeadCompatibility;
import dev.purifiedundead.config.PurifiedUndeadConfig;
import dev.purifiedundead.content.ModItems;
import dev.purifiedundead.purification.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

public final class UndeadCompatSmoke {
    private static void check(boolean v, String m) {
        if (!v) throw new IllegalStateException("UNDEAD_COMPAT_FAILED: " + m);
    }

    public static void run(net.minecraft.server.MinecraftServer server) {
        var level = server.overworld();
        var p =
                new net.minecraftforge.common.util.FakePlayer(
                        level,
                        new com.mojang.authlib.GameProfile(
                                java.util.UUID.randomUUID(), "CompatSmoke"));
        var nativeMob = UndeadCompatFixtures.NATIVE.create(level);
        var tagged = UndeadCompatFixtures.TAGGED.create(level);
        var living = UndeadCompatFixtures.LIVING.create(level);
        check(UndeadCompatibility.fragmentSource(nativeMob), "native modded undead fallback");
        check(UndeadCompatibility.fragmentSource(tagged), "optional shared tag");
        check(!UndeadCompatibility.fragmentSource(living), "ordinary modded mob excluded");
        check(
                !UndeadCompatibility.fragmentSource(EntityType.WITHER.create(level)),
                "vanilla drop selection unchanged");
        check(
                UndeadCompatibility.fragmentSource(EntityType.ZOMBIE.create(level)),
                "vanilla zombie preserved");
        check(
                !UndeadCompatibility.fragmentSource(UndeadCompatFixtures.EXCLUDED.create(level)),
                "exclusion overrides explicit and native eligibility");
        var auto = PurifiedUndeadConfig.VALUES.autoDetectModdedUndeadDrops;
        boolean was = auto.get();
        try {
            auto.set(false);
            check(!UndeadCompatibility.fragmentSource(nativeMob), "fallback toggle");
            check(UndeadCompatibility.fragmentSource(tagged), "explicit tags survive toggle");
        } finally {
            auto.set(was);
        }
        check(!LilyDiaryProgress.record(p, tagged), "unworn diary");
        PurificationProgress.unlock(p);
        CuriosApi.getCuriosInventory(p)
                .orElseThrow(() -> new IllegalStateException("Curios"))
                .getCurios()
                .get("wanderer_log")
                .getStacks()
                .setStackInSlot(0, new ItemStack(ModItems.LILY_DIARY.get()));
        check(
                LilyDiaryProgress.record(p, tagged) && LilyDiaryProgress.record(p, nativeMob),
                "tagged and native diary types");
        check(
                !LilyDiaryProgress.record(p, tagged)
                        && !LilyDiaryProgress.record(p, living)
                        && LilyDiaryProgress.count(p) == 2,
                "distinct types and living rejection");
        var chance = PurifiedUndeadConfig.VALUES.fragmentDropChance;
        double old = chance.get();
        try {
            chance.set(1.0);
            for (var victim : new LivingEntity[] {tagged, nativeMob, living}) {
                var drops = new java.util.ArrayList<net.minecraft.world.entity.item.ItemEntity>();
                var source = level.damageSources().playerAttack(p);
                new dev.purifiedundead.progress.BlightMaterialDropEvents()
                        .onLivingDrops(
                                new net.minecraftforge.event.entity.living.LivingDropsEvent(
                                        victim, source, drops, 0, false));
                int count =
                        drops.stream()
                                .mapToInt(
                                        d ->
                                                d.getItem().is(ModItems.BLIGHT_FRAGMENT.get())
                                                        ? d.getItem().getCount()
                                                        : 0)
                                .sum();
                check(
                        victim == living ? count == 0 : count >= 1 && count <= 8,
                        "actual drop event: "
                                + victim.getType()
                                + " count="
                                + count
                                + " chance="
                                + chance.get()
                                + " auto="
                                + auto.get());
            }
        } finally {
            chance.set(old);
        }
        System.out.println(
                "UNDEAD_COMPAT_OK: optional tags, absent IDs, native fallback/toggle, living exclusion, vanilla preservation, actual drops, equipped diary and type deduplication");
    }
}
