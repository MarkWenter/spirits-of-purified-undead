package dev.purifiedundead.content;

import dev.purifiedundead.PurifiedUndead;
import dev.purifiedundead.config.PurifiedUndeadConfig;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModPotions {
    public static final int BLIGHTED_TRANSFORMATION_DURATION = 20 * 60 * 10;

    private static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(ForgeRegistries.POTIONS, PurifiedUndead.MOD_ID);

    public static final RegistryObject<Potion> BLIGHT_ELIXIR = POTIONS.register("blight_elixir",
            () -> new Potion("blight_elixir", new MobEffectInstance(
                    ModEffects.BLIGHTED_TRANSFORMATION.get(),
                    PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.transformationDurationTicks))));

    private ModPotions() {
    }

    public static void register(IEventBus bus) {
        POTIONS.register(bus);
    }
}
