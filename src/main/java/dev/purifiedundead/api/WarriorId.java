package dev.purifiedundead.api;

import dev.purifiedundead.content.ModItems;
import net.minecraft.world.item.Item;

/** Stable acquisition identifiers. Guardians is the single paired-warrior reward. */
public enum WarriorId {
    FERIN,
    GROTH,
    JULIUS,
    GUARDIANS,
    ULV,
    ELEINE,
    HOENIR,
    FADEN;

    public String id() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }

    public Item item() {
        return switch (this) {
            case FERIN -> ModItems.FERIN_WARRIOR.get();
            case GROTH -> ModItems.GROTH_WARRIOR.get();
            case JULIUS -> ModItems.JULIUS_WARRIOR.get();
            case GUARDIANS -> ModItems.GUARDIAN_WARRIORS.get();
            case ULV -> ModItems.ULV_WARRIOR.get();
            case ELEINE -> ModItems.ELEINE_WARRIOR.get();
            case HOENIR -> ModItems.HOENIR_WARRIOR.get();
            case FADEN -> ModItems.FADEN_WARRIOR.get();
        };
    }
}
