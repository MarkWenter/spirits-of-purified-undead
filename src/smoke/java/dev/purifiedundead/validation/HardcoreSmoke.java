package dev.purifiedundead.validation;
import dev.purifiedundead.content.*;
import dev.purifiedundead.progress.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.effect.*;
public final class HardcoreSmoke {
 private static void check(boolean v,String s){if(!v)throw new IllegalStateException("HARDCORE_FAILED: "+s);}
 private static ServerPlayer player(net.minecraft.server.MinecraftServer s)throws Exception{
  var p=new ServerPlayer(s,s.overworld(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"HardcoreTest"));
  p.connection=new net.minecraftforge.common.util.FakePlayer(s.overworld(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"NetworkStub")).connection;
  var field=ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");field.setAccessible(true);field.setInt(p,0);
  p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.setPos(0,100,0);return p;
 }
 private static void blight(ServerPlayer p){p.addEffect(new MobEffectInstance(ModEffects.BLIGHTED_TRANSFORMATION.get(),12000));}
 private static int contracts(ServerPlayer p){int n=0;for(var s:p.getInventory().items)if(s.is(ModItems.ANCIENT_CONTRACT.get()))n+=s.getCount();return n;}
 public static void run(net.minecraft.server.MinecraftServer server)throws Exception{
  var data=server.getWorldData();var settings=data.getLevelSettings();java.lang.reflect.Field field=null;
  for(var f:data.getClass().getDeclaredFields())if(f.getType()==net.minecraft.world.level.LevelSettings.class){field=f;break;}
  check(field!=null,"test world settings");field.setAccessible(true);
  var properties=((net.minecraft.server.dedicated.DedicatedServer)server).getProperties();
  var hardcore=properties.getClass().getField("hardcore");hardcore.setAccessible(true);boolean originalHardcore=hardcore.getBoolean(properties);
  try {
   hardcore.setBoolean(properties,false);
   field.set(data,new net.minecraft.world.level.LevelSettings(settings.levelName(),settings.gameType(),false,settings.difficulty(),settings.allowCommands(),settings.gameRules(),settings.getDataConfiguration()));
   var ordinary=player(server);blight(ordinary);check(!HardcoreBlightRescue.rescue(ordinary)&&ordinary.hasEffect(ModEffects.BLIGHTED_TRANSFORMATION.get())&&contracts(ordinary)==0,"ordinary mode unchanged");
   field.set(data,new net.minecraft.world.level.LevelSettings(settings.levelName(),settings.gameType(),true,settings.difficulty(),settings.allowCommands(),settings.gameRules(),settings.getDataConfiguration()));
   hardcore.setBoolean(properties,true);
   check(server.isHardcore(),"real hardcore world flag");
   var fresh=player(server);check(!HardcoreBlightRescue.rescue(fresh),"no effect no rescue");blight(fresh);fresh.hurt(fresh.damageSources().generic(),100);
   check(fresh.isAlive()&&fresh.getHealth()==1&&contracts(fresh)==1,"fatal hit prevented and contract given");check(!fresh.hasEffect(ModEffects.BLIGHTED_TRANSFORMATION.get()),"ritual consumed");
   check(fresh.getEffect(MobEffects.REGENERATION).getDuration()==900&&fresh.getEffect(MobEffects.REGENERATION).getAmplifier()==1&&fresh.hasEffect(MobEffects.ABSORPTION)&&fresh.hasEffect(MobEffects.FIRE_RESISTANCE),"vanilla totem buffs");
   check(!HardcoreBlightRescue.rescue(fresh),"cannot repeat without another dose");blight(fresh);HardcoreBlightRescue.rescue(fresh);check(contracts(fresh)==1,"contract never duplicates");
   var held=player(server);held.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,new ItemStack(Items.TOTEM_OF_UNDYING));blight(held);held.hurt(held.damageSources().generic(),100);
   check(held.isAlive()&&held.getOffhandItem().isEmpty()&&contracts(held)==1&&!held.hasEffect(ModEffects.BLIGHTED_TRANSFORMATION.get()),"real held totem also awards contract");
   var relic=player(server);dev.purifiedundead.purification.PurificationProgress.unlock(relic);
   top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(relic).orElseThrow(()->new IllegalStateException("Curios missing")).getCurios().get("white_witch_relic").getStacks().setStackInSlot(0,new ItemStack(ModItems.WHITE_PRIESTESS_STATUE.get()));
   blight(relic);relic.hurt(relic.damageSources().generic(),100);check(relic.isAlive()&&contracts(relic)==1&&relic.getPersistentData().getCompound("purified_undead:white_witch_relics").contains("white_priestess_statue"),"relic rescue awards contract before clearing blight");
   var config=dev.purifiedundead.config.PurifiedUndeadConfig.VALUES.contractAcquisitionEnabled;boolean enabled=config.get();
   try{config.set(false);var disabled=player(server);blight(disabled);check(!HardcoreBlightRescue.rescue(disabled)&&contracts(disabled)==0,"disabled acquisition respected");}finally{config.set(enabled);}
   var full=player(server);for(int i=0;i<36;i++)full.getInventory().setItem(i,new ItemStack(Items.COBBLESTONE,64));blight(full);full.hurt(full.damageSources().generic(),100);check(full.isAlive()&&full.getPersistentData().getCompound("purified_undead:warrior_progress").getBoolean("contract_pending"),"full inventory survives with pending reward");full.getInventory().setItem(0,ItemStack.EMPTY);WarriorRewardService.retryPending(full);check(contracts(full)==1,"pending delivery");
   var a=server.getAdvancements().getAdvancement(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("purified_undead","journey/hidden_power"));check(fresh.getAdvancements().getOrStartProgress(a).isDone(),"hidden power awarded");
   System.out.println("HARDCORE_OK: real fatal damage survived, totem buffs, effect consumed, ordinary/no-effect guards, no duplicate contract, real held totem, relic rescue, disabled config, pending delivery, advancement");
  }finally{field.set(data,settings);hardcore.setBoolean(properties,originalHardcore);}
 }
}

