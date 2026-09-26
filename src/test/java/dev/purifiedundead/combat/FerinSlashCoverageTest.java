package dev.purifiedundead.combat;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class FerinSlashCoverageTest {
 private boolean hit(int stage,AABB box,int start,int end) {
  var state=new FerinPlayerState();state.captureStage(Vec3.ZERO,0,10,1);
  for(int tick=start;tick<=end;tick++) for(var s:FerinBladeTrajectory.sweeps(stage,tick))
   if(FerinSweptBlade.intersects(state.toWorld(s.from()),state.toWorld(s.to()),box)) return true;
  return false;
 }
 private AABB box(double x,double y,double z){return new AABB(x-.05,y-.05,z-.05,x+.05,y+.05,z+.05);}
 @Test void wideSweepHitsBeyondOldSwordAndHasRealOuterMisses() {
  for(int s=1;s<=5;s++) {
   double reach=FerinBladeTrajectory.reach(s);
   assertTrue(hit(s,box(0,1.05,reach-.2),1,FerinBladeTrajectory.sweepEnd(s)),"stage "+s);
   assertFalse(hit(s,box(0,1.05,reach+FerinBladeTrajectory.travelDistance(s)+.7),1,10));
  }
 }
 @Test void launchedWaveDamagesNewTargetsAtRequestedDistance() {
  for(int s=1;s<=5;s++) {
   var target=box(0,1.05,FerinBladeTrajectory.reach(s)+FerinBladeTrajectory.travelDistance(s)-.2);
   assertFalse(hit(s,target,1,FerinBladeTrajectory.sweepEnd(s)));
   assertTrue(hit(s,target,FerinBladeTrajectory.sweepEnd(s)+1,FerinBladeTrajectory.activeEnd(s)),"stage "+s);
  }
 }
 @Test void ringHitsBehindPlayerButFrontalDiagonalsDoNot() {
  assertTrue(hit(4,box(0,1.05,-2.8),1,5));assertTrue(hit(5,box(0,1.05,-5.8),1,5));
  assertFalse(hit(1,box(0,1.05,-2),1,6));assertFalse(hit(2,box(0,1.05,-2),1,6));
 }
 @Test void curvedSubstepsDoNotSkipTinyTargetsAtFastestPartOfRing() {
  for(int s=4;s<=5;s++) for(int i=0;i<=90;i++) {
   var p=FerinBladeTrajectory.blade(s,i/90.0,0).tip();
   assertTrue(hit(s,box(-p.right(),p.up(),p.forward()),1,FerinBladeTrajectory.sweepEnd(s)),"stage "+s+" sample "+i);
  }
 }
 @Test void worldRotationUsesPlayerOriginWithoutOldCompanionOffset() {
  var state=new FerinPlayerState();var origin=new Vec3(10,64,20);
  for(float yaw:new float[]{0,90,180,270}) {
   state.captureStage(origin,yaw,10,1);assertEquals(origin,state.companionAnchor());
   var p=FerinBladeTrajectory.blade(1,.5,2);var w=state.toWorld(p);
   assertEquals(7,w.tip().subtract(origin).dot(state.forward),1e-6);
   assertEquals(0,w.tip().subtract(origin).dot(state.right),1e-6);
  }
 }
 @Test void firstSlashMovesFromCameraLeftToCameraRightAtEveryYaw() {
  var state=new FerinPlayerState();
  for(float yaw:new float[]{0,90,180,270}) {
   state.captureStage(Vec3.ZERO,yaw,10,1);
   double rad=Math.toRadians(yaw);
   Vec3 cameraRight=new Vec3(-Math.cos(rad),0,-Math.sin(rad));
   var begin=state.toWorld(FerinBladeTrajectory.blade(1,0,0)).tip();
   var end=state.toWorld(FerinBladeTrajectory.blade(1,1,0)).tip();
   assertTrue(begin.dot(cameraRight)<0 && end.dot(cameraRight)>0);
   assertTrue(begin.y>end.y);
  }
 }
 @Test void gapBetweenPlayerAndLaunchedWaveStillDamagesAtFinalTravelTick() {
  for(int stage=1;stage<=5;stage++) {
   int tick=FerinBladeTrajectory.activeEnd(stage);
   assertTrue(hit(stage,box(0,1.05,.5),tick,tick),"near player stage "+stage);
   assertTrue(hit(stage,box(0,1.05,FerinBladeTrajectory.travelDistance(stage)/2),tick,tick),"gap stage "+stage);
  }
 }
 @Test void secondSlashMovesFromCameraRightDownToLeftUpAtEveryYaw() {
  var state=new FerinPlayerState();
  for(float yaw:new float[]{0,90,180,270}) {
   state.captureStage(Vec3.ZERO,yaw,10,1);
   double rad=Math.toRadians(yaw);Vec3 cameraRight=new Vec3(-Math.cos(rad),0,-Math.sin(rad));
   var begin=state.toWorld(FerinBladeTrajectory.blade(2,0,0)).tip();
   var middle=state.toWorld(FerinBladeTrajectory.blade(2,.5,0)).tip();
   var end=state.toWorld(FerinBladeTrajectory.blade(2,1,0)).tip();
   assertTrue(begin.dot(cameraRight)>middle.dot(cameraRight));
   assertTrue(middle.dot(cameraRight)>end.dot(cameraRight));
   assertTrue(begin.y<middle.y && middle.y<end.y);
  }
 }
}
