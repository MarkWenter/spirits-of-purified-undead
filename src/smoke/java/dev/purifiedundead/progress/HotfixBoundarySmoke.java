package dev.purifiedundead.progress;

import dev.purifiedundead.content.ModItems;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;

/** Real fault cases for the post-removal commit boundary, decoder ownership and magic adapter. */
public final class HotfixBoundarySmoke {
    public static boolean failDecode;
    public static int decodeFaults;
    private static Object failRemoved;
    private static int removalFaults;

    private static void check(boolean ok, String message) {
        if (!ok)
            throw new IllegalStateException("HOTFIX_TRANSACTION_BOUNDARIES_FAILED: " + message);
    }

    public static final class RemovalListener {
        @net.minecraftforge.eventbus.api.SubscribeEvent
        public void leave(net.minecraftforge.event.entity.EntityLeaveLevelEvent event) {
            if (event.getEntity() == failRemoved) {
                check(event.getEntity().isRemoved(), "listener must run after removed marking");
                removalFaults++;
                throw new IllegalStateException("expected post-removal listener failure");
            }
        }
    }

    private static void decoder(MinecraftServer server) throws Exception {
        var player = ProjectAuditSmoke.player(server);
        var probe = new ItemStack(ModItems.FERIN_WARRIOR.get());
        var payload = new CompoundTag();
        payload.putString("hotfix_decode_probe", "original value");
        probe.setTag(payload);
        var serialized = probe.save(new net.minecraft.nbt.CompoundTag());
        var before = serialized.copy();
        var entries = new ListTag();
        entries.add(serialized);
        var buffer = new CompoundTag();
        buffer.put("stacks", entries);
        String key = "purified_undead:retained_accessories";
        player.getPersistentData().put(key, buffer);
        decodeFaults = 0;
        failDecode = true;
        try {
            AccessoryDeathRetentionService.restore(player);
            AccessoryDeathRetentionService.restore(player);
            check(decodeFaults == 2, "mutating decoder actually ran twice");
            check(
                    entries.size() == 1 && entries.getCompound(0).equals(before),
                    "failed decoder must preserve original nested NBT");
            check(
                    player.getInventory().countItem(probe.getItem()) == 0,
                    "failed decoder must not deliver an item");
        } finally {
            failDecode = false;
        }
        AccessoryDeathRetentionService.restore(player);
        AccessoryDeathRetentionService.restore(player);
        check(
                !player.getPersistentData().contains(key)
                        && player.getInventory().countItem(probe.getItem()) == 1,
                "decoder recovery delivers exactly once");
    }

    private static void removed(MinecraftServer server) {
        var level = server.overworld();
        var spawn = level.getSharedSpawnPos();
        level.getChunk(spawn);
        var original = EntityType.ZOMBIE.create(level);
        original.setPos(spawn.getX() + .5, 150, spawn.getZ() + .5);
        original.setNoAi(true);
        original.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND));
        check(level.addFreshEntity(original), "post-removal source spawn");
        var replacement = EntityType.ZOMBIE.create(level);
        replacement.moveTo(original.position());
        replacement.setNoAi(true);
        var listener = new RemovalListener();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(listener);
        failRemoved = original;
        removalFaults = 0;
        try {
            try {
                ConversionCommit.replace(original, replacement, level, java.util.List.of());
                throw new AssertionError("post-removal failure was swallowed");
            } catch (IllegalStateException expected) {
                check(
                        expected.getMessage().equals("expected post-removal listener failure"),
                        "original removal failure must propagate");
            }
            check(removalFaults == 1 && original.isRemoved(), "post-removal injector actually ran");
            check(!replacement.isRemoved(), "published replacement must survive");
            check(
                    level
                                    .getEntitiesOfClass(
                                            ItemEntity.class,
                                            replacement.getBoundingBox().inflate(4))
                                    .stream()
                                    .filter(drop -> drop.getItem().is(Items.DIAMOND))
                                    .mapToInt(drop -> drop.getItem().getCount())
                                    .sum()
                            == 1,
                    "published equipment must survive exactly once");
        } finally {
            failRemoved = null;
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(listener);
            replacement.discard();
            if (!original.isRemoved()) original.discard();
            level.getEntitiesOfClass(ItemEntity.class, original.getBoundingBox().inflate(4))
                    .forEach(Entity::discard);
        }
    }

    public static final class OrbListener {
        final java.util.List<dev.purifiedundead.entity.EleineMagicOrbEntity> orbs =
                new java.util.ArrayList<>();

        @net.minecraftforge.eventbus.api.SubscribeEvent
        public void join(net.minecraftforge.event.entity.EntityJoinLevelEvent event) {
            if (event.getEntity() instanceof dev.purifiedundead.entity.EleineMagicOrbEntity orb)
                orbs.add(orb);
        }
    }

    private static void orb(ServerPlayer attacker, float amount) {
        var target = EntityType.COW.create(attacker.serverLevel());
        target.setPos(attacker.getX() + 3, attacker.getY(), attacker.getZ());
        var source = attacker.damageSources().playerAttack(attacker);
        var adapter = new dev.purifiedundead.combat.EleineCombatEvents();
        adapter.rememberMelee(
                new net.minecraftforge.event.entity.living.LivingHurtEvent(target, source, amount));
        adapter.onMeleeDamage(
                new net.minecraftforge.event.entity.living.LivingDamageEvent(target, source, 1));
    }

    private static void orb(MinecraftServer server) throws Exception {
        var values = dev.purifiedundead.config.PurifiedUndeadConfig.VALUES;
        double beforeChance = values.eleineOrbChance.get(),
                beforeRatio = values.eleineOrbDamageRatio.get();
        var listener = new OrbListener();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(listener);
        try {
            values.eleineOrbChance.set(1.0);
            values.eleineOrbDamageRatio.set(20.0);
            var attacker = equipped(server, true, true);
            attacker.serverLevel().getChunk(attacker.blockPosition());
            orb(attacker, Float.MAX_VALUE);
            check(listener.orbs.isEmpty(), "overflow must not publish an infinite-damage orb");
            orb(attacker, 10);
            check(listener.orbs.size() == 1, "normal orb spawn remains enabled");
        } finally {
            values.eleineOrbChance.set(beforeChance);
            values.eleineOrbDamageRatio.set(beforeRatio);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(listener);
            listener.orbs.forEach(Entity::discard);
        }
    }

    private static ServerPlayer equipped(MinecraftServer server, boolean contract, boolean eleine)
            throws Exception {
        var player = ProjectAuditSmoke.player(server);
        var handler =
                top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player)
                        .orElseThrow(() -> new IllegalStateException("Curios missing"));
        if (contract)
            handler.getCurios()
                    .get("ancient_contract")
                    .getStacks()
                    .setStackInSlot(0, new ItemStack(ModItems.ANCIENT_CONTRACT.get()));
        if (eleine) {
            var stacks = handler.getCurios().get("undead_warrior").getStacks();
            if (stacks.getSlots() < 1) stacks.grow(1);
            stacks.setStackInSlot(0, new ItemStack(ModItems.ELEINE_WARRIOR.get()));
        }
        return player;
    }

    private static float magic(ServerPlayer target, ServerPlayer attacker, float amount) {
        var source =
                attacker == null
                        ? target.damageSources().magic()
                        : target.damageSources().indirectMagic(attacker, attacker);
        var event =
                new net.minecraftforge.event.entity.living.LivingHurtEvent(target, source, amount);
        new dev.purifiedundead.combat.EleineCombatEvents().onMagicHurt(event);
        return event.getAmount();
    }

    private static void magic(MinecraftServer server) throws Exception {
        var value = dev.purifiedundead.config.PurifiedUndeadConfig.VALUES.eleineMagicMultiplier;
        double before = value.get();
        try {
            value.set(1.3);
            var plain = equipped(server, false, false);
            var cursed = equipped(server, true, false);
            var reversed = equipped(server, true, true);
            check(Math.abs(magic(cursed, plain, 10) - 13) < .0001, "normal incoming multiplier");
            check(Math.abs(magic(plain, reversed, 10) - 13) < .0001, "normal outgoing multiplier");
            check(
                    Math.abs(magic(cursed, reversed, 10) - 16.9) < .0001,
                    "normal combined multiplier");
            check(
                    magic(cursed, plain, Float.MAX_VALUE) == Float.MAX_VALUE,
                    "incoming overflow must retain finite original event damage");
            check(
                    magic(cursed, null, Float.MAX_VALUE) == Float.MAX_VALUE,
                    "non-player source must not receive infinity");
            check(
                    magic(plain, reversed, Float.MAX_VALUE) == Float.MAX_VALUE,
                    "outgoing overflow must retain finite original event damage");
            float combined = Float.MAX_VALUE / 1.5F;
            check(
                    Float.isFinite(
                            dev.purifiedundead.combat.EleineModel.incomingMagicDamage(
                                    combined, true, false)),
                    "combined test first step finite");
            check(
                    magic(cursed, reversed, combined) == combined,
                    "second-step overflow must restore the whole original event amount");
        } finally {
            value.set(before);
        }
    }

    public static void run(MinecraftServer server) throws Exception {
        decoder(server);
        removed(server);
        magic(server);
        orb(server);
        System.out.println(
                "HOTFIX_TRANSACTION_BOUNDARIES_OK: mutating decoder input isolated,"
                        + " post-removal listener error preserves replacement and gear, normal and extreme magic");
    }
}
