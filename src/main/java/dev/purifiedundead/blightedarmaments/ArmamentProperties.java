package dev.purifiedundead.blightedarmaments;

import com.mojang.logging.LogUtils;

import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

final class ArmamentProperties {
    private final String file;
    private final Properties values;

    ArmamentProperties(String file) {
        this.file = file;
        this.values = load();
    }

    private Properties load() {
        Path path = FMLPaths.CONFIGDIR.get().resolve(file);
        Properties values = new Properties();
        try {
            if (!Files.exists(path)) {
                try (var out = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW);
                        var template =
                                ArmamentProperties.class.getResourceAsStream("/defaults/" + file)) {
                    if (template == null)
                        throw new IllegalStateException("Missing bundled " + file);
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
                            "Could not read {}. Using configured defaults for this launch.",
                            path,
                            failure);
            values.clear();
        }
        return values;
    }

    int integer(String key, int fallback, int min, int max) {
        String raw = values.getProperty(key);
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
                        file,
                        min,
                        max,
                        fallback);
        return fallback;
    }

    float decimal(String key, float fallback, float min, float max) {
        String raw = values.getProperty(key);
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
                        file,
                        min,
                        max,
                        fallback);
        return fallback;
    }
}
