package dev.purifiedundead.purification;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.*;
import dev.purifiedundead.content.ModItems;
import top.theillusivec4.curios.api.CuriosApi;
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid="purified_undead")
public final class LilyDiaryProgress {
 public static final String KILLS="purified_undead:lily_types",GIFT="purified_undead:diary_received";
 public static final net.minecraft.tags.TagKey<net.minecraft.world.entity.EntityType<?>> UNDEAD=net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE,ResourceLocation.fromNamespaceAndPath("purified_undead","lily_undead"));
 private static final java.util.UUID BONUS=java.util.UUID.fromString("149e6b4d-9c9d-4e39-b5e0-6b0b1e119642");
 public static int count(Player p){return p.getPersistentData().getList(KILLS,Tag.TAG_STRING).size();}
 public static boolean equipped(Player p){return CuriosApi.getCuriosInventory(p).map(h->{var v=h.getCurios().get("wanderer_log");if(v==null)return false;var s=v.getStacks();for(int i=0;i<s.getSlots();i++)if(s.getStackInSlot(i).is(ModItems.LILY_DIARY.get()))return true;return false;}).orElse(false);}
 public static boolean record(Player p,LivingEntity victim){
  if(!equipped(p)||!(victim.getType().is(UNDEAD)||victim.getMobType()==net.minecraft.world.entity.MobType.UNDEAD))return false;
  String id=BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType()).toString();var list=p.getPersistentData().getList(KILLS,Tag.TAG_STRING);
  for(int i=0;i<list.size();i++)if(id.equals(list.getString(i)))return false;
  list.add(StringTag.valueOf(id));p.getPersistentData().put(KILLS,list);apply(p,list.size());return true;
 }
 public static void apply(Player p,int count){
  if(p.level().isClientSide)return;
  var critical=net.minecraftforge.registries.ForgeRegistries.ATTRIBUTES.getValue(ResourceLocation.fromNamespaceAndPath("attributeslib","crit_chance"));
  if(critical!=null)update(p.getAttribute(critical),count*.03);
  update(p.getAttribute(Attributes.LUCK),count*.5);
 }
 private static void update(AttributeInstance a,double amount){if(a==null)return;var old=a.getModifier(BONUS);if(old!=null&&Math.abs(old.getAmount()-amount)<1e-9)return;if(old!=null)a.removeModifier(BONUS);if(amount!=0)a.addTransientModifier(new AttributeModifier(BONUS,"Lily diary",amount,AttributeModifier.Operation.ADDITION));}
 public static void grant(Player p){if(p.getPersistentData().getBoolean(GIFT))return;var book=DiaryBridge.create();if(book.isEmpty())return;
  if(!p.getInventory().add(book)&&!book.isEmpty())p.drop(book,false);p.getPersistentData().putBoolean(GIFT,true);
 }
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void login(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent e){grant(e.getEntity());}
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void clone(net.minecraftforge.event.entity.player.PlayerEvent.Clone e){var from=e.getOriginal().getPersistentData();var to=e.getEntity().getPersistentData();if(from.contains(KILLS))to.put(KILLS,from.getList(KILLS,Tag.TAG_STRING).copy());if(from.getBoolean(GIFT))to.putBoolean(GIFT,true);}
 @net.minecraftforge.eventbus.api.SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.LOWEST) public static void kill(net.minecraftforge.event.entity.living.LivingDeathEvent e){if(e.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer p)record(p,e.getEntity());}
}
