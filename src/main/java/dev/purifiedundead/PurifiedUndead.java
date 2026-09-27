package dev.purifiedundead;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.ModLoadingContext;
import dev.purifiedundead.config.PurifiedUndeadConfig;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraft.world.item.CreativeModeTabs;
import dev.purifiedundead.content.ModItems;
import dev.purifiedundead.content.ModEntities;
import dev.purifiedundead.content.ModParticles;
import dev.purifiedundead.content.ModEnchantments;
import dev.purifiedundead.content.ModEffects;
import dev.purifiedundead.content.ModPotions;
import dev.purifiedundead.content.item.AncientContractItem;
import dev.purifiedundead.content.item.FerinWarriorItem;
import dev.purifiedundead.combat.FerinCombatEvents;
import dev.purifiedundead.combat.ModDamageTypes;
import dev.purifiedundead.combat.ContractProtectionEvents;
import dev.purifiedundead.combat.ContractCurseIntegrityEvents;
import dev.purifiedundead.combat.GrothCombatEvents;
import dev.purifiedundead.combat.JuliusCombatEvents;
import dev.purifiedundead.progress.ContractProgressEvents;
import dev.purifiedundead.progress.AccessoryDeathRetentionEvents;
import dev.purifiedundead.progress.BlightMaterialDropEvents;
import dev.purifiedundead.progress.GrothAcquisitionEvents;
import dev.purifiedundead.progress.JuliusAcquisitionEvents;
import dev.purifiedundead.progress.GuardianAcquisitionEvents;
import dev.purifiedundead.combat.GuardianArmorEvents;
import dev.purifiedundead.combat.GuardianMovementService;
import dev.purifiedundead.combat.UlvCombatEvents;
import dev.purifiedundead.progress.UlvAcquisitionEvents;
import dev.purifiedundead.progress.EleineAcquisitionEvents;
import dev.purifiedundead.combat.EleineCombatEvents;
import dev.purifiedundead.progress.HoenirAcquisitionEvents;
import dev.purifiedundead.combat.HoenirCombatEvents;
import dev.purifiedundead.progress.FadenAcquisitionEvents;
import dev.purifiedundead.combat.FadenCombatEvents;
import dev.purifiedundead.content.ModLootModifiers;
import dev.purifiedundead.network.ModNetwork;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import dev.purifiedundead.progress.BlightElixirBrewingRecipe;
import net.minecraft.core.registries.Registries;
import top.theillusivec4.curios.api.CuriosApi;
import org.slf4j.Logger;

/** Environment bootstrap only; gameplay is introduced in subsequent milestones. */
@Mod(PurifiedUndead.MOD_ID)
public final class PurifiedUndead {
    public static final String MOD_ID = "purified_undead";
    private static final Logger LOGGER = LogUtils.getLogger();

    public PurifiedUndead(FMLJavaModLoadingContext context) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, PurifiedUndeadConfig.SPEC,
                "purified_undead-common.toml");
        var modBus = context.getModEventBus();
        modBus.addListener((net.minecraftforge.fml.event.config.ModConfigEvent.Loading event) -> {
            if (event.getConfig().getSpec() == PurifiedUndeadConfig.SPEC) PurifiedUndeadConfig.migrateBalanceDefaults();
        });
        ModItems.register(modBus);
        ModEntities.register(modBus);
        ModParticles.register(modBus);
        ModEnchantments.register(modBus);
        ModEffects.register(modBus);
        ModPotions.register(modBus);
        dev.purifiedundead.content.ModSounds.register(modBus);
        ModLootModifiers.register(modBus);
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::addCreativeTabItems);
        modBus.addListener(this::createAttributes);
        MinecraftForge.EVENT_BUS.register(new FerinCombatEvents());
        MinecraftForge.EVENT_BUS.register(new ContractProgressEvents());
        MinecraftForge.EVENT_BUS.register(new AccessoryDeathRetentionEvents());
        MinecraftForge.EVENT_BUS.register(new ContractProtectionEvents());
        MinecraftForge.EVENT_BUS.register(new ContractCurseIntegrityEvents());
        MinecraftForge.EVENT_BUS.register(new BlightMaterialDropEvents());
        MinecraftForge.EVENT_BUS.register(new GrothAcquisitionEvents());
        MinecraftForge.EVENT_BUS.register(new GrothCombatEvents());
        MinecraftForge.EVENT_BUS.register(new JuliusAcquisitionEvents());
        MinecraftForge.EVENT_BUS.register(new JuliusCombatEvents());
        MinecraftForge.EVENT_BUS.register(new GuardianAcquisitionEvents());
        MinecraftForge.EVENT_BUS.register(new GuardianArmorEvents());
        MinecraftForge.EVENT_BUS.register(new GuardianMovementService());
        MinecraftForge.EVENT_BUS.register(new UlvAcquisitionEvents());
        MinecraftForge.EVENT_BUS.register(new UlvCombatEvents());
        MinecraftForge.EVENT_BUS.register(new EleineAcquisitionEvents());
        MinecraftForge.EVENT_BUS.register(new EleineCombatEvents());
        MinecraftForge.EVENT_BUS.register(new HoenirAcquisitionEvents());
        MinecraftForge.EVENT_BUS.register(new HoenirCombatEvents());
        MinecraftForge.EVENT_BUS.register(new FadenAcquisitionEvents());
        MinecraftForge.EVENT_BUS.register(new FadenCombatEvents());
        MinecraftForge.EVENT_BUS.register(new dev.purifiedundead.content.relic.RelicEvents());
        MinecraftForge.EVENT_BUS.addListener(this::serverStarted);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModNetwork.register();
            BrewingRecipeRegistry.addRecipe(new BlightElixirBrewingRecipe());
        });
        LOGGER.info("Purified Undead environment ready: Curios={}, GeckoLib={}, Attributes={}, Placebo={}",
                ModList.get().isLoaded("curios"), ModList.get().isLoaded("geckolib"),
                ModList.get().isLoaded("attributeslib"), ModList.get().isLoaded("placebo"));
    }

    private void addCreativeTabItems(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.ANCIENT_CONTRACT);
            event.accept(ModItems.FERIN_WARRIOR);
            event.accept(ModItems.BLIGHT_FRAGMENT);
            event.accept(ModItems.BLIGHTED_SPIRIT);
            event.accept(ModItems.GROTH_WARRIOR);
            event.accept(ModItems.JULIUS_WARRIOR);
            event.accept(ModItems.FORMER_ORNAMENT);
            event.accept(ModItems.GUARDIAN_WARRIORS);
            event.accept(ModItems.SNOW_FLOWER);
            event.accept(ModItems.ULV_WARRIOR);
            event.accept(ModItems.TIRED_HEART);
            event.accept(ModItems.ELEINE_WARRIOR);
            event.accept(ModItems.HOENIR_WARRIOR);
            event.accept(ModItems.FADEN_WARRIOR);
            event.accept(ModItems.BLOODSTAINED_RIBBON);
            event.accept(ModItems.ANCIENT_DRAGON_CLAW);
            event.accept(ModItems.WEATHERED_WARRIOR_NECKLACE);
            event.accept(ModItems.SOILED_SILVER_ROSARY);
            event.accept(ModItems.WHITE_PRIESTESS_STATUE);
            event.accept(ModItems.BLIGHTED_FINGER);
            event.accept(ModItems.KINGS_SHIELD_BADGE);
            event.accept(ModItems.WHITE_PRIESTESS_EARRINGS);
        }
    }

    private void createAttributes(final EntityAttributeCreationEvent event) {
        event.put(ModEntities.BLIGHTED_GOLEM.get(), IronGolem.createAttributes()
                .add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 200).build());
        event.put(ModEntities.BLIGHTED_KING.get(), net.minecraft.world.entity.monster.Evoker.createAttributes()
                .add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 100).build());
    }

    private void serverStarted(final ServerStartedEvent event) {
        boolean contractSlotLoaded = CuriosApi.getPlayerSlots(event.getServer().overworld())
                .containsKey(AncientContractItem.SLOT_ID);
        boolean warriorSlotLoaded = CuriosApi.getPlayerSlots(event.getServer().overworld())
                .containsKey(FerinWarriorItem.SLOT_ID);
        boolean relicSlotLoaded = CuriosApi.getPlayerSlots(event.getServer().overworld())
                .containsKey(AncientContractItem.WHITE_WITCH_RELIC_SLOT_ID);
        boolean ferinDamageTypeLoaded = event.getServer().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE).containsKey(ModDamageTypes.FERIN_ASSIST.location());
        boolean snowFlowerRecipeLoaded = event.getServer().getRecipeManager().byKey(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MOD_ID, "snow_flower")).isPresent();
        boolean tiredHeartRecipeLoaded = event.getServer().getRecipeManager().byKey(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MOD_ID, "tired_heart")).isPresent();
        boolean eleineDamageTypeLoaded = event.getServer().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE).containsKey(ModDamageTypes.ELEINE_MAGIC_ORB.location());
        net.minecraft.world.item.ItemStack awkwardPotion = net.minecraft.world.item.alchemy.PotionUtils.setPotion(
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.POTION),
                net.minecraft.world.item.alchemy.Potions.AWKWARD);
        net.minecraft.world.item.ItemStack elixirOutput = BrewingRecipeRegistry.getOutput(
                awkwardPotion, new net.minecraft.world.item.ItemStack(ModItems.BLIGHTED_SPIRIT.get()));
        boolean blightElixirRecipeLoaded = net.minecraft.world.item.alchemy.PotionUtils.getPotion(elixirOutput)
                == ModPotions.BLIGHT_ELIXIR.get();
        LOGGER.info("Purified Undead server validation: Ancient Contract registered={}, contract slot loaded={}, "
                        + "Ferin Warrior registered={}, Ferin entity registered={}, warrior slot loaded={}, "
                        + "white witch relic slot loaded={}, eight curses registered={}, "
                        + "Groth registered={}, blighted golem registered={}, stun registered={}, Julius registered={}, "
                        + "Guardians registered={}, Ulv registered={}, Eleine registered={}, Eleine orb registered={}, Hoenir registered={}, Faden registered={}, blight elixir recipe loaded={}, "
                        + "Ferin damage type loaded={}, Ulv damage types loaded={}, Eleine damage type loaded={}, "
                        + "snow flower recipe loaded={}, tired heart recipe loaded={}, "
                        + "slash particle registered={}, Hoenir mark particle registered={}, Faden fortune modifier registered={}",
                ModItems.ANCIENT_CONTRACT.isPresent(), contractSlotLoaded, ModItems.FERIN_WARRIOR.isPresent(),
                ModEntities.FERIN.isPresent(), warriorSlotLoaded, relicSlotLoaded,
                ModEnchantments.BLIGHT_CURSES.stream().allMatch(net.minecraftforge.registries.RegistryObject::isPresent),
                ModItems.GROTH_WARRIOR.isPresent(), ModEntities.BLIGHTED_GOLEM.isPresent(),
                ModEffects.STUNNED.isPresent(), ModItems.JULIUS_WARRIOR.isPresent(),
                ModItems.GUARDIAN_WARRIORS.isPresent(),
                ModItems.ULV_WARRIOR.isPresent(),
                ModItems.ELEINE_WARRIOR.isPresent(), ModEntities.ELEINE_MAGIC_ORB.isPresent(),
                ModItems.HOENIR_WARRIOR.isPresent(),
                ModItems.FADEN_WARRIOR.isPresent(),
                ModPotions.BLIGHT_ELIXIR.isPresent() && ModEffects.BLIGHTED_TRANSFORMATION.isPresent()
                        && blightElixirRecipeLoaded,
                ferinDamageTypeLoaded,
                event.getServer().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                        .containsKey(ModDamageTypes.ULV_BLIGHT.location())
                        && event.getServer().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                        .containsKey(ModDamageTypes.ULV_FOLLOW_UP.location()),
                eleineDamageTypeLoaded, snowFlowerRecipeLoaded, tiredHeartRecipeLoaded,
                ModParticles.FERIN_SLASH.isPresent(), ModParticles.HOENIR_MARK.isPresent(),
                ModLootModifiers.FADEN_FORTUNE.isPresent());
    }
}
