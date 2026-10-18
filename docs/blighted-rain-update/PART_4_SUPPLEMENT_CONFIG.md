# 独立饰品配置

文件：游戏实例的 `config/purified_undead-accessories.toml`。默认值保留原玩法。建议退出游戏/服务器后编辑并重启；联机双方使用相同版本，玩法结算以服务器为准。比例1.0=100%，20刻=1秒，效果等级放大器0=I级、1=II级。

首次升级自动迁移旧 `purified_undead-common.toml` 的饰品章节及旧 slate 文件中的闪耀守护宝具生命加成；保留 `*.before-accessories.toml` 备份。已有 accessories 文件优先，不会反复覆盖。其余 common 的槽位、获取路线和材料设置继续有效，石板记忆技能参数仍在 `purified_undead-slate.toml`。

可用 `/purifiedundead config` 下的 accessories 分组查询；TOML修改按重启生效处理。不要把示例文件覆盖到已经定制过的配置上。

|章节|对象|
|---|---|
|talisman|污秽护身符、洁白护身符成长及费林阶段解锁|
|ferin / groth / julius / guardians / ulv / eleine / hoenir / faden|对应八组战士（古老民族之契触发的协同及各自污秽/逆转加护）|
|whiteWitchRelics|遗物总开关、叠加、全局效果和冷却倍率|
|individual.blighted_finger / ancient_dragon_claw / weathered_warrior_necklace / bloodstained_ribbon / soiled_silver_rosary / kings_shield_badge|六件常规遗物的各自加成|
|individual.white_priestess_statue / white_priestess_earrings|两件保命遗物的各自冷却|
|individual.statue / earrings|两件保命遗物的救命血量和附加效果|
|individual.lily_diary|莉莉手记每种亡灵的暴击/幸运增长|
|individual.shining_guardian_treasure|闪耀的守护宝具生命加成|

## 完整键与默认值

共 81 项。下表来自最终发布包首次启动生成的默认文件。

|键|默认值|范围|
|---|---|---|
|`talisman.maxLevel`|`15`|0 ~ 100|
|`talisman.blightedSpiritsPerLevel`|`4`|1 ~ 64|
|`talisman.baseIncomingDamageMultiplier`|`1.2`|0.0 ~ 10.0|
|`talisman.damageMultiplierReductionPerLevel`|`0.04`|0.0 ~ 1.0|
|`talisman.ferinFourthStageLevel`|`5`|0 ~ 100|
|`talisman.ferinFifthStageLevel`|`10`|0 ~ 100|
|`ferin.unreversedMeleeMultiplier`|`0.7`|0.0 ~ 10.0|
|`ferin.reversedMeleeMultiplier`|`1.3`|0.0 ~ 10.0|
|`ferin.lifestealRatio`|`0.2`|0.0 ~ 10.0|
|`ferin.stageDamageMultipliers`|`[0.75, 0.75, 0.65, 0.65, 1.25]`|按配置注释|
|`ferin.summonCooldownTicks`|`40`|0 ~ 12000|
|`ferin.comboLockTicks`|`10`|0 ~ 12000|
|`ferin.comboWindowTicks`|`24`|1 ~ 12000|
|`ferin.finalActionDurationTicks`|`16`|0 ~ 12000|
|`ferin.exitDurationTicks`|`4`|0 ~ 12000|
|`groth.unreversedIncomingDamageMultiplier`|`1.2`|0.0 ~ 10.0|
|`groth.reversedGroundIncomingDamageMultiplier`|`0.8`|0.0 ~ 10.0|
|`groth.unreversedMiningSpeedMultiplier`|`0.9`|0.0 ~ 10.0|
|`groth.criticalDamageBonus`|`0.5`|0.0 ~ 20.0|
|`groth.stunDurationTicks`|`60`|1 ~ 12000|
|`groth.stunCooldownTicks`|`200`|0 ~ 72000|
|`julius.unreversedFinalMeleeDamagePenalty`|`1.0`|0.0 ~ 100.0|
|`julius.unreversedReachPenalty`|`1.0`|0.0 ~ 16.0|
|`julius.reversedReachBonus`|`1.0`|0.0 ~ 16.0|
|`julius.unreversedSprintMultiplier`|`0.8`|0.0 ~ 10.0|
|`julius.reversedSprintMultiplier`|`1.2`|0.0 ~ 10.0|
|`guardians.armorFlatChange`|`5.0`|0.0 ~ 1024.0|
|`guardians.armorAndToughnessScale`|`0.1`|0.0 ~ 10.0|
|`guardians.doubleJumpVelocity`|`0.52`|0.0 ~ 10.0|
|`guardians.airDashSpeed`|`1.15`|0.0 ~ 10.0|
|`ulv.unreversedAttackSpeedMultiplier`|`0.8`|0.0 ~ 10.0|
|`ulv.reversedAttackSpeedMultiplier`|`1.2`|0.0 ~ 10.0|
|`ulv.backlashMaxHealthRatio`|`0.05`|0.0 ~ 10.0|
|`ulv.followUpDamageRatio`|`0.5`|0.0 ~ 20.0|
|`ulv.maxComboLayers`|`5`|1 ~ 100|
|`ulv.comboWindowTicks`|`100`|1 ~ 72000|
|`ulv.damageBonusPerLayer`|`0.1`|0.0 ~ 10.0|
|`eleine.magicDamageMultiplier`|`1.3`|0.0 ~ 10.0|
|`eleine.unreversedSwimSpeedMultiplier`|`0.65`|0.0 ~ 10.0|
|`eleine.orbChance`|`0.3`|0.0 ~ 1.0|
|`eleine.orbDamageRatio`|`0.45`|0.0 ~ 20.0|
|`eleine.waterBreathingTicks`|`200`|1 ~ 72000|
|`eleine.drownedKillsRequired`|`3`|1 ~ 1000|
|`hoenir.negativeEffectsRequired`|`3`|1 ~ 255|
|`hoenir.markDurationTicks`|`200`|1 ~ 72000|
|`hoenir.regenerationMaxHealthRatioPerSecond`|`0.05`|0.0 ~ 10.0|
|`hoenir.ferinMarkedDamageMultiplier`|`1.5`|0.0 ~ 20.0|
|`hoenir.reversedHarmfulDurationMultiplier`|`0.5`|0.0 ~ 10.0|
|`hoenir.unreversedAmplifierIncrease`|`1`|0 ~ 255|
|`faden.knockbackResistancePenalty`|`0.15`|0.0 ~ 1.0|
|`faden.poisonChance`|`0.2`|0.0 ~ 1.0|
|`faden.poisonDurationTicks`|`400`|1 ~ 72000|
|`faden.poisonAmplifier`|`0`|0 ~ 255|
|`faden.fortuneAndLootingBonus`|`1`|0 ~ 255|
|`whiteWitchRelics.enabled`|`true`|按配置注释|
|`whiteWitchRelics.allowDuplicates`|`false`|按配置注释|
|`whiteWitchRelics.globalEffectScale`|`1.0`|0.0 ~ 100.0|
|`whiteWitchRelics.globalCooldownScale`|`1.0`|0.01 ~ 100.0|
|`individual.shining_guardian_treasure.maxHealthBonus`|`1.0`|0.0 ~ 10.0|
|`individual.blighted_finger.damageBonus`|`0.25`|0.0 ~ 10.0|
|`individual.ancient_dragon_claw.projectileDamageBonus`|`1.35`|0.0 ~ 20.0|
|`individual.weathered_warrior_necklace.missingHealthHealingRatio`|`0.35`|0.0 ~ 1.0|
|`individual.bloodstained_ribbon.experienceBonus`|`0.25`|0.0 ~ 20.0|
|`individual.soiled_silver_rosary.maxHealthBonus`|`0.05`|0.0 ~ 10.0|
|`individual.kings_shield_badge.maxHealthBonus`|`0.25`|0.0 ~ 10.0|
|`individual.lily_diary.criticalChancePerUndeadType`|`0.03`|0.0 ~ 1.0|
|`individual.lily_diary.luckPerUndeadType`|`0.5`|0.0 ~ 100.0|
|`individual.white_priestess_statue.cooldownTicks`|`2400`|1 ~ 720000|
|`individual.white_priestess_earrings.cooldownTicks`|`12000`|1 ~ 720000|
|`individual.statue.RescueHealth`|`1.0`|0.0 ~ 2048.0|
|`individual.statue.RegenerationTicks`|`900`|0 ~ 720000|
|`individual.statue.RegenerationAmplifier`|`1`|0 ~ 255|
|`individual.statue.AbsorptionTicks`|`100`|0 ~ 720000|
|`individual.statue.AbsorptionAmplifier`|`1`|0 ~ 255|
|`individual.statue.FireResistanceTicks`|`800`|0 ~ 720000|
|`individual.earrings.RescueHealth`|`1.0`|0.0 ~ 2048.0|
|`individual.earrings.RegenerationTicks`|`900`|0 ~ 720000|
|`individual.earrings.RegenerationAmplifier`|`1`|0 ~ 255|
|`individual.earrings.AbsorptionTicks`|`100`|0 ~ 720000|
|`individual.earrings.AbsorptionAmplifier`|`1`|0 ~ 255|
|`individual.earrings.FireResistanceTicks`|`800`|0 ~ 720000|
