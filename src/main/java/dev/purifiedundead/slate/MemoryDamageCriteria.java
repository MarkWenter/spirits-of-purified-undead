package dev.purifiedundead.slate;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.damagesource.DamageSource;
/** The extra identities exist only inside a loot/kill criterion evaluation, never combat. */
public final class MemoryDamageCriteria {
 private static final ThreadLocal<DamageSource> SOURCE=new ThreadLocal<>();
 public static boolean active(DamageSource source){return SOURCE.get()==source;}
 public static boolean matches(DamageSourcePredicate predicate,ServerLevel level,Vec3 position,DamageSource source){
  if(!(source.getEntity() instanceof net.minecraft.server.level.ServerPlayer p)||!MemoryStorage.active(p,"hoenir"))return predicate.matches(level,position,source);
  DamageSource previous=SOURCE.get();SOURCE.set(source);
  try{return predicate.matches(level,position,source);}finally{if(previous==null)SOURCE.remove();else SOURCE.set(previous);}
 }
 private MemoryDamageCriteria(){}
}
