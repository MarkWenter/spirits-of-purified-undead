package dev.purifiedundead.blightedarmaments;

import com.mojang.logging.LogUtils;

import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

/** Registry-time settings. A single immutable snapshot is shared by tools and ore tracking. */
public final class BreathgoldSettings {
    private static final String FILE = "purified_undead-breathgold.properties";
    private static final Properties VALUES = load();
    public static final int CONVERSION_TICKS = integer("conversionTicks", 200, 1, 1200000);
    static final int MATERIAL_STACK = integer("materialStackSize", 64, 1, 64);
    static final int ORE_STACK = integer("oreStackSize", 64, 1, 64);
    static final int DURABILITY = integer("toolDurability", 375, 1, 1000000);
    static final float MINING_SPEED = decimal("miningSpeed", 6, 0, 1024);
    static final int ENCHANTABILITY = integer("enchantability", 14, 0, 255);
    static final float TIER_DAMAGE = decimal("tierDamageBonus", 2, 0, 1024);

    private BreathgoldSettings() {}

    private static Properties load() {
        Path path = FMLPaths.CONFIGDIR.get().resolve(FILE);
        Properties values = new Properties();
        try {
            if (!Files.exists(path)) {
                try (var out = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW);
                        var template =
                                BreathgoldSettings.class.getResourceAsStream("/defaults/" + FILE)) {
                    if (template == null)
                        throw new IllegalStateException("Missing bundled " + FILE);
                    template.transferTo(out);
                } catch (FileAlreadyExistsException concurrentCreation) {
                    // Another launcher using this instance created the configuration first.
                }
            }
            try (var reader = Files.newBufferedReader(path)) {
                values.load(reader);
            }
        } catch (IOException | IllegalArgumentException failure) {
            LogUtils.getLogger()
                    .warn(
                            "Could not read {}. Using Breathgold defaults for this launch.",
                            path,
                            failure);
            values.clear();
        }
        return values;
    }

    private static int integer(String key, int fallback, int min, int max) {
        String raw = VALUES.getProperty(key);
        if (raw == null) return fallback;
        try {
            int value = Integer.parseInt(raw.trim());
            if (value >= min && value <= max) return value;
        } catch (NumberFormatException ignored) {
            // Report malformed and out-of-range values at the same boundary.
        }
        LogUtils.getLogger()
                .warn(
                        "Invalid {} in {}. Expected {}..{}, using {}.",
                        key,
                        FILE,
                        min,
                        max,
                        fallback);
        return fallback;
    }

    private static float decimal(String key, float fallback, float min, float max) {
        String raw = VALUES.getProperty(key);
        if (raw == null) return fallback;
        try {
            float value = Float.parseFloat(raw.trim());
            if (Float.isFinite(value) && value >= min && value <= max) return value;
        } catch (NumberFormatException ignored) {
            // Use the same fallback as for out-of-range numbers.
        }
        LogUtils.getLogger()
                .warn(
                        "Invalid {} in {}. Expected {}..{}, using {}.",
                        key,
                        FILE,
                        min,
                        max,
                        fallback);
        return fallback;
    }

    static int damage(String tool, int fallback) {
        return integer(tool + ".damage", fallback, -2, 1024);
    }

    static float attackSpeed(String tool, float fallback) {
        return decimal(tool + ".attackSpeed", fallback, -3.99F, 16);
    }

    static float shovelDamage() {
        return decimal("shovel.damage", 1.5F, -2, 1024);
    }
}
