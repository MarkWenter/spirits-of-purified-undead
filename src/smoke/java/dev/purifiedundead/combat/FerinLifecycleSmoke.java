package dev.purifiedundead.combat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;

public final class FerinLifecycleSmoke {
    public static void run(ServerPlayer original, ServerPlayer replacement) {
        var events = new FerinCombatEvents();
        var state = new FerinPlayerState();
        var timings = new FerinComboTimings(40,10,24,16,4);
        state.combo.onQualifyingHit(100,3,timings);
        state.continuation.request(1,100);
        FerinPlayerStateStorage.save(original,state);
        events.onPlayerClone(new PlayerEvent.Clone(replacement,original,true));
        var after=FerinPlayerStateStorage.load(replacement);
        check(after.combo.stage()==0 && after.combo.cooldownUntil()==140,"clone cancels attack but preserves cooldown");
        state.cancelActive();
        state.combo.onQualifyingHit(140,3,timings);
        FerinPlayerStateStorage.save(replacement,state);
        events.onDimensionChanged(new PlayerEvent.PlayerChangedDimensionEvent(replacement,Level.OVERWORLD,Level.NETHER));
        after=FerinPlayerStateStorage.load(replacement);
        check(after.combo.stage()==0 && after.combo.cooldownUntil()==180,"dimension cancels stale origin but preserves cooldown");
        System.out.println("FERIN_LIFECYCLE_OK: clone, dimension, saved cooldown");
    }
    private static void check(boolean ok,String reason){if(!ok)throw new IllegalStateException("FERIN_LIFECYCLE_FAILED: "+reason);}
}
