package dev.purifiedundead.blightedrain;

import com.google.gson.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.ResourceLocation;
import java.util.Set;
import java.util.HashSet;
import java.nio.file.Files;

/** Small optional immunity file, independent of the deferred future full weather settings UI. */
public final class RainImmunity {
    private static Set<ResourceLocation> immune = Set.of();

    private RainImmunity() {}

    public static boolean contains(EntityType<?> type) {
        return immune.contains(BuiltInRegistries.ENTITY_TYPE.getKey(type));
    }

    public static void load() {
        var path =
                net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR
                        .get()
                        .resolve("purified_undead-blighted-rain-immunity.json");
        try {
            if (!Files.exists(path)) Files.writeString(path, "{\"immuneEntityTypes\": []}\n");
            if (Files.size(path) > 65536)
                throw new IllegalArgumentException("Immunity file exceeds 64 KiB");
            var array =
                    JsonParser.parseString(Files.readString(path))
                            .getAsJsonObject()
                            .getAsJsonArray("immuneEntityTypes");
            if (array == null || array.size() > 1024)
                throw new IllegalArgumentException("Expected at most 1024 immuneEntityTypes");
            var next = new HashSet<ResourceLocation>();
            for (var value : array) {
                if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString())
                    throw new IllegalArgumentException("Expected entity ID string");
                String name = value.getAsString();
                var id = name.length() <= 256 ? ResourceLocation.tryParse(name) : null;
                if (id == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(id))
                    throw new IllegalArgumentException("Unknown entity ID: " + name);
                next.add(id);
            }
            immune = Set.copyOf(next);
        } catch (java.io.IOException | RuntimeException failure) {
            com.mojang.logging.LogUtils.getLogger()
                    .warn(
                            "Rain immunity configuration rejected; previous values retained",
                            failure);
        }
    }

    public static void clear() {
        immune = Set.of();
    }
}
