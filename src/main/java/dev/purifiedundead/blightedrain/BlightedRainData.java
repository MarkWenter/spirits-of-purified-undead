package dev.purifiedundead.blightedrain;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class BlightedRainData extends SavedData {
    final BlightedRainModel model = new BlightedRainModel();
    private boolean weatherLocked = true, enabled = true;
    private boolean initialized;
    long lastProcessedTime = Long.MIN_VALUE;

    public boolean weatherLocked() {
        return weatherLocked;
    }

    public boolean enabled() {
        return enabled;
    }

    public void setWeatherLocked(boolean value) {
        weatherLocked = value;
        setDirty();
    }

    public void setEnabled(boolean value) {
        enabled = value;
        setDirty();
    }

    public static BlightedRainData get(ServerLevel level) {
        if (level.dimension() != net.minecraft.world.level.Level.OVERWORLD)
            throw new IllegalArgumentException("Overworld state only");
        BlightedRainData result =
                level.getDataStorage()
                        .computeIfAbsent(
                                BlightedRainData::load,
                                BlightedRainData::new,
                                "purified_undead_blighted_rain");
        if (!result.initialized) {
            result.model.restore(
                    result.loadedPhase,
                    result.loadedDeadline,
                    result.loadedDuration,
                    level.getGameTime());
            result.initialized = true;
        }
        return result;
    }

    private int loadedPhase, loadedDuration = 6000;
    private long loadedDeadline;

    static BlightedRainData load(CompoundTag tag) {
        var d = new BlightedRainData();
        d.weatherLocked = !tag.contains("weatherLocked") || tag.getBoolean("weatherLocked");
        d.enabled = !tag.contains("enabled") || tag.getBoolean("enabled");
        d.loadedPhase = tag.getInt("phase");
        d.loadedDeadline = tag.getLong("deadline");
        d.loadedDuration = tag.getInt("duration");
        return d;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("weatherLocked", weatherLocked);
        tag.putBoolean("enabled", enabled);
        tag.putInt("phase", model.phase().ordinal());
        tag.putLong("deadline", model.deadline());
        tag.putInt("duration", model.duration());
        return tag;
    }
}
