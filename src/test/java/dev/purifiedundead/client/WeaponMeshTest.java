package dev.purifiedundead.client;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

class WeaponMeshTest {
    private static final String QUAD = "[[0,0,0,0,0],[1,0,0,1,0],[1,1,0,1,1],[0,1,0,0,1]]";

    private static ByteArrayInputStream input(String text) {
        return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void readsAllFiveProductionMeshesWithoutChangingTheirGeometry() throws IOException {
        var files =
                Map.of(
                        "echo/ferin_sword",
                        140,
                        "guardian_wave/siegrid_flail",
                        580,
                        "guardian_wave/silva_hammer",
                        352,
                        "guardian_wave/groth_maul",
                        312,
                        "guardian_wave/julius_lance",
                        300);
        var assets = Path.of("src/main/resources/assets/purified_undead");
        for (var entry : files.entrySet()) {
            try (var stream = Files.newInputStream(assets.resolve(entry.getKey() + ".json"))) {
                var faces = WeaponMesh.read(stream);
                assertEquals(entry.getValue().intValue(), faces.size());
                for (var face : faces) assertEquals(1F, face.normal().length(), 1e-5F);
            }
        }
        var face = WeaponMesh.read(input("[" + QUAD + "]")).get(0);
        assertArrayEquals(new float[] {1, 1, 0, 1, 1}, face.vertices()[2]);
        assertEquals(1F, face.normal().z);
    }

    @Test
    void limitsBytesAndFacesIndependently() throws IOException {
        assertThrows(
                IOException.class,
                () ->
                        WeaponMesh.read(
                                new ByteArrayInputStream(new byte[WeaponMesh.MAX_BYTES + 1])));
        var atLimit = String.join(",", Collections.nCopies(WeaponMesh.MAX_FACES, QUAD));
        assertEquals(WeaponMesh.MAX_FACES, WeaponMesh.read(input("[" + atLimit + "]")).size());
        assertThrows(
                IOException.class, () -> WeaponMesh.read(input("[" + atLimit + "," + QUAD + "]")));
    }

    @Test
    void rejectsIncompleteFacesAndTrailingDataBeforeReturningAPartialMesh() throws IOException {
        assertTrue(WeaponMesh.read(input("[]")).isEmpty());
        for (String text :
                new String[] {
                    "[" + QUAD + ",[]]",
                    "[" + QUAD + "][]",
                    "[" + QUAD.replace("1,1,0,1,1", "1,1,0,1") + "]",
                    "[" + QUAD.replace("1,1,0,1,1", "0,0,0,1,1") + "]",
                    "[" + QUAD.replace("1,1,0,1,1", "1e100,1,0,1,1") + "]"
                }) {
            assertThrows(IOException.class, () -> WeaponMesh.read(input(text)));
        }
    }
}
