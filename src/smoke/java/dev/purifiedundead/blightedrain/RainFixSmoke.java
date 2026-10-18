package dev.purifiedundead.blightedrain;

import dev.purifiedundead.blightedrain.mob.*;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;

/** Exercises the real conversion, pickup and spawn-egg interaction paths. */
public final class RainFixSmoke {
    private static void check(boolean value, String message) {
        if (!value) throw new IllegalStateException("RAIN_FIX_FAILED: " + message);
    }

    public static void run(net.minecraft.server.MinecraftServer server) throws Exception {
        ServerLevel level = server.overworld();
        var source = EntityType.PILLAGER.create(level);
        source.setPos(55.5, 150, 80.5);
        source.setNoAi(true);
        level.getChunk(source.blockPosition());
        level.addFreshEntity(source);
        ItemStack bow = new ItemStack(Items.CROSSBOW);
        bow.setDamageValue(17);
        source.setItemSlot(EquipmentSlot.MAINHAND, bow.copy());
        var priorDrops =
                level
                        .getEntitiesOfClass(
                                net.minecraft.world.entity.item.ItemEntity.class,
                                source.getBoundingBox().inflate(1))
                        .stream()
                        .map(Entity::getUUID)
                        .collect(java.util.stream.Collectors.toSet());
        var next = (BlightedPillager) BlightedMobConversions.convert(source, level, false);
        try {
            check(next.getMainHandItem().isEmpty(), "original weapon drops");
            var drops =
                    level.getEntitiesOfClass(
                            net.minecraft.world.entity.item.ItemEntity.class,
                            next.getBoundingBox().inflate(1),
                            e ->
                                    !priorDrops.contains(e.getUUID())
                                            && e.getItem().is(Items.CROSSBOW));
            check(drops.size() == 1, "exactly one crossbow drop, found=" + drops.size());
            var pickup =
                    BlightedPillager.class.getDeclaredMethod(
                            "pickUpItem", net.minecraft.world.entity.item.ItemEntity.class);
            pickup.setAccessible(true);
            pickup.invoke(next, drops.get(0));
            check(
                    ItemStack.matches(next.getMainHandItem(), bow) && drops.get(0).isRemoved(),
                    "pickup preserves exact weapon without duplication");
            next.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            var cow = EntityType.COW.create(level);
            cow.setPos(55.5, 150, 80.5);
            cow.setNoAi(true);
            level.addFreshEntity(cow);
            try {
                float hp = cow.getHealth();
                check(
                        next.doHurtTarget(cow) && cow.getHealth() < hp,
                        "unarmed attack deals damage");
            } finally {
                cow.discard();
            }
        } finally {
            next.discard();
            source.discard();
        }
        int eggs = 0;
        for (var id : net.minecraft.core.registries.BuiltInRegistries.ITEM.keySet()) {
            if (!id.getNamespace().equals("purified_undead")
                    || !id.getPath().endsWith("_spawn_egg")) continue;
            var egg = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(id);
            var pos = new net.minecraft.core.BlockPos(55, 149, 80);
            level.getChunk(pos);
            var old = level.getBlockState(pos);
            level.setBlockAndUpdate(
                    pos, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            var above = level.getBlockState(pos.above());
            var higher = level.getBlockState(pos.above(2));
            level.setBlockAndUpdate(
                    pos.above(), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(
                    pos.above(2), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            try {
                var stack = new ItemStack(egg);
                var hit =
                        new net.minecraft.world.phys.BlockHitResult(
                                net.minecraft.world.phys.Vec3.atCenterOf(pos),
                                net.minecraft.core.Direction.UP,
                                pos,
                                false);
                var context =
                        new net.minecraft.world.item.context.UseOnContext(
                                level,
                                null,
                                net.minecraft.world.InteractionHand.MAIN_HAND,
                                stack,
                                hit) {};
                check(egg.useOn(context).consumesAction(), "egg interaction " + id);
                var expected =
                        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                                "purified_undead", id.getPath().replace("_spawn_egg", ""));
                var mobs =
                        level.getEntitiesOfClass(
                                Mob.class,
                                new net.minecraft.world.phys.AABB(pos.above()).inflate(1));
                check(
                        mobs.stream()
                                .anyMatch(
                                        m ->
                                                net.minecraft.core.registries.BuiltInRegistries
                                                        .ENTITY_TYPE
                                                        .getKey(m.getType())
                                                        .equals(expected)),
                        "egg spawned correct entity "
                                + id
                                + " actual="
                                + mobs.stream().map(m -> m.getType().toString()).toList());
                mobs.forEach(Entity::discard);
                eggs++;
            } finally {
                level.setBlockAndUpdate(pos, old);
                level.setBlockAndUpdate(pos.above(), above);
                level.setBlockAndUpdate(pos.above(2), higher);
            }
        }
        check(eggs == 18, "all eggs registered");
        System.out.println(
                "RAIN_FIX_SERVER_OK: crossbow drop/pickup, unarmed damage, "
                        + eggs
                        + " real egg interactions");
    }
}
