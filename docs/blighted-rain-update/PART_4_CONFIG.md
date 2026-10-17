# 配置键索引

1.1.0-blighted-rain.5 两版共用键名和默认值。以下为代码声明值；已有自定义数值不会由本索引覆盖。`balanceRevision` / `slate.revision` 是内部历史迁移标记，实际生成文件可能已更新，请勿手动归零。概率 0–1，倍率 1 表示 100%，20 ticks = 1 秒。具体应用方式见[管理说明](PART_4_COMMANDS.md)。

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

## 各分组作用

- contract：战士/白巫女遗物槽数、污秽残片掉率、模组亡灵识别、秽鬼化持续时间。
- acquisition：契约及八组战士的自然获取开关、狼的最短睡眠时间。
- talisman：护身符等级上限、每级材料数、承伤倍率及随等级降低幅度、费林第四/五段解锁等级。
- ferin：逆转前后近战倍率、吸血比例、五段伤害倍率、召唤冷却、连击锁定/输入窗口及收招时间。
- groth：逆转前后承伤、挖掘速度、暴击加成、眩晕持续和冷却。
- julius：近战最终伤害扣减、攻击距离变化、逆转前后疾跑倍率。
- guardians：护甲固定变化、护甲与韧性比例、二段跳速度、空中冲刺速度。
- ulv：逆转前后攻击速度、反噬生命比例、追击伤害比例、连击层数/窗口和每层伤害。
- eleine：魔法伤害、逆转前游泳速度、光球概率和伤害、水下呼吸时长、获取所需击杀数。兼容键 drownedKillsRequired 现在统计溺尸、秽鬼溺尸、秽鬼女巫混合击杀。
- hoenir：获取所需负面效果种类数、标记持续、每秒生命恢复比例、费林对标记目标倍率、负面效果持续倍率及逆转前等级增量。获取目标现为任意秽鬼。
- faden：抗击退扣减、中毒概率/时长/等级、时运与抢夺加成。
- compatibility：姐妹与 Malum 灵魂收获联动开关。
- whiteWitchRelics：遗物总开关、重复装备、全局效果/冷却倍率。
- slate：暗号宝箱概率；各战士追忆的范围伤害、疾跑/移速伤害、游泳/光球、异常状态、姐妹增益和冷却、费林每段伤害加成、闪耀守护宝具生命加成。

## purified_undead-foundry.json

文件在 config/；recipes 数组控制铸造配方。支持其他已注册模组的物品 ID。

| 字段 | 作用 | 有效值 |
| --- | --- | --- |
| top / bottom / output | 上槽、下槽、成品物品 ID | 已注册、非空气；最长256字符 |
| topCount / bottomCount | 每批次上槽/下槽原料数量 | 1–64 |
| outputCount | 每批次产出数量 | 1–成品最大堆叠数 |
| consumeTop | 是否消耗上槽 | 必须显式 true/false；false保留催化物 |
| leftFuel / rightFuel | 每批次左/右燃料数量 | 1–64 |
| ticks | 每批次加工时间 | 1–32767刻，20刻=1秒 |
| blightedRainPart3 | 静雨信标默认配方一次性迁移标记 | 内部字段，勿删除以免重新补配方 |
| _help | 文件说明 | 不影响加工 |

最多4096条，文件不超过8 MiB，并受900000字节估算同步上限约束。启动跳过坏条目；整份解析失败则禁用加工，库存不变。`/purifiedundead config reload foundry` 整份严格验证，任一错误则保留当前运行配方；显式空数组可清空配方。已有进行中的配方变更后重新计时。

## purified_undead-blighted-rain-immunity.json

`immuneEntityTypes`：实体注册ID字符串数组，默认 `[]`，例：`["minecraft:cow"]`。为列出的类型添加污秽之雨免疫；最多1024项，单ID最长256字符，文件不超过64 KiB。无效文件保留上次有效值。启动读取，当前没有专门的重载指令。

## 世界选项（不是 TOML 文件）

创建世界→游戏规则→更新→“是否启用永雨”，默认开启。内部游戏规则 `purifiedUndeadEternalRain`。关闭后使用正常天气；以关闭状态创建的新世界，每位玩家首次进入获得一个秽雨纹章，重登不重复。自然雨天依然可能发生污秽之雨。旧世界不追溯赠送。

## 当前不能直接通过配置修改的内容

自然污秽之雨概率、预警/持续/冷却、转化阈值、各秽鬼属性与新增掉落概率、纹章/少年耐久和恢复速度、盾牌护甲、食品数值目前是代码规则，不是已开放的配置项。普通合成/烧制配方、掉落表和实体标签可通过数据包修改，但不等同于上述配置文件。

读取依据：两版 config/PurifiedUndeadConfig.java、slate/SlateConfig.java、foundry/FoundryRecipes.java、blightedrain/RainImmunity.java、RainWorldOptions.java。原配置键与自定义值均保留。
