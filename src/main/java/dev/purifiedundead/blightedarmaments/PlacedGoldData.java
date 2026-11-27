package dev.purifiedundead.blightedarmaments;

import dev.purifiedundead.blightedrain.BlightedRainData;
import dev.purifiedundead.blightedrain.RainExposure;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

/** Only player-placed normal gold ore is tracked. Exposure is continuous loaded time. */
public final class PlacedGoldData extends SavedData {
    /** Historical default retained for source compatibility. */
    @Deprecated public static final int REQUIRED_TICKS = 200;
    private final Map<Long, Integer> positions = new HashMap<>();

    public static PlacedGoldData get(ServerLevel level) {
        return level.getDataStorage()
                .computeIfAbsent(
                        PlacedGoldData::load, PlacedGoldData::new, "purified_undead_placed_gold");
    }

    public static PlacedGoldData load(CompoundTag tag) {
        var data = new PlacedGoldData();
        for (long pos : tag.getLongArray("positions")) data.positions.put(pos, 0);
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putLongArray(
                "positions", positions.keySet().stream().mapToLong(Long::longValue).toArray());
        return tag;
    }

    public void placed(BlockPos pos) {
        positions.put(pos.asLong(), 0);
        setDirty();
    }

    public void removed(BlockPos pos) {
        if (positions.remove(pos.asLong()) != null) setDirty();
    }

    public void tick(ServerLevel level) {
        if (positions.isEmpty()) return;
        boolean active = BlightedRainData.get(level).isActive();
        for (var it = positions.entrySet().iterator(); it.hasNext(); ) {
            var entry = it.next();
            BlockPos pos = BlockPos.of(entry.getKey());
            // Do not force-load chunks or count unloaded time toward the ten seconds.
            if (!level.hasChunkAt(pos)) {
                entry.setValue(0);
                continue;
            }
            if (!level.getBlockState(pos).is(Blocks.GOLD_ORE)) {
                it.remove();
                setDirty();
                continue;
            }
            int progress =
                    active && RainExposure.exposedAt(level, pos.above()) ? entry.getValue() + 1 : 0;
            if (progress >= BreathgoldSettings.CONVERSION_TICKS) {
                level.setBlockAndUpdate(pos, BreathgoldContent.ORE.get().defaultBlockState());
                it.remove();
                setDirty();
            } else entry.setValue(progress);
        }
    }
}
