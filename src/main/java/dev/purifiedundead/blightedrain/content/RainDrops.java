package dev.purifiedundead.blightedrain.content;

import dev.purifiedundead.blightedrain.mob.*;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;

public final class RainDrops {
    public static void add(net.minecraftforge.event.entity.living.LivingDropsEvent event) {
        var mob = event.getEntity();
        if (!(mob.level() instanceof net.minecraft.server.level.ServerLevel level)
                || !level.getGameRules()
                        .getBoolean(net.minecraft.world.level.GameRules.RULE_DOMOBLOOT)) return;
        var random = mob.getRandom();
        int looting = Math.max(0, Math.min(255, event.getLootingLevel()));
        if (mob instanceof BlightedVariant
                && (mob instanceof net.minecraft.world.entity.monster.Zombie
                        || mob instanceof net.minecraft.world.entity.monster.AbstractSkeleton)
                && random.nextFloat() < Math.min(1, .025F + .01F * looting))
            add(event, new ItemStack(RainItems.EMBLEM.get()));
        if (mob instanceof BlightedSpider && random.nextFloat() < .45F)
            add(
                    event,
                    new ItemStack(
                            RainItems.DUST.get(),
                            1 + random.nextInt(4) + Math.round(random.nextFloat() * looting)));
        if ((mob instanceof BlightedPillager
                        || mob instanceof BlightedVindicator
                        || mob.getType()
                                == dev.purifiedundead.content.ModEntities.BLIGHTED_KING.get())
                && random.nextFloat() < .2F) add(event, new ItemStack(RainItems.RUBY.get()));
        if (mob instanceof BlightedWitch && random.nextFloat() < .25F)
            add(
                    event,
                    dev.purifiedundead.progress.PureElixirBrewing.stack(
                            Items.POTION,
                            dev.purifiedundead.content.ModPotions.BLIGHT_ELIXIR.get()));
        if (mob instanceof BlightedPig
                || mob instanceof BlightedCow
                || mob instanceof BlightedSheep
                || mob instanceof BlightedChicken) {
            int count = 0;
            for (var drop : event.getDrops()) {
                var stack = drop.getItem();
                if (stack.is(Items.PORKCHOP)
                        || stack.is(Items.COOKED_PORKCHOP)
                        || stack.is(Items.BEEF)
                        || stack.is(Items.COOKED_BEEF)
                        || stack.is(Items.MUTTON)
                        || stack.is(Items.COOKED_MUTTON)
                        || stack.is(Items.CHICKEN)
                        || stack.is(Items.COOKED_CHICKEN)) count += stack.getCount();
            }
            while (count > 0) {
                int n = Math.min(64, count);
                add(event, new ItemStack(RainItems.MEAT.get(), n));
                count -= n;
            }
        }
    }

    private static void add(
            net.minecraftforge.event.entity.living.LivingDropsEvent event, ItemStack stack) {
        var mob = event.getEntity();
        event.getDrops()
                .add(new ItemEntity(mob.level(), mob.getX(), mob.getY(), mob.getZ(), stack));
    }

    private RainDrops() {}
}
