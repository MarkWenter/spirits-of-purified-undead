package dev.purifiedundead.mixin;

import com.google.gson.*;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

/** Production Forge resolves annotations with JVM internal class names, not Java dotted names. */
class ProductionRefmapTest {
    @Test
    void allMixinMappingsUseRuntimeClassKeysInBothContexts() throws Exception {
        try (var input = getClass().getResourceAsStream("/purified_undead.refmap.json")) {
            assertNotNull(input);
            var root =
                    JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8))
                            .getAsJsonObject();
            var mappings = root.getAsJsonObject("mappings");
            assertEquals(mappings, root.getAsJsonObject("data").getAsJsonObject("searge"));
            assertEquals(5, mappings.size());
            for (String key : mappings.keySet()) {
                assertTrue(key.startsWith("dev/purifiedundead/mixin/"), key);
                assertFalse(key.contains("."), key);
                assertNotNull(getClass().getResource("/" + key + ".class"), key);
            }
            var kill = mappings.getAsJsonObject("dev/purifiedundead/mixin/MemoryKillCriteriaMixin");
            assertEquals(
                    "m_48130_(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/storage/loot/LootContext;Lnet/minecraft/world/damagesource/DamageSource;)Z",
                    kill.get(
                                    "matches(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/storage/loot/LootContext;Lnet/minecraft/world/damagesource/DamageSource;)Z")
                            .getAsString());
            assertEquals(
                    "f_268479_:Lnet/minecraft/tags/TagKey;",
                    mappings.getAsJsonObject("dev/purifiedundead/mixin/MemoryTagPredicateAccessor")
                            .get("tag")
                            .getAsString());
        }
    }
}
