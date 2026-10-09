package dev.purifiedundead.api;

import dev.purifiedundead.progress.ProgressManagement;
import net.minecraft.server.level.ServerPlayer;

/** API v1. Server-thread only. Addons must enforce their own authorization/quest conditions.
 * Explicit grants bypass natural acquisition switches and never duplicate an already claimed reward.
 */
public final class ProgressApi {
    public static final int VERSION=1;
    public enum GrantResult { DELIVERED, PENDING, ALREADY_OBTAINED }
    private ProgressApi() {}
    public static ProgressSnapshot snapshot(ServerPlayer player) { return ProgressManagement.snapshot(player); }
    public static GrantResult grantWarrior(ServerPlayer player, WarriorId warrior) { return ProgressManagement.grant(player,warrior); }
    public static GrantResult grantContract(ServerPlayer player) { return ProgressManagement.grantContract(player); }
    /** Resets acquisition bookkeeping only. Items and vanilla advancements are retained. */
    public static void resetWarrior(ServerPlayer player, WarriorId warrior) { ProgressManagement.reset(player,warrior); }
    public static void setTalismanLevel(ServerPlayer player, int level) { ProgressManagement.setLevel(player,level); }
}
