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
        // Two separate UV regions preserve the generated alpha and place fingers around the slots.
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        g.blit(HANDS, x+7, y+29, 72, 82, 0, 0, 128, 256, 256, 256);
        g.blit(HANDS, x+121, y+33, 72, 82, 128, 0, 128, 256, 256, 256);
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
        int edge=active?0xff70bacb:0xff444047, blade=active?0xfff1ffff:0xff92959c;
        g.fill(x-1,y,x+2,y+4,edge);g.fill(x-3,y+4,x+4,y+37,edge);g.fill(x-1,y+5,x+2,y+36,blade);
        g.fill(x-10,y+35,x+11,y+38,edge);g.fill(x-12,y+31,x-9,y+36,edge);g.fill(x+10,y+31,x+13,y+36,edge);
        g.fill(x-2,y+38,x+3,y+51,edge);g.fill(x,y+39,x+1,y+50,blade);
        for(int j=0;j<10;j++){int w=j<5?j+1:10-j;g.fill(x-w,y+51+j,x+w+1,y+52+j,edge);if(w>1)g.fill(x-w+1,y+51+j,x+w,y+52+j,active?0xffff5265:0xff873343);}
    }
    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        renderBackground(g); super.render(g,mouseX,mouseY,partial); renderTooltip(g,mouseX,mouseY);
    }
    private record SlotPosition(int x,int y) {}
}
