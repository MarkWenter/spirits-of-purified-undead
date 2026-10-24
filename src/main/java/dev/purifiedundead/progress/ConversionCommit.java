package dev.purifiedundead.progress;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Commit replacement and detached equipment together; cancellation never destroys original gear.
 */
public final class ConversionCommit {
    private ConversionCommit() {}

    public static boolean replace(
            LivingEntity original,
            LivingEntity replacement,
            ServerLevel level,
            java.util.List<ItemStack> detached) {
        var gear = new java.util.EnumMap<EquipmentSlot, ItemStack>(EquipmentSlot.class);
        for (var slot : EquipmentSlot.values()) gear.put(slot, original.getItemBySlot(slot).copy());
        var drops = new java.util.ArrayList<ItemEntity>();
        var captured = original.captureDrops();
        var passengers = java.util.List.copyOf(original.getPassengers());
        Entity vehicle = original.getVehicle();
        boolean committed = false, gearChanged = false, mountsChanged = false;
        try {
            if (!level.addFreshEntity(replacement)) return false;
            for (var stack : gear.values())
                if (!stack.isEmpty()) drop(original, level, stack.copy(), drops);
            for (var stack : detached)
                if (!stack.isEmpty()) drop(original, level, stack.copy(), drops);
            gearChanged = true;
            for (var slot : EquipmentSlot.values()) original.setItemSlot(slot, ItemStack.EMPTY);
            mountsChanged = true;
            original.stopRiding();
            original.ejectPassengers();
            original.discard();
            committed = true;
            return true;
        } catch (RuntimeException | LinkageError failure) {
            // Do not pretend a transaction can be rolled back after the source was removed.
            if (original.isRemoved()) throw failure;
            dev.purifiedundead.util.FailureIsolation.report(
                    "entity conversion rolled back", failure);
            return false;
        } finally {
            if (!committed) {
                Throwable rollbackFailure = null;
                for (var drop : drops) {
                    if (captured != null)
                        rollbackFailure = rollback(rollbackFailure, () -> captured.remove(drop));
                    rollbackFailure = rollback(rollbackFailure, drop::discard);
                }
                rollbackFailure = rollback(rollbackFailure, replacement::discard);
                if (gearChanged)
                    for (var entry : gear.entrySet())
                        rollbackFailure =
                                rollback(
                                        rollbackFailure,
                                        () ->
                                                original.setItemSlot(
                                                        entry.getKey(), entry.getValue()));
                if (mountsChanged) {
                    if (vehicle != null && original.getVehicle() == null)
                        rollbackFailure =
                                rollback(
                                        rollbackFailure,
                                        () -> {
                                            if (!original.startRiding(vehicle))
                                                throw new IllegalStateException(
                                                        "Vehicle rollback rejected");
                                        });
                    for (var passenger : passengers)
                        if (passenger.getVehicle() == null)
                            rollbackFailure =
                                    rollback(
                                            rollbackFailure,
                                            () -> {
                                                if (!passenger.startRiding(original))
                                                    throw new IllegalStateException(
                                                            "Passenger rollback rejected");
                                            });
                }
                if (rollbackFailure != null)
                    throw new AssertionError(
                            "Cannot safely roll back entity conversion", rollbackFailure);
            }
        }
    }

    private static Throwable rollback(Throwable previous, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException | LinkageError failure) {
            if (previous == null) return failure;
            if (previous != failure) previous.addSuppressed(failure);
        }
        return previous;
    }

    private static void drop(
            LivingEntity original,
            ServerLevel level,
            ItemStack stack,
            java.util.List<ItemEntity> drops) {
        // Capture first: vanilla spawnAtLocation discards addFreshEntity's boolean result.
        var previous = original.captureDrops();
        var staged = new java.util.ArrayList<ItemEntity>();
        ItemEntity entity;
        original.captureDrops(staged);
        try {
            entity = original.spawnAtLocation(stack);
        } finally {
            original.captureDrops(previous);
            drops.addAll(staged);
        }
        if (entity == null || !staged.contains(entity))
            throw new IllegalStateException("Detached equipment was not captured");
        for (var item : staged) {
            if (previous != null) previous.add(item);
            else if (!level.addFreshEntity(item))
                throw new IllegalStateException("Detached equipment spawn rejected");
        }
    }
}
