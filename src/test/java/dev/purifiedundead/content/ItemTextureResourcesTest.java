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
    private static final Set<String> FORMAL_ITEMS =
            Set.of(
                    "ferin_warrior",
                    "groth_warrior",
                    "guardian_warriors",
                    "julius_warrior",
                    "ulv_warrior",
                    "eleine_warrior",
                    "hoenir_warrior",
                    "faden_warrior",
                    "blighted_spirit",
                    "blight_fragment",
                    "ancient_contract",
                    "snow_flower",
                    "former_ornament",
                    "tired_heart");

    @Test
    void formalItemTexturesAreMinecraftSizedAndUseTransparency() throws Exception {
        for (String id : FORMAL_ITEMS) {
            Path path = ASSETS.resolve("textures/item/" + id + ".png");
            var image = ImageIO.read(path.toFile());
            assertNotNull(image, "Unreadable PNG " + path);
            assertEquals(32, image.getWidth(), id + " width");
            assertEquals(32, image.getHeight(), id + " height");

            int visible = 0;
            int transparent = 0;
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int alpha = image.getRGB(x, y) >>> 24;
                    visible += alpha >= 64 ? 1 : 0;
                    transparent += alpha == 0 ? 1 : 0;
                }
            }
            assertTrue(visible >= 24, id + " must remain readable after 32x32 reduction");
            assertTrue(transparent >= 128, id + " must remain an isolated sprite on transparency");
        }
    }

    @Test
    void everyFormalTextureHasMatchingGeneratedItemModel() throws Exception {
        for (String id : FORMAL_ITEMS) {
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
