package dev.purifiedundead.blightedrain;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class BlightedRainData extends SavedData {
    final BlightedRainModel model = new BlightedRainModel();
    private boolean weatherLocked = true, enabled = true;
    private boolean initialized;
    boolean forced;
    boolean wasSuppressed;
    private boolean initialEmblemEligible;
    private final java.util.Set<java.util.UUID> emblemRecipients = new java.util.HashSet<>();

    private static BlightedRainData create(ServerLevel level) {
        var data = new BlightedRainData();
        data.weatherLocked = level.getGameRules().getBoolean(RainWorldOptions.ETERNAL_RAIN);
        data.initialEmblemEligible = !data.weatherLocked;
        data.setDirty();
        return data;
    }

    /** World ledger survives player death and prevents repeat login gifts. */
    public void welcome(net.minecraft.server.level.ServerPlayer player) {
        if (!initialEmblemEligible || emblemRecipients.contains(player.getUUID())) return;
        var gift =
                new net.minecraft.world.item.ItemStack(
                        dev.purifiedundead.blightedrain.content.RainItems.EMBLEM.get());
        if (!player.getInventory().add(gift) && !gift.isEmpty()) {
            if (player.drop(gift, false) == null) return;
        }
        emblemRecipients.add(player.getUUID());
        setDirty();
    }

    private int loadedPause = -1;
    private final java.util.Set<Long> activeBeacons = new java.util.HashSet<>();

    public boolean suppressed() {
        return !activeBeacons.isEmpty();
    }

    public void setBeacon(net.minecraft.core.BlockPos pos, boolean active) {
        boolean changed =
                active ? activeBeacons.add(pos.asLong()) : activeBeacons.remove(pos.asLong());
        if (changed) setDirty();
    }

    /** Never loads a chunk: an unloaded beacon retains its last authoritative state. */
    void validateBeacons(ServerLevel level) {
        for (var iterator = activeBeacons.iterator(); iterator.hasNext(); ) {
            long packed = iterator.next();
            var pos = net.minecraft.core.BlockPos.of(packed);
            if (level.hasChunkAt(pos)
                    && !dev.purifiedundead.blightedrain.content.StillRainBeaconBlock.isActive(
                            level, pos)) {
                iterator.remove();
                setDirty();
            }
        }
    }

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
                                () -> create(level),
                                "purified_undead_blighted_rain");
        if (!result.initialized) {
            result.model.restore(
                    result.loadedPhase,
                    result.loadedDeadline,
                    result.loadedDuration,
                    level.getGameTime());
            result.model.restorePause(result.loadedPause);
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
        d.forced = tag.getBoolean("forced");
        d.initialEmblemEligible = tag.getBoolean("initialEmblemEligible");
        for (var entry : tag.getList("emblemRecipients", 8)) {
            try {
                d.emblemRecipients.add(java.util.UUID.fromString(entry.getAsString()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        d.loadedPause = tag.contains("pausedCooldown") ? tag.getInt("pausedCooldown") : -1;
        for (long pos : tag.getLongArray("activeBeacons")) d.activeBeacons.add(pos);
        return d;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("weatherLocked", weatherLocked);
        tag.putBoolean("enabled", enabled);
        tag.putInt("phase", model.phase().ordinal());
        tag.putLong("deadline", model.deadline());
        tag.putInt("duration", model.duration());
        tag.putBoolean("forced", forced);
        tag.putBoolean("initialEmblemEligible", initialEmblemEligible);
        var recipients = new net.minecraft.nbt.ListTag();
        emblemRecipients.forEach(
                id -> recipients.add(net.minecraft.nbt.StringTag.valueOf(id.toString())));
        tag.put("emblemRecipients", recipients);
        tag.putInt("pausedCooldown", model.pausedCooldown());
        tag.putLongArray(
                "activeBeacons", activeBeacons.stream().mapToLong(Long::longValue).toArray());
        return tag;
    }
}
