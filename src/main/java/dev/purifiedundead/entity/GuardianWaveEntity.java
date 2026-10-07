package dev.purifiedundead.entity;

import dev.purifiedundead.combat.GuardianWaveCombat;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import net.minecraft.network.syncher.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.*;
import java.util.*;

/** A short-lived, bounded melee wave. Not saved, not a projectile, and never loads chunks. */
public final class GuardianWaveEntity extends Entity {
    public static final double RANGE = 7D, SPEED = 1D;
    public static final int LIFETIME = 7;
    private static final EntityDataAccessor<Integer> OWNER = SynchedEntityData.defineId(GuardianWaveEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SHOT = SynchedEntityData.defineId(GuardianWaveEntity.class,EntityDataSerializers.INT);
    private boolean predicted;
    private float projectileMultiplier = 1F;
    private static final EntityDataAccessor<Float> ROLL=SynchedEntityData.defineId(GuardianWaveEntity.class,EntityDataSerializers.FLOAT);
    private final Set<UUID> hit=new HashSet<>();
    private GuardianWaveCombat.State attack;
    private ItemStack weapon=ItemStack.EMPTY;
    private Vec3 origin=Vec3.ZERO,direction=Vec3.ZERO;
    public GuardianWaveEntity(EntityType<? extends GuardianWaveEntity> type,Level level){super(type,level);noPhysics=true;}
    @Override protected void defineSynchedData(){entityData.define(ROLL,0F);entityData.define(OWNER,-1);entityData.define(SHOT,0);}
    public float roll(){return entityData.get(ROLL);}
    public boolean predicted(){return predicted;}
    public int ownerId(){return entityData.get(OWNER);}
    public int shotId(){return entityData.get(SHOT);}
    public void launch(GuardianWaveCombat.State state,ItemStack sword){launch(state,sword,0);}
    public void launch(GuardianWaveCombat.State state,ItemStack sword,int shot){
        attack=state;weapon=sword;
        projectileMultiplier=dev.purifiedundead.compat.GuardianProjectileBonus.multiplier(state.owner());
        initialize(state.owner(),shot);
    }
    public void predict(net.minecraft.world.entity.player.Player player,int shot){
        if(!level().isClientSide)throw new IllegalStateException("Prediction is visual-only");
        predicted=true;initialize(player,shot);
    }
    private void initialize(net.minecraft.world.entity.player.Player player,int shot){
        origin=player.getEyePosition().add(0,-0.35,0);direction=player.getLookAngle();
        setPos(origin);xo=xOld=origin.x;yo=yOld=origin.y;zo=zOld=origin.z;
        setYRot(player.getYRot());setXRot(player.getXRot());yRotO=getYRot();xRotO=getXRot();
        entityData.set(OWNER,player.getId());entityData.set(SHOT,shot);
        // Shared deterministic cosmetic roll; the client does not choose any combat values.
        entityData.set(ROLL,(float)Math.floorMod(shot*1103515245+player.getId(),18000)/100F);
        setDeltaMovement(direction.scale(SPEED));
    }
    @Override public void tick(){
        if(isRemoved())return;
        super.tick();
        if(level().isClientSide){
            if(predicted){
                var owner=level().getEntity(ownerId());
                if(owner==null||!owner.isAlive()||owner.isSpectator()||tickCount>LIFETIME){discard();return;}
                Vec3 to=origin.add(direction.scale(Math.min(RANGE,tickCount*SPEED)));
                if(!loaded(position(),to)){discard();return;}
                var wall=level().clip(new ClipContext(position(),to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));
                boolean blocked=wall.getType()!=HitResult.Type.MISS;
                setPos(blocked?wall.getLocation():to);
                if(blocked||tickCount>=LIFETIME)discard();
            }else if(tickCount>40)discard(); // Bound stale visuals even if a disconnect loses removal.
            return;
        }
        if(attack==null||tickCount>LIFETIME||!attack.owner().isAlive()||attack.owner().isSpectator()||attack.owner().level()!=level()){discard();return;}
        Vec3 from=position(),to=origin.add(direction.scale(Math.min(RANGE,tickCount*SPEED)));
        if(!loaded(from,to)){discard();return;}
        var wall=level().clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));
        boolean blocked=wall.getType()!=HitResult.Type.MISS;if(blocked)to=wall.getLocation();
        var query=new AABB(from,to).inflate(1.3);
        for(var target:level().getEntitiesOfClass(LivingEntity.class,query,t->t.isAlive()&&t.isAttackable()&&dev.purifiedundead.slate.MemoryEffects.legal(attack.owner(),t))){
            if(hit.contains(target.getUUID()))continue;

            Vec3 nearest=new Vec3(net.minecraft.util.Mth.clamp(to.x,target.getBoundingBox().minX,target.getBoundingBox().maxX),net.minecraft.util.Mth.clamp(to.y,target.getBoundingBox().minY,target.getBoundingBox().maxY),net.minecraft.util.Mth.clamp(to.z,target.getBoundingBox().minZ,target.getBoundingBox().maxZ));
            if(nearest.distanceToSqr(origin)>RANGE*RANGE+0.0001||nearest.subtract(origin).dot(direction)<0)continue;
            if(target.getBoundingBox().inflate(1.0).clip(from,to).isEmpty()&&!target.getBoundingBox().inflate(1.0).contains(from))continue;
            if(level().clip(new ClipContext(origin,nearest,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this)).getType()!=HitResult.Type.MISS)continue;
            hit.add(target.getUUID());damage(target);
        }
        setPos(to);if(blocked||tickCount>=LIFETIME)discard();
    }
    private boolean loaded(Vec3 from,Vec3 to){
        if(!Double.isFinite(to.x)||!Double.isFinite(to.y)||!Double.isFinite(to.z))return false;
        return level().hasChunksAt(net.minecraft.core.BlockPos.containing(Math.min(from.x,to.x)-1.3,Math.min(from.y,to.y),Math.min(from.z,to.z)-1.3),net.minecraft.core.BlockPos.containing(Math.max(from.x,to.x)+1.3,Math.max(from.y,to.y),Math.max(from.z,to.z)+1.3));
    }
    private void damage(LivingEntity target){
        var p=attack.owner();float enchant=net.minecraft.world.item.enchantment.EnchantmentHelper.getDamageBonus(weapon,target.getMobType())*attack.strength();
        float amount=(attack.baseDamage()+enchant)*1.2F*attack.critical()*projectileMultiplier;
        if(dev.purifiedundead.slate.MemoryEffects.active(p,"ferin"))amount*=1+dev.purifiedundead.slate.SlateConfig.get(dev.purifiedundead.slate.SlateConfig.ferinBonus);
        if(!Float.isFinite(amount)||amount<=0)return;
        final float damage=amount;int previous=target.invulnerableTime;
        try{target.invulnerableTime=0;GuardianWaveCombat.with(attack,()->{if(target.hurt(p.damageSources().playerAttack(p),damage)){p.setLastHurtMob(target);int knockback=weapon.getEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.KNOCKBACK)+(attack.sprinting()&&attack.strength()>0.9F?1:0);if(knockback>0)target.knockback(knockback*.5,Math.sin(Math.toRadians(getYRot())),-Math.cos(Math.toRadians(getYRot())));net.minecraft.world.item.enchantment.EnchantmentHelper.doPostHurtEffects(target,p);net.minecraft.world.item.enchantment.EnchantmentHelper.doPostDamageEffects(p,target);int fire=weapon.getEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.FIRE_ASPECT);if(fire>0)target.setSecondsOnFire(fire*4);}});}
        finally{target.invulnerableTime=Math.max(previous,target.invulnerableTime);}
    }
    @Override public boolean isPickable(){return false;}
    @Override public boolean isAttackable(){return false;}
    @Override public boolean shouldBeSaved(){return false;}
    @Override protected void readAdditionalSaveData(CompoundTag tag){}
    @Override protected void addAdditionalSaveData(CompoundTag tag){}
    @Override public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getAddEntityPacket(){return net.minecraftforge.network.NetworkHooks.getEntitySpawningPacket(this);}
}
