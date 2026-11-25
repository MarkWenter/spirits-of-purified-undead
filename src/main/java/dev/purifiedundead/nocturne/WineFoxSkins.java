package dev.purifiedundead.nocturne;

import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;

/** Built-in YSM 2.6.5 / TLM 1.5.3 display identities. Server-safe; no model or texture loading. */
final class WineFoxSkins {
    private static final Map<String, Map<String, String>> MODELS = build();
    private WineFoxSkins() {}

    private static Map<String, Map<String, String>> build() {
        Map<String, Map<String, String>> models = new HashMap<>();
        models.put("01_taisho_maid", Map.of("", "geckolib:winefox", "skin", "geckolib:winefox",
                "skin_white", "geckolib:winefox_blue"));
        add(models, "02_new_year", "winefox_new_year", "skin");
        add(models, "03_astronaut", "winefox_astronaut", "skin");
        add(models, "04_kongfu", "winefox_kongfu", "default", "black", "yellow", "red", "default_red", "red_more");
        add(models, "05_magical", "winefox_magical", "magic", "winefox", "ice", "flower", "blood", "water");
        add(models, "06_hanfu", "winefox_hanfu", "default");
        add(models, "07_jk", "winefox_jk", "skin");
        add(models, "08_sta", "sta", "texture");
        add(models, "09_hailuo", "hailuo_new_year", "texture");
        add(models, "10_zhiban", "zhiban_new_year", "default");
        add(models, "11_salesperson", "winefox_salesperson", "default", "blue");
        add(models, "12_little", "winefox_little", "skin");
        add(models, "13_matured", "winefox_matured", "default");
        add(models, "14_momo", "winefox_momo", "skin_pink", "skin_green");
        add(models, "15_kluonoa", "kluonoa", "texture");
        add(models, "16_tactics", "winefox_tactics", "tactics");
        add(models, "17_mini", "winefox_mini", "texture");
        add(models, "18_wedding", "winefox_wedding", "skin");
        add(models, "19_nine_tailed", "winefox_nine_tailed", "skin", "skin_white");
        add(models, "20_survivor", "winefox_survivor", "mitao");
        add(models, "21_saint", "winefox_saint", "skin", "skinBlack");
        add(models, "22_elf", "winefox_elf", "skin");
        return Map.copyOf(models);
    }

    private static void add(Map<String, Map<String, String>> models, String folder, String model, String... textures) {
        Map<String, String> colors = new HashMap<>();
        String base = "geckolib:" + model;
        colors.put("", base);
        colors.put(textures[0], base);
        // CustomModelPack.decorate registers each extra texture under base + "_" + MD5(texture path).
        for (int i = 1; i < textures.length; i++) {
            String path = "textures/entity/" + model + "_" + i + ".png";
            String hash = textureHash(path);
            colors.put(textures[i], base + "_" + hash);
        }
        models.put(folder, Map.copyOf(colors));
    }

    private static String textureHash(String path) {
        try {
            var digest = java.security.MessageDigest.getInstance("MD5");
            return java.util.HexFormat.of().formatHex(digest.digest(path.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException("TLM extra texture ID requires MD5", ex);
        }
    }

    static void capture(CompoundTag slice, CompoundTag previous) {
        String model = slice.getString("ysmModel");
        if (!model.startsWith("wine_fox/")) return;
        if (previous.contains("geckoEcho") && model.equals(previous.getString("ysmModel"))
                && slice.getString("ysmTexture").equals(previous.getString("ysmTexture"))
                && slice.getCompound("variables").equals(previous.getCompound("variables"))) {
            slice.putString("geckoEcho", previous.getString("geckoEcho"));
            return;
        }
        // An empty result is cached in the slice, but never sent to clients.
        slice.putString("geckoEcho", "");
        var colors = MODELS.get(model.substring("wine_fox/".length()));
        if (colors == null) return;
        var vars = slice.getCompound("variables");
        for (String name : vars.getAllKeys()) {
            // Some built-ins use these controls to replace the whole body (e.g. the small fox form).
            if (vars.getFloat(name) != 0) return;
        }
        String texture = slice.getString("ysmTexture");
        if (texture.startsWith("textures/")) texture = texture.substring(9);
        if (texture.endsWith(".png")) texture = texture.substring(0, texture.length() - 4);
        var target = colors.get(texture);
        if (target != null) slice.putString("geckoEcho", target);
    }
}
