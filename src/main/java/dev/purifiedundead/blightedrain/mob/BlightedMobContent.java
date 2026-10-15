package dev.purifiedundead.blightedrain.mob;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.horse.*;

public final class BlightedMobContent {
    private static final net.minecraftforge.registries.DeferredRegister<EntityType<?>> TYPES =
            net.minecraftforge.registries.DeferredRegister.create(
                    net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES, "purified_undead");
    public static final net.minecraftforge.registries.RegistryObject<EntityType<BlightedZombie>>
            ZOMBIE =
                    TYPES.register(
                            "blighted_zombie",
                            () ->
                                    EntityType.Builder.of(BlightedZombie::new, MobCategory.MONSTER)
                                            .sized(0.6F, 1.95F)
                                            .clientTrackingRange(8)
                                            .build("purified_undead:blighted_zombie"));
    public static final net.minecraftforge.registries.RegistryObject<EntityType<BlightedSkeleton>>
            SKELETON =
                    TYPES.register(
                            "blighted_skeleton",
                            () ->
                                    EntityType.Builder.of(
                                                    BlightedSkeleton::new, MobCategory.MONSTER)
                                            .sized(0.6F, 1.99F)
                                            .clientTrackingRange(8)
                                            .build("purified_undead:blighted_skeleton"));
    public static final net.minecraftforge.registries.RegistryObject<EntityType<BlightedDrowned>>
            DROWNED =
                    TYPES.register(
                            "blighted_drowned",
                            () ->
                                    EntityType.Builder.of(BlightedDrowned::new, MobCategory.MONSTER)
                                            .sized(0.6F, 1.95F)
                                            .clientTrackingRange(8)
                                            .build("purified_undead:blighted_drowned"));
    public static final net.minecraftforge.registries.RegistryObject<EntityType<BlightedHusk>>
            HUSK =
                    TYPES.register(
                            "blighted_husk",
                            () ->
                                    EntityType.Builder.of(BlightedHusk::new, MobCategory.MONSTER)
                                            .sized(0.6F, 1.95F)
                                            .clientTrackingRange(8)
                                            .build("purified_undead:blighted_husk"));
    public static final net.minecraftforge.registries.RegistryObject<EntityType<BlightedStray>>
            STRAY =
                    TYPES.register(
                            "blighted_stray",
                            () ->
                                    EntityType.Builder.of(BlightedStray::new, MobCategory.MONSTER)
                                            .sized(0.6F, 1.99F)
                                            .clientTrackingRange(8)
                                            .build("purified_undead:blighted_stray"));
    public static final net.minecraftforge.registries.RegistryObject<EntityType<BlightedVillager>>
            VILLAGER =
                    TYPES.register(
                            "blighted_villager",
                            () ->
                                    EntityType.Builder.of(
                                                    BlightedVillager::new, MobCategory.MONSTER)
                                            .sized(0.6F, 1.95F)
                                            .clientTrackingRange(8)
                                            .build("purified_undead:blighted_villager"));
    public static final net.minecraftforge.registries.RegistryObject<EntityType<BlightedPillager>>
            PILLAGER =
                    TYPES.register(
                            "blighted_pillager",
                            () ->
                                    EntityType.Builder.of(
                                                    BlightedPillager::new, MobCategory.MONSTER)
                                            .sized(0.6F, 1.95F)
                                            .clientTrackingRange(8)
                                            .build("purified_undead:blighted_pillager"));
    public static final net.minecraftforge.registries.RegistryObject<EntityType<BlightedVindicator>>
            VINDICATOR =
                    TYPES.register(
                            "blighted_vindicator",
                            () ->
                                    EntityType.Builder.of(
                                                    BlightedVindicator::new, MobCategory.MONSTER)
                                            .sized(0.6F, 1.95F)
                                            .clientTrackingRange(8)
                                            .build("purified_undead:blighted_vindicator"));
    public static final net.minecraftforge.registries.RegistryObject<EntityType<BlightedWitch>>
            WITCH =
                    TYPES.register(
                            "blighted_witch",
                            () ->
                                    EntityType.Builder.of(BlightedWitch::new, MobCategory.MONSTER)
                                            .sized(0.6F, 1.95F)
                                            .clientTrackingRange(8)
                                            .build("purified_undead:blighted_witch"));
    public static final net.minecraftforge.registries.RegistryObject<EntityType<BlightedPig>> PIG =
            TYPES.register(
                    "blighted_pig",
                    () ->
                            EntityType.Builder.of(BlightedPig::new, MobCategory.CREATURE)
                                    .sized(0.9F, 0.9F)
                                    .clientTrackingRange(8)
                                    .build("purified_undead:blighted_pig"));
    public static final net.minecraftforge.registries.RegistryObject<EntityType<BlightedCow>> COW =
            TYPES.register(
                    "blighted_cow",
                    () ->
                            EntityType.Builder.of(BlightedCow::new, MobCategory.CREATURE)
                                    .sized(0.9F, 1.4F)
                                    .clientTrackingRange(8)
                                    .build("purified_undead:blighted_cow"));
    public static final net.minecraftforge.registries.RegistryObject<EntityType<BlightedSheep>>
            SHEEP =
                    TYPES.register(
                            "blighted_sheep",
                            () ->
                                    EntityType.Builder.of(BlightedSheep::new, MobCategory.CREATURE)
                                            .sized(0.9F, 1.3F)
                                            .clientTrackingRange(8)
                                            .build("purified_undead:blighted_sheep"));
    public static final net.minecraftforge.registries.RegistryObject<EntityType<BlightedChicken>>
            CHICKEN =
                    TYPES.register(
                            "blighted_chicken",
                            () ->
                                    EntityType.Builder.of(
                                                    BlightedChicken::new, MobCategory.CREATURE)
                                            .sized(0.4F, 0.7F)
                                            .clientTrackingRange(8)
                                            .build("purified_undead:blighted_chicken"));
    public static final net.minecraftforge.registries.RegistryObject<EntityType<BlightedHorse>>
            HORSE =
                    TYPES.register(
                            "blighted_horse",
                            () ->
                                    EntityType.Builder.of(BlightedHorse::new, MobCategory.CREATURE)
                                            .sized(1.3964844F, 1.6F)
                                            .clientTrackingRange(8)
                                            .build("purified_undead:blighted_horse"));
    public static final net.minecraftforge.registries.RegistryObject<EntityType<BlightedDonkey>>
            DONKEY =
                    TYPES.register(
                            "blighted_donkey",
                            () ->
                                    EntityType.Builder.of(BlightedDonkey::new, MobCategory.CREATURE)
                                            .sized(1.3964844F, 1.5F)
                                            .clientTrackingRange(8)
                                            .build("purified_undead:blighted_donkey"));

    public static void register(net.minecraftforge.eventbus.api.IEventBus bus) {
        TYPES.register(bus);
        bus.addListener(BlightedMobContent::attributes);
    }

    private static void attributes(net.minecraftforge.event.entity.EntityAttributeCreationEvent e) {
        e.put(
                ZOMBIE.get(),
                Zombie.createAttributes()
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 40)
                        .build());
        e.put(
                SKELETON.get(),
                AbstractSkeleton.createAttributes()
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 40)
                        .build());
        e.put(
                DROWNED.get(),
                Zombie.createAttributes()
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 40)
                        .build());
        e.put(
                HUSK.get(),
                Zombie.createAttributes()
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 40)
                        .build());
        e.put(
                STRAY.get(),
                AbstractSkeleton.createAttributes()
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 40)
                        .build());
        e.put(
                VILLAGER.get(),
                Zombie.createAttributes()
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 40)
                        .build());
        e.put(
                PILLAGER.get(),
                Pillager.createAttributes()
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE, 3.0)
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 48)
                        .build());
        e.put(
                VINDICATOR.get(),
                Vindicator.createAttributes()
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 48)
                        .build());
        e.put(
                WITCH.get(),
                Witch.createAttributes()
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 52)
                        .build());
        e.put(
                PIG.get(),
                Pig.createAttributes()
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 20)
                        .build());
        e.put(
                COW.get(),
                Cow.createAttributes()
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 20)
                        .build());
        e.put(
                SHEEP.get(),
                Sheep.createAttributes()
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 16)
                        .build());
        e.put(
                CHICKEN.get(),
                Chicken.createAttributes()
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 8)
                        .build());
        e.put(
                HORSE.get(),
                AbstractHorse.createBaseHorseAttributes()
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 106)
                        .build());
        e.put(
                DONKEY.get(),
                AbstractChestedHorse.createBaseChestedHorseAttributes()
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 106)
                        .build());
    }

    private BlightedMobContent() {}
}
