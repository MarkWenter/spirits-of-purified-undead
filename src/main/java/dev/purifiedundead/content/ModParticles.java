package dev.purifiedundead.content;

import dev.purifiedundead.PurifiedUndead;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModParticles {
    private static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, PurifiedUndead.MOD_ID);

    public static final RegistryObject<SimpleParticleType> FERIN_SLASH =
            PARTICLES.register("ferin_slash", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> HOENIR_MARK =
            PARTICLES.register("hoenir_mark", () -> new SimpleParticleType(true));

    private ModParticles() {
    }

    public static void register(IEventBus bus) {
        PARTICLES.register(bus);
    }
}
