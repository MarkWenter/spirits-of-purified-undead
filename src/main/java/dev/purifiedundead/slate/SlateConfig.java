package dev.purifiedundead.slate;

import net.minecraftforge.common.ForgeConfigSpec;

/** Dedicated, documented settings for the second slate update. */
public final class SlateConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.DoubleValue chestChance, grothDamage, sprintDamage, speedStepDamage, swimBonus, orbChance, orbDamage, statusChance, ferinBonus, healthBonus;
    public static final ForgeConfigSpec.IntValue statusTicks, sistersTicks, sistersCooldown, revision;
    static {
        var b = new ForgeConfigSpec.Builder();
        b.comment("Slate update / 石板更新第二部分。Foundry recipes: purified_undead-foundry.json; use /purifiedundead config reload foundry after editing JSON; restart for TOML. 石板数值修改建议两端同步后重启，铸造JSON支持安全重载。").push("slate");
        chestChance=b.comment("Cipher fragments: chance per Overworld/Nether treasure chest, 1-5 fragments.").defineInRange("cipherChestChance",.25,0,1);
        grothDamage=b.comment("Plunging splash damage fraction; radius 1.5 blocks.").defineInRange("grothSplashDamage",.5,0,10);
        sprintDamage=b.defineInRange("juliusSprintDamage",.2,0,10);
        speedStepDamage=b.comment("Extra damage fraction per +10% movement speed above the player's base attribute.").defineInRange("juliusDamagePerSpeedStep",.05,0,10);
        swimBonus=b.defineInRange("eleineSwimBonus",.5,0,10);
        orbChance=b.defineInRange("eleineOrbChance",1.0,0,1);
        orbDamage=b.defineInRange("eleineOrbDamageFraction",.9,0,10);
        statusChance=b.comment("Independent roll for EACH of wither, poison, fire, freezing.").defineInRange("hoenirStatusChance",.25,0,1);
        statusTicks=b.comment("20 ticks = 1 second; effects use amplifier 0 (level I).").defineInRange("hoenirStatusTicks",100,1,12000);
        sistersTicks=b.defineInRange("sistersBuffTicks",1000,1,72000);
        sistersCooldown=b.defineInRange("sistersCooldownTicks",3600,1,72000);
        revision=b.comment("Internal default migration revision.").defineInRange("revision",0,0,100);
        ferinBonus=b.defineInRange("ferinStageDamageBonus",1.5,0,10);
        healthBonus=b.comment("Multiplicative total maximum-health bonus; 1.0 doubles maximum health.").defineInRange("shiningGuardianHealthBonus",1.0,0,10);
        b.pop(); SPEC=b.build();
    }
    public static <T> T get(ForgeConfigSpec.ConfigValue<T> value) {return dev.purifiedundead.config.PurifiedUndeadConfig.get(value);}
    public static void migrate() {
        if (get(revision) < 1) {
            if (get(ferinBonus) == .5D) ferinBonus.set(1.5D);
            revision.set(1); SPEC.save();
        }
    }
    private SlateConfig() {}
}
