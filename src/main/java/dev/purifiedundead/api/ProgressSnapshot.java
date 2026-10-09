package dev.purifiedundead.api;

import java.util.Map;

/** Immutable player acquisition data; possession/equipment is deliberately separate. */
public record ProgressSnapshot(Map<WarriorId, RewardState> warriors, RewardState contract,
                               int talismanLevel, int eleineDrownedKills) {
    public ProgressSnapshot { warriors = Map.copyOf(warriors); }
    public record RewardState(boolean obtained, boolean pending) {}
}
