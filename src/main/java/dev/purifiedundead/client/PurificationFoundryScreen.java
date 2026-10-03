package dev.purifiedundead.client;

import dev.purifiedundead.foundry.PurificationFoundryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class PurificationFoundryScreen extends AbstractContainerScreen<PurificationFoundryMenu> {
    private static final ResourceLocation HANDS = ResourceLocation.fromNamespaceAndPath("purified_undead", "textures/gui/foundry_hands.png");
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
        g.blit(HANDS, x+129, y+42, 64, 52, 64, 0, 64, 52, 128, 52);
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        sword(g, x+100, y+46, false);
        int filled = menu.progressPixels(61);
        if (filled > 0) { g.enableScissor(x+86,y+46,x+115,y+46+filled); sword(g,x+100,y+46,true); g.disableScissor(); }
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)slot(g,x+20+col*18,y+150+row*18);
        for(int col=0;col<9;col++)slot(g,x+20+col*18,y+208);
    }
    private static void slot(GuiGraphics g, int x, int y) {
        g.fill(x-1,y-1,x+17,y+17,0xff373737);g.fill(x,y,x+17,y+17,0xffffffff);g.fill(x,y,x+16,y+16,0xff8b8b8b);
    }
    private static void sword(GuiGraphics g, int x, int y, boolean active) {
        int edge = active ? 0xff518a9e : 0xff45434d;
        int shade = active ? 0xff94d9e6 : 0xff777e8a;
        int light = active ? 0xffefffff : 0xffc5ccd1;
        int gem = active ? 0xffff6579 : 0xff934658;
        // Pommel above the grip; the blade tapers down toward the output slot.
        for (int row = 0; row < 9; row++) {
            int half = Math.min(row, 8-row);
            g.fill(x-half, y+row, x+half+1, y+row+1, edge);
            if (half > 0) g.fill(x-half+1, y+row, x+half, y+row+1, gem);
        }
        g.fill(x-2,y+9,x+3,y+20,edge);
        g.fill(x,y+10,x+1,y+20,shade);
        for (int row=11;row<20;row+=3) g.fill(x-1,y+row,x+2,y+row+1,light);
        // Swept, stepped crossguard and a small central collar.
        for (int side : new int[]{-1,1}) {
            for (int step=0;step<4;step++) {
                int offset=4+step*2, top=21-step;
                int left=side<0?x-offset-2:x+offset;
                g.fill(left,y+top,left+3,y+top+3,edge);
                g.fill(left,y+top,left+2,y+top+1,shade);
            }
        }
        g.fill(x-4,y+20,x+5,y+25,edge);
        g.fill(x-2,y+21,x+3,y+24,shade);
        g.fill(x,y+21,x+1,y+24,light);
        // Narrow two-tone blade with a visible ridge and a seven-pixel tip.
        for (int row=25;row<61;row++) {
            int half=row<54?3:Math.max(0,(60-row)/2);
            g.fill(x-half,y+row,x+half+1,y+row+1,edge);
            if(half>0) {
                g.fill(x-half+1,y+row,x+1,y+row+1,light);
                g.fill(x+1,y+row,x+half,y+row+1,shade);
            }
        }
    }
    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        renderBackground(g); super.render(g,mouseX,mouseY,partial); renderTooltip(g,mouseX,mouseY);
    }
    private record SlotPosition(int x,int y) {}
}
