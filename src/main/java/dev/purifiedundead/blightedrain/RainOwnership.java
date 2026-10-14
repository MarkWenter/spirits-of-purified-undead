package dev.purifiedundead.blightedrain;

import dev.purifiedundead.mixin.RainFoxAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.Fox;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/** Explicit owner adapters; never searches arbitrary NBT or resolves an online owner per tick. */
public final class RainOwnership {
    private static final List<Predicate<LivingEntity>> ADAPTERS = new ArrayList<>();

    private RainOwnership() {}

    public static void registerAdapter(Predicate<LivingEntity> adapter) {
        ADAPTERS.add(java.util.Objects.requireNonNull(adapter));
    }

    public static boolean protectedOwner(LivingEntity mob) {
        if (mob instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null) return true;
        if (mob instanceof Fox fox
                && ((RainFoxAccessor) fox)
                        .purifiedUndead$trusted().stream().anyMatch(java.util.Objects::nonNull))
            return true;
        for (var adapter : ADAPTERS) if (adapter.test(mob)) return true;
        return false;
    }
}
