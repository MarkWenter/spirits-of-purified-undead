package dev.purifiedundead.nocturne;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/** Wire projection of a binding snapshot. Never send combat attributes, inventory or nested item tags. */
public final class EchoAppearance {
    static final int MAX_VARIABLES = 64;
    private EchoAppearance() {}

    static boolean validVariable(String name, float value) {
        return name != null && !name.isEmpty() && name.length() <= 64 && Float.isFinite(value);
    }

    public static CompoundTag fromSnapshot(CompoundTag source) {
        var out = new CompoundTag();
        for (String key : new String[]{"model", "ysmModel", "ysmTexture", "geckoEcho", "backpack", "backpackItem", "backpackFluid"}) {
            String value = source.getString(key);
            if (!value.isEmpty() && value.length() <= 256) out.putString(key, value);
        }
        if (!out.contains("model") && !out.contains("ysmModel")) return new CompoundTag();
        for (String key : new String[]{"leftHanded", "showBackpack", "showBackItem"}) {
            if (source.contains(key, Tag.TAG_BYTE)) out.putBoolean(key, source.getBoolean(key));
        }
        EchoItemAppearance.project(source, out, "displayStack");
        EchoItemAppearance.project(source, out, "headStack");
        var variables = new CompoundTag();
        var input = source.getCompound("variables");
        for (String key : input.getAllKeys()) {
            if (variables.size() == MAX_VARIABLES) break;
            float value = input.getFloat(key);
            if (input.contains(key, Tag.TAG_FLOAT) && validVariable(key, value)) variables.putFloat(key, value);
        }
        if (!variables.isEmpty()) out.put("variables", variables);
        return out;
    }
}
