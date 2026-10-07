package dev.purifiedundead.validation;
import dev.purifiedundead.combat.*;
import dev.purifiedundead.content.*;
import dev.purifiedundead.entity.GuardianWaveEntity;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
public final class GuardianWaveSmoke {
 private static void check(boolean ok,String why){if(!ok)throw new IllegalStateException("GUARDIAN_WAVE_FAILED: "+why);}
 private static net.minecraft.world.entity.animal.Cow cow(net.minecraft.server.level.ServerLevel l,double x,double z){var c=EntityType.COW.create(l);c.setPos(x,160,z);c.setNoAi(true);c.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);c.setHealth(1000);l.addFreshEntity(c);return c;}
 private static GuardianWaveEntity wave(net.minecraft.server.level.ServerPlayer p,boolean falling,float damage,float crit){var w=ModEntities.GUARDIAN_WAVE.get().create(p.serverLevel());w.launch(new GuardianWaveCombat.State(p,falling,false,1,damage,crit),new ItemStack(ModItems.BLIGHTED_GUARDIAN.get()));p.serverLevel().addFreshEntity(w);return w;}
 public static void run(net.minecraft.server.MinecraftServer server){
  var l=server.overworld();l.getChunk(3,3);var p=new net.minecraftforge.common.util.FakePlayer(l,new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"GuardianWave"));p.setPos(50,160,50);p.setYRot(0);p.setXRot(0);p.setOnGround(true);
  var targets=new java.util.ArrayList<LivingEntity>();
  try{
   var chance=net.minecraftforge.registries.ForgeRegistries.ATTRIBUTES.getValue(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("attributeslib","crit_chance"));if(chance!=null&&p.getAttribute(chance)!=null)p.getAttribute(chance).setBaseValue(0);
   var sword=new ItemStack(ModItems.BLIGHTED_GUARDIAN.get());p.setItemSlot(EquipmentSlot.MAINHAND,sword);
   var modifiers=sword.getAttributeModifiers(EquipmentSlot.MAINHAND);check(modifiers.get(Attributes.ARMOR).stream().mapToDouble(a->a.getAmount()).sum()==10,"held armor +10");check(sword.getAttributeModifiers(EquipmentSlot.OFFHAND).get(Attributes.ARMOR).isEmpty(),"no offhand armor");
   var counts=new java.util.HashMap<String,Integer>();
   for(var enchant:net.minecraft.core.registries.BuiltInRegistries.ENCHANTMENT){
    var id=net.minecraft.core.registries.BuiltInRegistries.ENCHANTMENT.getKey(enchant);if(!java.util.Set.of("apotheosis","celestial_enchantments","goety").contains(id.getNamespace())||enchant.isCurse())continue;
    if(dev.purifiedundead.compat.GuardianEnchantments.accepts(enchant)){
     check(enchant.canEnchant(sword),"optional anvil eligibility "+id);check(sword.canApplyAtEnchantingTable(enchant),"optional table eligibility "+id);
     var menu=new net.minecraft.world.inventory.AnvilMenu(21,p.getInventory());menu.getSlot(0).set(sword.copy());menu.getSlot(1).set(EnchantedBookItem.createForEnchantment(new net.minecraft.world.item.enchantment.EnchantmentInstance(enchant,1)));menu.createResult();check(!menu.getSlot(2).getItem().isEmpty(),"real anvil result "+id);counts.merge(id.getNamespace(),1,Integer::sum);
    }
   }
   if(!counts.isEmpty()){check(counts.getOrDefault("celestial_enchantments",0)>0&&counts.getOrDefault("apotheosis",0)>0&&counts.getOrDefault("goety",0)>0,"all three optional mods exercised");System.out.println("GUARDIAN_ENCHANT_OK: actual anvil and table eligibility "+counts);}
   var c=cow(l,50,52);targets.add(c);var outside=cow(l,50,54);targets.add(outside);
   c.hurt(p.damageSources().playerAttack(p),10);float before=c.getHealth();check(c.invulnerableTime>0,"direct hit grants hurt cooldown");
   var w=wave(p,false,10,1);for(int i=0;i<6;i++){w.tickCount++;w.tick();}check(Math.abs(before-c.getHealth()-12)<.05,"wave adds separate 120 percent hit: "+(before-c.getHealth()));check(outside.getHealth()==1000,"three block limit");check(w.isRemoved(),"wave expires");
   before=c.getHealth();w=wave(p,false,10,2);for(int i=0;i<6;i++){w.tickCount++;w.tick();}check(Math.abs(before-c.getHealth()-24)<.05,"independent crit multiplier");
   for(int y=160;y<=162;y++)l.setBlockAndUpdate(new BlockPos(50,y,51),Blocks.STONE.defaultBlockState());before=c.getHealth();w=wave(p,false,10,1);for(int i=0;i<6&&!w.isRemoved();i++){w.tickCount++;w.tick();}check(before==c.getHealth(),"solid wall blocks wave");
   for(int y=160;y<=162;y++)l.setBlockAndUpdate(new BlockPos(50,y,51),Blocks.AIR.defaultBlockState());
   var ally=cow(l,50,51.5);targets.add(ally);var board=server.getScoreboard();var team=board.addPlayerTeam("guardian_wave_test");board.addPlayerToTeam(p.getScoreboardName(),team);board.addPlayerToTeam(ally.getScoreboardName(),team);w=wave(p,false,10,1);for(int i=0;i<6;i++){w.tickCount++;w.tick();}check(ally.getHealth()==1000,"ally ignored");board.removePlayerTeam(team);ally.discard();
   dev.purifiedundead.purification.PurificationProgress.unlock(p);var curios=top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(p).orElseThrow(IllegalStateException::new);
   for(var entry:curios.getCurios().entrySet())if(entry.getKey().equals("ancient_contract"))entry.getValue().getStacks().setStackInSlot(0,new ItemStack(ModItems.ANCIENT_CONTRACT.get()));
   var warriors=curios.getCurios().get("undead_warrior").getStacks();if(warriors.getSlots()<3)warriors.grow(3-warriors.getSlots());
   warriors.setStackInSlot(0,new ItemStack(ModItems.ELEINE_WARRIOR.get()));warriors.setStackInSlot(1,new ItemStack(ModItems.HOENIR_WARRIOR.get()));warriors.setStackInSlot(2,new ItemStack(ModItems.GROTH_WARRIOR.get()));
   var book=new ItemStack(ModItems.LILY_DIARY.get());var memories=net.minecraft.core.NonNullList.withSize(8,ItemStack.EMPTY);memories.set(4,new ItemStack(dev.purifiedundead.slate.SlateContent.MEMORIES.get("eleine").get()));dev.purifiedundead.slate.MemoryStorage.write(book,memories);curios.getCurios().get("wanderer_log").getStacks().setStackInSlot(0,book);
   int orbs=l.getEntitiesOfClass(dev.purifiedundead.entity.EleineMagicOrbEntity.class,p.getBoundingBox().inflate(10)).size();
   server.getWorldData().overworldData().setGameTime(l.getGameTime()+1);
   check(curios.isEquipped(ModItems.ANCIENT_CONTRACT.get()),"contract equipped for cooperative trigger");
   w=wave(p,true,10,1);for(int i=0;i<6;i++){w.tickCount++;w.tick();}check(p.getPersistentData().contains("purified_undead:ferin_state"),"wave-only hit triggers Ferin");
   check(HoenirCombatEvents.hasMark(p,c),"wave applies Hoenir mark");check(c.hasEffect(ModEffects.STUNNED.get()),"captured falling state triggers Groth after landing");check(l.getEntitiesOfClass(dev.purifiedundead.entity.EleineMagicOrbEntity.class,p.getBoundingBox().inflate(10)).size()>orbs,"wave triggers Eleine orb");
   GuardianWaveCombat.with(new GuardianWaveCombat.State(p,true,false,1,10,1),()->check(GuardianWaveCombat.falling(p),"captured jump survives landing"));check(!GuardianWaveCombat.active(),"context cleared");
   p.getPersistentData().remove("purified_undead:guardian_wave_tick");int count=l.getEntitiesOfClass(GuardianWaveEntity.class,p.getBoundingBox().inflate(5),e->!e.isRemoved()).size();GuardianWaveCombat.swing(p);GuardianWaveCombat.swing(p);check(l.getEntitiesOfClass(GuardianWaveEntity.class,p.getBoundingBox().inflate(5),e->!e.isRemoved()).size()==count+1,"air swing accepted and duplicate packet blocked");
   System.out.println("GUARDIAN_WAVE_OK: mainhand armor, separate direct/wave damage, 120 percent, independent crit, range, wall, allies, expiry, captured jump, Groth stun, Eleine orb, Hoenir mark, Ferin trigger and duplicate packet guard");
  }finally{for(var t:targets)t.discard();for(var w:l.getEntitiesOfClass(GuardianWaveEntity.class,p.getBoundingBox().inflate(6)))w.discard();for(int y=160;y<=162;y++)l.setBlockAndUpdate(new BlockPos(50,y,51),Blocks.AIR.defaultBlockState());p.discard();}
 }
}
