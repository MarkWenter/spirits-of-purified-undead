package dev.purifiedundead.blightedarmaments;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = "purified_undead")
public final class BreathgoldEvents {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void placed(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player
                && event.getLevel() instanceof ServerLevel level
                && level.dimension() == Level.OVERWORLD
                && event.getPlacedBlock().is(Blocks.GOLD_ORE))
            PlacedGoldData.get(level).placed(event.getPos());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void broken(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimension() == Level.OVERWORLD)
            PlacedGoldData.get(level).removed(event.getPos());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void tick(net.minecraftforge.event.TickEvent.LevelTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
        if (event.level instanceof ServerLevel level && level.dimension() == Level.OVERWORLD)
            PlacedGoldData.get(level).tick(level);
    }
}
