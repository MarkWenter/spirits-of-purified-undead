package dev.purifiedundead.content;

import com.mojang.serialization.Codec;
import dev.purifiedundead.PurifiedUndead;
import dev.purifiedundead.loot.FadenFortuneLootModifier;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModLootModifiers {
    private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> SERIALIZERS =
            DeferredRegister.create(
                    ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, PurifiedUndead.MOD_ID);

    public static final RegistryObject<Codec<FadenFortuneLootModifier>> FADEN_FORTUNE =
            SERIALIZERS.register("faden_fortune", () -> FadenFortuneLootModifier.CODEC);

    public static final RegistryObject<Codec<dev.purifiedundead.loot.WhiteWitchRelicLootModifier>>
            WHITE_WITCH_RELIC =
                    SERIALIZERS.register(
                            "white_witch_relic",
                            () -> dev.purifiedundead.loot.WhiteWitchRelicLootModifier.CODEC);

    public static final RegistryObject<Codec<dev.purifiedundead.loot.CipherLootModifier>> CIPHER =
            SERIALIZERS.register("cipher", () -> dev.purifiedundead.loot.CipherLootModifier.CODEC);

    private ModLootModifiers() {}

    public static void register(IEventBus bus) {
        SERIALIZERS.register(bus);
    }
}
