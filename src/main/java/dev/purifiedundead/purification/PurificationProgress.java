package dev.purifiedundead.purification;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import dev.purifiedundead.content.ModItems;
import top.theillusivec4.curios.api.CuriosApi;
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid="purified_undead")
public final class PurificationProgress {
 public static final String KEY="purified_undead:forbidden_fruit";
 private static final java.util.UUID ID=java.util.UUID.fromString("52358370-e56e-46c5-b767-625fd3113955");
 public static boolean used(Player p){return p.getPersistentData().getBoolean(KEY);}
 public static boolean contract(Player p){return CuriosApi.getCuriosInventory(p).map(h->h.getCurios().values().stream().anyMatch(v->{
  var slots=v.getStacks();for(int i=0;i<slots.getSlots();i++)if(slots.getStackInSlot(i).is(ModItems.ANCIENT_CONTRACT.get()))return true;return false;
 })).orElse(false);}
 public static void unlock(Player p){p.getPersistentData().putBoolean(KEY,true);ensure(p);}
 public static void ensure(Player p){if(p.level().isClientSide||!used(p))return;
  CuriosApi.getCuriosInventory(p).ifPresent(h->{for(String slot:new String[]{"white_witch_relic","wanderer_log"}){
   var v=h.getCurios().get(slot);if(v!=null&&!v.getModifiers().containsKey(ID)){h.addPermanentSlotModifier(slot,ID,"Pure forbidden fruit",1,AttributeModifier.Operation.ADDITION);}
  }});
 }
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void clone(net.minecraftforge.event.entity.player.PlayerEvent.Clone e){
  if(used(e.getOriginal()))e.getEntity().getPersistentData().putBoolean(KEY,true);
 }
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void login(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent e){ensure(e.getEntity());}
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.PlayerTickEvent e){if(e.phase!=net.minecraftforge.event.TickEvent.Phase.END)return; if(e.player.tickCount%20==0)ensure(e.player);}
}
