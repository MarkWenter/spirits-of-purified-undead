package dev.purifiedundead.client;

/** Cosmetic transforms in the existing wave's rolled frame. Model +Y points toward the head. */
enum GuardianWaveForm {
    FERIN("ferin_sword", 5.4F, .082F, 1.65F, 2.24F),
    SIEGRID("siegrid_flail", 22.5F, .069F, 0, 19.25F),
    SILVA("silva_hammer", 13.2F, .080F, -1.18F, 17.7F),
    GROTH("groth_maul", 23F, .069F, 0, 15.16F),
    JULIUS("julius_lance", 27F, .064F, 0, 15.3F);

    private static final GuardianWaveForm[] FORMS = values();
    final String asset;
    final float pivot, scale, turns, headStart;

    GuardianWaveForm(String asset, float pivot, float scale, float turns, float headStart) {
        this.asset = asset;
        this.pivot = pivot;
        this.scale = scale;
        this.turns = turns;
        this.headStart = headStart;
    }

    static GuardianWaveForm select(int ownerId, int shotId, int entityId) {
        // The predicted copy has a local entity ID, but shares owner and shot with every observer.
        // A server-only swing (shot zero) instead has one ordinary tracked entity ID everywhere.
        int seed = ownerId * 0x9e3779b9 + (shotId == 0 ? entityId : shotId);
        seed ^= seed >>> 16;
        seed *= 0x7feb352d;
        seed ^= seed >>> 15;
        seed *= 0x846ca68b;
        seed ^= seed >>> 16;
        return FORMS[Math.floorMod(seed, FORMS.length)];
    }

    float angle(float age, int lifetime) {
        return turns == 0 ? 0 : -24F + turns * 360F * age / lifetime;
    }
}
