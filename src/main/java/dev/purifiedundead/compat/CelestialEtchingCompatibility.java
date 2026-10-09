package dev.purifiedundead.compat;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import java.lang.reflect.Field;
import java.util.function.Predicate;
/** Optional Celestial Core bridge. Keeps its native prerequisites, chance, fallback and item emission. */
public final class CelestialEtchingCompatibility {
 private static final ThreadLocal<DamageSource> SOURCE=new ThreadLocal<>();
 private static Field itemField;private static boolean failed;
 public static boolean active(DamageSource source){return SOURCE.get()==source;}
 public static boolean test(Object modifier,Predicate<LootContext> conditions,LootContext context){
  // No reflection, equipment lookup or context allocation for unrelated loot modifiers.
  if(!modifier.getClass().getName().equals("com.xiaoyue.celestial_core.content.loot.AddItemModifier"))return conditions.test(context);
  var victim=context.getParamOrNull(LootContextParams.THIS_ENTITY);var source=context.getParamOrNull(LootContextParams.DAMAGE_SOURCE);
  if(victim==null||victim.getType()!=EntityType.WARDEN||source==null||!(source.getEntity() instanceof ServerPlayer p)
      ||!dev.purifiedundead.slate.MemoryStorage.active(p,"hoenir")||!isEtching(modifier))return conditions.test(context);
  var previous=SOURCE.get();SOURCE.set(source);
  try{return conditions.test(context);}finally{if(previous==null)SOURCE.remove();else SOURCE.set(previous);}
 }
 private static boolean isEtching(Object modifier){
  if(failed)return false;
  try{
   if(itemField==null)itemField=modifier.getClass().getField("item");
   return itemField.get(modifier) instanceof Item item&&BuiltInRegistries.ITEM.getKey(item).toString().equals("celestial_artifacts:nihility_etching");
  }catch(ReflectiveOperationException|LinkageError|RuntimeException ex){
   failed=true;com.mojang.logging.LogUtils.getLogger().warn("Celestial etching bridge unavailable; native loot conditions retained",ex);return false;
  }
 }
 private CelestialEtchingCompatibility(){}
}
