package dev.purifiedundead.content;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemTextureResourcesTest {
    private static final Path ASSETS = Path.of("src/main/resources/assets/purified_undead");
    private static final Set<String> ACCESSORIES =
            Set.of(
                    "ancient_contract",
                    "ferin_warrior",
                    "groth_warrior",
                    "guardian_warriors",
                    "julius_warrior",
                    "ulv_warrior",
                    "eleine_warrior",
                    "hoenir_warrior",
                    "faden_warrior",
                    "bloodstained_ribbon",
                    "ancient_dragon_claw",
                    "weathered_warrior_necklace",
                    "soiled_silver_rosary",
                    "white_priestess_statue",
                    "blighted_finger",
                    "kings_shield_badge",
                    "white_priestess_earrings",
                    "shining_guardian_treasure",
                    "lily_diary",
                    "blighted_memory_ferin",
                    "blighted_memory_groth",
                    "blighted_memory_guardians",
                    "blighted_memory_julius",
                    "blighted_memory_ulv",
                    "blighted_memory_eleine",
                    "blighted_memory_hoenir",
                    "blighted_memory_faden");

    private static final Set<String> SMALL_ITEMS =
            Set.of(
                    "blighted_spirit",
                    "blight_fragment",
                    "snow_flower",
                    "former_ornament",
                    "tired_heart");

    @Test
    void formalItemTexturesAreMinecraftSizedAndUseTransparency() throws Exception {
        checkTextures(ACCESSORIES, 64);
        checkTextures(SMALL_ITEMS, 32);
    }

    private void checkTextures(Set<String> items, int size) throws Exception {
        for (String id : items) {
            Path path = ASSETS.resolve("textures/item/" + id + ".png");
            var image = ImageIO.read(path.toFile());
            assertNotNull(image, "Unreadable PNG " + path);
            assertEquals(size, image.getWidth(), id + " width");
            assertEquals(size, image.getHeight(), id + " height");

            int visible = 0;
            int transparent = 0;
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int alpha = image.getRGB(x, y) >>> 24;
                    if (size == 64) {
                        assertTrue(alpha == 0 || alpha == 255, id + " contains a soft pixel edge");
                    }
                    visible += alpha >= 64 ? 1 : 0;
                    transparent += alpha == 0 ? 1 : 0;
                }
            }
            assertTrue(visible >= 24, id + " must have visible content");
            assertTrue(transparent >= 128, id + " must remain an isolated sprite on transparency");
        }
    }

    @Test
    void everyFormalTextureHasMatchingGeneratedItemModel() throws Exception {
        checkModels(ACCESSORIES);
        checkModels(SMALL_ITEMS);
    }

    private void checkModels(Set<String> items) throws Exception {
        for (String id : items) {
            Path modelPath = ASSETS.resolve("models/item/" + id + ".json");
            try (var reader = Files.newBufferedReader(modelPath)) {
                JsonObject model = JsonParser.parseReader(reader).getAsJsonObject();
                assertEquals("minecraft:item/generated", model.get("parent").getAsString(), id);
                assertEquals(
                        "purified_undead:item/" + id,
                        model.getAsJsonObject("textures").get("layer0").getAsString(),
                        id);
            }
        }
    }
}
