package dev.purifiedundead.progress;

import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;

/** Real inventory/world fault cases; never included in release artifacts. */
public final class FailureBoundarySmoke {
    public static Object badMenu;
    public static int syncFaults;
    public static Object badDiscard;
    public static boolean countCopies;
    public static int itemCopies;

    private static final class FaultZombie extends net.minecraft.world.entity.monster.Zombie {
        boolean failClear;

        FaultZombie(net.minecraft.server.level.ServerLevel level) {
            super(EntityType.ZOMBIE, level);
        }

        @Override
        public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
            super.setItemSlot(slot, stack);
            if (failClear && slot == EquipmentSlot.MAINHAND && stack.isEmpty()) {
                failClear = false;
                throw new IllegalStateException("expected gear clear failure");
            }
        }
    }

    private static void check(boolean ok, String message) {
        if (!ok) throw new IllegalStateException("FAILURE_BOUNDARIES_FAILED: " + message);
    }

    private static int count(ServerPlayer player, Item item) {
        int n = 0;
        for (var stack : player.getInventory().items) if (stack.is(item)) n += stack.getCount();
        return n;
    }

    public static final class RejectDrops {
        @net.minecraftforge.eventbus.api.SubscribeEvent
        public void join(net.minecraftforge.event.entity.EntityJoinLevelEvent event) {
            if (event.getEntity() instanceof ItemEntity item && item.getItem().is(Items.DIAMOND))
                event.setCanceled(true);
        }
    }

    public static void run(MinecraftServer server) throws Exception {
        var player = ProjectAuditSmoke.player(server);
        player.getInventory().clearContent();
        player.getInventory().setItem(0, new ItemStack(Items.STONE, 3));
        try {
            InventoryTransfer.insert(
                    player.getInventory(),
                    new ItemStack(Items.DIAMOND),
                    () -> {
                        throw new IllegalStateException("expected commit fault");
                    });
            throw new AssertionError("injected commit fault was swallowed");
        } catch (IllegalStateException expected) {
            check(
                    count(player, Items.DIAMOND) == 0 && count(player, Items.STONE) == 3,
                    "inventory rollback");
        }
        String key = "purified_undead:retained_accessories";
        var unknown = new CompoundTag();
        unknown.putString("id", "missing_test_mod:accessory");
        unknown.putByte("Count", (byte) 1);
        unknown.putInt("count", 1);
        unknown.putString("original_payload", "keep me");
        var valid = new ItemStack(dev.purifiedundead.content.ModItems.FERIN_WARRIOR.get());
        var entries = new ListTag();
        entries.add(unknown.copy());
        entries.add(valid.save(new CompoundTag()));
        var retained = new CompoundTag();
        retained.putInt("version", 1);
        retained.put("stacks", entries);
        player.getPersistentData().put(key, retained);
        AccessoryDeathRetentionService.restore(player);
        AccessoryDeathRetentionService.restore(player);
        check(count(player, valid.getItem()) == 1, "no repeated recovery");
        var remaining =
                player.getPersistentData().getCompound(key).getList("stacks", Tag.TAG_COMPOUND);
        check(
                remaining.size() == 1 && remaining.getCompound(0).equals(unknown),
                "unknown payload preserved");
        // A full inventory must not copy capability-heavy unrelated stacks or rewrite raw records.
        player.getInventory().clearContent();
        player.getAbilities().instabuild = false;
        for (int i = 0; i < 36; i++)
            player.getInventory().setItem(i, new ItemStack(Items.STONE, 64));
        itemCopies = 0;
        countCopies = true;
        try {
            check(
                    !InventoryTransfer.insert(
                            player.getInventory(),
                            new ItemStack(Items.DIAMOND),
                            () -> {
                                throw new AssertionError("full inventory committed");
                            }),
                    "full no-op");
        } finally {
            countCopies = false;
        }
        check(itemCopies == 0, "full inventory must not snapshot");
        var backlog = new ListTag();
        for (int i = 0; i < 128; i++) backlog.add(remaining.getCompound(0).copy());
        var fullTag = new CompoundTag();
        fullTag.put("stacks", backlog);
        player.getPersistentData().put(key, fullTag);
        for (int i = 0; i < 40; i++) AccessoryDeathRetentionService.restore(player);
        check(
                player.getPersistentData().getCompound(key) == fullTag
                        && fullTag.getList("stacks", Tag.TAG_COMPOUND) == backlog
                        && backlog.size() == 128,
                "unknown backlog remains unchanged");
        var validBacklog = new ListTag();
        for (int i = 0; i < 128; i++) validBacklog.add(valid.save(new CompoundTag()));
        fullTag.put("stacks", validBacklog);
        itemCopies = 0;
        countCopies = true;
        try {
            for (int i = 0; i < 3; i++) AccessoryDeathRetentionService.restore(player);
        } finally {
            countCopies = false;
        }
        check(
                itemCopies == 0 && validBacklog.size() == 128,
                "valid full backlog must not snapshot or rewrite");
        player.getInventory().setItem(0, ItemStack.EMPTY);
        AccessoryDeathRetentionService.restore(player);
        check(
                count(player, valid.getItem()) == 1 && validBacklog.size() == 127,
                "recovery resumes when a slot becomes available");
        player.getPersistentData().remove(key);
        player.getInventory().clearContent();
        // Inventory presentation can fail after delivery without making the reward pending again.
        badMenu = player.inventoryMenu;
        try {
            check(WarriorRewardService.grantContract(player), "first contract delivery");
            RewardSounds.tick(player);
            check(syncFaults > 0, "presentation fault injector ran");
            int attempts = syncFaults;
            for (int i = 0; i < 100; i++) RewardSounds.tick(player);
            check(syncFaults == attempts, "sync retry must wait after failure");
            check(!WarriorRewardService.grantContract(player), "no repeat after sync fault");
            check(
                    count(player, dev.purifiedundead.content.ModItems.ANCIENT_CONTRACT.get()) == 1,
                    "reward duplication after presentation failure");
        } finally {
            badMenu = null;
        }
        long syncTime = server.overworld().getGameTime();
        try {
            server.getWorldData().overworldData().setGameTime(syncTime + 20);
            RewardSounds.tick(player);
            check(
                    !player.getPersistentData().getBoolean("purified_undead:reward_inventory_sync"),
                    "sync recovers at next retry after presentation fault clears");
        } finally {
            server.getWorldData().overworldData().setGameTime(syncTime);
        }
        var level = server.overworld();
        var spawn = level.getSharedSpawnPos();
        level.getChunk(spawn);
        var original = EntityType.ZOMBIE.create(level);
        original.setPos(spawn.getX() + .5, 150, spawn.getZ() + .5);
        original.setNoAi(true);
        original.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND));
        check(level.addFreshEntity(original), "conversion source spawn");
        var replacement = EntityType.ZOMBIE.create(level);
        replacement.moveTo(original.position());
        replacement.setNoAi(true);
        var reject = new RejectDrops();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(reject);
        try {
            check(
                    !ConversionCommit.replace(original, replacement, level, java.util.List.of()),
                    "cancelled gear rejects conversion");
            check(
                    !original.isRemoved() && original.getMainHandItem().is(Items.DIAMOND),
                    "source gear intact");
            check(replacement.isRemoved(), "replacement rolled back");
            check(
                    level
                            .getEntitiesOfClass(
                                    ItemEntity.class, original.getBoundingBox().inflate(4))
                            .stream()
                            .noneMatch(item -> item.getItem().is(Items.DIAMOND)),
                    "no leaked cancelled drop");
        } finally {
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(reject);
        }
        var accepted = EntityType.ZOMBIE.create(level);
        accepted.moveTo(original.position());
        check(
                ConversionCommit.replace(original, accepted, level, java.util.List.of()),
                "normal conversion retained");
        check(original.isRemoved() && !accepted.isRemoved(), "normal source replacement");
        var drops =
                level.getEntitiesOfClass(ItemEntity.class, accepted.getBoundingBox().inflate(4));
        check(
                drops.stream()
                                .filter(item -> item.getItem().is(Items.DIAMOND))
                                .mapToInt(item -> item.getItem().getCount())
                                .sum()
                        == 1,
                "normal equipment dropped exactly once");
        drops.forEach(Entity::discard);
        accepted.discard();
        // A second failure in cleanup must still restore source gear and escape optional wrappers.
        var faultSource = new FaultZombie(level);
        faultSource.moveTo(accepted.position());
        faultSource.setNoAi(true);
        faultSource.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND));
        check(level.addFreshEntity(faultSource), "fault source spawn");
        var faultReplacement = EntityType.ZOMBIE.create(level);
        faultReplacement.moveTo(faultSource.position());
        faultReplacement.setNoAi(true);
        faultSource.failClear = true;
        badDiscard = faultReplacement;
        try {
            ConversionCommit.replace(faultSource, faultReplacement, level, java.util.List.of());
            throw new IllegalStateException("rollback failure was swallowed");
        } catch (AssertionError expected) {
            check(expected.getCause() != null, "rollback failure must have cause");
            check(
                    !faultSource.isRemoved() && faultSource.getMainHandItem().is(Items.DIAMOND),
                    "gear restoration continues after cleanup failure");
        } finally {
            badDiscard = null;
            faultReplacement.discard();
            faultSource.discard();
            level.getEntitiesOfClass(ItemEntity.class, faultSource.getBoundingBox().inflate(4))
                    .forEach(Entity::discard);
        }
        System.out.println(
                "HOTFIX_COST_BOUNDARIES_OK: zero full-inventory copies, unchanged 128-entry"
                        + " backlog, quiet unknown decoding, retry delay, cleanup second-failure"
                        + " restoration");
        check(
                dev.purifiedundead.combat.FerinCriticalModel.rollExternal(Double.NaN, 2, () -> .1)
                                .multiplier()
                        == 1,
                "invalid external attributes");
        HotfixBoundarySmoke.run(server);
        System.out.println(
                "FAILURE_BOUNDARIES_OK: rollback, unknown payload, reward sync fault, cancelled and"
                        + " normal conversion, numeric boundary");
    }
}
