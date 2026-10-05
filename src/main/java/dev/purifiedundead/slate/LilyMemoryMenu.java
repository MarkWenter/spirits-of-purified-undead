package dev.purifiedundead.slate;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;
import dev.purifiedundead.content.ModItems;

/** The open main-hand book is locked; every edit is saved immediately into that same stack. */
public final class LilyMemoryMenu extends AbstractContainerMenu {
    public static final int[][] POS={{92,20},{127,34},{142,69},{127,104},{92,118},{57,104},{42,69},{57,34}};
    private final Inventory inv;private final ItemStack book;private final int locked;private final SimpleContainer memories;
    public LilyMemoryMenu(int id,Inventory inv){
        super(SlateContent.MENU.get(),id);this.inv=inv;locked=inv.selected;book=inv.getItem(locked);memories=new SimpleContainer(8);
        var stored=MemoryStorage.read(book);for(int i=0;i<8;i++)memories.setItem(i,stored.get(i));
        memories.addListener(c->{if(!inv.player.level().isClientSide&&book.is(ModItems.LILY_DIARY.get())){var list=NonNullList.withSize(8,ItemStack.EMPTY);for(int i=0;i<8;i++)list.set(i,memories.getItem(i).copy());MemoryStorage.write(book,list);inv.setChanged();}});
        for(int i=0;i<8;i++){final int index=i;addSlot(new Slot(memories,i,POS[i][0],POS[i][1]){
            @Override public boolean mayPlace(ItemStack stack){String key=SlateContent.memoryKey(stack);if(key==null)return false;for(int n=0;n<8;n++)if(n!=index&&key.equals(SlateContent.memoryKey(memories.getItem(n))))return false;return true;}
            @Override public int getMaxStackSize(){return 1;}
        });}
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addPlayerSlot(col+row*9+9,20+col*18,151+row*18);
        for(int col=0;col<9;col++)addPlayerSlot(col,20+col*18,209);
    }
    private void addPlayerSlot(int inventoryIndex,int x,int y){addSlot(new Slot(inv,inventoryIndex,x,y){@Override public boolean mayPickup(Player p){return inventoryIndex!=locked;}@Override public boolean mayPlace(ItemStack s){return inventoryIndex!=locked;}});}
    @Override public boolean stillValid(Player p){return p.isAlive()&&p.getInventory().selected==locked&&p.getMainHandItem()==book&&book.is(ModItems.LILY_DIARY.get());}
    @Override public void clicked(int slot,int button,ClickType type,Player p){if(!p.level().isClientSide&&!stillValid(p))return;if(type==ClickType.SWAP&&button==locked)return;super.clicked(slot,button,type,p);}
    @Override public ItemStack quickMoveStack(Player p,int index){if(!p.level().isClientSide&&!stillValid(p))return ItemStack.EMPTY;if(index<0||index>=slots.size())return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem()||!slot.mayPickup(p))return ItemStack.EMPTY;var s=slot.getItem();var copy=s.copy();if(!moveItemStackTo(s,index<8?8:0,index<8?44:8,index<8))return ItemStack.EMPTY;if(s.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,s);return copy;}
}
