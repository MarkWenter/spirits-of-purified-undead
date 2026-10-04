package dev.purifiedundead.client;

import dev.purifiedundead.slate.LilyMemoryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Parchment pages and a circular eight-memory layout, in native GUI pixels. */
public final class LilyMemoryScreen extends AbstractContainerScreen<LilyMemoryMenu> {
    public LilyMemoryScreen(LilyMemoryMenu m,Inventory i,Component title){super(m,i,title);imageWidth=200;imageHeight=233;inventoryLabelX=20;inventoryLabelY=139;}
    @Override protected void init(){super.init();titleLabelX=(imageWidth-font.width(title))/2;}
    @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){int x=leftPos,y=topPos;
        g.fill(x,y,x+200,y+233,0xff453b37);g.fill(x+3,y+3,x+197,y+230,0xffa49378);g.fill(x+7,y+5,x+193,y+228,0xffd4c5a2);
        g.fill(x+98,y+17,x+100,y+136,0xffb5a585);g.fill(x+100,y+17,x+102,y+136,0xffe4d5b4);
        g.fill(x+9,y+8,x+12,y+225,0xff806d57);g.fill(x+187,y+8,x+190,y+225,0xffb6a381);
        for(int[] p:LilyMemoryMenu.POS)slot(g,x+p[0],y+p[1]);
        var mc=net.minecraft.client.Minecraft.getInstance();g.renderItem(new net.minecraft.world.item.ItemStack(dev.purifiedundead.content.ModItems.LILY_DIARY.get()),x+92,y+69);
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)slot(g,x+20+18*col,y+151+18*row);
        for(int col=0;col<9;col++)slot(g,x+20+18*col,y+209);
    }
    private void slot(GuiGraphics g,int x,int y){g.fill(x-1,y-1,x+17,y+17,0xff5e5145);g.fill(x,y,x+17,y+17,0xffeddec0);g.fill(x,y,x+16,y+16,0xff9c8b71);}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){renderBackground(g);super.render(g,mx,my,partial);renderTooltip(g,mx,my);}
}
