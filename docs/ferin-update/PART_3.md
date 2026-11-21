# 骑士寂夜 · 女仆虚影

Forge 1.20.1 与 NeoForge 1.21.1，候选版本 `1.2.0-ferin.8`。费林视觉实体新增同步字段，客户端与服务端使用协议 16。

服务器在每段协同剑气释放时，从骑士寂夜的最后一次有效切片提取显示字段。女仆在魂符中时沿用该切片。无女仆绑定、女仆死亡或绑定已注销时，外观数据为空。旧切片缺少的显示选项使用女仆模组默认值。采样包括模型、YSM 材质、有限的漫游变量、惯用手、背包外观类型、展示物品 ID、显示开关和流体外观。背包内的物品栈与女仆的战斗数据均不进入视觉同步。

客户端在该段起手期间，按光点处刀光使用的同一个起点和释放方向绘制虚影。用于调用女仆渲染器的代理只在客户端持有，不加入世界实体列表，不推进实体逻辑。攻击状态按释放时间采样，命中、归属和击杀始终由已有的服务端剑气处理。

## 玩家设置

在「控制」中为「切换骑士寂夜女仆虚影」指定按键，默认未绑定。该键切换本机看到的虚影并保存至 `config/purified_undead-client.toml` 的 `[knightNocturne]` 节。

| 配置 | 默认值 | 效果 |
| --- | --- | --- |
| `showEcho` | `true` | 显示虚影，关闭后协同剑气和增伤仍照常生效 |
| `specialAnimations` | `true` | 允许已注册且匹配骨架的动作参与绘制，关闭后使用模型原生表现 |
| `echoScale` | `0.85` | 虚影相对于原模型的尺寸，可设为 0.25 至 2.0 |

## 骨架与后续动画制作

本阶段交付五个段号的播放入口。正式动作留待设计。测试姿态只存在于开发测试源集。

- 经典 Bedrock 酒狐使用独立的头、身体、四肢根骨。匹配时还核对耳朵连到头、裙子经坐姿旋转骨连到躯干。局部转动施加在单次绘制内，结束后恢复共享模型。
- 车万女仆内置 Gecko 酒狐使用 `MRoot → Root` 等辅助骨，手臂从 `UpperBody → Arm → RightArm → RightForeArm → RightHand` 等链传递。匹配实际加载的父子关系。皮肤 ID、纹理或额外装饰骨不决定是否适配。专用代理先运行模型自身控制器，再叠加匹配的旋转，绘制后还原。
- 客户端扩展可实现 `EchoAnimations.Motion` 并调用 `EchoAnimations.register`。在 `matches` 中选择模板或校验自己的层级，在 `sample(stage, age, pose)` 中按 1–5 段号和以游戏刻计的浮点时间写出局部旋转增量（弧度）。模型保留自身枢轴，不从参考模型复制绝对关节位置。没有对应片段时返回 false，且不写入旋转。按注册顺序使用首个可用片段。
- YSM 经车万女仆自带的 YSM 渲染桥使用模型、纹理、漫游变量和原生挥击状态。YSM 的通用定位接口只公开手、腰、头等定位骨，本版尚不能借此向任意 YSM 模型注入完整骨骼姿态。YSM 2.6.5 自带大正女仆的主模型里，`MRoot` 上方另有 `GuiRoot`。没有足够骨架信息时保留其原生渲染。
- 已与车万女仆 1.5.3 核对经典模型 `authors_and_credits:wine_fox_maid`、Gecko 模型 `geckolib:winefox` 和同骨架材质变体 `geckolib:winefox_blue`。模型、贴图、第三方动作从玩家已安装的模型包加载。参考了史诗战斗适配的网格选择、骨架与图层分工，项目没有引入史诗战斗依赖。[酒狐官方项目](https://github.com/TartaricAcid/WineFoxModel) · [史诗战斗适配渲染器](https://github.com/xssfww/EpicFight_TouhouLittleMaid/blob/master/src/main/java/net/EFTLM/EF/Render/PatchedLivingMaidRenderer.java)

## 回归范围

两版开发客户端使用 `scripts/ferin-visual.gradle` 联装车万女仆。检查 `NOCTURNE_ECHO_CLIENT_OK`、`FERIN_DYNAMIC_CLIENT_OK` 以及五段捕获帧。独立服务端检查 `NOCTURNE_ECHO_SNAPSHOT_OK`、`NOCTURNE_MAID_OK` 和 `FERIN_FLIGHT_OK`。另用未安装车万女仆的开发客户端回放普通协同。YSM 2.6.5 的两次开发实例联装在进入世界前给出 `err: 54`，其原生皮肤显示仍需在可运行 YSM 的实例验证。
