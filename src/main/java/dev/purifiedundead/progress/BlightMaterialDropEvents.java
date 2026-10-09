package dev.purifiedundead.progress;

import dev.purifiedundead.config.PurifiedUndeadConfig;

import dev.purifiedundead.content.ModEntityTypeTags;
import dev.purifiedundead.content.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Initial, data-tag-driven source for the talisman's upgrade material. */
public final class BlightMaterialDropEvents {
    public static final float FRAGMENT_DROP_CHANCE = 0.25F;

    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)
                || !dev.purifiedundead.compat.UndeadCompatibility.fragmentSource(event.getEntity())
                || event.getEntity().getRandom().nextFloat()
                >= Math.min(1D,PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.fragmentDropChance)
                    * (dev.purifiedundead.slate.MemoryStorage.active(player,"faden")?2D:1D))) {
            return;
        }
        int looting = Math.max(0, Math.min(255, event.getLootingLevel()));
        int count = 1 + event.getEntity().getRandom().nextInt(8) + event.getEntity().getRandom().nextInt(looting + 1);
        while (count > 0) {
        int stackSize = Math.min(count, ModItems.BLIGHT_FRAGMENT.get().getDefaultInstance().getMaxStackSize());
        event.getDrops().add(new ItemEntity(event.getEntity().level(), event.getEntity().getX(),
                event.getEntity().getY(), event.getEntity().getZ(), new ItemStack(ModItems.BLIGHT_FRAGMENT.get(), stackSize)));
        count -= stackSize;
        }
    }
}
