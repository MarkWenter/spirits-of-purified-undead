package dev.purifiedundead.slate;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;

/** Client sends only held/released intent. Geometry and equipment are checked on the server every tick. */
public final class WallGrip {
    private static final String LAST="purified_undead:wall_grip_last", JUMP="purified_undead:wall_jump_at";
    private static final String UNTIL="purified_undead:wall_grip_input";
    public static void input(ServerPlayer p,boolean held){p.getPersistentData().putLong(UNTIL,held?p.level().getGameTime()+8:0);}
    public static boolean nearWall(net.minecraft.world.entity.player.Player p){var box=p.getBoundingBox().deflate(.01).inflate(.05,0,.05);return p.level().getBlockCollisions(p,box).iterator().hasNext();}
    private static final java.util.UUID GRAVITY=java.util.UUID.fromString("ad3f6b02-567a-4efc-8ce1-78abf0a459cb");
    private static void gravity(ServerPlayer p,boolean grip){var a=p.getAttribute(net.minecraftforge.common.ForgeMod.ENTITY_GRAVITY.get());if(a==null)return;if(grip&&a.getModifier(GRAVITY)==null)a.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(GRAVITY,"Wall grip",-1,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_TOTAL));else if(!grip)a.removeModifier(GRAVITY);}
    public static boolean eligible(net.minecraft.world.entity.player.Player p) {
        return p.isAlive()&&!p.isSpectator()&&!p.onGround()&&!p.isPassenger()&&!p.getAbilities().flying&&!p.isFallFlying()&&!p.isInWaterOrBubble()&&!p.isInLava()&&MemoryEffects.active(p,"ulv")&&nearWall(p);
    }
    public static boolean jump(ServerPlayer p){
        long now=p.level().getGameTime();var data=p.getPersistentData();
        if(!eligible(p)||!data.contains(LAST)||now-data.getLong(LAST)>8||now-data.getLong(LAST)<0
                ||(data.contains(JUMP)&&now-data.getLong(JUMP)<3))return false;
        data.putLong(JUMP,now);data.remove(LAST);gravity(p,false);
        var v=p.getDeltaMovement();p.setDeltaMovement(v.x,.52,v.z);p.fallDistance=0;p.hasImpulse=true;
        return true; // Owner already predicted the impulse; do not echo and replay it.
    }
    public static void tick(ServerPlayer p){
        if(p.getPersistentData().getLong(UNTIL)<p.level().getGameTime()||!MemoryEffects.active(p,"ulv")||p.onGround()||p.isPassenger()||p.getAbilities().flying||p.isFallFlying()||p.isInWaterOrBubble()||p.isInLava()||!nearWall(p)){gravity(p,false);return;}
        var v=p.getDeltaMovement();if(v.y>0){gravity(p,false);return;}
        gravity(p,true);p.getPersistentData().putLong(LAST,p.level().getGameTime());
        double y=Math.abs(v.y)<.08?0:v.y*.45;
        p.setDeltaMovement(v.x*.4,y,v.z*.4);p.fallDistance=0;p.hasImpulse=true;
        p.connection.send(new ClientboundSetEntityMotionPacket(p));
    }
}
