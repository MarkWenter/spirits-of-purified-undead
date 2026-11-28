package dev.purifiedundead.validation;

import com.mojang.logging.LogUtils;

import dev.purifiedundead.blightedarmaments.ImmaculateTools;

import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class ImmaculatePreviewScreen extends Screen {
    private int frames;
    private volatile boolean captured;

    public ImmaculatePreviewScreen() {
        super(Component.literal("Breathgold texture verification"));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        if (minecraft.getOverlay() != null) {
            frames = 0;
            return;
        }
        g.fill(0, 0, width, height, 0xff26242a);
        g.drawCenteredString(font, "PURIFIED UNDEAD / IMMACULATE", width / 2, 20, 0xffe6c58b);
        var items =
                java.util.List.of(
                        ImmaculateTools.SWORD.get(),
                        ImmaculateTools.SHOVEL.get(),
                        ImmaculateTools.AXE.get(),
                        ImmaculateTools.HOE.get(),
                        ImmaculateTools.PICKAXE.get());
        int start = (width - 5 * 42) / 2;
        for (int i = 0; i < items.size(); i++) {
            int x = start + (i % 5) * 42;
            int y = 42 + 43 * (i / 5);
            g.fill(x - 2, y - 3, x + 36, y + 35, 0xff504a50);
            g.pose().pushPose();
            g.pose().translate(x, y, 0);
            g.pose().scale(2, 2, 1);
            g.renderItem(new ItemStack(items.get(i)), 0, 0);
            g.pose().popPose();
        }
        for (int i = 0; i < items.size(); i++) {
            g.pose().pushPose();
            g.pose().translate(start + i * 42 + 14, 123, 50);
            g.pose().scale(34, -34, 34);
            g.pose().mulPose(com.mojang.math.Axis.YP.rotationDegrees(60));
            minecraft
                    .getItemRenderer()
                    .renderStatic(
                            new ItemStack(items.get(i)),
                            net.minecraft.world.item.ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
                            15728880,
                            net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                            g.pose(),
                            g.bufferSource(),
                            null,
                            0);
            g.pose().popPose();
        }
        g.drawCenteredString(font, "Inventory / right hand", width / 2, 165, 0xffddd5ce);
        if (++frames == 40) {
            g.flush();
            Screenshot.grab(
                    minecraft.gameDirectory,
                    "immaculate-tools.png",
                    minecraft.getMainRenderTarget(),
                    message -> {
                        captured = true;
                        LogUtils.getLogger()
                                .info(
                                        "IMMACULATE_CLIENT_OK models=5 screenshot={}",
                                        message.getString());
                    });
        }
        if (captured && frames > 55) minecraft.stop();
    }
}
