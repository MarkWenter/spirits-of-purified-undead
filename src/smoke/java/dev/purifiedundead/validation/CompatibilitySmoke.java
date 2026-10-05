package dev.purifiedundead.validation;
import dev.purifiedundead.slate.*;
import dev.purifiedundead.foundry.*;
import dev.purifiedundead.content.ModItems;
import net.minecraft.world.item.*;
import net.minecraft.core.*;
import net.minecraft.network.FriendlyByteBuf;
import java.util.*;
/** Bounded stress and malformed-input checks, development servers only. */
public final class CompatibilitySmoke {
 private static void check(boolean ok,String why){if(!ok)throw new IllegalStateException("COMPATIBILITY_FAILED: "+why);}
 public static void run(net.minecraft.server.MinecraftServer server){try{runChecked(server);}catch(ReflectiveOperationException e){throw new IllegalStateException("COMPATIBILITY_FAILED",e);}}
 private static void runChecked(net.minecraft.server.MinecraftServer server) throws ReflectiveOperationException {
  var book=new ItemStack(ModItems.LILY_DIARY.get());var memories=NonNullList.withSize(8,ItemStack.EMPTY);
  for(int i=0;i<8;i++)memories.set(i,new ItemStack(SlateContent.MEMORIES.get(SlateContent.WARRIORS[i]).get()));
  MemoryStorage.write(book,memories);check(MemoryStorage.mask(book)==255,"all eight identities");
  var bean=(com.sun.management.ThreadMXBean)java.lang.management.ManagementFactory.getThreadMXBean();long thread=Thread.currentThread().getId();
  for(int i=0;i<2000;i++)MemoryStorage.mask(book);
  long before=bean.getThreadAllocatedBytes(thread);int value=0;for(int i=0;i<2000;i++)value|=MemoryStorage.mask(book);long fast=bean.getThreadAllocatedBytes(thread)-before;
  before=bean.getThreadAllocatedBytes(thread);for(int i=0;i<2000;i++)for(var item:MemoryStorage.read(book))value|=MemoryStorage.bit(SlateContent.memoryKey(item));long legacy=bean.getThreadAllocatedBytes(thread)-before;
  check(value==255&&fast<legacy,"effect lookup allocates less than full deserialization: "+fast+" vs "+legacy);
  memories.set(0,ItemStack.EMPTY);MemoryStorage.write(book,memories);check(MemoryStorage.mask(book)==254,"same-tick content mutation visible");
  var player=new net.minecraftforge.common.util.FakePlayer(server.overworld(),new com.mojang.authlib.GameProfile(UUID.randomUUID(),"CompatSmoke"));player.getInventory().selected=0;player.getInventory().setItem(0,book);
  var menu=new LilyMemoryMenu(1,player.getInventory());player.getInventory().setItem(0,new ItemStack(Items.STONE));
  check(menu.quickMoveStack(player,1).isEmpty(),"stale book menu cannot extract memories");check(MemoryStorage.mask(book)==254,"detached book unchanged");
  var bytes=new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
  try {bytes.writeVarInt(1);bytes.writeUtf("minecraft:coal");bytes.writeVarInt(0);bytes.writeUtf("minecraft:coal");bytes.writeVarInt(1);bytes.writeUtf("minecraft:diamond");bytes.writeVarInt(2);bytes.writeBoolean(true);bytes.writeVarInt(1);bytes.writeVarInt(1);bytes.writeVarInt(400);
   boolean rejected=false;try{FoundryRecipes.read(bytes);}catch(IllegalArgumentException expected){rejected=true;}check(rejected,"zero-count network recipe rejected before processing");
  }finally{bytes.release();}
  var field=FoundryRecipes.class.getDeclaredField("recipes");field.setAccessible(true);var original=FoundryRecipes.all();
  try {
   var large=new ArrayList<FoundryRecipes.Recipe>();for(int i=0;i<4095;i++)large.add(new FoundryRecipes.Recipe("minecraft:stone",64,"minecraft:dirt",64,"minecraft:diamond",1,true,1,1,400));
   var coal=FoundryRecipes.defaults().get(0);large.add(coal);FoundryRecipes.validateSync(large);field.set(null,List.copyOf(large));
   check(FoundryRecipes.find(new ItemStack(Items.COAL,64),new ItemStack(Items.COAL,64)).equals(coal),"4096 recipe index preserves matching");
   check(!FoundryRecipes.top(ItemStack.EMPTY)&&!FoundryRecipes.bottom(new ItemStack(Items.APPLE)),"indexed input exclusions");
   var idle=new PurificationFoundryEntity(new BlockPos(48,120,48),FoundryContent.BLOCK.get().defaultBlockState());idle.setLevel(server.overworld());
   before=System.nanoTime();for(int i=0;i<100000;i++)idle.process();long elapsed=System.nanoTime()-before;
   check(idle.isEmpty(),"100000 idle ticks preserve inventory");
   System.out.println("COMPATIBILITY_PERF: mask bytes="+fast+" legacy bytes="+legacy+" idle 100000 ticks ns="+elapsed);
  }finally{field.set(null,original);FoundryRecipes.find(new ItemStack(Items.COAL),new ItemStack(Items.COAL));player.discard();}
  System.out.println("COMPATIBILITY_OK: low-allocation identities, immediate mutation, stale menu, malformed packet, 4096 recipes and 100000 idle ticks");
 }
}
