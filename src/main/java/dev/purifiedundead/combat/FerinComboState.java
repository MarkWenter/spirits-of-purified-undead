package dev.purifiedundead.combat;

/**
 * Pure server-side combo state. It never decides whether an attack qualifies;
 * callers must only submit verified player direct-melee damage events.
 */
public final class FerinComboState {
    public enum Result { STARTED, ADVANCED, LOCKED, WINDOW_EXPIRED, MAX_STAGE, COOLDOWN }

    private int stage;
    private long stageStartedAt;
    private long cooldownUntil;
    private long exitUntil;
    private boolean exiting;

    public Result onQualifyingHit(long gameTick, int maximumStages, FerinComboTimings timings) {
        validateMaximumStages(maximumStages);
        tick(gameTick, maximumStages, timings);

        if (exiting || (stage == 0 && gameTick < cooldownUntil)) {
            return Result.COOLDOWN;
        }
        if (stage == 0) {
            startStage(1, gameTick, timings);
            return Result.STARTED;
        }

        long age = gameTick - stageStartedAt;
        if (stage >= maximumStages) {
            return Result.MAX_STAGE;
        }
        if (age < timings.comboLock()) {
            return Result.LOCKED;
        }
        if (age >= timings.comboWindow()) {
            beginExit(gameTick, timings);
            return Result.WINDOW_EXPIRED;
        }

        startStage(stage + 1, gameTick, timings);
        return Result.ADVANCED;
    }

    public void tick(long gameTick, int maximumStages, FerinComboTimings timings) {
        validateMaximumStages(maximumStages);
        if (exiting) {
            if (gameTick >= exitUntil) {
                exiting = false;
                stage = 0;
            }
            return;
        }
        if (stage == 0) {
            return;
        }

        long age = gameTick - stageStartedAt;
        long endAt = stage >= maximumStages ? timings.finalActionDuration() : timings.comboWindow();
        if (age >= endAt) {
            beginExit(gameTick, timings);
        }
    }

    /** Stop a spatial attack when its owner changes lifecycle; retain the summon cooldown. */
    public void cancelActive() {
        stage = 0;
        exiting = false;
        exitUntil = 0;
    }

    private void startStage(int newStage, long gameTick, FerinComboTimings timings) {
        stage = newStage;
        stageStartedAt = gameTick;
        cooldownUntil = gameTick + timings.summonCooldown();
        exiting = false;
    }

    private void beginExit(long gameTick, FerinComboTimings timings) {
        exiting = true;
        exitUntil = gameTick + timings.exitDuration();
    }

    private static void validateMaximumStages(int maximumStages) {
        if (maximumStages < 1 || maximumStages > 5) {
            throw new IllegalArgumentException("Ferin supports one to five stages");
        }
    }

    public int stage() { return stage; }
    public long stageStartedAt() { return stageStartedAt; }
    public long cooldownUntil() { return cooldownUntil; }
    public boolean exiting() { return exiting; }
    public boolean ready(long gameTick) { return stage == 0 && !exiting && gameTick >= cooldownUntil; }

    public Snapshot snapshot() {
        return new Snapshot(stage, stageStartedAt, cooldownUntil, exitUntil, exiting);
    }

    public static FerinComboState restore(Snapshot snapshot) {
        if (snapshot.stage() < 0 || snapshot.stage() > 5) {
            throw new IllegalArgumentException("Saved Ferin stage must be between zero and five");
        }
        var state = new FerinComboState();
        state.stage = snapshot.stage();
        state.stageStartedAt = snapshot.stageStartedAt();
        state.cooldownUntil = snapshot.cooldownUntil();
        state.exitUntil = snapshot.exitUntil();
        state.exiting = snapshot.exiting();
        return state;
    }

    public record Snapshot(int stage, long stageStartedAt, long cooldownUntil, long exitUntil, boolean exiting) { }
}
