# 净化更新 · 第二部分（0.39.0）

Forge 1.20.1 与 NeoForge 1.21.1 同步实现。白巫女手记暂不更新。

## 物品效果

| 物品 / 注册名 | 效果 |
| --- | --- |
| 洁白莲花 `white_lotus` | 持续使用 30 刻（1.5 秒），消耗一个，恢复最大生命值的 50%，不超过生命上限；不受饱食度限制。 |
| 猩红莲花 `scarlet_lotus` | 持续使用 30 刻，消耗一个，给予 50 秒力量 IV。 |
| 纯净禁果 `pure_forbidden_fruit` | 必须实际佩戴古老民族之契才能食用。每名玩家只能获得一次奖励：永久增加 1 个白巫女饰品槽和 1 个流浪日志槽。死亡、重登、移除契约均保留。流浪日志饰品尚未加入。 |
| 纯净触碰 `pure_touch` | 材料；灰色描述：Amidst a Collapsed world, her words echo out |
| 秽鬼之守护 `blighted_guardian` | 剑，耐久 7989，基础总攻击伤害 23、攻击速度 2.2，支持横扫、普通及兼容的模组剑类附魔，自带经验修补，拒绝诅咒。灰色描述：Thank you for finding your way to me. |

禁果无契约时显示深红色“你无力净化这份污秽……”，重复食用被拒绝，不扣除物品。使用中途移除契约也不会结算。创造模式保留原版不消耗物品惯例，但一次性解锁限制不变。

剑的 23 是含玩家基础 1 点的总值，物品属性为 +22；2.2 是含基础 4 的总值，物品属性为 -1.8。经验修补在合成产出中直接存在，并在服务器背包更新时恢复、清除外部强加的诅咒。磨石不能永久移除其固有修补。普通剑类附魔仍受原版冲突规则约束。

## 无垢护甲

| 部位 / 注册名 | 耐久 | 护甲 | 韧性 | 击退抗性 | 附加减伤 |
| --- | ---: | ---: | ---: | ---: | --- |
| 头甲 `immaculate_helmet` | 667 | 3 | 3 | 1.0 | 弹射物 35% |
| 胸甲 `immaculate_chestplate` | 898 | 8 | 3 | 1.0 | 魔法 70% |
| 腿甲 `immaculate_leggings` | 847 | 6 | 3 | 1.0 | 火焰 35% |
| 足甲 `immaculate_boots` | 738 | 3 | 3 | 1.0 | 冻结 35% |

击退抗性按需求字面使用 1.0（即单件已达到原版 100% 上限）。伤害分类使用原版伤害类型标签，魔法使用 `purified_undead:immaculate_magic`，包含原版魔法、间接魔法、凋零、龙息及可选公共魔法标签；其他模组可通过数据包扩充该标签。多种标签同时命中时减伤相乘。绕过无敌的特殊伤害不受影响。

四件装备仍是真正的护甲，保留穿戴渲染、耐久、附魔和正常护甲计算。针对“无甲判定”的兼容如下：

- Forge 神秘遗物 2.30.1：使用其公开 `GolemHeart.EXCLUDED_ARMOR` 排除列表。
- NeoForge 神秘遗物+ 1.0.0：使用其 `enigmaticlegacyplus:armor_check_exclusions` 物品标签。
- 两版均实装验证：全套无垢护甲不阻止魔像之心的无甲判定，换上普通铁头盔则正常阻止。
- 不强制安装上述模组。其他模组若直接检查装备栏是否为空，仍会看到无垢护甲；不能在不破坏原版穿戴系统的情况下统一伪装所有此类检查。

参考实现：[Forge 排除列表](https://github.com/Aizistral-Studios/Enigmatic-Legacy/blob/1.20.X/src/main/java/com/aizistral/enigmaticlegacy/items/GolemHeart.java)、[NeoForge 无甲判定](https://github.com/Auviotre/Enigmatic-Legacy-Plus/blob/main/src/main/java/auviotre/enigmatic/legacy/handlers/EnigmaticHandler.java)。

## 配方

下列均为有序配方，空表示空槽，每次产出 1 个。

洁白莲花：
```text
空    纯净结晶 空
骨粉  睡莲     骨粉
空    骨粉     空
```
猩红莲花：
```text
空     怨念污秽 空
红石粉 睡莲     红石粉
空     红石粉   空
```
纯净禁果：
```text
钻石     净化弧钢   钻石
净化弧钢 附魔金苹果 净化弧钢
净化弧钢 金块       净化弧钢
```
纯净触碰：
```text
净化弧钢 纯净结晶 净化弧钢
怨念污秽 下界之星 怨念污秽
净化弧钢 纯净结晶 净化弧钢
```
秽鬼之守护：
```text
空 下界合金锭 空
空 下界合金锭 空
空 纯净触碰   空
```
四件护甲分别使用锻造台：净化弧钢锻造模板 + 对应下界合金护甲 + 净化弧钢。消耗模板与材料，保留原装备损耗、自定义名称、附魔等原版锻造数据。护甲与剑可以使用净化弧钢维修。

## 美术

净化弧钢采用更宽、连续扭转的金蓝纹路。所有新增图案由内置 imagegen 基于用户参考图制作。物品图为透明像素贴图，通常 64×64，长剑为 128×128 保留细长刃与水晶细节。剑使用 `minecraft:item/handheld`，由原版生成带侧面厚度的模型，客户端验证厚度为 1/16 方块。

穿戴护甲有独立的 64×32 两层 UV 贴图，按用户此前许可用像素脚本精确对齐原版模型。原始生成图、规范化预览保存在本机 `art/purification-update/part2/`；两版最终资源保存在各自 `src/main/resources/assets/purified_undead/textures/`。提示词见 [art-prompts-039.json](art-prompts-039.json)。
