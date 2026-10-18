package dev.purifiedundead.combat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class AbilityPreferences {
    public static final String FERIN_DISABLED = "purified_undead:ferin_disabled";
    public static final String MOVEMENT_DISABLED = "purified_undead:guardian_movement_disabled";

    private AbilityPreferences() {}

    public static boolean ferin(Player p) {
        return !p.getPersistentData().getBoolean(FERIN_DISABLED);
    }

    public static boolean movement(Player p) {
        return !p.getPersistentData().getBoolean(MOVEMENT_DISABLED);
    }

    public static void toggle(ServerPlayer p, boolean ferin) {
        String key = ferin ? FERIN_DISABLED : MOVEMENT_DISABLED;
        boolean disabled = !p.getPersistentData().getBoolean(key);
        p.getPersistentData().putBoolean(key, disabled);
        sync(p);
        p.displayClientMessage(
                net.minecraft.network.chat.Component.translatable(
                        "message.purified_undead."
                                + (ferin ? "ferin" : "guardian_movement")
                                + (disabled ? ".disabled" : ".enabled")),
                true);
    }

    public static void copy(Player from, Player to) {
        to.getPersistentData().putBoolean(FERIN_DISABLED, !ferin(from));
        to.getPersistentData().putBoolean(MOVEMENT_DISABLED, !movement(from));
    }

    public static void sync(ServerPlayer p) {
        if (p.connection == null) return;
        var packet = new dev.purifiedundead.network.AbilitySettingsPacket(ferin(p), movement(p));
        dev.purifiedundead.network.ModNetwork.CHANNEL.send(
                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> p), packet);
    }
}
