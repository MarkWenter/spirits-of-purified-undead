# 秽鬼武装第三步 · 息金锭与甲胄

开发版本 `1.3.0-armaments.3`，适用 Forge 1.20.1 和 NeoForge 1.21.1。

## 获取与锻造

熔炉用 200 刻、高炉用 100 刻将一份息金烧成一个息金锭，每份提供 1 点熔炼经验。工作台也可在正中放一份怨念污秽，周围八格各放一个金锭，一次合成八个息金锭。五种息金工具和四件息金甲胄采用原版对应形状，金属材料为息金锭。无垢甲胄在锻造台以相应部位的息金甲胄为底材，沿用原来的升级模板和追加材料。

| 部位 | 默认防御 | 默认耐久 |
| --- | ---: | ---: |
| 头盔 | 2 | 248 |
| 胸甲 | 6 | 360 |
| 腿甲 | 5 | 338 |
| 靴子 | 2 | 293 |

甲胄采用铁甲的防御、韧性和击退抗性，耐久为铁甲的 1.5 倍，遇半点向上取整。默认附魔能力为工具 22、甲胄 25。

## 配置与 KubeJS

两端改动 `config/purified_undead-breathgold.properties` 后重启。使用 `ingotStackSize`、`toolEnchantability`、`armorEnchantability`，以及每个部位的 `.durability` 和 `.defense`（例如 `helmet.durability=248`）。旧配置缺项使用新默认值。旧版 `enchantability` 的非默认值继续生效；若整合包需要保留旧默认值 14，可明确设置 `toolEnchantability=14`。

物品可用稳定的 `purified_undead:breathgold_ingot` 和 `purified_undead:breathgold_<部位>` 访问，部位为 `helmet`、`chestplate`、`leggings`、`boots`。四件装备归入物品标签 `purified_undead:breathgold_armor`。修复沿用 `purified_undead:breathgold_repair_materials`，默认包含息金与息金锭。两版的 KubeJS 服务端脚本均可按配方 ID 替换合成、烧炼或锻造配方。例如：

```js
ServerEvents.recipes(event => {
  event.remove({ id: 'purified_undead:breathgold_ingot_from_blasting' })
})
```

## 美术与验证

息金锭及四件甲胄图标为 32×32 PNG，每个原版逻辑像素占 2×2 个同色像素。穿戴层使用原版 64×32 UV。息金斧和锄的图标与手持模型共用水平镜像后的贴图。

独立服务端回归覆盖十种工作台配方、两种烧炼、四件无垢甲胄锻造及自定义名称和损伤保留。两个版本均使用默认配置和安装 KubeJS 的自定义配置测试。KubeJS 脚本检查新锭、头盔耐久和甲胄标签。日志位于本地 `outputs/armaments-3/`。
