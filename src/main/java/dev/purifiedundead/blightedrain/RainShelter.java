package dev.purifiedundead.blightedrain;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;

/** Maintain vanilla HIDE activity without creating a raid or simulating a bell. */
public final class RainShelter {
    private static final String KEY = "purified_undead:rain_shelter";

    private RainShelter() {}

    public static void tick(Villager villager, boolean threatened) {
        if (villager.isNoAi() || villager.tickCount % 20 != 0) return;
        var level = (ServerLevel) villager.level();
        var brain = villager.getBrain();
        var tag = villager.getPersistentData();
        if (!threatened || !RainExposure.precipitation(level, villager.blockPosition())) {
            if (tag.contains(KEY)) {
                var own = BlockPos.of(tag.getLong(KEY));
                if (brain.getMemory(MemoryModuleType.HIDING_PLACE)
                        .map(p -> p.pos().equals(own))
                        .orElse(false)) {
                    brain.eraseMemory(MemoryModuleType.HIDING_PLACE);
                    if (brain.getMemory(MemoryModuleType.WALK_TARGET)
                            .map(w -> w.getTarget().currentBlockPosition().equals(own))
                            .orElse(false)) brain.eraseMemory(MemoryModuleType.WALK_TARGET);
                    brain.updateActivityFromSchedule(level.getDayTime(), level.getGameTime());
                }
                tag.remove(KEY);
            }
            return;
        }
        if (brain.isActive(Activity.RAID) || brain.isActive(Activity.PANIC)) return;
        BlockPos target = tag.contains(KEY) ? BlockPos.of(tag.getLong(KEY)) : null;
        if (target == null || !safe(level, target)) {
            if (safe(level, villager.blockPosition())) target = villager.blockPosition();
            else
                target =
                        brain.getMemory(MemoryModuleType.HOME)
                                .filter(
                                        p ->
                                                p.dimension() == level.dimension()
                                                        && safe(level, p.pos()))
                                .map(GlobalPos::pos)
                                .orElse(null);
            if (target == null)
                target =
                        level.getPoiManager()
                                .findAll(
                                        h -> h.is(PoiTypes.HOME),
                                        p -> safe(level, p),
                                        villager.blockPosition(),
                                        32,
                                        PoiManager.Occupancy.ANY)
                                .sorted(
                                        java.util.Comparator.comparingDouble(
                                                p -> p.distSqr(villager.blockPosition())))
                                .limit(4)
                                .filter(p -> villager.getNavigation().createPath(p, 1) != null)
                                .findFirst()
                                .orElse(null);
            if (target == null) return;
            tag.putLong(KEY, target.asLong());
        }
        brain.setMemory(MemoryModuleType.HIDING_PLACE, GlobalPos.of(level.dimension(), target));
        brain.setActiveActivityIfPossible(Activity.HIDE);
        if (!target.closerToCenterThan(villager.position(), 1.5))
            brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(target, .65F, 1));
        else {
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
            villager.getNavigation().stop();
        }
    }

    private static boolean safe(ServerLevel level, BlockPos pos) {
        return level.hasChunkAt(pos)
                && (!level.canSeeSky(pos.above())
                        || level.getHeightmapPos(
                                                net.minecraft.world.level.levelgen.Heightmap.Types
                                                        .MOTION_BLOCKING,
                                                pos.above())
                                        .getY()
                                > pos.above().getY())
                && level.getFluidState(pos).isEmpty();
    }
}
