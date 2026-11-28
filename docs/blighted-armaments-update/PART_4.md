# 秽鬼武装第四步 · 无垢工具

开发版本 `1.3.0-armaments.4`，Forge 1.20.1 与 NeoForge 1.21.1。

五件工具使用净化弧钢锻造模板、对应息金工具和一份净化弧钢进行原版锻造。默认采掘等级、速度、附魔能力和攻击速度随对应钻石工具，攻击伤害各增加 1，耐久均为 2031。图标与手持沿用原版工具的显示方式，贴图为 32×32 PNG，逻辑网格为 16×16。

| 工具 | 服务端行为 |
| --- | --- |
| 无垢剑 | 直接近战造成伤害后，从目标当前生效的有益状态中移除一项，并在该实体上永久封禁该效果 ID。每次命中最多一项。有害和中性状态保留。 |
| 无垢锹 | 挖泥土或草方块时有 30% 概率额外获得 1 至 4 个污秽碎片。 |
| 无垢斧 | 砍原木每块修复 1 点，满耐久时维持满值。木板按普通工具方式磨损。 |
| 无垢锄 | 破坏成熟作物时有 30% 概率额外结算一次该方块的掉落表。两次都用实际持锄的时运。 |
| 无垢镐 | 挖掘可正确采收的矿石时有 30% 概率额外结算一次掉落表，两次均受时运影响。额外结果会过滤矿石方块，所以精准采集只能取回一份矿石。 |

### 无垢剑与模组兼容

封禁保存在目标实体的持久数据中，以带命名空间的效果 ID 为键。重载实体后继续生效，玩家重生时复制封禁记录。攻击时只检查当前效果，找到一个有益效果就停止。重施时按键查询，无逐刻巡查、全局实体缓存或全局效果注册表修改。如果其他模组阻止移除某个效果，仍在生效的效果会撤销本次新封禁。

Forge 订阅效果适用性事件，以及 L2Library 的 `ForceAddEffectEvent`。NeoForge 订阅对应的适用性事件和 L2Core 的强制事件。可选事件类只在注册时解析，游戏内查询不使用反射。整合包中直接修改其他模组私有状态、完全绕过效果和 L2 强制事件的能力，仍由其自身逻辑决定。

### 配置与 KubeJS

两端修改 `config/purified_undead-immaculate-tools.properties` 后重启。可设置 `durability`（1 至 1000000）、`extraDamage`（相对钻石，0 至 1024）、`logRepair`（0 恢复正常磨损）、`swordSeal`（0 停止产生新封禁，1 启用），以及 `soilChance`、`cropChance`、`oreChance`（0 至 1）。

物品标签为 `purified_undead:immaculate_tools`。五个原版锻造配方 ID 与各自产物相同，可通过 `ServerEvents.recipes` 替换。方块标签 `purified_undead:immaculate_logs`、`immaculate_soils`、`immaculate_crops` 和 `immaculate_ores` 均在同一命名空间。原木继承 `minecraft:logs`，作物继承 `minecraft:crops`，矿石继承 Forge 的 `forge:ores` 或 NeoForge 的 `c:ores`，并显式接入息金矿。作物使用原版成熟判定或整数 `age` 属性的最大值；标签中的无年龄方块由整合包界定其可收获状态。通过方块掉落表采收时才结算额外收获。

```js
// kubejs/server_scripts
ServerEvents.tags('block', event => {
  event.add('purified_undead:immaculate_ores', 'example:silver_ore')
})
const SPU = Java.loadClass('dev.purifiedundead.api.PurifiedUndeadScripts')
// 在服务端事件中查询该实体的封禁记录
function speedSealed(entity) {
  return SPU.immaculateEffectSealed(entity, 'minecraft:speed')
}
```

### 开发回归

两版验证五种实际锻造输入、耐久、黑曜石采收、砍原木和木板的耐久差异、成熟作物与时运矿产的双次结算、幼苗及精准采集。剑的回归分别检查单次命中、持续封禁、保存加载、其他实体和重复施加效果。可选联动组使用真实 L2Library/L2Core 强制写入入口以及 L2 Hostility 的 `SelfEffectTrait.tick`。服务端标记为 `IMMACULATE_TOOLS_OK`、`IMMACULATE_L2_FORCE_OK`、`IMMACULATE_L2_TRAIT_OK` 和 `IMMACULATE_KUBE_OK`。本地运行记录在 `outputs/armaments-4/`。
