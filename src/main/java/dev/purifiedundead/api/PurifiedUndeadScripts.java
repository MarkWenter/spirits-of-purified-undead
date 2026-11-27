package dev.purifiedundead.api;

import dev.purifiedundead.blightedarmaments.BreathgoldSettings;
import dev.purifiedundead.blightedrain.BlightedRainData;
import dev.purifiedundead.blightedrain.RainExposure;
import dev.purifiedundead.foundry.FoundryRecipes;
import dev.purifiedundead.progress.ContractProgressService;
import dev.purifiedundead.slate.MemoryStorage;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import top.theillusivec4.curios.api.CuriosApi;

/**
 * Stable entry points for trusted server_scripts. No script-engine classes on the mod classpath.
 */
public final class PurifiedUndeadScripts {
    private PurifiedUndeadScripts() {}

    public static int breathgoldConversionTicks() {
        return BreathgoldSettings.CONVERSION_TICKS;
    }

    private static void requireServerThread(ServerLevel level) {
        if (!level.getServer().isSameThread())
            throw new IllegalStateException("Expected server thread");
    }

    public static boolean rainActive(ServerLevel level) {
        requireServerThread(level);
        return level.dimension() == Level.OVERWORLD && BlightedRainData.get(level).isActive();
    }

    public static boolean exposedToBlightedRain(ServerLevel level, BlockPos pos) {
        return rainActive(level) && RainExposure.exposedAt(level, pos);
    }

    public static boolean contractEquipped(ServerPlayer player) {
        requireServerThread(player.serverLevel());
        return ContractProgressService.hasContract(player);
    }

    public static boolean warriorEquipped(ServerPlayer player, WarriorId warrior) {
        requireServerThread(player.serverLevel());
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.isEquipped(warrior.item()))
                .orElse(false);
    }

    public static boolean memoryActive(ServerPlayer player, String warrior) {
        requireServerThread(player.serverLevel());
        return MemoryStorage.active(player, warrior);
    }

    /** Preview one batch, without consuming items or reserving fuel. */
    public static ItemStack foundryResult(ServerLevel level, ItemStack top, ItemStack bottom) {
        requireServerThread(level);
        var recipe = FoundryRecipes.find(top, bottom);
        return recipe == null ? ItemStack.EMPTY : recipe.result();
    }
}
