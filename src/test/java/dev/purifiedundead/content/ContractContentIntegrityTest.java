package dev.purifiedundead.content;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContractContentIntegrityTest {
    private static final Path DATA = Path.of("src/main/resources/data");
    private static final Path ASSETS = Path.of("src/main/resources/assets/purified_undead");

    @Test
    void contractDefinesEightDistinctWarriorsAndEightDistinctBlights() {
        assertEquals(8, ContractRoster.WARRIOR_SLOTS);
        assertEquals(8, ContractRoster.WARRIOR_ITEM_IDS.size());
        assertEquals(8, new HashSet<>(ContractRoster.WARRIOR_ITEM_IDS).size());
        assertEquals(8, ContractRoster.BLIGHT_IDS.size());
        assertEquals(8, new HashSet<>(ContractRoster.BLIGHT_IDS).size());
    }

    @Test
    void curiosWarriorTagMatchesTheCanonicalRoster() throws IOException {
        Set<String> expected = new HashSet<>();
        ContractRoster.WARRIOR_ITEM_IDS.forEach(id -> expected.add("purified_undead:" + id));

        JsonObject tag = parse(DATA.resolve("curios/tags/items/undead_warrior.json"));
        assertEquals(expected, strings(tag.getAsJsonArray("values")));
        assertEquals(ContractRoster.WARRIOR_SLOTS, expected.size());
    }

    @Test
    void deathRetentionTagContainsContractAndEveryWarrior() throws IOException {
        Set<String> expected = new HashSet<>();
        expected.add("purified_undead:ancient_contract");
        for (var kind : dev.purifiedundead.content.relic.RelicKind.values())
            expected.add("purified_undead:" + kind.id);
        ContractRoster.WARRIOR_ITEM_IDS.forEach(id -> expected.add("purified_undead:" + id));

        JsonObject tag = parse(DATA.resolve("purified_undead/tags/items/accessories.json"));
        assertEquals(expected, strings(tag.getAsJsonArray("values")));
    }

    @Test
    void curiosSlotsRetainEquippedAccessoriesOnDeath() throws IOException {
        for (String slot : Set.of("ancient_contract", "undead_warrior", "white_witch_relic")) {
            JsonObject definition =
                    parse(DATA.resolve("purified_undead/curios/slots/" + slot + ".json"));
            assertEquals("ALWAYS_KEEP", definition.get("drop_rule").getAsString());
        }
    }

    @Test
    void contractTooltipDefinesOneDescriptionAndOneAnswerForEveryBlight() throws IOException {
        for (String language : Set.of("zh_cn", "en_us")) {
            JsonObject entries = parse(ASSETS.resolve("lang/" + language + ".json"));
            for (int index = 1; index <= ContractRoster.WARRIOR_SLOTS; index++) {
                assertEquals(
                        true, entries.has("item.purified_undead.ancient_contract.blight." + index));
                assertEquals(
                        true, entries.has("item.purified_undead.ancient_contract.answer." + index));
            }
            assertEquals(true, entries.has("item.purified_undead.ancient_contract.talisman"));
            assertEquals(false, entries.has("item.purified_undead.ancient_contract.blight"));
        }
    }

    private static Set<String> strings(JsonArray values) {
        Set<String> result = new HashSet<>();
        values.forEach(value -> result.add(value.getAsString()));
        return result;
    }

    private static JsonObject parse(Path path) throws IOException {
        try (var reader = Files.newBufferedReader(path)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}
