package dev.purifiedundead.blightedrain;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;

/** Vanilla shelter semantics extended to snow. Water contact alone does not grant immunity. */
public final class RainExposure {
    private RainExposure() {}

    public static boolean precipitation(Level level, BlockPos pos) {
        return level.getBiome(pos).value().getPrecipitationAt(pos) != Biome.Precipitation.NONE;
    }

    public static boolean exposedAt(Level level, BlockPos pos) {
        return level.dimension() == Level.OVERWORLD
                && level.isRaining()
                && level.hasChunkAt(pos)
                && level.canSeeSky(pos)
                && level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).getY() <= pos.getY()
                && precipitation(level, pos);
    }

    public static boolean exposed(LivingEntity entity) {
        BlockPos feet = entity.blockPosition();
        BlockPos top = BlockPos.containing(feet.getX(), entity.getBoundingBox().maxY, feet.getZ());
        return exposedAt(entity.level(), feet) || exposedAt(entity.level(), top);
    }
}
