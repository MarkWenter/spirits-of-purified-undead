package dev.purifiedundead.foundry;

import com.google.gson.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Validated server configuration, reloaded at server start. Invalid files never overwrite user edits. */
public final class FoundryRecipes {
    public record Recipe(
            String top,
            int topCount,
            String bottom,
            int bottomCount,
            String output,
            int outputCount,
            boolean consumeTop,
            int leftFuel,
            int rightFuel,
            int ticks) {
        public boolean matches(ItemStack a, ItemStack b) {
            return id(a).equals(top)
                    && a.getCount() >= topCount
                    && id(b).equals(bottom)
                    && b.getCount() >= bottomCount;
        }

        public ItemStack result() {
            return new ItemStack(
                    BuiltInRegistries.ITEM.get(ResourceLocation.parse(output)), outputCount);
        }

        public int batch(ItemStack a, ItemStack b, ItemStack left, ItemStack right) {
            if (!matches(a, b)) return 0;
            int n =
                    Math.min(
                            b.getCount() / bottomCount,
                            Math.min(left.getCount() / leftFuel, right.getCount() / rightFuel));
            if (consumeTop) n = Math.min(n, a.getCount() / topCount);
            n = Math.min(n, result().getMaxStackSize() / outputCount);
            if (consumeTop && n > 0 && !fitsRemainder(a, b, n)) return 0;
            return n;
        }

        /** The completed batch must leave at most one stack of input in the upper slot. */
        public boolean fitsRemainder(ItemStack a, ItemStack b, int n) {
            int upper = a.getCount() - topCount * n, lower = b.getCount() - bottomCount * n;
            if (upper == 0 || lower == 0) return true;
            return ItemStack.isSameItemSameTags(a, b) && upper + lower <= a.getMaxStackSize();
        }
    }

    private static List<Recipe> recipes = List.of();

    public static String id(ItemStack s) {
        return s.isEmpty() ? "" : BuiltInRegistries.ITEM.getKey(s.getItem()).toString();
    }

    private record Index(
            List<Recipe> source, Map<String, List<Recipe>> tops, Set<String> bottoms) {}

    private static Index index = new Index(List.of(), Map.of(), Set.of());

    private static Index index() {
        if (index.source() != recipes) {
            var tops = new HashMap<String, List<Recipe>>();
            var bottoms = new HashSet<String>();
            for (var recipe : recipes) {
                tops.computeIfAbsent(recipe.top(), k -> new ArrayList<>()).add(recipe);
                bottoms.add(recipe.bottom());
            }
            tops.replaceAll((k, v) -> List.copyOf(v));
            index = new Index(recipes, Map.copyOf(tops), Set.copyOf(bottoms));
        }
        return index;
    }

    public static Recipe find(ItemStack a, ItemStack b) {
        if (a.isEmpty() || b.isEmpty()) return null;
        for (var r : index().tops().getOrDefault(id(a), List.of())) if (r.matches(a, b)) return r;
        return null;
    }

    public static boolean bottom(ItemStack s) {
        return !s.isEmpty() && index().bottoms().contains(id(s));
    }

    public static boolean top(ItemStack s) {
        return !s.isEmpty() && index().tops().containsKey(id(s));
    }

    public static List<Recipe> all() {
        return recipes;
    }

    public static List<Recipe> defaults() {
        var list = new ArrayList<Recipe>();
        list.add(
                new Recipe(
                        "minecraft:coal",
                        1,
                        "minecraft:coal",
                        1,
                        "minecraft:diamond",
                        2,
                        true,
                        1,
                        1,
                        400));
        list.add(
                new Recipe(
                        "minecraft:iron_ingot",
                        4,
                        "minecraft:gold_ingot",
                        4,
                        "minecraft:netherite_scrap",
                        1,
                        true,
                        1,
                        1,
                        400));
        list.add(
                new Recipe(
                        "minecraft:amethyst_shard",
                        1,
                        "minecraft:ink_sac",
                        1,
                        "minecraft:echo_shard",
                        1,
                        true,
                        1,
                        1,
                        400));
        list.add(
                new Recipe(
                        "minecraft:gold_ingot",
                        2,
                        "minecraft:stick",
                        1,
                        "minecraft:clock",
                        1,
                        true,
                        1,
                        1,
                        400));
        list.add(
                new Recipe(
                        "minecraft:diamond",
                        16,
                        "minecraft:wither_skeleton_skull",
                        1,
                        "minecraft:nether_star",
                        1,
                        true,
                        1,
                        1,
                        400));
        for (String k : dev.purifiedundead.slate.SlateContent.WARRIORS)
            if (!k.equals("ferin"))
                list.add(
                        new Recipe(
                                "purified_undead:"
                                        + (k.equals("guardians")
                                                ? "guardian_warriors"
                                                : k + "_warrior"),
                                1,
                                "purified_undead:slate_fragment",
                                1,
                                "purified_undead:forged_slate_" + k,
                                1,
                                false,
                                1,
                                1,
                                400));
        return list;
    }

    public static void load() {
        Path path =
                net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR
                        .get()
                        .resolve("purified_undead-foundry.json");
        var gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            if (!Files.exists(path)) {
                var root = new JsonObject();
                root.addProperty(
                        "_help",
                        "A=top / 上槽; B=bottom / 下槽. consumeTop=false preserves A. Counts and fuels are per result batch unit; ticks=400 is 20 seconds. Use /purifiedundead config reload foundry after changes, or restart. IDs may come from any mod; invalid entries are skipped with a warning.");
                root.add("recipes", gson.toJsonTree(defaults()));
                Files.writeString(path, gson.toJson(root), StandardCharsets.UTF_8);
            }
            if (Files.size(path) > 8 * 1024 * 1024)
                throw new IllegalArgumentException("Foundry configuration exceeds 8 MiB");
            var root =
                    JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8))
                            .getAsJsonObject();
            var loaded = new ArrayList<Recipe>();
            if (root.getAsJsonArray("recipes").size() > 4096)
                throw new IllegalArgumentException("At most 4096 foundry recipes are supported");
            for (var element : root.getAsJsonArray("recipes"))
                try {
                    var object = element.getAsJsonObject();
                    if (!object.has("consumeTop")
                            || !object.get("consumeTop").isJsonPrimitive()
                            || !object.get("consumeTop").getAsJsonPrimitive().isBoolean())
                        throw new IllegalArgumentException(
                                "consumeTop must be an explicit boolean");
                    var recipe = gson.fromJson(element, Recipe.class);
                    validate(recipe);
                    loaded.add(recipe);
                } catch (RuntimeException bad) {
                    com.mojang.logging.LogUtils.getLogger()
                            .warn("Invalid foundry recipe skipped: {}", bad.getMessage());
                }
            if (loaded.size() > 4096)
                throw new IllegalArgumentException("At most 4096 foundry recipes are supported");
            validateSync(loaded);
            recipes = List.copyOf(loaded);
        } catch (Exception e) {
            recipes = List.of();
            com.mojang.logging.LogUtils.getLogger()
                    .error(
                            "Foundry configuration could not load; processing disabled, inventory untouched: {}",
                            path,
                            e);
        }
    }

    /** Administrative reload: validate the whole file before replacing the active recipes. */
    public static int reloadStrict(net.minecraft.server.MinecraftServer server)
            throws java.io.IOException {
        if (!server.isSameThread())
            throw new IllegalStateException("Foundry reload requires the server thread");
        Path path =
                net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR
                        .get()
                        .resolve("purified_undead-foundry.json");
        if (Files.size(path) > 8 * 1024 * 1024)
            throw new IllegalArgumentException("Foundry configuration exceeds 8 MiB");
        var root =
                JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8))
                        .getAsJsonObject();
        var array = root.getAsJsonArray("recipes");
        if (array == null || array.size() > 4096)
            throw new IllegalArgumentException("Expected recipes array with at most 4096 entries");
        var loaded = new ArrayList<Recipe>();
        var gson = new Gson();
        for (var element : array) {
            var object = element.getAsJsonObject();
            if (!object.has("consumeTop")
                    || !object.get("consumeTop").isJsonPrimitive()
                    || !object.get("consumeTop").getAsJsonPrimitive().isBoolean())
                throw new IllegalArgumentException("consumeTop must be an explicit boolean");
            var recipe = gson.fromJson(element, Recipe.class);
            validate(recipe);
            loaded.add(recipe);
        }
        validateSync(loaded);
        // Commit only after every entry validates. Invalid reloads leave the running recipes
        // intact.
        recipes = List.copyOf(loaded);
        var packet = new dev.purifiedundead.network.FoundryRecipesPacket(recipes);
        for (var player : server.getPlayerList().getPlayers())
            dev.purifiedundead.network.ModNetwork.CHANNEL.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player), packet);
        return recipes.size();
    }

    /** Structural checks are registry-independent and safe on the network decoding thread. */
    public static void validateShape(Recipe r) {
        if (r == null) throw new IllegalArgumentException("Null foundry recipe");
        for (String id : new String[] {r.top, r.bottom, r.output})
            if (id == null || id.length() > 256 || ResourceLocation.tryParse(id) == null)
                throw new IllegalArgumentException("Invalid foundry item ID");
        if (r.topCount < 1
                || r.topCount > 64
                || r.bottomCount < 1
                || r.bottomCount > 64
                || r.outputCount < 1
                || r.leftFuel < 1
                || r.leftFuel > 64
                || r.rightFuel < 1
                || r.rightFuel > 64
                || r.ticks < 1
                || r.ticks > 32767)
            throw new IllegalArgumentException("Invalid foundry quantities");
    }

    public static void validateSync(List<Recipe> values) {
        if (values.size() > 4096) throw new IllegalArgumentException("Too many foundry recipes");
        long bytes = 5;
        for (var r : values) {
            validateShape(r);
            bytes += r.top.length() + r.bottom.length() + r.output.length() + 40;
        }
        if (bytes > 900000)
            throw new IllegalArgumentException("Foundry recipes exceed safe network payload size");
    }

    public static void validate(Recipe r) {
        validateShape(r);
        for (String s : new String[] {r.top, r.bottom, r.output}) {
            var id = ResourceLocation.tryParse(s == null ? "" : s);
            if (id == null
                    || !BuiltInRegistries.ITEM.containsKey(id)
                    || BuiltInRegistries.ITEM.get(id) == Items.AIR)
                throw new IllegalArgumentException("Unknown item: " + s);
        }
        if (r.topCount < 1
                || r.bottomCount < 1
                || r.outputCount < 1
                || r.topCount > 64
                || r.bottomCount > 64
                || r.outputCount > r.result().getMaxStackSize()
                || r.leftFuel < 1
                || r.leftFuel > 64
                || r.rightFuel < 1
                || r.rightFuel > 64
                || r.ticks < 1
                || r.ticks > 32767)
            throw new IllegalArgumentException("Invalid count/fuel/ticks: " + r);
    }

    public static void write(net.minecraft.network.FriendlyByteBuf b, List<Recipe> values) {
        validateSync(values);
        b.writeVarInt(values.size());
        for (var r : values) {
            b.writeUtf(r.top, 256);
            b.writeVarInt(r.topCount);
            b.writeUtf(r.bottom, 256);
            b.writeVarInt(r.bottomCount);
            b.writeUtf(r.output, 256);
            b.writeVarInt(r.outputCount);
            b.writeBoolean(r.consumeTop);
            b.writeVarInt(r.leftFuel);
            b.writeVarInt(r.rightFuel);
            b.writeVarInt(r.ticks);
        }
    }

    public static List<Recipe> read(net.minecraft.network.FriendlyByteBuf b) {
        int count = b.readVarInt();
        if (count < 0 || count > 4096) throw new IllegalArgumentException("Invalid recipe count");
        var list = new ArrayList<Recipe>();
        for (int i = 0; i < count; i++)
            list.add(
                    new Recipe(
                            b.readUtf(256),
                            b.readVarInt(),
                            b.readUtf(256),
                            b.readVarInt(),
                            b.readUtf(256),
                            b.readVarInt(),
                            b.readBoolean(),
                            b.readVarInt(),
                            b.readVarInt(),
                            b.readVarInt()));
        validateSync(list);
        return List.copyOf(list);
    }

    private FoundryRecipes() {}
}
