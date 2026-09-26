package dev.purifiedundead.entity;
import org.junit.jupiter.api.Test;
import dev.purifiedundead.combat.FerinBladeTrajectory;
import static org.junit.jupiter.api.Assertions.*;
class FerinSlashMeshTest {
 @Test void idleAndExpiredSlashesAreInvisible() {
  assertTrue(FerinSlashMesh.sample(0,4).isEmpty());assertTrue(FerinSlashMesh.sample(6,4).isEmpty());
  assertTrue(FerinSlashMesh.sample(1,Double.NaN).isEmpty());
  for(int s=1;s<=5;s++) {
   assertTrue(FerinSlashMesh.sample(s,1).isEmpty());
   assertTrue(FerinSlashMesh.sample(s,FerinBladeTrajectory.activeEnd(s)+1).isEmpty());
   assertFalse(FerinSlashMesh.sample(s,2).isEmpty());
  }
 }
 @Test void geometryRemainsFiniteDuringFastSweepAndTravel() {
  for(int s=1;s<=5;s++) for(double t=1.01;t<10;t+=0.125) for(var q:FerinSlashMesh.sample(s,t))
   for(var v:new FerinSlashMesh.Vertex[]{q.a(),q.b(),q.c(),q.d()}) {
    assertTrue(Double.isFinite(v.right())&&Double.isFinite(v.up())&&Double.isFinite(v.forward()));
    assertTrue(v.alpha()>=0&&v.alpha()<=255);
    assertTrue(Math.abs(v.right())<8&&Math.abs(v.up())<9&&Math.abs(v.forward())<12);
   }
 }
 @Test void travellingVisibleBladeUsesTheSameTranslationAsDamage() {
  for(int s=1;s<=5;s++) {
   var early=FerinSlashMesh.sample(s,FerinBladeTrajectory.sweepEnd(s)).get(25).a();
   var late=FerinSlashMesh.sample(s,FerinBladeTrajectory.activeEnd(s)).get(25).a();
   assertEquals(early.right(),late.right(),1e-9);assertEquals(early.up(),late.up(),1e-9);
   assertEquals(FerinBladeTrajectory.travelDistance(s),late.forward()-early.forward(),1e-9);
  }
 }
 @Test void releasedFirstAndSecondSlashesKeepOppositeVisibleHeads() {
  double[] x=new double[2],y=new double[2];
  for(int stage=1;stage<=2;stage++) {
   double weight=0;
   for(var q:FerinSlashMesh.sample(stage,5))for(var v:new FerinSlashMesh.Vertex[]{q.a(),q.b(),q.c(),q.d()}) {
    double w=v.alpha()*v.alpha();weight+=w;x[stage-1]+=v.right()*w;y[stage-1]+=v.up()*w;
   }
   x[stage-1]/=weight;y[stage-1]/=weight;
  }
  assertTrue(x[0]>0 && x[1]<0,"Visible head must preserve sweep direction after release");
  assertTrue(y[0]<y[1],"Return slash must emphasize the upper-left head");
 }
 @Test void actualRibbonFaceTiltsEightDegreesWithoutMovingCuttingReach() {
  for(int stage=1;stage<=5;stage++) {
   double p=1.0/(stage>=4?144:80);
   var v=FerinSlashMesh.sample(stage,FerinBladeTrajectory.sweepEnd(stage)).get(0).b();
   var pose=FerinBladeTrajectory.blade(stage,p,0);
   var t=pose.tip();var r=pose.root();
   var d=new net.minecraft.world.phys.Vec3(t.right()-r.right(),t.up()-r.up(),t.forward()-r.forward()).normalize();
   var delta=new net.minecraft.world.phys.Vec3(v.right()-t.right(),v.up()-t.up(),v.forward()-t.forward());
   double inward=-delta.dot(d);
   double normal=Math.sqrt(Math.max(0,delta.lengthSqr()-inward*inward));
   assertEquals(8,Math.toDegrees(Math.atan2(normal,inward)),0.01);
  }
 }
}
