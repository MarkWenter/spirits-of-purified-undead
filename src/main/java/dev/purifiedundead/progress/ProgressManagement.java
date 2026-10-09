package dev.purifiedundead.progress;

import dev.purifiedundead.api.*;
import dev.purifiedundead.content.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import java.util.EnumMap;

/** Validated, explicit administrative operations; never called from a tick handler. */
public final class ProgressManagement {
    private static final ThreadLocal<Boolean> NOTIFYING = ThreadLocal.withInitial(() -> false);
    private static long lastFailureNanos;
    private static boolean failureLogged;
    private static int suppressedFailures;

    private ProgressManagement() {}

    private static void notificationFailed(Throwable ex) {
        long now = System.nanoTime();
        if (!failureLogged
                || now - lastFailureNanos >= java.util.concurrent.TimeUnit.MINUTES.toNanos(1)) {
            com.mojang.logging.LogUtils.getLogger()
                    .error(
                            "Addon progress notification failed; saved progression retained ({} repeated failures suppressed)",
                            suppressedFailures,
                            ex);
            failureLogged = true;
            lastFailureNanos = now;
            suppressedFailures = 0;
        } else if (suppressedFailures < Integer.MAX_VALUE) suppressedFailures++;
    }

    private static void thread(ServerPlayer p) {
        if (p == null || !p.server.isSameThread())
            throw new IllegalStateException("Progress access requires the server thread");
    }

    private static void mutation(ServerPlayer p) {
        thread(p);
        if (NOTIFYING.get())
            throw new IllegalStateException(
                    "Do not mutate progression from a progress notification");
    }

    public static ProgressSnapshot snapshot(ServerPlayer p) {
        thread(p);
        return snapshot(WarriorProgressStorage.load(p));
    }

    static ProgressSnapshot snapshot(WarriorProgress progress) {
        var t = progress.save();
        var states = new EnumMap<WarriorId, ProgressSnapshot.RewardState>(WarriorId.class);
        for (var w : WarriorId.values())
            states.put(
                    w,
                    new ProgressSnapshot.RewardState(
                            t.getBoolean(w.id() + "_obtained"), t.getBoolean(w.id() + "_pending")));
        return new ProgressSnapshot(
                states,
                new ProgressSnapshot.RewardState(
                        progress.contractObtained(), progress.contractPending()),
                progress.talismanLevel(),
                progress.eleineDrownedKills());
    }

    static void notifyChange(ServerPlayer p, ProgressSnapshot before, WarriorProgress progress) {
        var after = snapshot(progress);
        if (before.equals(after) || NOTIFYING.get()) return;
        NOTIFYING.set(true);
        try {
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
                    new ProgressChangedEvent(p, before, after));
        } catch (RuntimeException | LinkageError ex) {
            notificationFailed(ex);
        } finally {
            NOTIFYING.remove();
        }
    }

    public static ProgressApi.GrantResult grant(ServerPlayer p, WarriorId w) {
        mutation(p);
        java.util.Objects.requireNonNull(w);
        return grant(p, w.id(), w.item());
    }

    public static ProgressApi.GrantResult grantContract(ServerPlayer p) {
        mutation(p);
        return grant(p, "contract", ModItems.ANCIENT_CONTRACT.get());
    }

    private static ProgressApi.GrantResult grant(
            ServerPlayer p, String id, net.minecraft.world.item.Item item) {
        var tag = WarriorProgressStorage.load(p).save();
        if (tag.getBoolean(id + "_obtained")) return ProgressApi.GrantResult.ALREADY_OBTAINED;
        boolean delivered = WarriorRewardService.insert(p, item);
        tag.putBoolean(id + "_obtained", true);
        tag.putBoolean(id + "_pending", !delivered);
        WarriorProgressStorage.save(p, WarriorProgress.load(tag));
        return delivered ? ProgressApi.GrantResult.DELIVERED : ProgressApi.GrantResult.PENDING;
    }

    public static void reset(ServerPlayer p, WarriorId w) {
        mutation(p);
        java.util.Objects.requireNonNull(w);
        var tag = WarriorProgressStorage.load(p).save();
        tag.putBoolean(w.id() + "_obtained", false);
        tag.putBoolean(w.id() + "_pending", false);
        if (w == WarriorId.ELEINE) tag.putInt("eleine_drowned_kills", 0);
        WarriorProgressStorage.save(p, WarriorProgress.load(tag));
    }

    public static void setLevel(ServerPlayer p, int level) {
        mutation(p);
        if (level < 0 || level > WhiteWitchTalisman.maxLevel())
            throw new IllegalArgumentException("Level outside configured range");
        var tag = WarriorProgressStorage.load(p).save();
        tag.putInt("talisman_level", level);
        WarriorProgressStorage.save(p, WarriorProgress.load(tag));
    }

    /** Collect only exposed player inventory/equipment, not nested bags, ender chests or world containers. */
    public static int collect(ServerPlayer p, WarriorId w) {
        mutation(p);
        var item = java.util.Objects.requireNonNull(w).item();
        int[] removed = {0};
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
            var s = p.getInventory().getItem(i);
            if (s.is(item)) {
                removed[0] += s.getCount();
                p.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }
        var carried = p.containerMenu.getCarried();
        if (carried.is(item)) {
            removed[0] += carried.getCount();
            p.containerMenu.setCarried(ItemStack.EMPTY);
        }
        top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(p)
                .ifPresent(
                        h ->
                                h.getCurios()
                                        .values()
                                        .forEach(
                                                v -> {
                                                    for (var stacks :
                                                            new top.theillusivec4.curios.api.type
                                                                            .inventory
                                                                            .IDynamicStackHandler
                                                                    [] {
                                                                v.getStacks(), v.getCosmeticStacks()
                                                            })
                                                        for (int i = 0;
                                                                i < stacks.getSlots();
                                                                i++) {
                                                            var s = stacks.getStackInSlot(i);
                                                            if (s.is(item)) {
                                                                removed[0] += s.getCount();
                                                                stacks.setStackInSlot(
                                                                        i, ItemStack.EMPTY);
                                                            }
                                                        }
                                                }));
        // A collected pending reward must not reappear next tick. Keep it claimed.
        var tag = WarriorProgressStorage.load(p).save();
        tag.putBoolean(w.id() + "_obtained", true);
        tag.putBoolean(w.id() + "_pending", false);
        WarriorProgressStorage.save(p, WarriorProgress.load(tag));
        p.getInventory().setChanged();
        p.containerMenu.broadcastChanges();
        return removed[0];
    }
}
