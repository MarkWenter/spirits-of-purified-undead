package dev.purifiedundead.config;

import net.minecraftforge.common.ForgeConfigSpec;

/** Server-authoritative common configuration. Defaults preserve the confirmed first-release rules. */
public final class PurifiedUndeadConfig {
    public static final ForgeConfigSpec SPEC;
    public static final Values VALUES;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        VALUES = new Values(builder);
        SPEC = builder.build();
    }

    private PurifiedUndeadConfig() {
    }

    /** Uses the declared default during registry construction and pure tests, before Forge attaches a file. */
    public static <T> T get(ForgeConfigSpec.ConfigValue<T> value) {
        try {
            return value.get();
        } catch (IllegalStateException notLoadedYet) {
            return value.getDefault();
        }
    }

    /** One-time migration of previous defaults; retain explicit non-default balance values. */
    public static void migrateBalanceDefaults() {
        int revision = VALUES.balanceRevision.get();
        if (revision >= 2) return;
        if (revision < 1) {
            if (VALUES.fragmentDropChance.get() == 0.125D) VALUES.fragmentDropChance.set(0.25D);
            if (VALUES.hoenirNegativeEffectsRequired.get() == 5) VALUES.hoenirNegativeEffectsRequired.set(3);
        }
        if (VALUES.fragmentDropChance.get() == 0.25D) VALUES.fragmentDropChance.set(0.35D);
        VALUES.balanceRevision.set(2);
        SPEC.save();
    }


    public static final class Values {
        public final ForgeConfigSpec.IntValue balanceRevision;
        public final ForgeConfigSpec.DoubleValue juliusMeleeDamagePenalty;
        public final ForgeConfigSpec.IntValue warriorSlots;
        public final ForgeConfigSpec.IntValue whiteWitchRelicSlots;
        public final ForgeConfigSpec.DoubleValue fragmentDropChance;
        public final ForgeConfigSpec.IntValue transformationDurationTicks;
        public final ForgeConfigSpec.BooleanValue contractAcquisitionEnabled;
        public final ForgeConfigSpec.BooleanValue ferinAcquisitionEnabled;
        public final ForgeConfigSpec.BooleanValue grothAcquisitionEnabled;
        public final ForgeConfigSpec.BooleanValue juliusAcquisitionEnabled;
        public final ForgeConfigSpec.BooleanValue guardianAcquisitionEnabled;
        public final ForgeConfigSpec.BooleanValue ulvAcquisitionEnabled;
        public final ForgeConfigSpec.BooleanValue eleineAcquisitionEnabled;
        public final ForgeConfigSpec.BooleanValue hoenirAcquisitionEnabled;
        public final ForgeConfigSpec.BooleanValue fadenAcquisitionEnabled;
        public final ForgeConfigSpec.IntValue ulvMinimumSleepTicks;

        public final ForgeConfigSpec.IntValue talismanMaxLevel;
        public final ForgeConfigSpec.IntValue spiritsPerTalismanLevel;
        public final ForgeConfigSpec.DoubleValue talismanBaseIncomingMultiplier;
        public final ForgeConfigSpec.DoubleValue talismanReductionPerLevel;
        public final ForgeConfigSpec.IntValue ferinFourthStageLevel;
        public final ForgeConfigSpec.IntValue ferinFifthStageLevel;

        public final ForgeConfigSpec.DoubleValue ferinUnreversedMeleeMultiplier;
        public final ForgeConfigSpec.DoubleValue ferinReversedMeleeMultiplier;
        public final ForgeConfigSpec.DoubleValue ferinLifestealRatio;
        public final ForgeConfigSpec.ConfigValue<java.util.List<? extends Double>> ferinStageDamageMultipliers;
        public final ForgeConfigSpec.IntValue ferinSummonCooldownTicks;
        public final ForgeConfigSpec.IntValue ferinComboLockTicks;
        public final ForgeConfigSpec.IntValue ferinComboWindowTicks;
        public final ForgeConfigSpec.IntValue ferinFinalActionDurationTicks;
        public final ForgeConfigSpec.IntValue ferinExitDurationTicks;

        public final ForgeConfigSpec.DoubleValue grothUnreversedDamageMultiplier;
        public final ForgeConfigSpec.DoubleValue grothGroundDamageMultiplier;
        public final ForgeConfigSpec.DoubleValue grothMiningSpeedMultiplier;
        public final ForgeConfigSpec.DoubleValue grothCriticalDamageBonus;
        public final ForgeConfigSpec.IntValue grothStunDurationTicks;
        public final ForgeConfigSpec.IntValue grothStunCooldownTicks;

        public final ForgeConfigSpec.DoubleValue juliusReachPenalty;
        public final ForgeConfigSpec.DoubleValue juliusReachBonus;
        public final ForgeConfigSpec.DoubleValue juliusSprintPenaltyMultiplier;
        public final ForgeConfigSpec.DoubleValue juliusSprintBonusMultiplier;

        public final ForgeConfigSpec.DoubleValue guardianArmorFlatChange;
        public final ForgeConfigSpec.DoubleValue guardianArmorScale;
        public final ForgeConfigSpec.DoubleValue guardianDoubleJumpVelocity;
        public final ForgeConfigSpec.DoubleValue guardianAirDashSpeed;

        public final ForgeConfigSpec.DoubleValue ulvAttackSpeedPenaltyMultiplier;
        public final ForgeConfigSpec.DoubleValue ulvAttackSpeedBonusMultiplier;
        public final ForgeConfigSpec.DoubleValue ulvBacklashMaxHealthRatio;
        public final ForgeConfigSpec.DoubleValue ulvFollowUpDamageRatio;
        public final ForgeConfigSpec.IntValue ulvMaxLayers;
        public final ForgeConfigSpec.IntValue ulvComboWindowTicks;
        public final ForgeConfigSpec.DoubleValue ulvBonusPerLayer;

        public final ForgeConfigSpec.DoubleValue eleineMagicMultiplier;
        public final ForgeConfigSpec.DoubleValue eleineSwimSpeedMultiplier;
        public final ForgeConfigSpec.DoubleValue eleineOrbChance;
        public final ForgeConfigSpec.DoubleValue eleineOrbDamageRatio;
        public final ForgeConfigSpec.IntValue eleineWaterBreathingTicks;
        public final ForgeConfigSpec.IntValue eleineDrownedKillsRequired;

        public final ForgeConfigSpec.IntValue hoenirNegativeEffectsRequired;
        public final ForgeConfigSpec.IntValue hoenirMarkDurationTicks;
        public final ForgeConfigSpec.DoubleValue hoenirRegenerationRatio;
        public final ForgeConfigSpec.DoubleValue hoenirFerinMarkMultiplier;
        public final ForgeConfigSpec.DoubleValue hoenirHarmfulDurationMultiplier;
        public final ForgeConfigSpec.IntValue hoenirUnreversedAmplifierIncrease;

        public final ForgeConfigSpec.DoubleValue fadenKnockbackResistancePenalty;
        public final ForgeConfigSpec.DoubleValue fadenPoisonChance;
        public final ForgeConfigSpec.IntValue fadenPoisonDurationTicks;
        public final ForgeConfigSpec.IntValue fadenPoisonAmplifier;
        public final ForgeConfigSpec.IntValue fadenFortuneLootingBonus;

        public final ForgeConfigSpec.BooleanValue whiteWitchRelicsEnabled;
        public final ForgeConfigSpec.BooleanValue allowDuplicateWhiteWitchRelics;
        public final ForgeConfigSpec.DoubleValue whiteWitchRelicEffectScale;
        public final ForgeConfigSpec.DoubleValue whiteWitchRelicCooldownScale;

        private Values(ForgeConfigSpec.Builder builder) {
            balanceRevision = builder.defineInRange("balanceRevision", 0, 0, 100);
            builder.push("contract");
            warriorSlots = builder.comment("Undead Warrior slots granted by an equipped contract. Restart required.")
                    .defineInRange("warriorSlots", 8, 0, 64);
            whiteWitchRelicSlots = builder.comment("White Witch Relic slots. Restart required.")
                    .defineInRange("whiteWitchRelicSlots", 3, 0, 16);
            fragmentDropChance = builder.defineInRange("blightFragmentDropChance", 0.35D, 0.0D, 1.0D);
            transformationDurationTicks = builder.comment("Blighted Transformation duration. 20 ticks = one second.")
                    .defineInRange("transformationDurationTicks", 12000, 20, 720000);
            builder.pop();

            builder.push("acquisition");
            builder.comment("Disable an acquisition route when a modpack supplies its own recipes or quests.");
            contractAcquisitionEnabled = builder.define("contractEnabled", true);
            ferinAcquisitionEnabled = builder.define("ferinEnabled", true);
            grothAcquisitionEnabled = builder.define("grothEnabled", true);
            juliusAcquisitionEnabled = builder.define("juliusEnabled", true);
            guardianAcquisitionEnabled = builder.define("guardiansEnabled", true);
            ulvAcquisitionEnabled = builder.define("ulvEnabled", true);
            eleineAcquisitionEnabled = builder.define("eleineEnabled", true);
            hoenirAcquisitionEnabled = builder.define("hoenirEnabled", true);
            fadenAcquisitionEnabled = builder.define("fadenEnabled", true);
            ulvMinimumSleepTicks = builder.defineInRange("ulvMinimumSleepTicks", 100, 1, 12000);
            builder.pop();

            builder.push("talisman");
            talismanMaxLevel = builder.defineInRange("maxLevel", 15, 0, 100);
            spiritsPerTalismanLevel = builder.defineInRange("blightedSpiritsPerLevel", 4, 1, 64);
            talismanBaseIncomingMultiplier = builder.defineInRange("baseIncomingDamageMultiplier", 1.20D, 0.0D, 10.0D);
            talismanReductionPerLevel = builder.defineInRange("damageMultiplierReductionPerLevel", 0.04D, 0.0D, 1.0D);
            ferinFourthStageLevel = builder.defineInRange("ferinFourthStageLevel", 5, 0, 100);
            ferinFifthStageLevel = builder.defineInRange("ferinFifthStageLevel", 10, 0, 100);
            builder.pop();

            builder.push("ferin");
            ferinUnreversedMeleeMultiplier = builder.defineInRange("unreversedMeleeMultiplier", 0.70D, 0.0D, 10.0D);
            ferinReversedMeleeMultiplier = builder.defineInRange("reversedMeleeMultiplier", 1.30D, 0.0D, 10.0D);
            ferinLifestealRatio = builder.defineInRange("lifestealRatio", 0.20D, 0.0D, 10.0D);
            ferinStageDamageMultipliers = builder.comment("Exactly five stage multipliers.")
                    .defineList("stageDamageMultipliers", java.util.List.of(0.75D, 0.75D, 0.65D, 0.65D, 1.25D),
                            value -> value instanceof Double number && number >= 0.0D && number <= 20.0D);
            ferinSummonCooldownTicks = builder.defineInRange("summonCooldownTicks", 40, 0, 12000);
            ferinComboLockTicks = builder.defineInRange("comboLockTicks", 10, 0, 12000);
            ferinComboWindowTicks = builder.defineInRange("comboWindowTicks", 24, 1, 12000);
            ferinFinalActionDurationTicks = builder.defineInRange("finalActionDurationTicks", 16, 0, 12000);
            ferinExitDurationTicks = builder.defineInRange("exitDurationTicks", 4, 0, 12000);
            builder.pop();

            builder.push("groth");
            grothUnreversedDamageMultiplier = builder.defineInRange("unreversedIncomingDamageMultiplier", 1.20D, 0.0D, 10.0D);
            grothGroundDamageMultiplier = builder.defineInRange("reversedGroundIncomingDamageMultiplier", 0.80D, 0.0D, 10.0D);
            grothMiningSpeedMultiplier = builder.defineInRange("unreversedMiningSpeedMultiplier", 0.90D, 0.0D, 10.0D);
            grothCriticalDamageBonus = builder.defineInRange("criticalDamageBonus", 0.50D, 0.0D, 20.0D);
            grothStunDurationTicks = builder.defineInRange("stunDurationTicks", 60, 1, 12000);
            grothStunCooldownTicks = builder.defineInRange("stunCooldownTicks", 200, 0, 72000);
            builder.pop();

            builder.push("julius");
            juliusMeleeDamagePenalty = builder.defineInRange("unreversedFinalMeleeDamagePenalty", 1.0D, 0.0D, 100.0D);
            juliusReachPenalty = builder.defineInRange("unreversedReachPenalty", 1.0D, 0.0D, 16.0D);
            juliusReachBonus = builder.defineInRange("reversedReachBonus", 1.0D, 0.0D, 16.0D);
            juliusSprintPenaltyMultiplier = builder.defineInRange("unreversedSprintMultiplier", 0.80D, 0.0D, 10.0D);
            juliusSprintBonusMultiplier = builder.defineInRange("reversedSprintMultiplier", 1.20D, 0.0D, 10.0D);
            builder.pop();

            builder.push("guardians");
            guardianArmorFlatChange = builder.defineInRange("armorFlatChange", 5.0D, 0.0D, 1024.0D);
            guardianArmorScale = builder.defineInRange("armorAndToughnessScale", 0.10D, 0.0D, 10.0D);
            guardianDoubleJumpVelocity = builder.defineInRange("doubleJumpVelocity", 0.52D, 0.0D, 10.0D);
            guardianAirDashSpeed = builder.defineInRange("airDashSpeed", 1.15D, 0.0D, 10.0D);
            builder.pop();

            builder.push("ulv");
            ulvAttackSpeedPenaltyMultiplier = builder.defineInRange("unreversedAttackSpeedMultiplier", 0.80D, 0.0D, 10.0D);
            ulvAttackSpeedBonusMultiplier = builder.defineInRange("reversedAttackSpeedMultiplier", 1.20D, 0.0D, 10.0D);
            ulvBacklashMaxHealthRatio = builder.defineInRange("backlashMaxHealthRatio", 0.05D, 0.0D, 10.0D);
            ulvFollowUpDamageRatio = builder.defineInRange("followUpDamageRatio", 0.50D, 0.0D, 20.0D);
            ulvMaxLayers = builder.defineInRange("maxComboLayers", 5, 1, 100);
            ulvComboWindowTicks = builder.defineInRange("comboWindowTicks", 100, 1, 72000);
            ulvBonusPerLayer = builder.defineInRange("damageBonusPerLayer", 0.10D, 0.0D, 10.0D);
            builder.pop();

            builder.push("eleine");
            eleineMagicMultiplier = builder.defineInRange("magicDamageMultiplier", 1.30D, 0.0D, 10.0D);
            eleineSwimSpeedMultiplier = builder.defineInRange("unreversedSwimSpeedMultiplier", 0.65D, 0.0D, 10.0D);
            eleineOrbChance = builder.defineInRange("orbChance", 0.30D, 0.0D, 1.0D);
            eleineOrbDamageRatio = builder.defineInRange("orbDamageRatio", 0.45D, 0.0D, 20.0D);
            eleineWaterBreathingTicks = builder.defineInRange("waterBreathingTicks", 200, 1, 72000);
            eleineDrownedKillsRequired = builder.defineInRange("drownedKillsRequired", 3, 1, 1000);
            builder.pop();

            builder.push("hoenir");
            hoenirNegativeEffectsRequired = builder.defineInRange("negativeEffectsRequired", 3, 1, 255);
            hoenirMarkDurationTicks = builder.defineInRange("markDurationTicks", 200, 1, 72000);
            hoenirRegenerationRatio = builder.defineInRange("regenerationMaxHealthRatioPerSecond", 0.05D, 0.0D, 10.0D);
            hoenirFerinMarkMultiplier = builder.defineInRange("ferinMarkedDamageMultiplier", 1.50D, 0.0D, 20.0D);
            hoenirHarmfulDurationMultiplier = builder.defineInRange("reversedHarmfulDurationMultiplier", 0.50D, 0.0D, 10.0D);
            hoenirUnreversedAmplifierIncrease = builder.defineInRange("unreversedAmplifierIncrease", 1, 0, 255);
            builder.pop();

            builder.push("faden");
            fadenKnockbackResistancePenalty = builder.defineInRange("knockbackResistancePenalty", 0.15D, 0.0D, 1.0D);
            fadenPoisonChance = builder.defineInRange("poisonChance", 0.20D, 0.0D, 1.0D);
            fadenPoisonDurationTicks = builder.defineInRange("poisonDurationTicks", 400, 1, 72000);
            fadenPoisonAmplifier = builder.defineInRange("poisonAmplifier", 0, 0, 255);
            fadenFortuneLootingBonus = builder.defineInRange("fortuneAndLootingBonus", 1, 0, 255);
            builder.pop();

            builder.push("whiteWitchRelics");
            whiteWitchRelicsEnabled = builder.comment("Master switch for White Witch Relic equipment effects.")
                    .define("enabled", true);
            allowDuplicateWhiteWitchRelics = builder.define("allowDuplicates", false);
            whiteWitchRelicEffectScale = builder.defineInRange("globalEffectScale", 1.0D, 0.0D, 100.0D);
            whiteWitchRelicCooldownScale = builder.defineInRange("globalCooldownScale", 1.0D, 0.01D, 100.0D);
            builder.pop();
        }
    }
}
