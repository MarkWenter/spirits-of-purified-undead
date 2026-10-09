package dev.purifiedundead.slate;
/** Invert vanilla discrete vertical motion, preserving existing jump boosts and gravity. */
public final class MemoryJump {
    public static double height(double velocity,double gravity) {
        double h=0; for(int i=0;i<512&&velocity>0;i++){h+=velocity;velocity=(velocity-gravity)*.98;} return h;
    }
    public static double raise(double velocity,double gravity,double extra) {
        if(!Double.isFinite(velocity)||!Double.isFinite(gravity)||gravity<=0||velocity<=0)return velocity;
        double target=height(velocity,gravity)+extra,lo=velocity,hi=velocity+Math.sqrt(2*gravity*extra)+1;
        for(int i=0;i<32;i++){double mid=(lo+hi)*.5;if(height(mid,gravity)<target)lo=mid;else hi=mid;}
        return (lo+hi)*.5;
    }
    private MemoryJump(){}
}
