package dev.purifiedundead.config;

import net.minecraftforge.common.ForgeConfigSpec;
import java.util.*;

/** Read-only catalog of existing keys; does not rewrite files or change client-local settings. */
public final class ConfigCatalog {
    private static final Map<String, ForgeConfigSpec.ConfigValue<?>> COMMON =
            collect(PurifiedUndeadConfig.VALUES);
    private static final Map<String, ForgeConfigSpec.ConfigValue<?>> SLATE =
            collect(dev.purifiedundead.slate.SlateConfig.class);

    private ConfigCatalog() {}

    private static Map<String, ForgeConfigSpec.ConfigValue<?>> collect(Object owner) {
        var result = new TreeMap<String, ForgeConfigSpec.ConfigValue<?>>();
        Class<?> type = owner instanceof Class<?> c ? c : owner.getClass();
        for (var field : type.getFields())
            try {
                Object value = field.get(owner instanceof Class<?> ? null : owner);
                if (value instanceof ForgeConfigSpec.ConfigValue<?> v)
                    result.put(String.join(".", v.getPath()), v);
            } catch (IllegalAccessException e) {
                throw new ExceptionInInitializerError(e);
            }
        return Collections.unmodifiableMap(result);
    }

    public static Map<String, ForgeConfigSpec.ConfigValue<?>> values(String file) {
        return switch (file) {
            case "common" -> COMMON;
            case "slate" -> SLATE;
            default -> throw new IllegalArgumentException("Use common or slate");
        };
    }

    public static Object value(String file, String key) {
        var v = values(file).get(key);
        if (v == null) throw new IllegalArgumentException("Unknown config key");
        return PurifiedUndeadConfig.get(v);
    }
}
