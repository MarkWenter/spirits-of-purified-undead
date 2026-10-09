package dev.purifiedundead.client;

/** Pure input edge rule that keeps the first jump press distinct from the double jump. */
final class GuardianInputModel {
    private GuardianInputModel() {}

    static boolean shouldDoubleJump(
            boolean jumpDown,
            boolean jumpWasDown,
            boolean jumpReleasedThisAir,
            boolean jumpedThisAir) {
        return jumpReleasedThisAir && jumpDown && !jumpWasDown && !jumpedThisAir;
    }
}
