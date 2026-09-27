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
        jumpVelocity = jump;
        dashSpeed = dash;
    }
    private static boolean jumpWasDown;
    private static boolean sprintWasDown;
    private static boolean jumpedThisAir;
    private static boolean dashedThisAir;
    private static boolean jumpReleasedThisAir;

    private GuardianInputEvents() {
    }

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
        if (minecraft.screen != null || !player.isAlive() || player.isSpectator()
                || player.isPassenger() || player.getAbilities().flying || player.isFallFlying()) {
            jumpWasDown = jumpDown;
            sprintWasDown = sprintDown;
            return;
        }
        if (player.onGround()) {
            jumpedThisAir = false;
            dashedThisAir = false;
            jumpReleasedThisAir = false;
        } else if (hasGuardians(player)) {
            if (!jumpDown) {
                jumpReleasedThisAir = true;
            }
            if (GuardianInputModel.shouldDoubleJump(
                    jumpDown, jumpWasDown, jumpReleasedThisAir, jumpedThisAir)) {
                ModNetwork.CHANNEL.sendToServer(new GuardianActionPacket(GuardianActionPacket.Action.DOUBLE_JUMP));
                player.setDeltaMovement(dev.purifiedundead.combat.GuardianMotion.jump(player.getDeltaMovement(), jumpVelocity));
                player.fallDistance = 0.0F;
                jumpedThisAir = true;
            }
            if (sprintDown && !sprintWasDown && !dashedThisAir) {
                ModNetwork.CHANNEL.sendToServer(new GuardianActionPacket(GuardianActionPacket.Action.AIR_DASH));
                player.setDeltaMovement(dev.purifiedundead.combat.GuardianMotion.dash(player.getDeltaMovement(), player.getYRot(), dashSpeed));
                dashedThisAir = true;
            }
        }
        jumpWasDown = jumpDown;
        sprintWasDown = sprintDown;
    }

    private static boolean hasGuardians(LocalPlayer player) {
        return CuriosApi.getCuriosInventory(player).map(handler ->
                handler.isEquipped(ModItems.ANCIENT_CONTRACT.get())
                        && handler.isEquipped(ModItems.GUARDIAN_WARRIORS.get())).orElse(false);
    }

    private static void resetAll() {
        previousPlayer = null;
        jumpWasDown = false;
        sprintWasDown = false;
        jumpedThisAir = false;
        dashedThisAir = false;
        jumpReleasedThisAir = false;
    }
}
