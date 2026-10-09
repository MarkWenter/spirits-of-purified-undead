package dev.purifiedundead.validation;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.resources.ResourceLocation;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(
        modid = "purified_undead",
        bus = net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD)
public final class UndeadCompatFixtures {
    public static EntityType<Zombie> NATIVE;
    public static EntityType<Cow> TAGGED;
    public static EntityType<Cow> LIVING;
    public static EntityType<Zombie> EXCLUDED;

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void register(net.minecraftforge.registries.RegisterEvent e) {
        e.register(
                net.minecraft.core.registries.Registries.ENTITY_TYPE,
                h -> {
                    EXCLUDED =
                            EntityType.Builder.<Zombie>of(Zombie::new, MobCategory.MONSTER)
                                    .build("compat_probe:excluded");
                    h.register(ResourceLocation.parse("compat_probe:excluded"), EXCLUDED);
                    NATIVE =
                            EntityType.Builder.<Zombie>of(Zombie::new, MobCategory.MONSTER)
                                    .build("compat_probe:native_undead");
                    TAGGED =
                            EntityType.Builder.of(Cow::new, MobCategory.MONSTER)
                                    .build("compat_probe:tagged_undead");
                    LIVING =
                            EntityType.Builder.of(Cow::new, MobCategory.MONSTER)
                                    .build("compat_probe:living");
                    h.register(ResourceLocation.parse("compat_probe:native_undead"), NATIVE);
                    h.register(ResourceLocation.parse("compat_probe:tagged_undead"), TAGGED);
                    h.register(ResourceLocation.parse("compat_probe:living"), LIVING);
                });
    }

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void attributes(net.minecraftforge.event.entity.EntityAttributeCreationEvent e) {
        e.put(EXCLUDED, Zombie.createAttributes().build());
        e.put(NATIVE, Zombie.createAttributes().build());
        e.put(TAGGED, Cow.createAttributes().build());
        e.put(LIVING, Cow.createAttributes().build());
    }
}
