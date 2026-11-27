package dev.purifiedundead.validation;

import com.mojang.logging.LogUtils;

import dev.purifiedundead.blightedarmaments.BreathgoldContent;

import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class BreathgoldPreviewScreen extends Screen {
    private int frames;
    private volatile boolean captured;

    public BreathgoldPreviewScreen() {
        super(Component.literal("Breathgold texture verification"));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        if (minecraft.getOverlay() != null) {
            frames = 0;
            return;
        }
        g.fill(0, 0, width, height, 0xff26242a);
        g.drawCenteredString(font, "PURIFIED UNDEAD / BREATHGOLD", width / 2, 20, 0xffe6c58b);
        var items =
                java.util.List.of(
                        BreathgoldContent.ORE_ITEM.get(),
                        BreathgoldContent.MATERIAL.get(),
                        BreathgoldContent.SWORD.get(),
                        BreathgoldContent.AXE.get(),
                        BreathgoldContent.HOE.get(),
                        BreathgoldContent.PICKAXE.get(),
                        BreathgoldContent.SHOVEL.get());
        int start = (width - 7 * 42) / 2;
        for (int i = 0; i < items.size(); i++) {
            int x = start + i * 42;
            g.fill(x - 2, 65, x + 36, 103, 0xff504a50);
            g.pose().pushPose();
            g.pose().translate(x, 68, 0);
            g.pose().scale(2, 2, 1);
            g.renderItem(new ItemStack(items.get(i)), 0, 0);
            g.pose().popPose();
        }
        g.drawCenteredString(
                font, "32 x 32 textures | iron tier | 375 durability", width / 2, 125, 0xffddd5ce);
        g.drawCenteredString(
                font,
                Component.translatable("tooltip.purified_undead.breathgold"),
                width / 2,
                145,
                0xffb67979);
        if (++frames == 40) {
            g.flush();
            Screenshot.grab(
                    minecraft.gameDirectory,
                    "breathgold-0.2.0.png",
                    minecraft.getMainRenderTarget(),
                    message -> {
                        captured = true;
                        LogUtils.getLogger()
                                .info(
                                        "BREATHGOLD_CLIENT_OK models=7 screenshot={}",
                                        message.getString());
                    });
        }
        if (captured && frames > 55) minecraft.stop();
    }
}
