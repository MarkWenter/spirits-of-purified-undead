package dev.purifiedundead.entity;

/** Shared stage-to-animation contract used by the synchronized entity and client renderer. */
public final class FerinVisualModel {
    private FerinVisualModel() {}

    public static String swordSocketForStage(int stage) {
        return stage == 1 ? "sword" : "sword_legacy";
    }

    public static String animationForStage(int stage) {
        if (stage < 1 || stage > 5) {
            return "animation.ferin.idle";
        }
        return "animation.ferin.stage_" + stage;
    }
}
