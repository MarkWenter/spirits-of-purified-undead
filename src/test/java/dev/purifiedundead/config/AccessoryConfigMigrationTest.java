package dev.purifiedundead.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class AccessoryConfigMigrationTest {
    @TempDir Path dir;

    @Test
    void preservesCustomizedValuesAndExistingDestination() throws Exception {
        String old =
                "balanceRevision = 2\n[ferin] # custom section comment\nlifestealRatio = 0.73\n[compatibility]\ncurios = true\n[whiteWitchRelics]\nglobalEffectScale = 1.7\n";
        Files.writeString(dir.resolve("purified_undead-common.toml"), old);
        AccessoryConfigMigration.prepare(dir);
        Path dest = dir.resolve("purified_undead-accessories.toml");
        String copied = Files.readString(dest);
        assertTrue(copied.contains("lifestealRatio = 0.73"));
        assertTrue(copied.contains("globalEffectScale = 1.7"));
        assertFalse(copied.contains("curios"));
        assertEquals(
                old,
                Files.readString(dir.resolve("purified_undead-common.before-accessories.toml")));
        Files.writeString(dest, "# customized independently\n");
        AccessoryConfigMigration.prepare(dir);
        assertEquals("# customized independently\n", Files.readString(dest));
    }

    @Test
    void catalogsSeparateBalanceFromGeneralSettings() {
        assertTrue(ConfigCatalog.values("accessories").containsKey("ferin.lifestealRatio"));
        assertTrue(
                ConfigCatalog.values("accessories")
                        .containsKey("individual.lily_diary.luckPerUndeadType"));
        assertFalse(ConfigCatalog.values("common").containsKey("ferin.lifestealRatio"));
        assertTrue(ConfigCatalog.values("common").containsKey("contract.warriorSlots"));
    }
}
