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
    private static final String MODEL = "purified_undead:block/purification_foundry";

    private record Face(List<Long> bounds, String direction, List<Double> uv) {}

    @Test
    void bakedModelUsesTheBedrockCuboidsAndFaceUvs() throws Exception {
        JsonObject geometry = read("geo/block/purification_foundry.geo.json")
                .getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
        JsonObject description = geometry.getAsJsonObject("description");
        double unitU = 16.0 / description.get("texture_width").getAsDouble();
        double unitV = 16.0 / description.get("texture_height").getAsDouble();
        Map<Face, Integer> bedrock = new HashMap<>();
        for (JsonElement bone : geometry.getAsJsonArray("bones")) {
            JsonArray cubes = bone.getAsJsonObject().getAsJsonArray("cubes");
            if (cubes == null) continue;
            for (JsonElement entry : cubes) {
                JsonObject cube = entry.getAsJsonObject();
                float[] origin = vector(cube, "origin");
                float[] size = vector(cube, "size");
                // Bedrock measures X in the opposite direction, and centres X/Z inside the block.
                List<Long> bounds = bounds(8-origin[0]-size[0], origin[1], 8+origin[2],
                        8-origin[0], origin[1]+size[1], 8+origin[2]+size[2]);
                for (var face : cube.getAsJsonObject("uv").entrySet()) {
                    String side = face.getKey();
                    JsonArray uv = face.getValue().getAsJsonObject().getAsJsonArray("uv");
                    JsonArray span = face.getValue().getAsJsonObject().getAsJsonArray("uv_size");
                    double u = uv.get(0).getAsDouble(), v = uv.get(1).getAsDouble();
                    double endU = u + span.get(0).getAsDouble();
                    double endV = v + span.get(1).getAsDouble();
                    // In the GeckoLib export, the horizontal faces start at the opposite UV corner.
                    List<Double> rect = side.equals("up") || side.equals("down")
                            ? List.of(endU * unitU, endV * unitV, u * unitU, v * unitV)
                            : List.of(u * unitU, v * unitV, endU * unitU, endV * unitV);
                    bedrock.merge(new Face(bounds, side, rect), 1, Integer::sum);
                }
            }
        }

        Map<Face, Integer> baked = new HashMap<>();
        for (JsonElement entry : read("models/block/purification_foundry.json").getAsJsonArray("elements")) {
            JsonObject element = entry.getAsJsonObject();
            float[] from = vector(element, "from");
            float[] to = vector(element, "to");
            List<Long> bounds = bounds(from[0], from[1], from[2], to[0], to[1], to[2]);
            for (var face : element.getAsJsonObject("faces").entrySet()) {
                JsonObject data = face.getValue().getAsJsonObject();
                assertEquals("#skin", data.get("texture").getAsString());
                JsonArray uv = data.getAsJsonArray("uv");
                List<Double> rect = List.of(uv.get(0).getAsDouble(), uv.get(1).getAsDouble(),
                        uv.get(2).getAsDouble(), uv.get(3).getAsDouble());
                baked.merge(new Face(bounds, face.getKey(), rect), 1, Integer::sum);
            }
        }
        assertFalse(bedrock.isEmpty());
        assertEquals(bedrock, baked);
    }

    @Test
    void itemAndAllEightBlockStatesUseTheCorrectModelAndSkin() throws Exception {
        assertEquals(MODEL, read("models/item/purification_foundry.json").get("parent").getAsString());
        JsonObject base = read("models/block/purification_foundry.json");
        JsonObject lit = read("models/block/purification_foundry_lit.json");
        assertEquals(MODEL, lit.get("parent").getAsString());
        assertEquals(MODEL, base.getAsJsonObject("textures").get("skin").getAsString());
        assertEquals(MODEL + "_lit", lit.getAsJsonObject("textures").get("skin").getAsString());

        JsonObject variants = read("blockstates/purification_foundry.json").getAsJsonObject("variants");
        List<String> directions = List.of("north", "east", "south", "west");
        for (int i = 0; i < directions.size(); i++) {
            for (boolean working : List.of(false, true)) {
                String key = "facing=" + directions.get(i) + ",lit=" + working;
                JsonObject variant = variants.getAsJsonObject(key);
                assertEquals(working ? MODEL + "_lit" : MODEL, variant.get("model").getAsString(), key);
                assertEquals(90 * i, variant.has("y") ? variant.get("y").getAsInt() : 0, key);
            }
        }
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
