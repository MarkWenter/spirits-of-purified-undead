package dev.purifiedundead.blightedrain.mob;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Shared transaction for environmental and item conversions. Exact vanilla sources only. */
public final class BlightedMobConversions {
    public static EntityType<?> nativeType(EntityType<?> type) {
        if (type == BlightedMobContent.SPIDER.get()) return EntityType.SPIDER;
        if (type == BlightedMobContent.ZOMBIE.get()) return EntityType.ZOMBIE;
        if (type == BlightedMobContent.SKELETON.get()) return EntityType.SKELETON;
        if (type == BlightedMobContent.DROWNED.get()) return EntityType.DROWNED;
        if (type == BlightedMobContent.HUSK.get()) return EntityType.HUSK;
        if (type == BlightedMobContent.STRAY.get()) return EntityType.STRAY;
        if (type == BlightedMobContent.VILLAGER.get()) return EntityType.VILLAGER;
        if (type == BlightedMobContent.PILLAGER.get()) return EntityType.PILLAGER;
        if (type == BlightedMobContent.VINDICATOR.get()) return EntityType.VINDICATOR;
        if (type == BlightedMobContent.WITCH.get()) return EntityType.WITCH;
        if (type == BlightedMobContent.PIG.get()) return EntityType.PIG;
        if (type == BlightedMobContent.COW.get()) return EntityType.COW;
        if (type == BlightedMobContent.SHEEP.get()) return EntityType.SHEEP;
        if (type == BlightedMobContent.CHICKEN.get()) return EntityType.CHICKEN;
        if (type == BlightedMobContent.HORSE.get()) return EntityType.HORSE;
        if (type == BlightedMobContent.DONKEY.get()) return EntityType.DONKEY;
        return type;
    }

    public static boolean candidate(Mob mob) {
        return target(mob) != null
                && !(mob instanceof net.minecraft.world.entity.npc.Villager)
                && mob.getType() != EntityType.CHICKEN;
    }

    public static boolean clickable(Mob mob) {
        return candidate(mob) && mob instanceof net.minecraft.world.entity.monster.Enemy;
    }

    private static EntityType<? extends Mob> target(Mob mob) {
        var type = mob.getType();
        if (type == EntityType.SPIDER) return BlightedMobContent.SPIDER.get();
        if (type == EntityType.ZOMBIE) return BlightedMobContent.ZOMBIE.get();
        if (type == EntityType.SKELETON) return BlightedMobContent.SKELETON.get();
        if (type == EntityType.DROWNED) return BlightedMobContent.DROWNED.get();
        if (type == EntityType.HUSK) return BlightedMobContent.HUSK.get();
        if (type == EntityType.STRAY) return BlightedMobContent.STRAY.get();
        if (type == EntityType.VILLAGER) return BlightedMobContent.VILLAGER.get();
        if (type == EntityType.PILLAGER) return BlightedMobContent.PILLAGER.get();
        if (type == EntityType.VINDICATOR) return BlightedMobContent.VINDICATOR.get();
        if (type == EntityType.WITCH) return BlightedMobContent.WITCH.get();
        if (type == EntityType.PIG) return BlightedMobContent.PIG.get();
        if (type == EntityType.COW) return BlightedMobContent.COW.get();
        if (type == EntityType.SHEEP) return BlightedMobContent.SHEEP.get();
        if (type == EntityType.CHICKEN) return BlightedMobContent.CHICKEN.get();
        if (type == EntityType.HORSE) return BlightedMobContent.HORSE.get();
        if (type == EntityType.DONKEY) return BlightedMobContent.DONKEY.get();
        return null;
    }

    public static Mob convert(Mob original, ServerLevel level, boolean lethalRainConversion) {
        if (original.isRemoved() || (!original.isAlive() && !lethalRainConversion)) return null;
        if (lethalRainConversion
                && original.getType() != EntityType.VILLAGER
                && original.getType() != EntityType.CHICKEN) return null;
        var type = target(original);
        if (type == null) return null;
        var next = type.create(level);
        if (next == null) return null;
        float health = original.getHealth(), maximum = original.getMaxHealth();
        var data = original.saveWithoutId(new net.minecraft.nbt.CompoundTag());
        data.remove("UUID");
        data.remove("Passengers");
        data.remove("RootVehicle");
        var detachedGear = new java.util.ArrayList<net.minecraft.world.item.ItemStack>();
        if (original instanceof net.minecraft.world.entity.animal.horse.AbstractHorse) {
            for (String key : new String[] {"SaddleItem"})
                if (data.contains(key, 10)) {
                    var stack = net.minecraft.world.item.ItemStack.of(data.getCompound(key));
                    if (!stack.isEmpty()) detachedGear.add(stack);
                    data.remove(key);
                }
        }
        // Forge mirrors horse armor into CHEST; the ordinary slot loop drops it once.
        if (original instanceof net.minecraft.world.entity.animal.horse.Horse)
            data.remove("ArmorItem");
        if (original instanceof net.minecraft.world.entity.animal.Pig pig && pig.isSaddled()) {
            data.putBoolean("Saddle", false);
            detachedGear.add(
                    new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SADDLE));
        }
        data.putInt("BlightedTraitsVersion", 1);
        next.load(data);
        next.getAttribute(Attributes.MOVEMENT_SPEED)
                .setBaseValue(
                        original.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue()
                                * BlightedMobRules.movementFactor(next));
        if (next instanceof BlightedPillager && next.getAttribute(Attributes.ATTACK_DAMAGE) != null)
            next.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(3.0);
        var attr = next.getAttribute(Attributes.MAX_HEALTH);
        var originalAttr = original.getAttribute(Attributes.MAX_HEALTH);
        attr.removeModifiers();
        attr.setBaseValue(originalAttr.getBaseValue() * BlightedMobRules.healthFactor(next));
        for (var modifier : originalAttr.getModifiers())
            attr.addPermanentModifier(
                    new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                            modifier.getId(),
                            modifier.getName(),
                            modifier.getAmount()
                                    * (modifier.getOperation()
                                                    == net.minecraft.world.entity.ai.attributes
                                                            .AttributeModifier.Operation.ADDITION
                                            ? BlightedMobRules.healthFactor(next)
                                            : 1),
                            modifier.getOperation()));
        next.setHealth(
                lethalRainConversion
                        ? next.getMaxHealth()
                        : dev.purifiedundead.entity.BlightedHealth.rescale(
                                health, maximum, next.getMaxHealth()));
        if (original instanceof net.minecraft.world.entity.AgeableMob age
                && next instanceof net.minecraft.world.entity.monster.Zombie zombie)
            zombie.setBaby(age.isBaby());
        for (var slot : EquipmentSlot.values())
            next.setItemSlot(slot, net.minecraft.world.item.ItemStack.EMPTY);
        next.setCanPickUpLoot(
                original.canPickUpLoot()
                        || original instanceof net.minecraft.world.entity.monster.Enemy);
        next.getPersistentData().remove("purified_undead:rain_conversion");
        next.getPersistentData().remove("purified_undead:rain_conversion_time");
        next.getPersistentData().remove("purified_undead:rain_retry");
        if (!level.addFreshEntity(next)) return null;
        for (var slot : EquipmentSlot.values()) {
            var stack = original.getItemBySlot(slot);
            if (!stack.isEmpty()) original.spawnAtLocation(stack.copy());
            original.setItemSlot(slot, net.minecraft.world.item.ItemStack.EMPTY);
        }
        for (var stack : detachedGear) original.spawnAtLocation(stack);
        original.stopRiding();
        original.ejectPassengers();
        original.discard();
        level.sendParticles(
                net.minecraft.core.particles.ParticleTypes.SOUL,
                next.getX(),
                next.getY() + 1,
                next.getZ(),
                24,
                .55,
                .8,
                .55,
                .04);
        return next;
    }

    private BlightedMobConversions() {}
}
