package dev.purifiedundead.content.relic;

public enum RelicKind {
    BLOODSTAINED_RIBBON("bloodstained_ribbon"),
    ANCIENT_DRAGON_CLAW("ancient_dragon_claw"),
    WEATHERED_WARRIOR_NECKLACE("weathered_warrior_necklace"),
    SOILED_SILVER_ROSARY("soiled_silver_rosary"),
    WHITE_PRIESTESS_STATUE("white_priestess_statue"),
    BLIGHTED_FINGER("blighted_finger"),
    KINGS_SHIELD_BADGE("kings_shield_badge"),
    WHITE_PRIESTESS_EARRINGS("white_priestess_earrings");

    public final String id;
    RelicKind(String id) { this.id = id; }
}
