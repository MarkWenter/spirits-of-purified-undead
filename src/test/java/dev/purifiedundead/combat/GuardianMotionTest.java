package dev.purifiedundead.combat;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class GuardianMotionTest {
 @Test void jumpingReplacesDescentAndPreservesHorizontalMomentum() {
  var motion=GuardianMotion.jump(new Vec3(0.3,-1.4,-0.2),0.52);
  assertEquals(0.52,motion.y); assertEquals(0.3,motion.x); assertEquals(-0.2,motion.z);
 }
 @Test void dashHasConstantHorizontalSpeedAndDoesNotCancelAscent() {
  for(int yaw=0;yaw<360;yaw+=15) {
   var motion=GuardianMotion.dash(new Vec3(0,0.4,0),yaw,1.15);
   assertEquals(1.15,Math.sqrt(motion.x*motion.x+motion.z*motion.z),1e-9);
   assertEquals(0.4,motion.y);
  }
  assertEquals(0.08,GuardianMotion.dash(new Vec3(0,-2,0),0,1.15).y);
 }
 @Test void queuedActionsExpireAndAreConsumedOnlyOnce() {
  var state=new GuardianMovementService.AirState();
  state.queueForTakeoff(dev.purifiedundead.network.GuardianActionPacket.Action.DOUBLE_JUMP,10);
  assertEquals(1,state.takePending(12).size());
  assertTrue(state.takePending(12).isEmpty());
  state.queueForTakeoff(dev.purifiedundead.network.GuardianActionPacket.Action.AIR_DASH,20);
  assertTrue(state.takePending(26).isEmpty());
 }
}
