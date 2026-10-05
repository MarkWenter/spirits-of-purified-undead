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
    /** Read identity only: never deserialize nested item capabilities during an effect check. */
    public static int mask(ItemStack book){
        if(!book.is(ModItems.LILY_DIARY.get()))return 0;
        int mask=0;
        if(!book.hasTag()||!book.getTag().contains("SlateMemories",net.minecraft.nbt.Tag.TAG_COMPOUND))return 0;
        var list=book.getTag().getCompound("SlateMemories").getList("Items",net.minecraft.nbt.Tag.TAG_COMPOUND);
        if(list.size()>256)return 0; // Malformed external data stays stored, but cannot monopolize a tick.
        // Match ContainerHelper's last-entry-wins behavior, including malformed duplicate slots.
        for(int slot=0;slot<8;slot++){
            int value=0;
            for(int n=0;n<list.size();n++){var tag=list.getCompound(n);if((tag.getByte("Slot")&255)!=slot)continue;
                value=0;if(tag.getByte("Count")<=0)continue;
                String id=tag.getString("id");for(int i=0;i<SlateContent.WARRIORS.length;i++)if(id.equals(IDS[i])){value=1<<i;break;}
            }
            mask|=value;
        }
        return mask;
    }
    private static final String[] IDS=java.util.Arrays.stream(SlateContent.WARRIORS).map(k->"purified_undead:blighted_memory_"+k).toArray(String[]::new);
    public static int bit(String key){if(key==null)return 0;for(int i=0;i<SlateContent.WARRIORS.length;i++)if(key.equals(SlateContent.WARRIORS[i]))return 1<<i;return 0;}
    public static boolean active(Player player,String key){
        int wanted=bit(key);if(wanted==0||!player.isAlive()||player.isSpectator())return false;
        return CuriosApi.getCuriosInventory(player).map(h->{var slot=h.getCurios().get("wanderer_log");if(slot==null)return false;
            for(int i=0;i<slot.getStacks().getSlots();i++)if((mask(slot.getStacks().getStackInSlot(i))&wanted)!=0)return true;return false;}).orElse(false);
    }
    private MemoryStorage(){}
}
