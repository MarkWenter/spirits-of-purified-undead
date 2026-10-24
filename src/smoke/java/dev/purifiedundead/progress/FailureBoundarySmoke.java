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
        // Inventory presentation can fail after delivery without making the reward pending again.
        badMenu = player.inventoryMenu;
        try {
            check(WarriorRewardService.grantContract(player), "first contract delivery");
            RewardSounds.tick(player);
            check(syncFaults > 0, "presentation fault injector ran");
            check(!WarriorRewardService.grantContract(player), "no repeat after sync fault");
            check(
                    count(player, dev.purifiedundead.content.ModItems.ANCIENT_CONTRACT.get()) == 1,
                    "reward duplication after presentation failure");
        } finally {
            badMenu = null;
        }
        RewardSounds.tick(player);
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
        check(
                dev.purifiedundead.combat.FerinCriticalModel.rollExternal(Double.NaN, 2, () -> .1)
                                .multiplier()
                        == 1,
                "invalid external attributes");
        System.out.println(
                "FAILURE_BOUNDARIES_OK: rollback, unknown payload, reward sync fault, cancelled and"
                    + " normal conversion, numeric boundary");
    }
}
