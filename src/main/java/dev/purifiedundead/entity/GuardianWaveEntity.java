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
    private static final EntityDataAccessor<Float> ROLL=SynchedEntityData.defineId(GuardianWaveEntity.class,EntityDataSerializers.FLOAT);
    private final Set<UUID> hit=new HashSet<>();
    private GuardianWaveCombat.State attack;
    private ItemStack weapon=ItemStack.EMPTY;
    private Vec3 origin=Vec3.ZERO,direction=Vec3.ZERO;
    public GuardianWaveEntity(EntityType<? extends GuardianWaveEntity> type,Level level){super(type,level);noPhysics=true;}
    @Override protected void defineSynchedData(){entityData.define(ROLL,0F);}
    public float roll(){return entityData.get(ROLL);}
    public void launch(GuardianWaveCombat.State state,ItemStack sword){
        attack=state;weapon=sword;var p=state.owner();origin=p.getEyePosition().add(0,-0.35,0);direction=p.getLookAngle();
        setPos(origin);setYRot(p.getYRot());setXRot(p.getXRot());yRotO=getYRot();xRotO=getXRot();entityData.set(ROLL,p.getRandom().nextFloat()*180);
    }
    @Override public void tick(){
        super.tick();
        if(level().isClientSide)return;
        if(attack==null||tickCount>6||!attack.owner().isAlive()||attack.owner().isSpectator()||attack.owner().level()!=level()){discard();return;}
        Vec3 from=position(),to=origin.add(direction.scale(tickCount*0.5));
        var wall=level().clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));
        boolean blocked=wall.getType()!=HitResult.Type.MISS;if(blocked)to=wall.getLocation();
        var query=new AABB(from,to).inflate(1.1);
        for(var target:level().getEntitiesOfClass(LivingEntity.class,query,t->t.isAlive()&&t.isAttackable()&&dev.purifiedundead.slate.MemoryEffects.legal(attack.owner(),t))){
            if(hit.contains(target.getUUID()))continue;

            Vec3 nearest=new Vec3(net.minecraft.util.Mth.clamp(to.x,target.getBoundingBox().minX,target.getBoundingBox().maxX),net.minecraft.util.Mth.clamp(to.y,target.getBoundingBox().minY,target.getBoundingBox().maxY),net.minecraft.util.Mth.clamp(to.z,target.getBoundingBox().minZ,target.getBoundingBox().maxZ));
            if(nearest.distanceToSqr(origin)>9.0001||nearest.subtract(origin).dot(direction)<0)continue;
            if(target.getBoundingBox().inflate(0.8).clip(from,to).isEmpty()&&!target.getBoundingBox().inflate(0.8).contains(from))continue;
            if(level().clip(new ClipContext(origin,nearest,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this)).getType()!=HitResult.Type.MISS)continue;
            hit.add(target.getUUID());damage(target);
        }
        setPos(to);if(blocked||tickCount==6)discard();
    }
    private void damage(LivingEntity target){
        var p=attack.owner();float enchant=net.minecraft.world.item.enchantment.EnchantmentHelper.getDamageBonus(weapon,target.getMobType())*attack.strength();
        float amount=(attack.baseDamage()+enchant)*1.2F*attack.critical();
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
