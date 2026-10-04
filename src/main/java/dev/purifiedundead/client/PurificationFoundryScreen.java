package dev.purifiedundead.client;

import dev.purifiedundead.foundry.PurificationFoundryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class PurificationFoundryScreen extends AbstractContainerScreen<PurificationFoundryMenu> {
    private static final ResourceLocation HANDS = ResourceLocation.fromNamespaceAndPath("purified_undead", "textures/gui/foundry_hands.png");
    private static final ResourceLocation HILT = ResourceLocation.fromNamespaceAndPath("purified_undead", "textures/gui/foundry_hilt.png");
    public PurificationFoundryScreen(PurificationFoundryMenu menu, Inventory inv, Component title) {
        super(menu, inv, title); imageWidth = 200; imageHeight = 232; inventoryLabelX = 20; inventoryLabelY = 139;
    }
    @Override protected void init() { super.init(); titleLabelX = (imageWidth - font.width(title)) / 2; }
    @Override protected void renderBg(GuiGraphics g, float partial, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        g.fill(x, y, x+200, y+232, 0xff373737);
        g.fill(x+1, y+1, x+199, y+231, 0xffffffff);
        g.fill(x+3, y+3, x+199, y+231, 0xff555555);
        g.fill(x+3, y+3, x+197, y+229, 0xffc6c6c6);
        for (SlotPosition s : new SlotPosition[]{new SlotPosition(92,23),new SlotPosition(56,65),new SlotPosition(136,65),new SlotPosition(92,113)}) slot(g, x+s.x, y+s.y);
        // Native GUI pixels: no fractional scaling of the two transparent arm sprites.
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        g.blit(HANDS, x+7, y+48, 64, 52, 0, 0, 64, 52, 128, 52);
        g.blit(HANDS, x+129, y+46, 64, 52, 64, 0, 64, 52, 128, 52);
        swordBlade(g, x+100, y+65, menu.progressPixels(42));
        g.blit(HILT, x+84, y+43, 32, 24, 0, 0, 32, 24, 32, 24);
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)slot(g,x+20+col*18,y+150+row*18);
        for(int col=0;col<9;col++)slot(g,x+20+col*18,y+208);
    }
    private static void slot(GuiGraphics g, int x, int y) {
        g.fill(x-1,y-1,x+17,y+17,0xff373737);g.fill(x,y,x+17,y+17,0xffffffff);g.fill(x,y,x+16,y+16,0xff8b8b8b);
    }
    private static void swordBlade(GuiGraphics g, int x, int y, int filled) {
        // One native GUI pixel per edge step, matching the hilt and hand sprites.
        // Only the hollow blade fills: no progress is spent on the handle or guard.
        for (int row = 0; row < 42; row++) {
            int half = row < 32 ? 5 : Math.max(0, (42-row)/2);
            g.fill(x-half,y+row,x+half+1,y+row+1,0xff38363e);
            if (half > 1) {
                g.fill(x-half+1,y+row,x-half+2,y+row+1,0xff929098);
                g.fill(x+half-1,y+row,x+half,y+row+1,0xff68656e);
                g.fill(x-half+2,y+row,x+half-1,y+row+1,row<filled?0xffffffff:0xffc6c6c6);
            } else if (row < filled) {
                // The last narrow rows let white reach the actual point at 100%.
                g.fill(x,y+row,x+1,y+row+1,0xffffffff);
            } else {
                g.fill(x,y+row,x+1,y+row+1,0xffc6c6c6);
            }
        }
    }
    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        renderBackground(g); super.render(g,mouseX,mouseY,partial); renderTooltip(g,mouseX,mouseY);
    }
    private record SlotPosition(int x,int y) {}
}
