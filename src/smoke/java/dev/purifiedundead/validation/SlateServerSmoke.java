package dev.purifiedundead.validation;

import dev.purifiedundead.slate.*;
import dev.purifiedundead.foundry.*;
import dev.purifiedundead.content.ModItems;
import net.minecraft.core.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffects;
import top.theillusivec4.curios.api.CuriosApi;

public final class SlateServerSmoke {
    private static void check(boolean ok,String message){if(!ok)throw new IllegalStateException("SLATE_SERVER_FAILED: "+message);}
    public static void run(net.minecraft.server.MinecraftServer server){
        var level=server.overworld();var pos=new BlockPos(42,120,42);
        var p=new net.minecraftforge.common.util.FakePlayer(level,new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"SlateSmoke"));p.setPos(42.5,120,43.5);
        try{
            CompatibilitySmoke.run(server);
            GuardianWaveSmoke.run(server);
            check(FoundryRecipes.all().size()>=12,"configured recipes loaded");
            for(var recipe:FoundryRecipes.defaults())FoundryRecipes.validate(recipe);
            boolean rejected=false;try{FoundryRecipes.validate(new FoundryRecipes.Recipe("bad:missing",1,"minecraft:stone",1,"minecraft:diamond",1,true,1,1,400));}catch(IllegalArgumentException e){rejected=true;}check(rejected,"invalid configured id rejected");
            level.setBlockAndUpdate(pos,FoundryContent.BLOCK.get().defaultBlockState());var be=(PurificationFoundryEntity)level.getBlockEntity(pos);
            be.setItem(0,new ItemStack(Items.COAL,64));be.setItem(1,new ItemStack(ModItems.BLIGHTED_SPIRIT.get(),64));be.setItem(2,new ItemStack(ModItems.PURE_CRYSTAL.get(),64));be.setItem(3,new ItemStack(Items.COAL,64));
            check(!be.canTakeItemThroughFace(3,be.getItem(3),Direction.DOWN),"unprocessed bottom ingredient cannot leak to hopper");
            for(int n=0;n<399;n++)be.process();check(be.getItem(3).is(Items.COAL),"no early output");be.process();
            check(be.getItem(3).is(Items.DIAMOND)&&be.getItem(3).getCount()==64&&be.getItem(0).getCount()==64&&be.getItem(1).getCount()==32&&be.getItem(2).getCount()==32&&be.getItem(4).isEmpty(),"capacity capped batch and exact remaining inputs");
            var saved=be.saveWithFullMetadata();var restored=new PurificationFoundryEntity(pos,be.getBlockState());restored.load(saved);
            check(restored.getItem(4).isEmpty()&&restored.getItem(0).getCount()==64&&restored.canTakeItemThroughFace(3,restored.getItem(3),Direction.DOWN),"pending input and product state persist");
            be.removeItem(3,64);for(int n=0;n<400;n++)be.process();check(be.getItem(3).isEmpty()&&be.getItem(0).getCount()==64,"combined remainder stays visible above after output removed");
            be.setItem(0,new ItemStack(Items.COAL,32));be.setItem(3,new ItemStack(Items.COAL,32));for(int n=0;n<400;n++)be.process();check(be.getItem(3).getCount()==64&&be.getItem(0).isEmpty()&&be.getItem(1).isEmpty(),"split remainder can be processed again");
            FoundryRemainderSmoke.run(be);

            be.clearContent();be.process();be.setItem(0,new ItemStack(ModItems.GROTH_WARRIOR.get()));be.setItem(1,new ItemStack(ModItems.BLIGHTED_SPIRIT.get(),64));be.setItem(2,new ItemStack(ModItems.PURE_CRYSTAL.get(),64));be.setItem(3,new ItemStack(SlateContent.TABLET.get(),64));for(int n=0;n<400;n++)be.process();
            check(be.getItem(0).is(ModItems.GROTH_WARRIOR.get())&&be.getItem(0).getCount()==1&&be.getItem(3).is(SlateContent.FORGED.get("groth").get())&&be.getItem(3).getCount()==64,"one retained warrior templates 64 products");
            for(var configured:FoundryRecipes.defaults()){
                be.clearContent();be.process();
                be.setItem(0,new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse(configured.top())),configured.topCount()*(configured.consumeTop()?2:1)));
                be.setItem(3,new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse(configured.bottom())),configured.bottomCount()*2));
                be.setItem(1,new ItemStack(ModItems.BLIGHTED_SPIRIT.get(),2));be.setItem(2,new ItemStack(ModItems.PURE_CRYSTAL.get(),2));for(int n=0;n<400;n++)be.process();
                check(be.getItem(3).is(configured.result().getItem())&&be.getItem(3).getCount()==configured.outputCount()*2&&be.getItem(1).isEmpty()&&be.getItem(2).isEmpty()&&(configured.consumeTop()?be.getItem(0).isEmpty():be.getItem(0).getCount()==configured.topCount()),"default recipe batch "+configured.output());
            }
            var recipe=new MemoryRecipe(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("purified_undead","test_memory"),net.minecraft.world.item.crafting.CraftingBookCategory.MISC);
            var dummy=new net.minecraft.world.inventory.AbstractContainerMenu(null,0){public ItemStack quickMoveStack(net.minecraft.world.entity.player.Player q,int i){return ItemStack.EMPTY;}public boolean stillValid(net.minecraft.world.entity.player.Player q){return true;}};
            var craft=new net.minecraft.world.inventory.TransientCraftingContainer(dummy,3,3);craft.setItem(0,new ItemStack(SlateContent.FORGED.get("groth").get()));craft.setItem(8,new ItemStack(SlateContent.CIPHER_TEXT.get()));
            check(recipe.matches(craft,level)&&recipe.assemble(craft,server.registryAccess()).is(SlateContent.MEMORIES.get("groth").get())&&recipe.getRemainingItems(craft).get(0).is(SlateContent.FORGED.get("groth").get())&&recipe.getRemainingItems(craft).get(8).isEmpty(),"memory crafting keeps tablet only");
            var book=new ItemStack(ModItems.LILY_DIARY.get());p.getInventory().selected=0;p.getInventory().setItem(0,book);var menu=new LilyMemoryMenu(1,p.getInventory());
            check(!menu.getSlot(0).mayPlace(new ItemStack(Items.DIRT)),"book refuses non-memory");menu.getSlot(0).set(new ItemStack(SlateContent.MEMORIES.get("groth").get()));check(!menu.getSlot(1).mayPlace(new ItemStack(SlateContent.MEMORIES.get("groth").get())),"book refuses duplicate");check(MemoryStorage.read(book).get(0).is(SlateContent.MEMORIES.get("groth").get()),"book changes saved immediately");
            check(!menu.getSlot(35).mayPickup(p),"main-hand book locked");check(!MemoryEffects.active(p,"groth"),"holding book never activates memories");
            var all=NonNullList.withSize(8,ItemStack.EMPTY);for(int i=0;i<7;i++)all.set(i,new ItemStack(SlateContent.MEMORIES.get(SlateContent.WARRIORS[i]).get()));MemoryStorage.write(book,all);
            dev.purifiedundead.purification.PurificationProgress.unlock(p);var curios=CuriosApi.getCuriosInventory(p).orElseThrow(IllegalStateException::new);var diarySlot=curios.getCurios().get("wanderer_log").getStacks();p.getInventory().setItem(0,ItemStack.EMPTY);diarySlot.setStackInSlot(0,book);
            check(MemoryEffects.active(p,"groth")&&MemoryEffects.active(p,"eleine")&&!MemoryEffects.active(p,"ferin"),"equipped book selects memories");
            for(int i=0;i<36;i++)p.getInventory().setItem(i,new ItemStack(Items.DIRT,64));MemoryRewards.scan(p);check(!p.getPersistentData().getBoolean("purified_undead:ferin_memory_received"),"full inventory keeps gift pending");p.getInventory().setItem(0,ItemStack.EMPTY);
            MemoryRewards.scan(p);MemoryRewards.scan(p);int gifts=0;for(var s:p.getInventory().items)if(s.is(SlateContent.MEMORIES.get("ferin").get()))gifts+=s.getCount();check(gifts==1,"collection gift exactly once");
            double health=p.getMaxHealth();var relicSlot=curios.getCurios().get("white_witch_relic").getStacks();relicSlot.setStackInSlot(0,new ItemStack(SlateContent.GUARDIAN.get()));p.setHealth(p.getMaxHealth());MemoryEffects.tickPlayer(p);check(Math.abs(p.getMaxHealth()-health*2)<.001,"guardian doubles maximum health");dev.purifiedundead.config.PurifiedUndeadConfig.VALUES.whiteWitchRelicEffectScale.set(.5);MemoryEffects.tickPlayer(p);check(Math.abs(p.getMaxHealth()-health*1.5)<.001,"guardian honors global relic scale");dev.purifiedundead.config.PurifiedUndeadConfig.VALUES.whiteWitchRelicEffectScale.set(1.0);relicSlot.setStackInSlot(0,ItemStack.EMPTY);MemoryEffects.tickPlayer(p);check(Math.abs(p.getMaxHealth()-health)<.001,"guardian removal restores attribute");
            p.setHealth(5);MemoryEffects.tickPlayer(p);check(p.hasEffect(MobEffects.DAMAGE_RESISTANCE)&&p.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier()==2&&p.getEffect(MobEffects.DAMAGE_RESISTANCE).getDuration()==1000,"sisters trigger III for 50 seconds");p.removeAllEffects();p.setHealth(20);MemoryEffects.tickPlayer(p);p.setHealth(5);MemoryEffects.tickPlayer(p);check(!p.hasEffect(MobEffects.DAMAGE_RESISTANCE),"sisters cooldown survives recovery");
            p.setSprinting(true);check(Math.abs(MemoryEffects.sprintMultiplier(p)-1.2)<.001,"sprint base bonus excludes vanilla sprint boost");
            p.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.MOVEMENT_SPEED,200,0));check(Math.abs(MemoryEffects.sprintMultiplier(p)-1.3)<.001,"speed I adds two damage steps");p.removeAllEffects();
            p.setSprinting(false);p.setHealth(20);p.removeAllEffects();p.tickCount=100;MemoryEffects.tickPlayer(p);
            check(p.getEffect(MobEffects.NIGHT_VISION).getDuration()==400,"night vision stays at 20 seconds");
            check(Math.abs(p.getAttribute(net.minecraftforge.common.ForgeMod.SWIM_SPEED.get()).getValue()-1.5)<.001,"swimming +50 percent");
            float hp=p.getHealth();p.hurt(p.damageSources().inFire(),4);check(p.getHealth()==hp,"fire damage immunity");
            var crit=net.minecraftforge.registries.ForgeRegistries.ATTRIBUTES.getValue(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("attributeslib","crit_chance"));if(crit!=null)p.getAttribute(crit).setBaseValue(0);
            var victim=net.minecraft.world.entity.EntityType.COW.create(level);var splash=net.minecraft.world.entity.EntityType.COW.create(level);
            victim.setPos(48,120,48);splash.setPos(49,120,48);level.addFreshEntity(victim);level.addFreshEntity(splash);
            try {SlateConfig.statusChance.set(1.0);p.fallDistance=2;p.setOnGround(false);float before=splash.getHealth();victim.hurt(p.damageSources().playerAttack(p),4);
                check(Math.abs(splash.getHealth()-(before-2))<.01,"plunge splash deals half damage actual="+splash.getHealth()+" expected="+(before-2));
                check(victim.hasEffect(MobEffects.WITHER)&&victim.hasEffect(MobEffects.POISON)&&victim.getRemainingFireTicks()==100&&victim.getPersistentData().getLong("purified_undead:memory_freeze_until")==level.getGameTime()+100,"four independent ailments use 5 seconds");
                check(victim.getEffect(MobEffects.WITHER).getAmplifier()==0,"ailment level I");
                MemoryEffects.groupStun(p,victim,40);check(splash.hasEffect(dev.purifiedundead.content.ModEffects.STUNNED.get()),"plunge group stun");
                var lootEvent=new net.minecraftforge.event.entity.living.LootingLevelEvent(victim,p.damageSources().playerAttack(p),3);net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(lootEvent);check(lootEvent.getLootingLevel()==5,"Faden adds two looting levels");
            }finally{SlateConfig.statusChance.set(.25);victim.discard();splash.discard();p.fallDistance=0;}
            var dying=net.minecraft.world.entity.EntityType.COW.create(level);var nearby=net.minecraft.world.entity.EntityType.COW.create(level);dying.setPos(48,120,48);nearby.setPos(49,120,48);level.addFreshEntity(dying);level.addFreshEntity(nearby);dying.setHealth(1);p.fallDistance=2;
            try{dying.hurt(p.damageSources().playerAttack(p),4);check(!dying.isAlive()&&Math.abs(nearby.getHealth()-8)<.01,"lethal plunge still splashes");}finally{dying.discard();nearby.discard();p.fallDistance=0;}
            var wall=new BlockPos(43,120,43);level.setBlockAndUpdate(wall,Blocks.STONE.defaultBlockState());p.setPos(42.69,120,43.5);p.setOnGround(false);p.setDeltaMovement(0,-1,0);WallGrip.input(p,true);WallGrip.tick(p);
            check(p.getDeltaMovement().y>-.5&&p.getAttribute(net.minecraftforge.common.ForgeMod.ENTITY_GRAVITY.get()).getValue()==0,"wall grip arrests fall and gravity");WallGrip.input(p,false);WallGrip.tick(p);check(p.getAttribute(net.minecraftforge.common.ForgeMod.ENTITY_GRAVITY.get()).getValue()>0,"release restores gravity");level.setBlockAndUpdate(wall,Blocks.AIR.defaultBlockState());
            var modifier=new dev.purifiedundead.loot.CipherLootModifier(new net.minecraft.world.level.storage.loot.predicates.LootItemCondition[0]);
            try {SlateConfig.chestChance.set(1.0);for(var dim:new net.minecraft.resources.ResourceKey[]{net.minecraft.world.level.Level.OVERWORLD,net.minecraft.world.level.Level.NETHER,net.minecraft.world.level.Level.END}){
                var lootLevel=server.getLevel(dim);var params=new net.minecraft.world.level.storage.loot.LootParams.Builder(lootLevel).withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN,p.position()).withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY,p).create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.CHEST);
                var context=new net.minecraft.world.level.storage.loot.LootContext.Builder(params).withQueriedLootTableId(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("other_structure","chests/test")).create(null);
                var drops=modifier.apply(new it.unimi.dsi.fastutil.objects.ObjectArrayList<>(),context);
                check(dim==net.minecraft.world.level.Level.END?drops.isEmpty():drops.size()==1&&drops.get(0).getCount()>=1&&drops.get(0).getCount()<=5,"modded chest dimensions/count including player opener");
            }}finally{SlateConfig.chestChance.set(.25);}
            var cloned=new net.minecraft.nbt.CompoundTag();MemoryRewards.copy(p.getPersistentData(),cloned);check(cloned.getBoolean("purified_undead:ferin_memory_received"),"reward clone flag");
            diarySlot.setStackInSlot(0,ItemStack.EMPTY);MemoryEffects.tickPlayer(p);check(!MemoryEffects.active(p,"groth"),"unequip isolates memory effects");
            System.out.println("SLATE_SERVER_OK: batch capacity/remainders/reload, retained template, recipe remainder, locked diary, duplicate rejection, equipped effects, one-time gift, health, cooldown, sprint/speed, splash, ailments, fire immunity, night vision, swim, looting, wall grip and chest dimensions");
        }finally{level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());}
    }
}
