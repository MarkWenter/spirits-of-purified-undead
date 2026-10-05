# 可选亡灵联动 · 0.46.0

针对用户提供的 261 模组清单扩展污秽碎片来源与莉莉手记识别。Forge 1.20.1、NeoForge 1.21.1 均实现；未安装对应模组时跳过可选注册 ID，不要求安装任何新增前置，也不修改第三方模组自身。

## 规则

- 新增共享标签 `purified_undead:compatible_undead`，被污秽碎片来源和莉莉手记亡灵标签共同引用。所有第三方 ID 均 `required: false`；原有铁魔法、生于混沌、灾变条目保留并补齐碎片来源。
- 碎片额外自动识别非原版的敌对亡灵：实体实现原版 Enemy，且 Forge 返回 MobType.UNDEAD，或 NeoForge 加入 minecraft:undead 标签。这样正确声明类型的移植版或整合包新增怪物无需逐个硬编码。
- 不按模组命名空间整体放行，也不根据名称包含 ghost、skeleton、undead 等字样猜测。原版碎片来源仍是原先七类，不顺便让原版凋零、幻翼、亡灵马掉落。
- 沿用现有玩家击杀条件、掉落概率配置（当前默认 35%）、基础 1—8 碎片和已有抢夺处理。显式标签与自动识别只进行一次掉落判定，不重复叠加。
- 莉莉手记仍须实际佩戴；按完整实体注册 ID 累计一次，保存玩家原有记录与数值，不清空历史。同一实体不同外观不重复计种类。
- 碎片排除标签优先级最高，默认排除 alexsmobs:murmur_head、alexsmobs:bone_serpent_part；不更改原有手记对原生亡灵的识别规则。

## 已核实的可选名单

共 101 个实体 ID（含既有兼容）。下面列的是选择性名单，不代表整个模组的全部怪物都是亡灵。

| 模组 ID | 实体 ID（省略前缀） |
| --- | --- |
| alexscaves | `brainiac`, `luxtructosaurus` |
| alexsmobs | `bone_serpent`, `murmur`, `skelewag`, `soul_vulture` |
| aquamirae | `captain_cornelia` |
| block_factorys_bosses | `frozen_skeleton`, `dragon_guard_sword`, `flaming_skeleton_guard_sword`, `flaming_skeleton_guard_fireball`, `soul_skeleton`, `soul_knight_wither_skeleton`, `pirate_captain`, `pirate_rook`, `crossbow_pirate` |
| born_in_chaos_v1 | `barrel_zombie`, `bonescaller`, `bonescaller_not_despawn`, `decaying_zombie`, `decaying_zombie_not_despawn`, `decrepit_skeleton`, `door_knight`, `door_knight_not_despawn`, `fallen_chaos_knight`, `skeleton_demoman`, `skeleton_thrasher`, `skeleton_thrasher_not_despawn`, `zombie_bruiser`, `zombie_clown`, `zombie_clown_not_despawn`, `zombie_fisherman`, `zombie_lumberjack` |
| bosses_of_mass_destruction | `lich` |
| bygonenether | `corpor`, `wither_skeleton_knight`, `wraither` |
| cataclysm | `ancient_remnant`, `aptrgangr`, `draugr`, `drowned_host`, `elite_draugr`, `ignited_berserker`, `ignited_revenant`, `kobolediator`, `koboleton`, `maledictus`, `royal_draugr`, `wadjet` |
| eeeabsmobs | `corpse`, `corpse_villager`, `corpse_warlock`, `immortal_archer`, `immortal_knight`, `immortal_mage`, `immortal_skeleton`, `immortal_warrior` |
| eternalnether | `corpor`, `wither_skeleton_knight`, `wraither` |
| goety | `apostle`, `bone_lord`, `border_wraith`, `cairn_necromancer`, `crypt_slime`, `frayed`, `haunted_armor`, `hostile_skeleton_pillager`, `hostile_zombie_vindicator`, `mossy_necromancer`, `muck_wraith`, `necromancer`, `rattled`, `reaper`, `skull_lord`, `wight`, `wither_necromancer`, `wraith` |
| graveyard | `corrupted_pillager`, `corrupted_vindicator`, `ghoul`, `lich`, `revenant`, `skeleton_creeper`, `wraith` |
| iceandfire | `dread_beast`, `dread_ghoul`, `dread_knight`, `dread_lich`, `dread_scuttler`, `dread_thrall`, `ghost` |
| irons_spellbooks | `necromancer` |
| meetyourfight | `bellringer`, `swampjaw` |
| twilightforest | `knight_phantom`, `lich`, `lich_minion`, `rising_zombie`, `skeleton_druid`, `wraith` |

墓园腐化灾厄村民、EEEAB 不朽骷髅、暮色巫妖／幻影骑士等通过显式名单补齐类型未标准声明的情况。不把墓园侍僧、普通灾厄村民、魔像、深暗生物、普通龙和野兽统一视为亡灵。Goety 奴仆、诡厄启示继承型实体等若正确声明敌对亡灵类型，可由自动识别处理；本次未为了凑名单额外列入所有召唤物。

## 1.21.1 与移植版边界

两版均携带这些可选 ID；只有实际安装并注册相同 ID 的实体才会匹配。此行为不意味着所有列表内模组都有 1.21.1 版本。已核对 Aquamirae 1.21.1 的 captain_cornelia；Bygone Nether 的后续版本 Eternal Nether 使用 eternalnether 前缀，三个对应实体均单独加入。其他移植版若改名，标准亡灵类型可走自动识别；若连类型声明也缺失，需要补充该移植版的实际 ID。

整合包专用的 thefool 系列、ydtwo、ruokmod 等目前只有模组名称，无法据此证明其全部怪物语义。Travel Optics、专用修正包以及未取得对应源码/JAR的扩展内容，不宣称已经逐个实体核实；可由标准类型识别覆盖的会生效，其余需提供实际 JAR／实体 ID 后补表。

## 配置与数据包

通用配置 `contract.autoDetectModdedUndeadDrops` 默认 true。关闭只停用自动识别，显式名单仍有效。原有 `contract.blightFragmentDropChance` 控制所有这些来源的掉率。

数据包可追加 `data/purified_undead/tags/entity_types/compatible_undead.json`（1.20.1），1.21.1 将目录改为 `entity_type`：

```json
{"replace":false,"values":[{"id":"your_mod:your_undead","required":false}]}
```

仅加碎片来源可编辑 `blight_fragment_sources.json`，仅加手记识别可编辑 `lily_undead.json`。要禁止某实体掉碎片，将其加入 `blight_fragment_exclusions.json`；它优先于自动和显式来源。修改数据包后重新加载／重启。无需安装该示例中的虚构模组。

## 核对依据

仅读取实体注册、类型／继承与作者提供的标签，不复制第三方实现到本模组。

- [墓园源码](https://github.com/finallion/The-Graveyard-FORGE/tree/1.20/src/main/java/com/finallion/graveyard)
- [Goety 源码](https://github.com/Polarice3/Goety-2/tree/1.20/src/main/java/com/Polarice3/Goety/common/entities)
- [EEEAB 源码](https://github.com/EEEAB/EEEABsMobs/tree/master/src/main/java/com/eeeab/eeeabsmobs/server)
- [灾变源码](https://github.com/lender544/new1.20.1/tree/master/src/main/java/com/github/L_Ender/cataclysm)
- [Alex 生物源码](https://github.com/AlexModGuy/AlexsMobs/tree/1.20/src/main/java/com/github/alexthe666/alexsmobs/entity)
- [Alex 洞穴源码](https://github.com/AlexModGuy/AlexsCaves/tree/main/src/main/java/com/github/alexmodguy/alexscaves/server/entity)
- [冰火源码](https://github.com/AlexModGuy/Ice_and_Fire/tree/1.20/src/main/java/com/github/alexthe666/iceandfire/entity)
- [暮色源码](https://github.com/TeamTwilight/twilightforest/tree/1.20.x/src/main/java/twilightforest/entity)
- [Aquamirae 源码](https://github.com/ObscuriaLithium/Aquamirae/tree/1.21.1)
- [Bosses of Mass Destruction Forge 源码](https://github.com/CERBON-MODS/Bosses-of-Mass-Destruction-FORGE/tree/1.20.1)
- [Meet Your Fight 源码](https://github.com/Lykrast/MeetYourFight)
- [Bygone Nether 源码](https://github.com/izofar/bygone-nether/tree/1.20.1)、[Eternal Nether 1.21.1](https://github.com/Fuzss/eternal-nether/tree/1.21.1)
- [Bosses’Rise 官方分发](https://modrinth.com/mod/bossesrise)：静态读取 2.1.2 Forge JAR 的 minecraft:skeletons 标签，确认九个实体。
- [Goety Revelation 官方分发](https://modrinth.com/mod/goety-revelation)：检查 2.3.4fix 资源；清单为 2.3.3fix，未冒称逐个验证其专用实体。

## 验证范围

两版执行构建、既有单元测试及独立开发服务端完整回归。新增 `UNDEAD_COMPAT_OK` 使用仅在测试环境注册的第三方命名空间实体，验证原生亡灵、非原生但被显式标记的亡灵、活体反例、掉落排除优先级、自动识别开关、实际 LivingDrops 事件、手记佩戴与去重、不存在的可选 ID 安全加载。测试不会进入生产 JAR。

这属于代码／注册资料核对及可选兼容机制运行验证，未将 261 个模组一起启动，也未对每一种第三方怪物完成游戏内击杀测试。白巫女手记、美术和既有战斗数值未修改。
