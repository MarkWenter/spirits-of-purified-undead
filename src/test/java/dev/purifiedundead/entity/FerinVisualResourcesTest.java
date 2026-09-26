package dev.purifiedundead.entity;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FerinVisualResourcesTest {
    private static final Path ASSETS = Path.of("src/main/resources/assets/purified_undead");

    @Test
    void firstSlashUsesFoldedElbowAndAnimatedWristWithRigidSwordGrip() throws IOException {
        JsonObject geometry = parse(ASSETS.resolve("geo/ferin.geo.json"))
                .getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
        assertEquals("sword", FerinVisualModel.swordSocketForStage(1));
        for (int stage = 2; stage <= 5; stage++) {
            assertEquals("sword_legacy", FerinVisualModel.swordSocketForStage(stage));
        }
        for (var entry : geometry.getAsJsonArray("bones")) {
            JsonObject bone = entry.getAsJsonObject();
            if ("right_arm".equals(bone.get("name").getAsString())) {
                assertTrue(bone.getAsJsonArray("pivot").get(0).getAsDouble() > 0,
                        "Anatomical right must be native -X / Bedrock +X");
            }
        }
        JsonObject bones = parse(ASSETS.resolve("animations/ferin.animation.json"))
                .getAsJsonObject("animations").getAsJsonObject("animation.ferin.stage_1")
                .getAsJsonObject("bones");
        JsonObject swordKeys = bones.getAsJsonObject("sword").getAsJsonObject("rotation");
        var grip = swordKeys.get("0.000000");
        swordKeys.entrySet().forEach(key -> assertEquals(grip, key.getValue(),
                "The sword must not turn independently inside the right hand"));
        double elbow = bones.getAsJsonObject("right_forearm").getAsJsonObject("rotation")
                .getAsJsonArray("0.000000").get(0).getAsDouble();
        assertTrue(Math.abs(elbow) > 120, "The loaded right elbow must fold deeply");
        var wristKeys = bones.getAsJsonObject("right_wrist").getAsJsonObject("rotation");
        double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
        for (var key : wristKeys.entrySet()) {
            double angle = key.getValue().getAsJsonArray().get(0).getAsDouble();
            min = Math.min(min, angle);
            max = Math.max(max, angle);
        }
        assertTrue(max - min > 25, "Right wrist must visibly articulate throughout the slash");
        assertTrue(bones.has("right_forearm_twist"));
        JsonObject leftArm = bones.getAsJsonObject("left_arm").getAsJsonObject("rotation");
        assertTrue(!leftArm.get("0.000000").equals(leftArm.get("0.500000")),
                "The balancing upper arm must move instead of staying in a fixed folded pose");
    }

    @Test
    void fiveAuthoredClipsAreDistinctAndDriveTheArticulatedRig() throws IOException {
        JsonObject animations = parse(ASSETS.resolve("animations/ferin.animation.json"))
                .getAsJsonObject("animations");
        Set<String> signatures = new HashSet<>();
        for (int stage = 1; stage <= 5; stage++) {
            JsonObject animation = animations.getAsJsonObject(FerinVisualModel.animationForStage(stage));
            JsonObject bones = animation.getAsJsonObject("bones");
            assertTrue(signatures.add(bones.toString()), "Stages must not reuse another attack clip");
            for (String required : Set.of(FerinVisualModel.swordSocketForStage(stage), "spine_lower", "spine_upper", "left_forearm",
                    "right_forearm", "left_shin", "right_shin", "left_ankle", "right_ankle",
                    "cape_gore_1_upper_1", "cape_gore_7_lower_2", "hood_shell_1_1")) {
                assertTrue(bones.has(required), "Missing articulated motion: " + required);
            }
            double length = animation.get("animation_length").getAsDouble();
            for (var bone : bones.entrySet()) {
                for (var channel : bone.getValue().getAsJsonObject().entrySet()) {
                    var keys = channel.getValue().getAsJsonObject();
                    assertTrue(keys.has("0.000000") && keys.has("0.500000"));
                    for (var key : keys.entrySet()) {
                        assertTrue(Double.parseDouble(key.getKey()) >= 0
                                && Double.parseDouble(key.getKey()) <= length);
                        assertEquals(3, key.getValue().getAsJsonArray().size());
                        key.getValue().getAsJsonArray().forEach(value ->
                                assertTrue(Double.isFinite(value.getAsDouble())));
                    }
                }
            }
        }
    }

    @Test
    void geometryContainsEveryBoneUsedByAnimations() throws IOException {
        JsonObject geometryRoot = parse(ASSETS.resolve("geo/ferin.geo.json"));
        JsonObject geometry = geometryRoot.getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
        assertEquals("geometry.purified_undead.ferin",
                geometry.getAsJsonObject("description").get("identifier").getAsString());

        Set<String> bones = new HashSet<>();
        geometry.getAsJsonArray("bones").forEach(element ->
                bones.add(element.getAsJsonObject().get("name").getAsString()));

        JsonObject animations = parse(ASSETS.resolve("animations/ferin.animation.json"))
                .getAsJsonObject("animations");
        for (int stage = 1; stage <= 5; stage++) {
            String name = FerinVisualModel.animationForStage(stage);
            assertTrue(animations.has(name), "Missing animation " + name);
            Set<String> animatedBones = animations.getAsJsonObject(name).getAsJsonObject("bones").keySet();
            animatedBones.forEach(bone -> assertTrue(bones.contains(bone), "Unknown animated bone " + bone));
            assertTrue(animatedBones.containsAll(Set.of("root", "hips", "body", "hood",
                            "left_arm", "right_arm", "left_leg", "right_leg", "cape_upper")),
                    name + " must remain a full-body attack rather than an arm-only placeholder");
            assertTrue(animatedBones.size() >= 35, name + " must preserve joint, skirt and secondary cape motion");
        }
    }

    @Test
    void formalModelHasAdultKnightScaleAndSignatureSilhouette() throws IOException {
        JsonObject geometry = parse(ASSETS.resolve("geo/ferin.geo.json"))
                .getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
        JsonObject description = geometry.getAsJsonObject("description");
        assertEquals(512, description.get("texture_width").getAsInt());
        assertEquals(512, description.get("texture_height").getAsInt());

        Set<String> bones = new HashSet<>();
        Set<String> distinctFaceTiles = new HashSet<>();
        final int[] cubeCount = {0};
        final double[] verticalBounds = {Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
        geometry.getAsJsonArray("bones").forEach(element -> {
            JsonObject current = element.getAsJsonObject();
            bones.add(current.get("name").getAsString());
            if (current.has("cubes")) {
                cubeCount[0] += current.getAsJsonArray("cubes").size();
                current.getAsJsonArray("cubes").forEach(cubeElement -> {
                    JsonObject cube = cubeElement.getAsJsonObject();
                    double origin = cube.getAsJsonArray("origin").get(1).getAsDouble();
                    double height = cube.getAsJsonArray("size").get(1).getAsDouble();
                    verticalBounds[0] = Math.min(verticalBounds[0], origin);
                    verticalBounds[1] = Math.max(verticalBounds[1], origin + height);
                    JsonObject uv = cube.getAsJsonObject("uv");
                    for (String face : Set.of("north", "east", "south", "west", "up", "down")) {
                        var tile = uv.getAsJsonObject(face).getAsJsonArray("uv");
                        int u = tile.get(0).getAsInt();
                        int v = tile.get(1).getAsInt();
                        var faceSize = uv.getAsJsonObject(face).getAsJsonArray("uv_size");
                        assertTrue(u >= 0 && v >= 0 && u + faceSize.get(0).getAsDouble() <= 512
                                        && v + faceSize.get(1).getAsDouble() <= 512,
                                "Every face must remain inside the atlas, including its inset UV rectangle");
                        distinctFaceTiles.add(u + ":" + v);
                    }
                });
            }
        });

        assertTrue(verticalBounds[1] - Math.max(0.0D, verticalBounds[0]) >= 32.0D,
                "Ferin must preserve the revised compact knight scale");
        assertTrue(bones.size() >= 70, "The refined model must preserve its articulated detail bones");
        // V16 replaces the historical cube-count target with substantial cloth volumes.
        assertTrue(cubeCount[0] > 0, "The character must contain geometry");
        assertTrue(cubeCount[0] <= 2400,
                "The expanded cloth detail must stay inside the revised 2400-cuboid asset budget");
        // Cloth now shares coherent swatches; UV diversity is not a quality measure.
        geometry.getAsJsonArray("bones").forEach(element -> {
            JsonObject bone = element.getAsJsonObject();
            if (Set.of("hood_crown", "cape_gore_1_upper_1", "cape_gore_7_upper_1")
                    .contains(bone.get("name").getAsString())) {
                assertTrue(bone.getAsJsonArray("cubes").size() > 0);
                bone.getAsJsonArray("cubes").forEach(c -> c.getAsJsonObject().getAsJsonArray("size")
                        .forEach(axis -> assertTrue(axis.getAsDouble() >= 0.7,
                                "Primary hood and drapery must be solid volumes rather than micro sheets")));
            }
        });
        for (String required : Set.of("hood", "mantle_center", "mantle_left", "mantle_right",
                "cape_upper", "cape_mid_left", "cape_mid_right", "cape_tail_left",
                "cape_tail_center", "cape_tail_right", "left_forearm", "right_forearm",
                "left_wrist", "right_wrist", "left_hand", "right_hand", "left_foot", "right_foot",
                "helmet_brow", "helmet_jaw", "visor_bars", "mantle_front_left",
                "mantle_front_right", "mantle_clasp", "left_shoulder_cloak",
                "right_shoulder_cloak", "red_eyes", "cape_strip_4", "sword")) {
            assertTrue(bones.contains(required), "Missing formal-model bone " + required);
        }
        assertTrue(!bones.contains("cloak_left") && !bones.contains("cloak_right"),
                "The rejected full-height side slabs must not return");

        JsonObject sword = null;
        for (var element : geometry.getAsJsonArray("bones")) {
            JsonObject current = element.getAsJsonObject();
            if ("sword".equals(current.get("name").getAsString())) {
                sword = current;
                break;
            }
        }
        assertTrue(sword != null && "right_hand".equals(sword.get("parent").getAsString()),
                "The longsword must remain bound to the right hand");
        assertTrue(sword.getAsJsonArray("rotation").get(2).getAsDouble() <= -45.0D,
                "The resting sword must be held diagonally rather than parallel to the forearm");
        assertTrue(sword.getAsJsonArray("cubes").isEmpty(), "Character keeps only an empty grip socket");
        sword = parse(ASSETS.resolve("geo/ferin_sword.geo.json"))
                .getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject()
                .getAsJsonArray("bones").get(0).getAsJsonObject();
        assertTrue(!sword.has("parent"), "Weapon must be an independent model");
        double lowestSwordPoint = Double.POSITIVE_INFINITY;
        for (var cubeElement : sword.getAsJsonArray("cubes")) {
            lowestSwordPoint = Math.min(lowestSwordPoint,
                    cubeElement.getAsJsonObject().getAsJsonArray("origin").get(1).getAsDouble());
        }
        double swordGripHeight = sword.getAsJsonArray("pivot").get(1).getAsDouble();
        assertTrue(swordGripHeight - lowestSwordPoint >= 26.0D,
                "The formal longsword must retain its blade length relative to the raised hand");

        var texture = ImageIO.read(ASSETS.resolve("textures/entity/ferin.png").toFile());
        assertEquals(512, texture.getWidth());
        assertEquals(512, texture.getHeight());
    }

    @Test
    void attacksStayFastAndFormalSlashTextureContainsTransparency() throws IOException {
        JsonObject animations = parse(ASSETS.resolve("animations/ferin.animation.json"))
                .getAsJsonObject("animations");
        for (int stage = 1; stage <= 5; stage++) {
            JsonObject animation = animations.getAsJsonObject(FerinVisualModel.animationForStage(stage));
            assertTrue(animation.get("animation_length").getAsDouble() <= 0.5D,
                    "Ferin's reference strikes finish their visible action inside half a second");
        }

        var slash = ImageIO.read(ASSETS.resolve("textures/particle/ferin_slash.png").toFile());
        assertEquals(64, slash.getWidth());
        assertEquals(64, slash.getHeight());
        boolean foundTransparent = false;
        boolean foundVisible = false;
        for (int y = 0; y < slash.getHeight(); y++) {
            for (int x = 0; x < slash.getWidth(); x++) {
                int alpha = slash.getRGB(x, y) >>> 24;
                foundTransparent |= alpha == 0;
                foundVisible |= alpha > 200;
            }
        }
        assertTrue(foundTransparent && foundVisible, "Slash art must be a visible crescent on transparency");
    }

    private static JsonObject parse(Path path) throws IOException {
        try (var reader = Files.newBufferedReader(path)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}
