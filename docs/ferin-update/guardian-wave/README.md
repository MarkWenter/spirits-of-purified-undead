# 秽鬼之守护 · 挥剑剑气

`1.2.0-ferin.23`。下表是秽鬼之守护每次挥剑可能显现的五种外观。紫色光迹从武器实际运动的部位引出，活动部位表面的光纹与尾迹使用同一套紫、粉配色。[五款模型](models.png) · [PixelFX 飞行预览](flight-preview.gif) · [Forge 实机动图](gameplay.gif)

| 武器 | 飞行与光迹 | Blockbench 工程 |
| --- | --- | --- |
| 费林的剑 | 原有剑模型翻转飞出，刃尖与中段扫出两层连在刃口上的紫色拖影。 | [费林的剑](../ferin_sword.bbmodel) |
| 西丽德的链锤 | 球头先行、链节拉直。球面有断续的光纹，三股紫烟从球头后侧拖开。 | [西丽德的链锤](siegrid_flail.bbmodel) |
| 西丽亚的巨锤 | 双面巨锤翻转，两片锤唇各自留下弧形残光。 | [西丽亚的巨锤](silva_hammer.bbmodel) |
| 格洛特的战锤 | 八棱锤头正向砸出，前后两圈锤箍的棱角拖出长短错开的光带。 | [格洛特的战锤](groth_maul.bbmodel) |
| 尤里乌斯的战枪 | 枪尖直进，细长的紫光贴着两侧刃线和中央枪脊，经枪肩向后延伸。 | [尤里乌斯的战枪](julius_lance.bbmodel) |

四个新工程均内嵌 64×64 贴图。费林的剑直接引用既有的网格与图集。剑气实体的渲染入口为 `GuardianWaveRenderer`，一次只绘制选中的武器。客户端用已同步的持有者 ID 和挥剑编号等权选取外观，本地预测和旁观客户端据此取得相同结果。没有预测编号的服务端攻击使用已同步的实体 ID。模型沿用原来剑气的俯仰、朝向和随机倾角。

`textures/effect/guardian_wave/surface_light.png` 的五个图块对应武器受光部位的 UV。`wakes.png` 每行属于一种武器，包含八个演变阶段。旋转武器的拖影按刃口或锤面在较早时刻的位置生成。直进武器的尾迹以球面、锤箍或枪锋为起点，随剑气实体一起消失。费林协同攻击由自己的渲染入口负责。[费林协同攻击记录](../DYNAMIC_SLASH.md)

两版分别用成品 JAR 进入开发世界，进行资源重载、本地挥剑预测、服务器确认与旁观视角测试。五种形态各抽取两种原有倾角拍摄。专用服务器检查了原有飞行距离、逐刻位移、伤害、墙体截断和重复包处理。与 ferin.22 的两个成品逐文件比较，旧游戏类中只有 `GuardianWaveRenderer.class` 发生改变。[ferin.22 的剑气渲染实现](https://github.com/MarkWenter/spirits-of-purified-undead/blob/d331072be7076eb5ba88b4fadf4b0b0dc2db4703/src/main/java/dev/purifiedundead/client/GuardianWaveRenderer.java)
