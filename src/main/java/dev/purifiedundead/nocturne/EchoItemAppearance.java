package dev.purifiedundead.nocturne;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Bounded appearance of a single display item. Never serialize an inventory or a complete item tag. */
public final class EchoItemAppearance {
    private static final int MAX_PATTERNS = 16;
    private EchoItemAppearance() {}

    public static CompoundTag capture(ItemStack stack) {
        var out = new CompoundTag();
        if (stack.isEmpty()) return out;
        out.putString("id", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        if (stack.hasFoil()) out.putBoolean("glint", true);
        var tag = stack.getTag();
        if (tag == null) return out;
        if (tag.contains("CustomModelData", Tag.TAG_INT)) out.putInt("model", tag.getInt("CustomModelData"));
        var display = tag.getCompound("display");
        if (display.contains("color", Tag.TAG_INT)) out.putInt("color", display.getInt("color"));
        if (stack.isDamaged()) out.putInt("damage", stack.getDamageValue());
        var raw = tag.getCompound("BlockEntityTag").getList("Patterns", Tag.TAG_COMPOUND);
        var patterns = new ListTag();
        for (int i = 0; i < Math.min(raw.size(), MAX_PATTERNS); i++)
            addPattern(patterns, raw.getCompound(i).getString("Pattern"), raw.getCompound(i).getInt("Color"));
        if (!patterns.isEmpty()) out.put("patterns", patterns);
        return out;
    }

    private static void addPattern(ListTag patterns, String pattern, int dye) {
        if (pattern.isEmpty() || pattern.length() > 128 || dye < 0 || dye > 15) return;
        var entry = new CompoundTag();
        entry.putString("pattern", pattern);
        entry.putInt("dye", dye);
        patterns.add(entry);
    }

    static void project(CompoundTag source, CompoundTag out, String key) {
        var raw = source.getCompound(key);
        String id = raw.getString("id");
        if (id.isEmpty() || id.length() > 256 || ResourceLocation.tryParse(id) == null) return;
        var item = new CompoundTag();
        item.putString("id", id);
        for (String field : new String[]{"model", "color", "damage"})
            if (raw.contains(field, Tag.TAG_INT)) item.putInt(field, raw.getInt(field));
        if (raw.contains("glint", Tag.TAG_BYTE)) item.putBoolean("glint", raw.getBoolean("glint"));
        var patterns = new ListTag();
        var list = raw.getList("patterns", Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(list.size(), MAX_PATTERNS); i++)
            addPattern(patterns, list.getCompound(i).getString("pattern"), list.getCompound(i).getInt("dye"));
        if (!patterns.isEmpty()) item.put("patterns", patterns);
        out.put(key, item);
    }

    public static ItemStack restore(CompoundTag view) {
        var id = ResourceLocation.tryParse(view.getString("id"));
        if (id == null) return ItemStack.EMPTY;
        var stack = BuiltInRegistries.ITEM.getOptional(id).map(ItemStack::new).orElse(ItemStack.EMPTY);
        if (stack.isEmpty()) return stack;
        if (view.getBoolean("glint") && !stack.hasFoil()) {
            // 1.20.1 uses a nonempty enchantment list for the glint. No real enchantment on the proxy.
            var shimmer = new ListTag();
            shimmer.add(new CompoundTag());
            stack.getOrCreateTag().put("Enchantments", shimmer);
        }
        if (view.contains("model")) stack.getOrCreateTag().putInt("CustomModelData", view.getInt("model"));
        if (view.contains("color")) stack.getOrCreateTagElement("display").putInt("color", view.getInt("color"));
        if (view.contains("damage") && stack.isDamageableItem())
            stack.setDamageValue(net.minecraft.util.Mth.clamp(view.getInt("damage"), 0, stack.getMaxDamage()));
        var patterns = new ListTag();
        var list = view.getList("patterns", Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(list.size(), MAX_PATTERNS); i++) {
            var layer = list.getCompound(i);
            var entry = new CompoundTag();
            entry.putString("Pattern", layer.getString("pattern"));
            entry.putInt("Color", layer.getInt("dye"));
            patterns.add(entry);
        }
        if (!patterns.isEmpty()) stack.getOrCreateTagElement("BlockEntityTag").put("Patterns", patterns);
        return stack;
    }
}
