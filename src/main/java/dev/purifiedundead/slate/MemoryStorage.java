package dev.purifiedundead.slate;

import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import dev.purifiedundead.content.ModItems;
import top.theillusivec4.curios.api.CuriosApi;

public final class MemoryStorage {
    public static NonNullList<ItemStack> read(ItemStack book){var items=NonNullList.withSize(8,ItemStack.EMPTY);if(book.hasTag())ContainerHelper.loadAllItems(book.getTag().getCompound("SlateMemories"),items);return items;}
    public static void write(ItemStack book,NonNullList<ItemStack> items){var tag=new net.minecraft.nbt.CompoundTag();ContainerHelper.saveAllItems(tag,items);book.getOrCreateTag().put("SlateMemories",tag);}
    public static boolean active(Player player,String key){
        if(!player.isAlive()||player.isSpectator())return false;
        return CuriosApi.getCuriosInventory(player).map(h->{var slot=h.getCurios().get("wanderer_log");if(slot==null)return false;
            for(int i=0;i<slot.getStacks().getSlots();i++){var book=slot.getStacks().getStackInSlot(i);if(book.is(ModItems.LILY_DIARY.get()))for(var s:read(book))if(key.equals(SlateContent.memoryKey(s)))return true;}return false;}).orElse(false);
    }
    private MemoryStorage(){}
}
