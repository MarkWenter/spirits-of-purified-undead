# 配置键索引

0.55.0 两版共用键名和默认值。以下为代码声明值；已有自定义数值不会由本索引覆盖。`balanceRevision` / `slate.revision` 是内部历史迁移标记，实际生成文件可能已更新，请勿手动归零。概率 0–1，倍率 1 表示 100%，20 ticks = 1 秒。具体应用方式见[管理说明](COMMANDS_AND_CONFIG.md)。

## purified_undead-common.toml

| 键 | 声明默认值 | 范围 |
| --- | --- | --- |
| `balanceRevision` | `0` | 0 — 100 |
| `contract.warriorSlots` | `8` | 0 — 64 |
| `contract.whiteWitchRelicSlots` | `3` | 0 — 16 |
| `contract.blightFragmentDropChance` | `0.35` | 0.0 — 1.0 |
| `contract.autoDetectModdedUndeadDrops` | `true` | true / false |
| `contract.transformationDurationTicks` | `12000` | 20 — 720000 |
| `acquisition.contractEnabled` | `true` | true / false |
| `acquisition.ferinEnabled` | `true` | true / false |
| `acquisition.grothEnabled` | `true` | true / false |
| `acquisition.juliusEnabled` | `true` | true / false |
| `acquisition.guardiansEnabled` | `true` | true / false |
| `acquisition.ulvEnabled` | `true` | true / false |
| `acquisition.eleineEnabled` | `true` | true / false |
| `acquisition.hoenirEnabled` | `true` | true / false |
| `acquisition.fadenEnabled` | `true` | true / false |
| `acquisition.ulvMinimumSleepTicks` | `100` | 1 — 12000 |
| `talisman.maxLevel` | `15` | 0 — 100 |
| `talisman.blightedSpiritsPerLevel` | `4` | 1 — 64 |
| `talisman.baseIncomingDamageMultiplier` | `1.20` | 0.0 — 10.0 |
| `talisman.damageMultiplierReductionPerLevel` | `0.04` | 0.0 — 1.0 |
| `talisman.ferinFourthStageLevel` | `5` | 0 — 100 |
| `talisman.ferinFifthStageLevel` | `10` | 0 — 100 |
| `ferin.unreversedMeleeMultiplier` | `0.70` | 0.0 — 10.0 |
| `ferin.reversedMeleeMultiplier` | `1.30` | 0.0 — 10.0 |
| `ferin.lifestealRatio` | `0.20` | 0.0 — 10.0 |
| `ferin.stageDamageMultipliers` | `[0.75, 0.75, 0.65, 0.65, 1.25]` | 五项；每项 0–20 |
| `ferin.summonCooldownTicks` | `40` | 0 — 12000 |
| `ferin.comboLockTicks` | `10` | 0 — 12000 |
| `ferin.comboWindowTicks` | `24` | 1 — 12000 |
| `ferin.finalActionDurationTicks` | `16` | 0 — 12000 |
| `ferin.exitDurationTicks` | `4` | 0 — 12000 |
| `groth.unreversedIncomingDamageMultiplier` | `1.20` | 0.0 — 10.0 |
| `groth.reversedGroundIncomingDamageMultiplier` | `0.80` | 0.0 — 10.0 |
| `groth.unreversedMiningSpeedMultiplier` | `0.90` | 0.0 — 10.0 |
| `groth.criticalDamageBonus` | `0.50` | 0.0 — 20.0 |
| `groth.stunDurationTicks` | `60` | 1 — 12000 |
| `groth.stunCooldownTicks` | `200` | 0 — 72000 |
| `julius.unreversedFinalMeleeDamagePenalty` | `1.0` | 0.0 — 100.0 |
| `julius.unreversedReachPenalty` | `1.0` | 0.0 — 16.0 |
| `julius.reversedReachBonus` | `1.0` | 0.0 — 16.0 |
| `julius.unreversedSprintMultiplier` | `0.80` | 0.0 — 10.0 |
| `julius.reversedSprintMultiplier` | `1.20` | 0.0 — 10.0 |
| `guardians.armorFlatChange` | `5.0` | 0.0 — 1024.0 |
| `guardians.armorAndToughnessScale` | `0.10` | 0.0 — 10.0 |
| `guardians.doubleJumpVelocity` | `0.52` | 0.0 — 10.0 |
| `guardians.airDashSpeed` | `1.15` | 0.0 — 10.0 |
| `ulv.unreversedAttackSpeedMultiplier` | `0.80` | 0.0 — 10.0 |
| `ulv.reversedAttackSpeedMultiplier` | `1.20` | 0.0 — 10.0 |
| `ulv.backlashMaxHealthRatio` | `0.05` | 0.0 — 10.0 |
| `ulv.followUpDamageRatio` | `0.50` | 0.0 — 20.0 |
| `ulv.maxComboLayers` | `5` | 1 — 100 |
| `ulv.comboWindowTicks` | `100` | 1 — 72000 |
| `ulv.damageBonusPerLayer` | `0.10` | 0.0 — 10.0 |
| `eleine.magicDamageMultiplier` | `1.30` | 0.0 — 10.0 |
| `eleine.unreversedSwimSpeedMultiplier` | `0.65` | 0.0 — 10.0 |
| `eleine.orbChance` | `0.30` | 0.0 — 1.0 |
| `eleine.orbDamageRatio` | `0.45` | 0.0 — 20.0 |
| `eleine.waterBreathingTicks` | `200` | 1 — 72000 |
| `eleine.drownedKillsRequired` | `3` | 1 — 1000 |
| `hoenir.negativeEffectsRequired` | `3` | 1 — 255 |
| `hoenir.markDurationTicks` | `200` | 1 — 72000 |
| `hoenir.regenerationMaxHealthRatioPerSecond` | `0.05` | 0.0 — 10.0 |
| `hoenir.ferinMarkedDamageMultiplier` | `1.50` | 0.0 — 20.0 |
| `hoenir.reversedHarmfulDurationMultiplier` | `0.50` | 0.0 — 10.0 |
| `hoenir.unreversedAmplifierIncrease` | `1` | 0 — 255 |
| `faden.knockbackResistancePenalty` | `0.15` | 0.0 — 1.0 |
| `faden.poisonChance` | `0.20` | 0.0 — 1.0 |
| `faden.poisonDurationTicks` | `400` | 1 — 72000 |
| `faden.poisonAmplifier` | `0` | 0 — 255 |
| `faden.fortuneAndLootingBonus` | `1` | 0 — 255 |
| `compatibility.guardianMalumSoulHarvest` | `true` | true / false |
| `whiteWitchRelics.enabled` | `true` | true / false |
| `whiteWitchRelics.allowDuplicates` | `false` | true / false |
| `whiteWitchRelics.globalEffectScale` | `1.0` | 0.0 — 100.0 |
| `whiteWitchRelics.globalCooldownScale` | `1.0` | 0.01 — 100.0 |

## purified_undead-slate.toml

| 键 | 声明默认值 | 范围 |
| --- | --- | --- |
| `slate.cipherChestChance` | `.25` | 0 — 1 |
| `slate.grothSplashDamage` | `.5` | 0 — 10 |
| `slate.juliusSprintDamage` | `.2` | 0 — 10 |
| `slate.juliusDamagePerSpeedStep` | `.05` | 0 — 10 |
| `slate.eleineSwimBonus` | `.5` | 0 — 10 |
| `slate.eleineOrbChance` | `1.0` | 0 — 1 |
| `slate.eleineOrbDamageFraction` | `.9` | 0 — 10 |
| `slate.hoenirStatusChance` | `.25` | 0 — 1 |
| `slate.hoenirStatusTicks` | `100` | 1 — 12000 |
| `slate.sistersBuffTicks` | `1000` | 1 — 72000 |
| `slate.sistersCooldownTicks` | `3600` | 1 — 72000 |
| `slate.revision` | `0` | 0 — 100 |
| `slate.ferinStageDamageBonus` | `1.5` | 0 — 10 |
| `slate.shiningGuardianHealthBonus` | `1.0` | 0 — 10 |
