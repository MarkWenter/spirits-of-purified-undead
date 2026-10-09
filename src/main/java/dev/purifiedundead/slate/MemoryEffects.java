package dev.purifiedundead.slate;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.effect.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import top.theillusivec4.curios.api.CuriosApi;
import dev.purifiedundead.content.ModItems;
import dev.purifiedundead.config.PurifiedUndeadConfig;
import java.util.UUID;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid="purified_undead")
public final class MemoryEffects {
    private static final UUID HEALTH=UUID.fromString("916c478f-5d45-431d-9339-43ce380d2b60"),SWIM=UUID.fromString("916c478f-5d45-431d-9339-43ce380d2b61");
    private static final String COOLDOWN="purified_undead:memory_sisters_until",LOW="purified_undead:memory_sisters_low",FREEZE="purified_undead:memory_freeze_until";
    private static boolean splashing;
    public static boolean active(Player p,String key){return MemoryStorage.active(p,key);}
    public static boolean legal(Player p,LivingEntity target){return target!=p&&(!target.isAlive()||target.isAttackable())&&!p.isAlliedTo(target)&&(!(target instanceof Player other)||p.canHarmPlayer(other))&&(!(target instanceof OwnableEntity pet)||!p.getUUID().equals(pet.getOwnerUUID()));}
    @SubscribeEvent public static void start(net.minecraftforge.event.server.ServerAboutToStartEvent e){SlateConfig.migrate();dev.purifiedundead.foundry.FoundryRecipes.load();}
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent e){if(e.phase!=TickEvent.Phase.END||!(e.player instanceof ServerPlayer p))return;tickPlayer(p);}
    public static void tickPlayer(ServerPlayer p){
        boolean living=p.isAlive()&&!p.isSpectator();
        boolean relic=living&&PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.whiteWitchRelicsEnabled)&&CuriosApi.getCuriosInventory(p).map(h->{var slot=h.getCurios().get("white_witch_relic");if(slot==null)return false;for(int i=0;i<slot.getStacks().getSlots();i++)if(slot.getStacks().getStackInSlot(i).is(SlateContent.GUARDIAN.get()))return true;return false;}).orElse(false);
        attribute(p.getAttribute(Attributes.MAX_HEALTH),HEALTH,relic?SlateConfig.get(SlateConfig.healthBonus)*PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.whiteWitchRelicEffectScale):0);
        if(p.getHealth()>p.getMaxHealth())p.setHealth(p.getMaxHealth());
        attribute(p.getAttribute(net.minecraftforge.common.ForgeMod.SWIM_SPEED.get()),SWIM,active(p,"eleine")?SlateConfig.get(SlateConfig.swimBonus):0);
        WallGrip.tick(p);
        if(!living)return;
        if(p.tickCount%100==0&&active(p,"ulv")) {
            var food=p.getFoodData(); food.setFoodLevel(Math.min(20,food.getFoodLevel()+4));
            food.setSaturation(Math.min(food.getFoodLevel(),food.getSaturationLevel()+4));
        }
        if(p.tickCount%100==0&&active(p,"faden"))p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,400,0,true,false,true));
        boolean low=p.getHealth()<p.getMaxHealth()*.3;
        if(active(p,"guardians")&&low&&!p.getPersistentData().getBoolean(LOW)&&p.server.overworld().getGameTime()>=p.getPersistentData().getLong(COOLDOWN)){
            p.getPersistentData().putLong(COOLDOWN,p.server.overworld().getGameTime()+SlateConfig.get(SlateConfig.sistersCooldown));
            for(var effect:new MobEffect[]{MobEffects.DAMAGE_RESISTANCE,MobEffects.DAMAGE_BOOST,MobEffects.REGENERATION})p.addEffect(new MobEffectInstance(effect,SlateConfig.get(SlateConfig.sistersTicks),2));
        }
        p.getPersistentData().putBoolean(LOW,low);
        MemoryRewards.scan(p);
    }
    private static void attribute(AttributeInstance a,UUID id,double amount){if(a==null)return;var old=a.getModifier(id);if(old!=null&&old.getAmount()==amount)return;if(old!=null)a.removeModifier(id);if(amount!=0)a.addTransientModifier(new AttributeModifier(id,"Slate memory",amount,AttributeModifier.Operation.MULTIPLY_TOTAL));}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void fire(LivingAttackEvent e){if(e.getEntity() instanceof ServerPlayer p&&((active(p,"hoenir")&&e.getSource().is(DamageTypeTags.IS_FIRE))||(active(p,"guardians")&&e.getSource().is(DamageTypeTags.IS_FALL))))e.setCanceled(true);}
    @SubscribeEvent public static void fall(LivingFallEvent e) {
        if(e.getEntity() instanceof Player p && active(p,"guardians")) e.setCanceled(true);
    }
    @SubscribeEvent public static void jump(LivingEvent.LivingJumpEvent e) {
        if(e.getEntity() instanceof Player p && active(p,"guardians")) {
            var v=p.getDeltaMovement();
            var a=p.getAttribute(net.minecraftforge.common.ForgeMod.ENTITY_GRAVITY.get());
            double g=a==null?.08:a.getValue();
            p.setDeltaMovement(v.x,MemoryJump.raise(v.y,g,.5),v.z);
        }
    }
    @SubscribeEvent(priority=EventPriority.NORMAL) public static void outgoing(LivingHurtEvent e){
        if(!(e.getSource().getEntity() instanceof ServerPlayer p)||e.getAmount()<=0||!active(p,"julius")||!dev.purifiedundead.combat.GuardianWaveCombat.sprinting(p))return;
        // Auxiliary attacks already inherit the boosted triggering hit; do not boost them a second time.
        if(e.getSource().is(DamageTypes.PLAYER_ATTACK)||e.getSource().is(DamageTypeTags.IS_PROJECTILE))e.setAmount(e.getAmount()*sprintMultiplier(p));
    }
    public static float sprintMultiplier(Player p){var speed=p.getAttribute(Attributes.MOVEMENT_SPEED);double ratio=speed==null||speed.getBaseValue()<=0?1:speed.getValue()/speed.getBaseValue();
        // Vanilla sprint's +30% is the condition, not an extra external speed bonus.
        if(dev.purifiedundead.combat.GuardianWaveCombat.sprinting(p))ratio/=1.3;
        if(!Double.isFinite(ratio))ratio=1;
        double steps=Math.floor(Math.max(0,ratio-1)*10+1e-6);return (float)(1+SlateConfig.get(SlateConfig.sprintDamage)+steps*SlateConfig.get(SlateConfig.speedStepDamage));}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void melee(LivingDamageEvent e){
        if(splashing||!(e.getSource().getEntity() instanceof ServerPlayer p)||e.getSource().getDirectEntity()!=p||!e.getSource().is(DamageTypes.PLAYER_ATTACK)||e.getAmount()<=0)return;
        var target=e.getEntity();if(!legal(p,target))return;
        if(active(p,"groth")&&dev.purifiedundead.combat.GuardianWaveCombat.falling(p)){
            try{ splashing=true;for(var other:p.serverLevel().getEntitiesOfClass(LivingEntity.class,target.getBoundingBox().inflate(1.5),t->t.isAlive()&&legal(p,t)&&t!=target&&t.distanceToSqr(target)<=2.25))other.hurt(p.damageSources().indirectMagic(null,p),(float)(e.getAmount()*SlateConfig.get(SlateConfig.grothDamage)));}finally{splashing=false;}
        }
        if(active(p,"hoenir")){
            int ticks=SlateConfig.get(SlateConfig.statusTicks);double chance=SlateConfig.get(SlateConfig.statusChance);
            if(p.getRandom().nextDouble()<chance)target.addEffect(new MobEffectInstance(MobEffects.WITHER,ticks,0));
            if(p.getRandom().nextDouble()<chance)target.addEffect(new MobEffectInstance(MobEffects.POISON,ticks,0));
            if(p.getRandom().nextDouble()<chance)target.setSecondsOnFire((ticks+19)/20);
            if(p.getRandom().nextDouble()<chance)target.getPersistentData().putLong(FREEZE,target.level().getGameTime()+ticks);
        }
    }
    @SubscribeEvent public static void freeze(LivingEvent.LivingTickEvent e){var t=e.getEntity();if(t.level().isClientSide)return;long until=t.getPersistentData().getLong(FREEZE);if(until==0)return;if(until>t.level().getGameTime()&&t.canFreeze())t.setTicksFrozen(t.getTicksRequiredToFreeze()+20);else {t.getPersistentData().remove(FREEZE);t.setTicksFrozen(0);}}
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p){var packet=new dev.purifiedundead.network.FoundryRecipesPacket(dev.purifiedundead.foundry.FoundryRecipes.all());dev.purifiedundead.network.ModNetwork.CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(()->p),packet);}}
    @SubscribeEvent public static void crafted(PlayerEvent.ItemCraftedEvent e){if(e.getEntity() instanceof ServerPlayer p)MemoryRewards.record(p,e.getCrafting());}
    @SubscribeEvent public static void picked(PlayerEvent.ItemPickupEvent e){if(e.getEntity() instanceof ServerPlayer p)MemoryRewards.record(p,e.getStack());}
    @SubscribeEvent public static void clone(PlayerEvent.Clone e){var a=e.getOriginal().getPersistentData();var b=e.getEntity().getPersistentData();b.putLong(COOLDOWN,a.getLong(COOLDOWN));MemoryRewards.copy(a,b);}
    public static void groupStun(ServerPlayer p,LivingEntity target,int ticks){if(!active(p,"groth"))return;for(var other:p.serverLevel().getEntitiesOfClass(LivingEntity.class,target.getBoundingBox().inflate(1.5),t->t.isAlive()&&legal(p,t)&&t!=target&&t.distanceToSqr(target)<=2.25))other.addEffect(new MobEffectInstance(dev.purifiedundead.content.ModEffects.STUNNED.get(),ticks,0));}
}
