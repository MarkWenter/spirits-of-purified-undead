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

    public static final int PURE_ELIXIR_COLOR = 0x78D7FF;
    public static final RegistryObject<Potion> PURE_ELIXIR = POTIONS.register("pure_elixir", () -> pure(1800, 1));
    public static final RegistryObject<Potion> LONG_PURE_ELIXIR = POTIONS.register("long_pure_elixir", () -> pure(4800, 1));
    public static final RegistryObject<Potion> STRONG_PURE_ELIXIR = POTIONS.register("strong_pure_elixir", () -> pure(900, 2));

    private static Potion pure(int duration, int amplifier) {
        return new Potion("pure_elixir",
                new MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, duration, amplifier),
                new MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, duration, amplifier),
                new MobEffectInstance(net.minecraft.world.effect.MobEffects.ABSORPTION, duration, amplifier));
    }

    private ModPotions() {
    }

    public static void register(IEventBus bus) {
        POTIONS.register(bus);
    }
}
