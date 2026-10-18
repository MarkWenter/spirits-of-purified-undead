package dev.purifiedundead.config;

import java.nio.file.*;
import java.io.IOException;
import java.util.Set;

/** Copy old balance sections once before either config spec loads; never overwrite user edits. */
public final class AccessoryConfigMigration {
    private AccessoryConfigMigration() {}

    public static final Set<String> SECTIONS =
            Set.of(
                    "talisman",
                    "ferin",
                    "groth",
                    "julius",
                    "guardians",
                    "ulv",
                    "eleine",
                    "hoenir",
                    "faden",
                    "whiteWitchRelics",
                    "individual");

    public static boolean accessory(String key) {
        return SECTIONS.contains(key.split("\\.")[0]);
    }

    public static void prepare(Path directory) {
        Path old = directory.resolve("purified_undead-common.toml");
        Path next = directory.resolve("purified_undead-accessories.toml");
        if (Files.exists(next) || !Files.isRegularFile(old)) return;
        try {
            var source =
                    new com.electronwill.nightconfig.toml.TomlParser().parse(Files.readString(old));
            var migrated = com.electronwill.nightconfig.core.CommentedConfig.inMemory();
            for (String section : SECTIONS) {
                if (source.contains(section)) migrated.set(section, source.get(section));
            }
            Path slate = directory.resolve("purified_undead-slate.toml");
            if (Files.isRegularFile(slate)) {
                var previous =
                        new com.electronwill.nightconfig.toml.TomlParser()
                                .parse(Files.readString(slate));
                if (previous.contains("slate.shiningGuardianHealthBonus")) {
                    migrated.set(
                            "individual.shining_guardian_treasure.maxHealthBonus",
                            previous.get("slate.shiningGuardianHealthBonus"));
                    Path slateBackup =
                            directory.resolve("purified_undead-slate.before-accessories.toml");
                    if (!Files.exists(slateBackup)) Files.copy(slate, slateBackup);
                }
            }
            String out = new com.electronwill.nightconfig.toml.TomlWriter().writeToString(migrated);
            Path backup = directory.resolve("purified_undead-common.before-accessories.toml");
            if (!Files.exists(backup)) Files.copy(old, backup);
            Files.writeString(next, out, StandardOpenOption.CREATE_NEW);
        } catch (IOException failure) {
            throw new IllegalStateException(
                    "Cannot safely migrate accessory config: " + next, failure);
        }
    }
}
