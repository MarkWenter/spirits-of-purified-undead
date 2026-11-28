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
                        BreathgoldContent.INGOT.get(),
                        BreathgoldContent.SWORD.get(),
                        BreathgoldContent.AXE.get(),
                        BreathgoldContent.HOE.get(),
                        BreathgoldContent.PICKAXE.get(),
                        BreathgoldContent.SHOVEL.get(),
                        BreathgoldContent.HELMET.get(),
                        BreathgoldContent.CHESTPLATE.get(),
                        BreathgoldContent.LEGGINGS.get(),
                        BreathgoldContent.BOOTS.get());
        int start = (width - 6 * 42) / 2;
        for (int i = 0; i < items.size(); i++) {
            int x = start + (i % 6) * 42;
            int y = 42 + 43 * (i / 6);
            g.fill(x - 2, y - 3, x + 36, y + 35, 0xff504a50);
            g.pose().pushPose();
            g.pose().translate(x, y, 0);
            g.pose().scale(2, 2, 1);
            g.renderItem(new ItemStack(items.get(i)), 0, 0);
            g.pose().popPose();
        }
        g.drawCenteredString(
                font, "Breathgold ingot, tools and armor", width / 2, 125, 0xffddd5ce);
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
                    "breathgold-armaments-3.png",
                    minecraft.getMainRenderTarget(),
                    message -> {
                        captured = true;
                        LogUtils.getLogger()
                                .info(
                                        "BREATHGOLD_CLIENT_OK models=12 screenshot={}",
                                        message.getString());
                    });
        }
        if (captured && frames > 55) minecraft.stop();
    }
}
