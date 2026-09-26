package dev.purifiedundead.content;

import java.util.List;

/** Canonical contract capacity, warrior roster, and blight roster. */
public final class ContractRoster {
    public static final int WARRIOR_SLOTS = 8;
    public static final List<String> WARRIOR_ITEM_IDS = List.of(
            "ferin_warrior", "groth_warrior", "julius_warrior", "guardian_warriors",
            "ulv_warrior", "eleine_warrior", "hoenir_warrior", "faden_warrior");
    public static final List<String> BLIGHT_IDS = List.of(
            "ancient_blight", "elder_warrior_blight", "guardian_blight", "knight_captain_blight",
            "mad_knight_blight", "dark_witch_blight", "abyss_guardian_blight", "heretic_blight");

    private ContractRoster() {
    }
}
