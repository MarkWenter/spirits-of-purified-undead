package dev.purifiedundead.foundry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class FoundryModelResourcesTest {
    private static final Path ASSETS = Path.of("src/main/resources/assets/purified_undead");

    @Test
    void inventoryAndPlacedModelsUseTheSameCuboids() throws Exception {
        JsonArray bones = read("geo/block/purification_foundry.geo.json")
                .getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject().getAsJsonArray("bones");
        Map<List<Long>, Integer> placed = new HashMap<>();
        for (JsonElement bone : bones) {
            JsonArray cubes = bone.getAsJsonObject().getAsJsonArray("cubes");
            if (cubes == null) continue;
            for (JsonElement entry : cubes) {
                JsonObject cube = entry.getAsJsonObject();
                float[] origin = vector(cube, "origin");
                float[] size = vector(cube, "size");
                // Bedrock measures X in the opposite direction. Its X/Z origin is the block centre.
                placed.merge(bounds(8-origin[0]-size[0], origin[1], 8+origin[2],
                        8-origin[0], origin[1]+size[1], 8+origin[2]+size[2]), 1, Integer::sum);
            }
        }

        Map<List<Long>, Integer> inventory = new HashMap<>();
        for (JsonElement entry : read("models/block/purification_foundry.json").getAsJsonArray("elements")) {
            float[] from = vector(entry.getAsJsonObject(), "from");
            float[] to = vector(entry.getAsJsonObject(), "to");
            inventory.merge(bounds(from[0], from[1], from[2], to[0], to[1], to[2]), 1, Integer::sum);
        }
        assertFalse(placed.isEmpty());
        assertEquals(placed, inventory);
    }

    private static JsonObject read(String resource) throws Exception {
        return JsonParser.parseString(Files.readString(ASSETS.resolve(resource))).getAsJsonObject();
    }

    private static float[] vector(JsonObject object, String key) {
        JsonArray values = object.getAsJsonArray(key);
        return new float[] {values.get(0).getAsFloat(), values.get(1).getAsFloat(), values.get(2).getAsFloat()};
    }

    private static List<Long> bounds(double x0, double y0, double z0, double x1, double y1, double z1) {
        return List.of(Math.round(x0*1000), Math.round(y0*1000), Math.round(z0*1000),
                Math.round(x1*1000), Math.round(y1*1000), Math.round(z1*1000));
    }
}
