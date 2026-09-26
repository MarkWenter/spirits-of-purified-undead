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
    private static final double DOUBLE_JUMP_VELOCITY = 0.52D;
    private static final double AIR_DASH_SPEED = 1.15D;
    private static final Map<UUID, AirState> STATES = new HashMap<>();
    static final long REQUEST_GRACE_TICKS = 5L;

    public static void perform(ServerPlayer player, GuardianActionPacket.Action action) {
        if (player.getAbilities().flying || player.isFallFlying() || !isEquipped(player)) {
            return;
        }
        AirState state = STATES.computeIfAbsent(player.getUUID(), ignored -> new AirState());
        if (player.onGround()) {
            state.queueForTakeoff(action, player.level().getGameTime());
            return;
        }
        performAirborne(player, state, action);
    }

    private static void performAirborne(ServerPlayer player, AirState state, GuardianActionPacket.Action action) {
        if (action == GuardianActionPacket.Action.DOUBLE_JUMP && !state.jumped) {
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x,
                    PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.guardianDoubleJumpVelocity), motion.z);
            player.fallDistance = 0.0F;
            player.hasImpulse = true;
            player.connection.send(new ClientboundSetEntityMotionPacket(player));
            state.jumped = true;
        } else if (action == GuardianActionPacket.Action.AIR_DASH && !state.dashed) {
            Vec3 look = player.getLookAngle();
            Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);
            if (horizontal.lengthSqr() < 1.0E-6D) {
                return;
            }
            horizontal = horizontal.normalize().scale(PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.guardianAirDashSpeed));
            player.setDeltaMovement(horizontal.x, Math.max(0.08D, player.getDeltaMovement().y), horizontal.z);
            player.hasImpulse = true;
            player.connection.send(new ClientboundSetEntityMotionPacket(player));
            state.dashed = true;
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
    }

    @SubscribeEvent
    public void onServerStopped(net.minecraftforge.event.server.ServerStoppedEvent event) {
        STATES.clear();
    }

    private static boolean isEquipped(ServerPlayer player) {
        return CuriosApi.getCuriosInventory(player).map(handler ->
                handler.isEquipped(ModItems.ANCIENT_CONTRACT.get())
                        && handler.isEquipped(ModItems.GUARDIAN_WARRIORS.get())).orElse(false);
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
