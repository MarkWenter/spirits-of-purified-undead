package dev.purifiedundead.slate;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import dev.purifiedundead.content.ModItems;

/** Historical collection mask; delivery is retried while alive and never duplicates on clone/login. */
public final class MemoryRewards {
    private static final String MASK="purified_undead:memories_collected",GIFT="purified_undead:ferin_memory_received";
    public static void record(ServerPlayer p,ItemStack s){String k=SlateContent.memoryKey(s);if(k!=null)for(int i=0;i<7;i++)if(SlateContent.WARRIORS[i].equals(k))p.getPersistentData().putInt(MASK,p.getPersistentData().getInt(MASK)|(1<<i));}
    private static void inspect(ServerPlayer p,ItemStack s){record(p,s);if(s.is(ModItems.LILY_DIARY.get()))for(var memory:MemoryStorage.read(s))record(p,memory);}
    public static void scan(ServerPlayer p){if(!p.isAlive()||p.isSpectator())return;
        for(int i=0;i<p.getInventory().getContainerSize();i++)inspect(p,p.getInventory().getItem(i));inspect(p,p.containerMenu.getCarried());
        top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(p).ifPresent(h->h.getCurios().values().forEach(slot->{for(int i=0;i<slot.getStacks().getSlots();i++)inspect(p,slot.getStacks().getStackInSlot(i));}));
        if(p.getPersistentData().getInt(MASK)==127&&!p.getPersistentData().getBoolean(GIFT)&&dev.purifiedundead.progress.WarriorRewardService.insert(p,SlateContent.MEMORIES.get("ferin").get()))p.getPersistentData().putBoolean(GIFT,true);
    }
    public static void copy(CompoundTag a,CompoundTag b){b.putInt(MASK,a.getInt(MASK));b.putBoolean(GIFT,a.getBoolean(GIFT));}
}
