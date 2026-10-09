package dev.purifiedundead.combat;

import dev.purifiedundead.content.ModItems;
import dev.purifiedundead.config.PurifiedUndeadConfig;
import dev.purifiedundead.network.GuardianActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

/** Server authority for one double jump and one dash per airborne period. */
public final class GuardianMovementService {
    private static final Map<UUID, AirState> STATES = new HashMap<>();
    private static final Map<UUID, MovementRequestBudget> REQUESTS = new HashMap<>();
    static final long REQUEST_GRACE_TICKS = 5L;

    public static void perform(ServerPlayer player, GuardianActionPacket.Action action) {
        if (!REQUESTS.computeIfAbsent(player.getUUID(), ignored -> new MovementRequestBudget())
                .allow(player.server.getTickCount())) return;
        if (action == GuardianActionPacket.Action.WALL_JUMP) {
            if (!dev.purifiedundead.slate.WallGrip.jump(player)) rejectPrediction(player);
            return;
        }
        if (action == GuardianActionPacket.Action.GRIP_HELD
                || action == GuardianActionPacket.Action.GRIP_RELEASED) {
            dev.purifiedundead.slate.WallGrip.input(
                    player, action == GuardianActionPacket.Action.GRIP_HELD);
            return;
        }
        if (!player.isAlive()
                || player.isSpectator()
                || player.isPassenger()
                || player.getAbilities().flying
                || player.isFallFlying()
                || !isEquipped(player)) {
            rejectPrediction(player);
            return;
        }
        AirState state = STATES.computeIfAbsent(player.getUUID(), ignored -> new AirState());
        if (player.onGround()) {
            state.queueForTakeoff(action, player.level().getGameTime());
            return;
        }
        performAirborne(player, state, action);
    }

    private static void performAirborne(
            ServerPlayer player, AirState state, GuardianActionPacket.Action action) {
        if (action == GuardianActionPacket.Action.DOUBLE_JUMP && !state.jumped) {
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(
                    motion.x,
                    PurifiedUndeadConfig.get(
                            PurifiedUndeadConfig.VALUES.guardianDoubleJumpVelocity),
                    motion.z);
            player.fallDistance = 0.0F;
            player.hasImpulse = true;
            // The owner already predicted this impulse; echoing it would replay the jump after a
            // round trip.
            state.jumped = true;
        } else if (action == GuardianActionPacket.Action.AIR_DASH && !state.dashed) {
            player.setDeltaMovement(
                    GuardianMotion.dash(
                            player.getDeltaMovement(),
                            player.getYRot(),
                            PurifiedUndeadConfig.get(
                                    PurifiedUndeadConfig.VALUES.guardianAirDashSpeed)));
            player.hasImpulse = true;
            // The owner already predicted this impulse; echoing it would replay the jump after a
            // round trip.
            state.dashed = true;
        } else {
            rejectPrediction(player);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        AirState state = STATES.get(player.getUUID());
        if (state == null) {
            return;
        }
        long gameTime = player.level().getGameTime();
        if (!isEquipped(player) || player.getAbilities().flying || player.isFallFlying()) {
            STATES.remove(player.getUUID());
        } else if (player.onGround()) {
            if (!state.hasLivePending(gameTime)) {
                STATES.remove(player.getUUID());
            }
        } else {
            for (GuardianActionPacket.Action action : state.takePending(gameTime)) {
                performAirborne(player, state, action);
            }
        }
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        STATES.remove(event.getEntity().getUUID());
        REQUESTS.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public void onServerStopped(net.minecraftforge.event.server.ServerStoppedEvent event) {
        STATES.clear();
        REQUESTS.clear();
    }

    private static void rejectPrediction(ServerPlayer player) {
        player.connection.teleport(
                player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
        player.connection.send(new ClientboundSetEntityMotionPacket(player));
    }

    private static void syncMotion(ServerPlayer player) {
        var packet =
                new dev.purifiedundead.network.GuardianMotionSettingsPacket(
                        PurifiedUndeadConfig.get(
                                PurifiedUndeadConfig.VALUES.guardianDoubleJumpVelocity),
                        PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.guardianAirDashSpeed));
        dev.purifiedundead.network.ModNetwork.CHANNEL.send(
                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player), packet);
    }

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) syncMotion(player);
    }

    @SubscribeEvent
    public void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            STATES.remove(player.getUUID());
            syncMotion(player);
        }
    }

    @SubscribeEvent
    public void onDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            STATES.remove(player.getUUID());
            syncMotion(player);
        }
    }

    private static boolean isEquipped(ServerPlayer player) {
        return CuriosApi.getCuriosInventory(player)
                .map(
                        handler ->
                                handler.isEquipped(ModItems.ANCIENT_CONTRACT.get())
                                        && handler.isEquipped(ModItems.GUARDIAN_WARRIORS.get()))
                .orElse(false);
    }

    static final class AirState {
        private boolean jumped;
        private boolean dashed;
        private final EnumSet<GuardianActionPacket.Action> pending =
                EnumSet.noneOf(GuardianActionPacket.Action.class);
        private long pendingUntil = Long.MIN_VALUE;

        void queueForTakeoff(GuardianActionPacket.Action action, long gameTime) {
            if (!hasLivePending(gameTime)) {
                jumped = false;
                dashed = false;
                pending.clear();
            }
            pending.add(action);
            pendingUntil = gameTime + REQUEST_GRACE_TICKS;
        }

        boolean hasLivePending(long gameTime) {
            if (pending.isEmpty() || gameTime > pendingUntil) {
                pending.clear();
                return false;
            }
            return true;
        }

        EnumSet<GuardianActionPacket.Action> takePending(long gameTime) {
            if (!hasLivePending(gameTime)) {
                return EnumSet.noneOf(GuardianActionPacket.Action.class);
            }
            EnumSet<GuardianActionPacket.Action> actions = EnumSet.copyOf(pending);
            pending.clear();
            return actions;
        }
    }
}
