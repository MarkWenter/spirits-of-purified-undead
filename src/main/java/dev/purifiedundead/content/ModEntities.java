package dev.purifiedundead.content;

import dev.purifiedundead.PurifiedUndead;
import dev.purifiedundead.entity.FerinEntity;
import dev.purifiedundead.entity.BlightedGolemEntity;
import dev.purifiedundead.entity.EleineMagicOrbEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    private static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, PurifiedUndead.MOD_ID);

    public static final RegistryObject<EntityType<dev.purifiedundead.entity.ContractWispEntity>>
            CONTRACT_WISP =
                    ENTITIES.register(
                            "contract_wisp",
                            () ->
                                    EntityType.Builder
                                            .<dev.purifiedundead.entity.ContractWispEntity>of(
                                                    dev.purifiedundead.entity.ContractWispEntity
                                                            ::new,
                                                    MobCategory.MISC)
                                            .sized(0F, 0F)
                                            .noSave()
                                            .noSummon()
                                            .fireImmune()
                                            .clientTrackingRange(1)
                                            .updateInterval(Integer.MAX_VALUE)
                                            .build(PurifiedUndead.MOD_ID + ":contract_wisp"));

    public static final RegistryObject<EntityType<dev.purifiedundead.entity.GuardianWaveEntity>>
            GUARDIAN_WAVE =
                    ENTITIES.register(
                            "guardian_wave",
                            () ->
                                    EntityType.Builder
                                            .<dev.purifiedundead.entity.GuardianWaveEntity>of(
                                                    dev.purifiedundead.entity.GuardianWaveEntity
                                                            ::new,
                                                    MobCategory.MISC)
                                            .sized(0.1F, 0.1F)
                                            .noSave()
                                            .noSummon()
                                            .fireImmune()
                                            .clientTrackingRange(4)
                                            .updateInterval(1)
                                            .build("purified_undead:guardian_wave"));

    public static final RegistryObject<EntityType<FerinEntity>> FERIN =
            ENTITIES.register(
                    "ferin",
                    () ->
                            EntityType.Builder.<FerinEntity>of(FerinEntity::new, MobCategory.MISC)
                                    .sized(0.75F, 1.8F)
                                    .clientTrackingRange(
                                            4) // Unit is chunks: 4 * 16 = 64 blocks, not 1024.
                                    .updateInterval(1)
                                    .build(PurifiedUndead.MOD_ID + ":ferin"));
    public static final RegistryObject<EntityType<BlightedGolemEntity>> BLIGHTED_GOLEM =
            ENTITIES.register(
                    "blighted_golem",
                    () ->
                            EntityType.Builder.of(BlightedGolemEntity::new, MobCategory.MISC)
                                    .sized(1.4F, 2.7F)
                                    .clientTrackingRange(10)
                                    .build(PurifiedUndead.MOD_ID + ":blighted_golem"));
    public static final RegistryObject<EntityType<EleineMagicOrbEntity>> ELEINE_MAGIC_ORB =
            ENTITIES.register(
                    "eleine_magic_orb",
                    () ->
                            EntityType.Builder.<EleineMagicOrbEntity>of(
                                            EleineMagicOrbEntity::new, MobCategory.MISC)
                                    .sized(0.35F, 0.35F)
                                    .clientTrackingRange(4)
                                    .updateInterval(1)
                                    .build(PurifiedUndead.MOD_ID + ":eleine_magic_orb"));

    public static final RegistryObject<EntityType<dev.purifiedundead.entity.BlightedKingEntity>>
            BLIGHTED_KING =
                    ENTITIES.register(
                            "blighted_king",
                            () ->
                                    EntityType.Builder
                                            .<dev.purifiedundead.entity.BlightedKingEntity>of(
                                                    dev.purifiedundead.entity.BlightedKingEntity
                                                            ::new,
                                                    MobCategory.MONSTER)
                                            .sized(0.6F, 1.95F)
                                            .clientTrackingRange(8)
                                            .build(PurifiedUndead.MOD_ID + ":blighted_king"));

    private ModEntities() {}

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
    }
}
