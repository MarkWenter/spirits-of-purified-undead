package dev.purifiedundead.slate;

import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class MemoryDropChance {
    public static boolean applies(LootContext c) {
        if (!(c.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof LivingEntity victim)
                || victim instanceof Player
                || c.hasParam(LootContextParams.BLOCK_STATE)
                || !c.hasParam(LootContextParams.DAMAGE_SOURCE)) return false;
        var source = c.getParamOrNull(LootContextParams.DAMAGE_SOURCE);
        return source != null
                && source.getEntity() instanceof ServerPlayer p
                && MemoryStorage.active(p, "faden");
    }

    private MemoryDropChance() {}
}
