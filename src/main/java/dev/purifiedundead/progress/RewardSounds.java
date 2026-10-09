package dev.purifiedundead.progress;

import dev.purifiedundead.content.ModSounds;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

/** Deliver privately on the next live tick, including rewards granted during respawn cloning. */
public final class RewardSounds {
    private static final String QUEUE = "purified_undead:reward_sounds";

    private RewardSounds() {}

    public static void onDelivered(ServerPlayer player, Item item) {
        var id = ForgeRegistries.ITEMS.getKey(item);
        if (id == null || !id.getNamespace().equals("purified_undead")) return;
        String sound =
                switch (id.getPath()) {
                    case "ancient_contract" -> "ancient_contract";
                    case "groth_warrior" -> "groth";
                    case "julius_warrior" -> "julius";
                    case "guardian_warriors" -> "guardians";
                    case "ulv_warrior" -> "ulv";
                    case "eleine_warrior" -> "eleine";
                    case "faden_warrior" -> "faden";
                    default -> null; // No supplied sound for Hoenir or Ferin.
                };
        if (sound != null) enqueue(player, sound);
    }

    public static void onUpgrade(ServerPlayer player, int level) {
        enqueue(
                player,
                level >= WhiteWitchTalisman.maxLevel() ? "talisman_max" : "ancient_contract");
    }

    public static void onPurification(ServerPlayer player) {
        enqueue(player, "ancient_contract");
    }

    private static void enqueue(ServerPlayer player, String sound) {
        ListTag queue = player.getPersistentData().getList(QUEUE, Tag.TAG_STRING);
        queue.add(StringTag.valueOf(sound));
        player.getPersistentData().put(QUEUE, queue);
    }

    public static void tick(ServerPlayer player) {
        if (!player.isAlive()
                || player.connection == null
                || !player.getPersistentData().contains(QUEUE, Tag.TAG_LIST)) return;
        ListTag queue = player.getPersistentData().getList(QUEUE, Tag.TAG_STRING);
        if (queue.isEmpty()) return;
        player.getPersistentData().remove(QUEUE);
        for (int i = 0; i < queue.size(); i++) {
            SoundEvent sound = ModSounds.find(queue.getString(i));
            if (sound != null) player.playNotifySound(sound, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    public static void copy(ServerPlayer original, ServerPlayer replacement) {
        if (original.getPersistentData().contains(QUEUE, Tag.TAG_LIST)) {
            replacement
                    .getPersistentData()
                    .put(QUEUE, original.getPersistentData().getList(QUEUE, Tag.TAG_STRING).copy());
        }
    }
}
