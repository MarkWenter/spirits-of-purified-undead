package dev.purifiedundead.slate;

import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.damagesource.DamageSource;

/** The extra identities exist only inside a loot/kill criterion evaluation, never combat. */
public final class MemoryDamageCriteria {
    private static final ThreadLocal<DamageSource> SOURCE = new ThreadLocal<>();

    public static boolean active(DamageSource source) {
        return SOURCE.get() == source;
    }

    public static boolean matches(
            DamageSourcePredicate predicate,
            ServerLevel level,
            Vec3 position,
            DamageSource source) {
        return evaluate(source, () -> predicate.matches(level, position, source));
    }

    /** Preserve the wrapped operation so other mods can participate in the same criterion. */
    public static boolean evaluate(
            DamageSource source, java.util.function.BooleanSupplier operation) {
        if (!(source.getEntity() instanceof net.minecraft.server.level.ServerPlayer p)
                || !MemoryStorage.active(p, "hoenir")) return operation.getAsBoolean();
        DamageSource previous = SOURCE.get();
        SOURCE.set(source);
        try {
            return operation.getAsBoolean();
        } finally {
            if (previous == null) SOURCE.remove();
            else SOURCE.set(previous);
        }
    }

    private MemoryDamageCriteria() {}
}
