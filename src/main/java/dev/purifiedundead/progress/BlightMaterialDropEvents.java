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
    public static final float FRAGMENT_DROP_CHANCE = 0.125F;

    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer)
                || !event.getEntity().getType().is(ModEntityTypeTags.BLIGHT_FRAGMENT_SOURCES)
                || event.getEntity().getRandom().nextFloat()
                >= PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.fragmentDropChance)) {
            return;
        }
        event.getDrops().add(new ItemEntity(event.getEntity().level(), event.getEntity().getX(),
                event.getEntity().getY(), event.getEntity().getZ(), new ItemStack(ModItems.BLIGHT_FRAGMENT.get())));
    }
}
