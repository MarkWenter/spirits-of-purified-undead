# 秽鬼武装第二部分 · 息金配置与 KubeJS

适用于 Forge 1.20.1 和 NeoForge 1.21.1，开发版本 `1.3.0-armaments.2`。首次启动会生成 `config/purified_undead-breathgold.properties`。将同一份文件分发给客户端与服务器，修改后重启。工具构造时会固定耐久及攻击修饰，游戏内的配方重载不会重新构造物品。

| 配置键 | 默认值 | 含义和范围 |
| --- | --- | --- |
| conversionTicks | 200 | 连续有效污秽降水游戏刻，1–1,200,000。默认约 10 秒。 |
| materialStackSize、oreStackSize | 各 64 | 息金材料与矿石物品的堆叠上限，各 1–64。 |
| toolDurability | 375 | 五件工具的耐久，1–1,000,000。 |
| miningSpeed | 6 | 工具对有效方块的基础采掘速度，0–1024。 |
| enchantability | 14 | 附魔能力，0–255。 |
| tierDamageBonus | 2 | 息金工具材质的攻击加值，0–1024。 |
| 各工具的 .damage | 剑 4、斧 6、锄 −2、镐 1、锹 1.5 | 与材质攻击加值相加，范围 −2–1024；锹支持小数。 |
| 各工具的 .attackSpeed | 剑 −2.4、斧 −3.1、锄 −1、镐 −2.8、锹 −3 | 在玩家基础攻速上加算，范围 −3.99–16。 |

键名前缀依次为 `sword`、`axe`、`hoe`、`pickaxe`、`shovel`。例如 `sword.damage=6` 配合默认材质加值，物品提供 +8 攻击伤害。缺项沿用默认值；超界值和非有限数值按项回退并记录日志。运行中的矿石追踪使用已读取的阈值。原铁级采掘资格、降水判定、位置存档和一次性进度逻辑沿用已有实现。

## 整合包脚本

KubeJS 是可选前置。两版都支持通过 `Java.loadClass` 加载 `dev.purifiedundead.api` 下的接口。以下示例放在整合包的 `kubejs/server_scripts/purified_undead.js`。默认修复标签只含息金；示例让铜锭也能修复同一套工具。

```js
const SPU = Java.loadClass('dev.purifiedundead.api.PurifiedUndeadScripts')
const Progress = Java.loadClass('dev.purifiedundead.api.ProgressApi')
const Warrior = Java.loadClass('dev.purifiedundead.api.WarriorId')

ServerEvents.tags('item', event => {
  event.add('purified_undead:breathgold_repair_materials', 'minecraft:copper_ingot')
})

PlayerEvents.loggedIn(event => {
  const player = event.player
  const level = player.serverLevel()
  const state = Progress.snapshot(player)
  console.info('护身符等级 ' + state.talismanLevel()
    + '，契约佩戴 ' + SPU.contractEquipped(player)
    + '，费林佩戴 ' + SPU.warriorEquipped(player, Warrior.FERIN)
    + '，费林追忆 ' + SPU.memoryActive(player, 'ferin'))
  console.info('污秽雨 ' + SPU.rainActive(level)
    + '，当前位置暴露 ' + SPU.exposedToBlightedRain(level, player.blockPosition())
    + '，息金所需刻数 ' + SPU.breathgoldConversionTicks())
})
```

普通工作台配方使用 `ServerEvents.recipes`，例如 `event.remove({ id: 'purified_undead:breathgold_sword' })`。实体归属和污秽雨适配可使用现有实体类型标签，例如 `purified_undead:compatible_undead`、`purified_undead:blighted_rain_immune`。涉及掉落时使用对应版本的战利品表格式。

纯化铸造器使用独立配方文件 `config/purified_undead-foundry.json`。在服务器线程调用 `SPU.foundryResult(level, topStack, bottomStack)`，可按当前配方和输入数量预览一批产物；无匹配时返回空栈。查询不涉及燃料、耗时、消耗或发奖。更新铸造规则可使用原有的严格重载命令 `/purifiedundead config reload foundry`。

### 任务奖励与通知

任务条件应由服务端脚本判断，通过后调用 `Progress.grantWarrior(player, Warrior.GROTH)` 或 `Progress.grantContract(player)`。返回值区分已发放、等待补发、已领取。进度重置、护身符等级和不可变快照见[进度 API v1](../ADDON_API.md)。脚本更改进度会沿用本体的主线程检查、奖励去重、保存和通知。

以下订阅放在 `kubejs/startup_scripts`。Forge 1.20.1 使用 `ForgeEvents`，NeoForge 1.21.1 使用 `NativeEvents`。回调收到的是保存后的通知；需要触发另一次进度操作时，在下一游戏刻重新判断条件。

```js
const ProgressChanged = Java.loadClass('dev.purifiedundead.api.ProgressChangedEvent')
function onProgress(event) {
  console.info('护身符 ' + event.before().talismanLevel() + ' → ' + event.after().talismanLevel())
}
// Forge 1.20.1
ForgeEvents.onEvent(ProgressChanged, onProgress)
// NeoForge 1.21.1 使用下行替换上一行
// NativeEvents.onEvent(ProgressChanged, onProgress)
```

资源类型、脚本目录与 Java 类过滤机制见 [KubeJS 官方开发说明](https://kubejs.com/wiki/other/source-code)。

## 本轮验证

Forge 使用 KubeJS 2001.6.5-build.16，NeoForge 使用 2101.7.2-build.381。两版各运行有 KubeJS（自定义配置）和无 KubeJS（默认配置）的隔离服务端。自定义组实测第 36/37 刻的转化边界、481 耐久、32 个材料堆叠、+8 剑攻击修饰、脚本添加的铜锭修复标签和非法采掘速度 NaN 的单项回退。脚本读取了玩家进度、契约、战士、追忆、雨状态与铸造匹配结果，并收到实际进度变更事件。服务器日志分别核对 SPU_KUBE_RUNTIME_OK、SPU_PROGRESS_EVENT_OK、BREATHGOLD_SERVER_OK 及 PURIFICATION_SERVER_OK。
