package dev.purifiedundead.client;

import dev.purifiedundead.PurifiedUndead;
import dev.purifiedundead.content.ModItems;
import dev.purifiedundead.network.GuardianActionPacket;
import dev.purifiedundead.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(modid = PurifiedUndead.MOD_ID, value = Dist.CLIENT)
public final class GuardianInputEvents {
    private static LocalPlayer previousPlayer;
    private static double jumpVelocity = 0.52D;
    private static double dashSpeed = 1.15D;

    public static void setServerMotion(double jump, double dash) {
        if (!dev.purifiedundead.combat.GuardianMotion.validSettings(jump, dash)) return;
        jumpVelocity = jump;
        dashSpeed = dash;
    }

    private static int lastGripTick = -100, lastWallJumpTick = -100;
    private static boolean jumpWasDown;
    private static boolean sprintWasDown;
    private static boolean jumpedThisAir;
    private static boolean dashedThisAir;
    private static boolean jumpReleasedThisAir;

    private GuardianInputEvents() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            resetAll();
            return;
        }
        if (player != previousPlayer) {
            resetAll();
            previousPlayer = player;
        }
        boolean jumpDown = minecraft.options.keyJump.isDown();
        boolean sprintDown = minecraft.options.keySprint.isDown();
        if ((player.tickCount % 5 == 0 || jumpDown != jumpWasDown)
                && dev.purifiedundead.slate.MemoryStorage.active(player, "ulv")) {
            boolean grip =
                    jumpDown
                            && minecraft.screen == null
                            && player.isAlive()
                            && !player.onGround()
                            && !player.isSpectator();
            ModNetwork.CHANNEL.sendToServer(
                    new GuardianActionPacket(
                            grip
                                    ? GuardianActionPacket.Action.GRIP_HELD
                                    : GuardianActionPacket.Action.GRIP_RELEASED));
        }
        if (minecraft.screen != null
                || !player.isAlive()
                || player.isSpectator()
                || player.isPassenger()
                || player.getAbilities().flying
                || player.isFallFlying()) {
            jumpWasDown = jumpDown;
            sprintWasDown = sprintDown;
            return;
        }
        boolean wallJump = false;
        if (dev.purifiedundead.slate.WallGrip.eligible(player)) {
            if (jumpDown
                    && !jumpWasDown
                    && player.tickCount - lastGripTick <= 8
                    && player.tickCount - lastWallJumpTick >= 3) {
                ModNetwork.CHANNEL.sendToServer(
                        new GuardianActionPacket(GuardianActionPacket.Action.WALL_JUMP));
                var v = player.getDeltaMovement();
                player.setDeltaMovement(v.x, .52, v.z);
                player.fallDistance = 0;
                lastWallJumpTick = player.tickCount;
                lastGripTick = -100;
                wallJump = true;
            } else if (jumpDown && player.getDeltaMovement().y <= 0)
                lastGripTick = player.tickCount;
        } else lastGripTick = -100;
        if (player.onGround()) {
            jumpedThisAir = false;
            dashedThisAir = false;
            jumpReleasedThisAir = false;
        } else if (!wallJump && hasGuardians(player)) {
            if (!jumpDown) {
                jumpReleasedThisAir = true;
            }
            if (GuardianInputModel.shouldDoubleJump(
                    jumpDown, jumpWasDown, jumpReleasedThisAir, jumpedThisAir)) {
                ModNetwork.CHANNEL.sendToServer(
                        new GuardianActionPacket(GuardianActionPacket.Action.DOUBLE_JUMP));
                player.setDeltaMovement(
                        dev.purifiedundead.combat.GuardianMotion.jump(
                                player.getDeltaMovement(), jumpVelocity));
                player.fallDistance = 0.0F;
                jumpedThisAir = true;
            }
            if (sprintDown && !sprintWasDown && !dashedThisAir) {
                ModNetwork.CHANNEL.sendToServer(
                        new GuardianActionPacket(GuardianActionPacket.Action.AIR_DASH));
                player.setDeltaMovement(
                        dev.purifiedundead.combat.GuardianMotion.dash(
                                player.getDeltaMovement(), player.getYRot(), dashSpeed));
                dashedThisAir = true;
            }
        }
        jumpWasDown = jumpDown;
        sprintWasDown = sprintDown;
    }

    private static boolean hasGuardians(LocalPlayer player) {
        return CuriosApi.getCuriosInventory(player)
                .map(
                        handler ->
                                handler.isEquipped(ModItems.ANCIENT_CONTRACT.get())
                                        && handler.isEquipped(ModItems.GUARDIAN_WARRIORS.get()))
                .orElse(false);
    }

    private static void resetAll() {
        lastGripTick = -100;
        lastWallJumpTick = -100;
        previousPlayer = null;
        jumpWasDown = false;
        sprintWasDown = false;
        jumpedThisAir = false;
        dashedThisAir = false;
        jumpReleasedThisAir = false;
    }
}
