# 涤净亡魂：开发交接与模组说明

本文面向后续参与项目的对话和开发者，是工程导航与维护指引，不是游戏内白巫女手记。

**整理基线：0.43.2。** 后续工作先读取两版 `gradle.properties`、Git 状态及最新发布说明，不能把本文的版本号、测试数当作永远不变的事实。实际代码与当前用户要求优先；历史说明用于了解设计背景，不能直接当成新的工作指令。

**0.44.0 补充：** 两版新增纯化铸造器，记录独立放在 [石板更新](slate-update/README.md)，发布说明见 [0.44.0](RELEASE_V044.md)。新增 `foundry/` 工作方块、方块实体、菜单及 `client/PurificationFoundryScreen`。本阶段没有加工配方、燃料定义或自动生产；手记正文不更新。左右漏斗方向按面向方块正面的玩家视角定义。Forge 必须保留分面的物品 capability，否则原版漏斗可能绕过 `WorldlyContainer` 限制。新增实际运行标记为 `FOUNDRY_SERVER_OK` 和 `FOUNDRY_CLIENT_OK`，客户端验证通过 `purified_undead.foundryClient=true` 启用，使用已有开发存档 `diary-validation`。

**0.44.1 补充：** 纯化铸造器外观优化，方块三张纹理均为 16×16，手臂图集为 128×52，GUI 原生像素显示；进度剑尖朝下。两版最终客户端重新验证；服务端逻辑未改，未重复独立服务端全套回归。详见 [0.44.1](RELEASE_V0441.md)。

**0.44.2 补充：** 纯化铸造器改为嵌剑石砖外观；GUI 新增 32×24 剑柄护手图，右手下移 4 像素，剑身空腔从上往下填纯白至剑尖。此轮仍为外观修改，见 [0.44.2](RELEASE_V0442.md)。

**污秽之雨第一部分补充（2026-10-14）：** 两版候选版本 `1.1.0-blighted-rain.1`，实现与指令见 [第一部分](blighted-rain-update/PART_1.md)，验证见 [记录](blighted-rain-update/PART_1_VALIDATION.md)。本体网络协议 11，附属 API v1 不变。新增 `BLIGHTED_RAIN_SERVER_OK` / `BLIGHTED_RAIN_CLIENT_OK`，独立开发实例及着色器实际画面都需核对。极限秽鬼化一次性保命已落实。Forge GitHub 仅推送 `污秽之雨更新` 分支，不合并主分支；白巫女手记未改。

**污秽之雨第二部分补充（2026-10-15）：** 两版候选版本 `1.1.0-blighted-rain.2`、网络协议 12。新增预警音效与秽鬼生物：Forge 15 类、NeoForge 16 类；一般转化 200 刻，村民/鸡雨伤死亡转化，亡灵免雨伤但幻翼除外，全部秽鬼无日晒燃烧。新生物攻击时间钩子仅对新增派生类生效。实现与资源约定见 [第二部分](blighted-rain-update/PART_2.md)，核对 `BLIGHTED_MOBS_SERVER_OK` / `BLIGHTED_MOBS_CLIENT_OK` 以及原有完整回归结束标记。白巫女手记不改，Forge 仍仅推更新分支。

**污秽之雨第四部分补充（2026-10-18）：** 两版 `1.1.0-blighted-rain.5`，盾牌实体收尖模型、三条获取条件更新、创建世界永雨游戏规则和首次赠送纹章、可放置莲花（原物品图标及食用保留）。见 [第四部分](blighted-rain-update/PART_4.md)、[配置](blighted-rain-update/PART_4_CONFIG.md)、[指令](blighted-rain-update/PART_4_COMMANDS.md)。运行核对 `RAIN_PART4_SERVER_OK`、`RAIN_PART4_CLIENT_OK`、`RAIN_PART4_WORLD_OPTION_OK`。白巫女手记未改。

**第四部分补充（1.1.0-blighted-rain.6）：** 信标强制雨恢复、独立饰品数值、默认未绑定的技能开关、手记3、稳定污秽之雪和模组生物雨伤兼容。网络协议13；见 [补充说明](blighted-rain-update/PART_4_SUPPLEMENT.md)。回归新增 `RAIN6_SERVER_OK`、`RAIN6_TOGGLE_NETWORK_OK`、`RAIN6_CLIENT_KEYS_JEI_OK`。

## 1. 先找到正确的工程

工作区根目录：`I:/spirits of purified undead`。文中没有特别注明的相对路径均以此为基准。

| 位置 | 用途 |
| --- | --- |
| 根目录 `src/`、`build.gradle`、`gradle.properties` | **Forge 1.20.1 的实际开发工程**，也是公开 Git 仓库 |
| `1.21.1neoforge版本/` | **NeoForge 1.21.1 的实际开发工程**，有独立源码、构建文件和产物 |
| `1.20.1forge版本/工程源码/` | 交付用源码快照，不能误当成 Forge 主工程开发 |
| `1.20.1forge版本/安装包与依赖/` | Forge 安装 JAR 与对应前置 |
| `1.21.1neoforge版本/发布包/mods/` | NeoForge 安装 JAR 与对应前置 |
| 根目录 `build/libs/` | Forge 构建产物 |
| `1.21.1neoforge版本/build/libs/` | NeoForge 构建产物 |
| 两版目录的 `验证记录/<版本>/` | 本地测试日志、结果、SHA-256；不等同于新一轮验证 |
| `docs/` | 发布说明、功能说明及审查记录 |
| `docs/local-audit/`、`art/` | 本地验证辅助文件和美术工作资料，默认不进入公开仓库 |
| `I:/The Art of ENDER LILIES Quietus of the Knights` | 用户提供的参考资料目录，不是模组工程 |

两个版本目录被根仓库 `.gitignore` 排除。仅根目录 `git status` 干净，**不能证明 NeoForge 没有未记录的改动**；对 NeoForge 要单独核对文件、日志和交付产物。不要为方便而解除整个目录的忽略，也不要把前置 JAR、参考素材、存档或本地缓存提交到公开仓库。

公开仓库：<https://github.com/MarkWenter/spirits-of-purified-undead>。当前主要发布对象为 Forge 1.20.1；用户要求同步两版时，NeoForge 也必须实际实现、构建与交付，不能只改 Forge。

## 2. 模组定位与现有内容

模组名：涤净亡魂 / Spirits of Purified Undead。模组 ID：`purified_undead`；Java 包：`dev.purifiedundead`。这是以《ENDER LILIES》为题材的非官方同人模组。素材和许可范围见 [NOTICE](../NOTICE.md)，不要默认参考素材可以公开再分发。

当前实现主要包括：

- 古老民族之契、八组战士饰品、对应污秽与解除后的加护；契约在生存中不可卸下，创造模式保留管理恢复途径。
- 费林/菲琳（代码统一使用 `Ferin`）协同连击、格洛特、尤里乌斯、姐妹（西丽亚与西丽德）、狼、伊莱恩、海尼尔及法腾的获取和战斗效果。姐妹是一组战士，不要误增一组注册物品。
- 护身符成长、材料掉落与酿造、满背包奖励补发、饰品死亡保留。
- 八件白巫女遗物及独立保命冷却、配方与宝箱掉落。
- 纯净结晶、纯净灵药及其强化/延时与喷溅/滞留形式、莲花、纯净禁果、无垢护甲、纯净触碰、秽鬼之守护等净化内容。
- 白巫女手记、莉莉手记、首次登录赠书；12 项进度与旧存档补发；秽鬼化图标。
- 契约光球表现、部分可选动态光源/光影与无甲判定兼容。

不要仅依据旧 README 中的“初版”“尚未加入”等描述判断当前功能是否存在；应检查后续发布记录与实现。

### 费林协同攻击的表现历程：角色劈砍 → 暂停模型开发 → 独立刀光

**原本的表现方案是费林角色模型显现、持剑挥砍，并配合刀光完成协同攻击。当前仅显示刀光，是后续暂时简化的结果；不是一开始就只设计了刀光，也不表示原角色动作已完成或永久取消。**

1. **角色与长剑阶段。** 早期先建立服务端剑刃轨迹、连段与命中原型，再接入 GeckoLib 的费林角色模型和五段动作。角色在玩家近侧显现，通过身体、手臂、手腕、持剑及披风等骨骼表现挥砍。此后多轮调整兜帽、面甲、披风、甲胄、比例、材质与动作，曾拆分角色和独立长剑工程。`docs/FERIN_FORMAL_MODEL.md` 只记载其中一个阶段，不能把该文档的“第十版”误当成整个模型开发的最终进度。
2. **动作调整仍未完成。** 封存记录中的后期工程经历 v19 骨骼重构、v20 五段动作，以及 v21/v22 第一段修正。第一段的空间姿态、持剑手和腕肘联动仍未达到原作复刻要求，模型动作尚未得到用户认可。静态预览、骨骼检查和离线采样通过，不等于模型动作已通过验收。
3. **2026-09-23 封存并暂停。** 当时的 `0.25.0-dev` 模型版与源码、美术、动画和检查资料封存到 `archive/ferin-model-unfinished-2026-09-23/`。主线暂时隐藏角色与长剑，改用独立刀光。此决定是暂停模型工作并提供可用的协同攻击表现，不能在后续交接中写成“模型已完成”。
4. **刀光主线继续迭代。** `FerinRenderer` 改为直接绘制 `FerinSlashMesh`，不再走角色 GeckoLib 渲染或挂载剑层；随后继续调整刀光的方向可读性、拖尾、曲面倾角等。当前两版以独立刀光表现协同斩击，待机不显示费林角色或长剑。保留下来的 `FerinEntity` 是同步和表现的载体，不能仅凭该实体或旧模型文件存在就认定游戏仍在显示角色劈砍。

**简化的是表现形式，不是取消协同战斗逻辑。** 切换刀光时保留了五连段触发、伤害、暴击与联动等规则；当前仍由服务端剑路决定命中，不是对原目标预定必中伤害，也不是用角色身体碰撞箱代替剑刃。刀光与剑路关联，但视觉渐隐、拖尾不能直接视为新增伤害判定。后续战斗修正应以当前代码及对应发布记录为准，不能将封存版整套覆盖回来。

#### 历史资料与以后恢复模型的入口

以下历史资料在当前本地工作区可查；其中 `archive/`、`art/` 和部分早期文档不在公开 Git 仓库内，新机器上缺失时应先定位资料，不要假定已随仓库下载：

- 封存说明：`archive/ferin-model-unfinished-2026-09-23/README.md`，记录未完成问题和恢复步骤。
- 历史源码与模型：同目录 `model-source-and-history.zip`；封存说明指定最近的动作工程为压缩包中的 `art/ferin-attacks-v22/ferin-stage1-right-handed.bbmodel`。
- 历史构建：同目录 `purified_undead-0.25.0-dev-UNFINISHED-MODEL.jar`，仅为未完成模型版回退参考，不是当前推荐安装包，不能与新版同放进 mods。
- 原始设计与录像分析：`docs/DESIGN_HANDOFF.md`、`docs/FERIN_VIDEO_ANALYSIS.md`。
- 核心原型与模型演进：`docs/FERIN_CORE_PROTOTYPE.md`、`docs/FERIN_FORMAL_MODEL.md`、本地 `art/ferin-*`。
- 切换刀光的说明与后续迭代：`docs/FERIN_SLASH_FIRST_RELEASE.md`、`docs/FERIN_SLASH_V2.md`、`docs/FERIN_SLASH_V3.md`、`docs/FERIN_SLASH_V4.md`。
- 当前实现与保留接口：`client/FerinRenderer.java`、`client/FerinModel.java`、`client/FerinSwordLayer.java`、`entity/FerinEntity.java`、`entity/FerinSlashMesh.java`，以及资源目录内保留的费林模型、动画和纹理。

如果用户以后要求继续模型工作，应先将封存资料提取到独立目录，对照当前代码恢复表现层；运行旧生成脚本前检查输出路径，防止覆盖现用资源。重点复核持剑手、腕肘联动、第一段空间姿态、五段动作与服务端剑路的关系，并同时进行 Blockbench 和游戏内检查。**不要因发现旧文件就自动重新启用角色模型，也不要将它们当作普通冗余直接删除。** 本节是历史交接与未来恢复指引，不是现在恢复模型的任务。

### 必须保留的设计约定

1. **Patchouli 已是必要前置**，不是可选联动。白巫女手记有合成与首次登录赠送两种来源。
2. **莉莉手记不可翻阅**，不做 Patchouli 书本适配；在 `wanderer_log` 槽中生效。佩戴期间按实体注册类型累计亡灵击杀，每种提供 3% 暴击率和 0.5 幸运；僵尸和溺尸等不同实体类型分别计数。记录属于玩家，不属于某一本物品。
3. 莉莉手记锻造：模板栏为空，白巫女手记作基础，任意支持的纯净灵药作添加物。不能只接受普通未强化饮用药水，也不能把其他 Patchouli 手册当作白巫女手记。
4. 普通模式契约获取通过秽鬼化死亡后重生；极限模式改为消耗秽鬼化、图腾式保命并发契约。兼容真实图腾及本模组保命遗物；契约一次性发放，满背包等待补发。
5. 纯净禁果永久槽位解锁与成长记录须跨死亡、重登保留，避免重复食用叠加。
6. 无垢护甲的物品栏图标采用平面像素图，穿戴贴图是另一套资源。剑和状态图标保持清晰块状像素，避免过密细节及透明裂缝。
7. 净化更新需要记录。**游戏内手记是否更新依当前用户请求决定**：早期“不要更新手记”与第三部分“增加手记内容”各有其范围，不能把某一次指示永久套用。
8. 不在缺陷修复中顺便改玩法数值、配方、注册 ID、存档键、美术或前置要求；功能调整按用户授权范围处理。

## 3. 代码地图

下列路径位于两版各自 `src/main/java/dev/purifiedundead/` 下。

| 文件 / 目录 | 主要职责 |
| --- | --- |
| `PurifiedUndead.java`、`content/Mod*.java` | 注册、事件安装、依赖接入与基础内容 |
| `config/PurifiedUndeadConfig.java` | 配置默认值、范围、旧默认值迁移 |
| `content/item/AncientContractItem.java`、战士 Item 类 | Curios 槽位、穿戴限制、说明、死亡规则 |
| `progress/WarriorProgress*`、`WarriorRewardService.java` | 玩家成长、一次性领取、待领取奖励及保存 |
| `progress/*AcquisitionEvents.java` | 各战士获取条件与仪式 |
| `progress/ContractProgressEvents.java`、`HardcoreBlightRescue.java` | 契约死亡/重生、极限保命及补发入口 |
| `progress/AccessoryDeathRetention*`、`RetainedAccessoryBuffer.java` | 死亡保留缓存与恢复 |
| `progress/ModAdvancements.java` | 事件发进度、历史证据与一次性迁移 |
| `combat/FerinCombatEvents.java`、`Ferin*State`、轨迹类 | 近战触发、连击、服务器命中判定与生命周期 |
| `combat/*CombatEvents.java`、`*Model.java` | 其他战士效果及可单测的计算规则 |
| `combat/GuardianMovementService.java`、`network/` | 移动请求与服务器权威判断 |
| `entity/`、`client/` | 实体、魔弹、渲染、输入与光球表现 |
| `content/relic/`、`loot/` | 遗物效果、保命、掉落修改 |
| `purification/` | 新装备、消耗品、禁果、莉莉手记、锻造、Patchouli 桥接 |
| NeoForge `mixin/` | 1.21.1 特定接口适配，如药效字段与法腾抢夺 |

资源位于各自 `src/main/resources/`。Forge 数据目录通常是 `advancements/`、`recipes/` 等复数；NeoForge 1.21.1 使用 `advancement/`、`recipe/` 等新路径。语言文件为 `assets/purified_undead/lang/{zh_cn,en_us}.json`。更新资源时同时核对两个版本的路径与格式，不要整目录盲拷。

## 4. 已知容易回归的逻辑

详细历史见 [0.43.2 检查记录](PROJECT_AUDIT_0432.md)。以下是后续改动应保留的约束：

- **死亡玩家仍可能继续 tick。** 保留饰品不得在死亡界面恢复进旧背包；待领取奖励不得向死者发放后清除 pending。重生后的存活角色才接收。极限保命的已确认流程先恢复生命，再进行即时奖励发放。
- 奖励使用现有 `WarriorRewardService` 与成长状态，不要为新事件另写绕开去重的直接赠物逻辑。真实送达后才授予相应“已获得”进度。
- 菲琳在重生、换维度、死亡、旁观或失去契约后停止旧活动攻击和排队输入，**保留原召唤冷却**；不可通过重生刷新冷却，也不可沿用旧世界坐标攻击新世界。
- **Forge 与 NeoForge 伤害事件语义不同。** Forge `LivingDamageEvent` 在生命扣除前且吸收已处理；NeoForge `LivingDamageEvent.Pre` 尚需考虑吸收，`Post` 已扣生命。NeoForge 吸血不能用“扣后生命＋伤害”倒推扣前生命，否则致死溢出伤害会过量回血。
- 伤害加成和协同攻击避免重复加成、递归触发、同一击多次结算；保留队友、宠物、PVP、无效目标和无伤害攻击过滤。
- Curios 查询有缓存；消耗品完成使用时的重要装备条件需核对当前实际槽位，不能只依赖可能过期的查询结果。属性修饰符保持稳定标识，卸下后撤销，不能每 tick 叠加。
- 旧存档进度补发只依据已有成长、库存、统计等证据，不能凭推断给全进度。数据包加载不完整时不能消耗迁移机会。
- 客户端只发送动作意图；伤害、目标、冷却与装备资格由服务器决定。避免把客户端渲染类引入服务端必经初始化。
- Forge 常见 UUID 属性修饰符、NBT 与 NeoForge 的 ResourceLocation 修饰符、Holder、数据组件、序列化 API 不可机械互换。

## 5. 环境与依赖

| 项目 | Forge | NeoForge |
| --- | --- | --- |
| Minecraft | 1.20.1 | 1.21.1 |
| 加载器基线 | 47.3.22 | 21.1.248 |
| Java | 17 | 21 |
| Curios | 5.14.1+1.20.1 | 9.5.1+1.21.1 |
| GeckoLib | 4.8.4 | 4.8.4 |
| Patchouli（必要） | 1.20.1-85-FORGE | 1.21.1-93-NEOFORGE |
| Apothic Attributes | 1.20.1-1.3.7 | 1.21.1-2.10.1 |
| Placebo | 1.20.1-8.6.3 | 1.21.1-9.9.2 |

Curios、GeckoLib、Patchouli 为必需依赖。Apothic Attributes 与 Placebo 在本地完整测试包中安装；不要因此误称当前模组元数据将属性库声明为必需。移除属性库会影响完整暴击属性效果，应按实际任务验证降级行为。

优先使用各工程 `scripts/gradle.ps1`：Forge 使用工程内 JDK 17；NeoForge 脚本优先使用本机 JDK 21。脚本临时设置环境变量并在结束后恢复，不修改全局 Java。共享缓存为根目录 `.gradle-user-home/`。这些本机路径在另一台电脑上可能不存在，先检查，不要擅自安装/替换系统运行环境。

## 6. 构建与验证

以下 PowerShell 命令从工作区根目录执行。依赖已有缓存时可使用 `--offline`；缺缓存时不能把离线解析失败当作源码错误。

```powershell
# Forge：构建、单测、收集安装用 JAR
& ./scripts/gradle.ps1 --offline build stageMods

# NeoForge：构建、单测、收集安装用 JAR
& './1.21.1neoforge版本/scripts/gradle.ps1' --offline build stageMods
```

`stageMods` 仅收集到 Forge 的 `build/pcl2-test-mods/`、NeoForge 的 `build/release-mods/`，**不会自动更新最终交付文件夹或 ZIP**。

需要运行全套开发服务端回归时：

```powershell
$auditInit = Join-Path (Get-Location) 'scripts/audit-server.gradle'
& ./scripts/gradle.ps1 --offline runServer -I $auditInit
& './1.21.1neoforge版本/scripts/gradle.ps1' --offline runServer -I $auditInit
```

只在开发测试环境使用上述回归选项。测试会创建测试玩家、调整测试世界状态并自动停止服务器，不能指向正式实例或正式存档。先检查构建文件的运行目录。`src/test` 为单测，`src/smoke` 为运行时验证；测试类不得打进生产 JAR。

**不要只看 Gradle 的 `BUILD SUCCESSFUL`。** 游戏侧测试异常后 Gradle 仍可能返回成功。还应核对无对应 `_FAILED`，并出现适用的通过标记：

- `PROJECT_AUDIT_OK`、`FERIN_LIFECYCLE_OK`、`HARDCORE_OK`
- `PURIFICATION_SERVER_OK`、`LILY_DIARY_OK`、`ADVANCEMENTS_OK`
- `PURIFIED_UNDEAD_RELIC_SMOKE_OK`、`PURIFIED_UNDEAD_BALANCE_SMOKE_OK`、`PURIFIED_UNDEAD_DIARY_SERVER_OK`
- 运行大体型目标验证时：`FERIN_LARGE_MOB_OK`

客户端基本加载可运行 `runClientSmoke`。进度/状态图、手记、光球等专项客户端测试需阅读对应 `src/smoke` 测试类的启用属性、测试世界名和停止条件；仅到标题页不能宣称验证了游戏内视觉。已有本地辅助文件 `docs/local-audit/adv043/client.gradle` 可作为进度图标验证参考，但它不随公开仓库分发。

0.43.2 基线：Forge 122 项、NeoForge 124 项单测通过，两版服务端与客户端检查通过；此数值只是历史基线。测试应随改动选择：纯文档改动做路径/链接核对即可；运行逻辑改动做相关回归及完整构建；视觉改动需要实际资源加载和画面核对。

## 7. 交付与 GitHub

用户要求发布时，先完成实现和验证，再发布具体成品。

1. 检查两版版本号与依赖元数据，不能将 Forge JAR 当成 NeoForge JAR。
2. 完成两版构建，核实 smoke/test 类未进入生产包；按改动检查资源引用、语言与数据格式。
3. 更新发布说明；净化内容同步记入 `docs/purification-update/`。游戏内白巫女手记单独按用户要求处理。
4. 将旧模组 JAR 归档到各交付目录的 `archive/before-<新版本>/`，保留可回退成品；最终 mods 目录不要放两份不同版本的本模组。
5. 从 staging 目录复制新成品及必要前置到对应交付目录；若提供 ZIP，同步更新。当前完整安装目录通常是本模组与五个前置共六个 JAR，依赖变化后以实际配置为准。
6. 保存日志、实际通过结果与 SHA-256，更新 Forge 源码交付快照。不要把历史截图或旧日志当作新版本测试证据。
7. 用户要求更新 1.20 GitHub 时，仅提交范围内的 Forge 源码、测试与文档；检查 `.gitignore` 是否需要对新文档逐个放行。根据请求更新提交、标签和 Release，并核对远端实际结果。
8. 凭据只通过已有安全机制使用，不写进脚本、文档、日志或聊天。日志在公开前检查个人信息。只做本地文档维护时，不需要因此自动升级模组版本或重新发布 JAR。

本地历史发布辅助脚本在 `docs/local-audit/`，它们可能写死版本与路径。使用前阅读并按本次目标调整，不能直接执行旧脚本覆盖交付文件。

## 8. 后续对话工作方式

- 开始时读取本文件与相关最新说明，查看 `git status`，并独立确认 NeoForge 当前内容。
- 先复现或用代码调用链确认问题，再做范围明确的修改；保留用户已有更改，不做无关大规模整理。
- 使用当前会话授权推进工作；缺少会影响实现的关键设计信息时再询问。本文不替代用户授权，也不要求额外的审批流程。
- 不因为两版长得相似就省略其中一版的编译和运行验证。不承诺所有第三方组合均无问题。
- 最终说明实际修改、已运行的验证、未覆盖范围和准确产物位置。代码冗余或格式问题若不影响行为，可记录后按任务范围整理，不混入无必要的玩法改动。
- 功能、目录、依赖或发布流程发生变化后同步维护本文件；不要无限复制多份指南造成内容分叉。

## 9. 常用资料入口

- [最新检查基线](PROJECT_AUDIT_0432.md) / [0.43.2 发布说明](RELEASE_V0432.md)
- [极限模式保护](RELEASE_V0431.md)
- [进度与旧存档补发](ADVANCEMENTS_043.md)
- [净化更新总记录](purification-update/README.md)
- [净化第二部分](purification-update/PART_2.md) / [第三部分](purification-update/PART_3.md)
- [手记验证](DIARY_VALIDATION.md) / [手记美术](DIARY_ART.md)
- [光球兼容范围](WISP_COMPATIBILITY.md)

这些文档含历史阶段描述；发生冲突时结合版本顺序和当前实现判断，必要时向用户说明差异。

## 0.45.1 后续交接补充

石板更新第二部分及其修正已实现，最新规则以 `docs/slate-update/PART_2.md`、`docs/RELEASE_V0451.md` 为准。莉莉手记现可主手使用打开八槽追忆界面；旧文中的未开放说明已过时。双消耗铸造必须能将余料合并在上槽，保留模板配方保留旧内部余料逻辑。JEI 可选集成位于 compat/jei，登录时同步服务器铸造配方。NeoForge 创造栏需同时包含 SlateContent 与 FoundryContent。第二部分原始图标只做透明边缘对齐，无额外降采样。双版本构建、独立服务端和有／无 JEI 客户端检查记录保存在各版本验证记录/0.45.1。白巫女手记保持不变。

## 0.46.0 可选亡灵联动

两版新增 compat/UndeadCompatibility 与 compatible_undead、blight_fragment_exclusions 实体标签。详细名单与移植边界见 UNDEAD_COMPATIBILITY.md。自动类型掉落由通用配置 contract.autoDetectModdedUndeadDrops 控制；原掉率与数量不变。新的运行验证标记为 UNDEAD_COMPAT_OK，测试用实体和数据标签仅属于 src/smoke，不得打进生产包。

## 0.46.1 Forge 最低版本调整

用户确认目标为 47.4.12（不是最初误写的 47.0.12）。Forge 开发与编译基线改为 47.4.12，mods.toml 范围改为 [47.4.12,48)。前置版本不变；NeoForge 工程和交付仍为 0.46.0。验证与交付见 RELEASE_V0461.md。后续应在最低支持基线上编译，避免无意引入更高版本接口。

## 0.46.2 JEI 空模板锻造

两版通过 compat/jei/LilySmithingExtension 适配空模板锻造；不要因 JEI 不显示成品而修改真实配方模板要求。Forge 用 -Pjei_version=zzpNSbJZ 验证整合包 JEI 15.20.0.116，默认仍为较新版本。SlateJeiSmoke 从模组自身运行实例检查来源索引，新增 LILY_JEI_OK、JEI_SOURCE_AUDIT_OK 标记，保留 SLATE_JEI_OK。详见 RELEASE_V0462.md。

## 0.46.3 Forge 47.3.22

Forge 编译基线与加载最低要求改为 47.3.22。污秽铁傀儡、污秽之王的掉落表引用改用显式 minecraft 命名空间，避免调用旧版没有的 withDefaultNamespace。前置不变，NeoForge 保持 0.46.2。见 RELEASE_V0463.md。

**0.47.0 补充：** 双版兼容性修复见 RELEASE_V047.md、COMPATIBILITY_AUDIT_047.md。新增 CompatibilitySmoke、JEI_COMPATIBILITY_OK。Forge 最低 47.3.22；当前测试 122/124 项。用户三个实例只读诊断与一次开发 JVM 原生失败保留 docs/local-audit/compat047，不公开原始 JVM 参数。整机死机根因未确认，不能把通过开发回归描述为所有整合包问题已解决。

**0.48.0 补充：** 秽鬼之守护新增主手护甲、服务器剑气与可选附魔参考武器规则。见 RELEASE_V048.md、purification-update/GUARDIAN_048.md。新网络协议 7；新增 GUARDIAN_WAVE_OK、GUARDIAN_ENCHANT_OK、GUARDIAN_CLIENT_OK。跳劈/疾跑状态用 GuardianWaveCombat 上下文保存；剑气可以触发费林/伊莱恩/海尼尔/格洛特，协同斩击自身不递归触发。可选模组测试临时依赖保存在 .tools 和 docs/local-audit/guardian048，不打包。

**0.49.0 补充：** 剑气改为本地即时预测、七格四刻、协议 8；GuardianProjectileBonus 仅桥接箭矢伤害数值。灵灾实际为 Malum，0.48.0 中 Goety 的识别有误，此版补齐 Malum 并保留旧兼容。见 RELEASE_V049.md、purification-update/GUARDIAN_049.md。

**0.50.0 补充：** Malum 收魂需要武器标签，不仅是附魔适用性。分别合并 soul_hunter_weapon / soul_shatter_capable_weapon；新增真实掉落验证 GUARDIAN_SOUL_DROPS_OK。剑气每刻 1 格、七刻七格，外观和命中宽度扩大 25%，协议 9。

**0.51.0 补充：** 神秘启旅 KubeJS 会清空 Malum 收魂标签。新增仅死亡、仅主手秽鬼之守护、仅缺失标签的原生暴露精魂补救；配置 compatibility.guardianMalumSoulHarvest。接口查找缓存，无每刻工作；测试需复现移除标签后剑刃/剑气/费林补刀，并检查关闭配置与普通武器。见 RELEASE_V051.md。

**0.52.0 补充：** 追忆增强与剑气 15 格见 RELEASE_V052.md、slate-update/ENHANCEMENTS_052.md。海尼尔只能在战利品/击杀进度条件作用域内增加标签，不得改写实际伤害身份；星月刻蚀板专项桥接保留所有其他条件。Forge 首次添加 Mixin，refmap 使用最低 Forge 基线的实际 SRG 映射，更新基线后需核对。新客户端标记 SLATE_CLIENT_JUMP_OK、SLATE_CLIENT_OK；专项 CELESTIAL_ETCHING_CRITERIA_OK。SmokeConfigIsolation 仅开发回归禁用配置文件 watcher，避免临时配置 set 触发旧文件异步回读，不进入发布包。光点遮挡按用户要求暂缓，写深度实验已撤销。

**0.52.1 启动修复：** Forge refmap 的类名键必须使用 JVM 内部斜杠格式。0.52.0 点分隔键在开发环境被名称重映射掩盖，正式启动失败。新增或修改 Mixin 后必须用实际发布 JAR 在独立正式加载器实例启动；核对 PRODUCTION_STARTUP_OK（主菜单且所有目标加载），不能只运行开发客户端/服务端。见 RELEASE_V0521.md 与 slate-update/HOTFIX_0521.md。

**0.53.0 补充：** 用户改写表25项已落实；BlightedSpiritItem净化奖励使用配置消耗数量。LilyDiaryTooltip只在客户端提示生成时读取已同步的手记专属修饰符，不要将玩家总暴击/幸运当作手记加值。未佩戴显示0，历史仍保留。新增PURIFICATION_RATIO_OK和ITEM_TEXT_CLIENT_OK。详见RELEASE_V053.md。

**0.54.0 手记更新：** 按新增内容2文档加入石板强化14条目，原净化物品中的剑和莉莉手记只补充段落，其他内容目录最后。当前6目录53条目130页，目录验证DIARY_CATEGORY_ORDER_OK。见RELEASE_V054.md与slate-update/DIARY_054.md。

**0.55.0 管理接口：** /purifiedundead 普通玩家只查自身，管理员权限2。API v1进度快照/授予/修改/非取消事件；自然与API获取共享存档通知。reset仅清记录，collect仅收回暴露背包/Curios并保留已获取。铸造JSON严格重载保留失败前配置并同步JEI，TOML建议重启。新标记ADMIN_API_OK与ADMIN_CLIENT_SYNC_OK，见RELEASE_V055.md、COMMANDS_AND_CONFIG.md、ADDON_API.md。

**0.56.0 规范审查：** 两版五个 Redirect 改为 WrapOperation；Forge 内嵌官方 MixinExtras 0.5.3，以 jarJar/reobfJarJar 产物交付，普通 jar 为 slim，禁止发布 slim。NeoForge 沿用加载器内置库。新增 AUDIT_BOUNDARIES_OK、MIXIN_CHAIN_OK；串联回归须加 scripts/audit-chain.gradle，测试 Mixin 不得进入生产包。见 COMPATIBILITY_AUDIT_056.md、CODE_STANDARDS.md。

**v1.0 正式编号：** 两版 mod_version 均为 1.0，无 dev 后缀；与 0.56.0 相比只更改版本元数据与新增 icon.png。Modrinth 页面不可使用该 AI 图标，平台发布方式等待用户选择；本地发布准备和隐私检查见 docs/local-audit/release100。

## 污秽之雨更新预备（2026-10-14）

两版在 blightedrain/ 建立空注册边界，接入本体初始化，具体玩法暂缓。没有新增注册内容、配置、存档或网络协议，版本仍为 1.0。后续实现先阅读 [更新入口](blighted-rain-update/README.md) 与 [架构约定](blighted-rain-update/ARCHITECTURE.md)。项目现状和实际验证独立记录在该目录。

**污秽之雨贴图重绘（2026-10-15）：** 两版 `1.1.0-blighted-rain.2.1` 仅更新新增秽鬼的资源，原有傀儡/国王贴图保留。20 张内置 imagegen 草稿经原版 UV 校准；复现脚本在 `scripts/blighted-textures/`。参见 [重绘说明](blighted-rain-update/TEXTURE_REPAINT.md)。画面测试新增幼年、背面、毛色/斑纹、剪毛/染色与职业服装案例，测试需排除无关 smoke 数据标签并禁用测试窗口失焦暂停。

**污秽之雨测试修正（2026-10-15）：** 两版 `1.1.0-blighted-rain.2.2` 新增 `content/BlightedSpawnEggs` 与 `ModCreativeTabs`。17/18 种秽鬼刷怪蛋在独立 `purified_undead:main` 创造栏；图标使用秽鬼化状态图。手记仅改创造栏归属，不改教程正文。掠夺者仍掉落原装备，但可重新拾弩，无弩时近战。新增 `RAIN_FIX_SERVER_OK` 和 `RAIN_FIX_CLIENT_OK`。详情见 [修正记录](blighted-rain-update/FIXES_2_2.md)。默认 Forge `run/server` 曾关闭契约自然获取，完整回归应使用已核对配置的独立开发实例。


**污秽独有特征补充（2026-10-16）：** 两版候选 `1.1.0-blighted-rain.3`，详见 [特征](blighted-rain-update/TRAITS.md) 与 [验证](blighted-rain-update/TRAITS_VALIDATION.md)。部分秽鬼生命改为原版 150%、移动减速，骷髅系攻击频率为原版 70%；新增纯净灵药治愈秽鬼村民、猪牛腐肉、剪毛红羊毛和秽鬼蜘蛛。已有生物通过 `BlightedTraitsVersion` 一次性迁移，NeoForge 属性字段为小写 `attributes`。新增 `BLIGHTED_TRAITS_SERVER_OK`。仅推污秽之雨分支。


## 污秽之雨第三部分（2026-10-17）

候选版 `1.1.0-blighted-rain.4`；见 [第三部分](blighted-rain-update/PART_3.md)。新增静雨信标持久化、强制雨纹章、材料食品、可恢复投掷物与盾牌。两版检查 `RAIN_PART3_SERVER_OK` / `RAIN_PART3_CLIENT_OK`。客户端和服务端 smoke 使用同一编译输出，必须先后运行，避免客户端缩减 source set 清除服务端测试类。白巫女手记不更新，Forge 仅推污秽之雨分支。


**1.1 正式发布：** 用户已授权将污秽之雨更新合并主干；两版正式版本为1.1，协议13。阶段文档中的“仅推分支/不合并”是历史发布范围，正式发布说明见 [1.1](RELEASE_V110.md)。

**1.1.1-hotfix.1 稳定性候选：** 用户将AMD/Oculus编译报告定义为问题1、稳定性扫描定义为问题2。本候选只修问题2，Forge在`1.1hotfix`分支，不合并main；两版协议13。视觉Mixin拆到`purified_undead.visual.mixins.json`，Forge清单及开发参数、NeoForge元数据均须加载两份配置。新故障注入回归标记`FAILURE_BOUNDARIES_OK`，与完整回归一起使用`audit-server.gradle`和兼容两版的`audit-chain.gradle`；测试Mixin不得进入发布包。处理范围与安全停止策略见[稳定性候选](STABILITY_HOTFIX_111.md)。


**1.1.1-hotfix.2 后续候选：** 同一hotfix分支保留既有故障隔离；两版增加满背包无变化快速路径，保留记录原位逐条提交，NeoForge静默解码失败结果，失败同步20至1200刻退避，清理二次失败时继续其余恢复并明确失败。新增实际标记`HOTFIX_COST_BOUNDARIES_OK`，与`FAILURE_BOUNDARIES_OK`及全套回归共同核对；测试复制计数与清理故障Mixin不得进入生产JAR。详见[稳定性候选](STABILITY_HOTFIX_111.md)末节，本轮本地验证目录`outputs/hotfix-cost-2026-10-25/`。


**1.1.1-hotfix.3 问题1候选：** 仅自定义天气shader资源独立顶点阶段、内联对应版本的原版雾公式、局部变量更名，保留hotfix.2全部稳定性及性能保护。两版fog_distance语义不同，严禁机械共用1.20.1公式。运行必须核对shader实际注册，不能把静默回退当作兼容成功；AMD实机待回测，详见[问题1候选](AMD_SHADER_HOTFIX_111.md)。


**1.1.1-hotfix.4 后续边界候选：** 修正转换源已移除后的错误回滚、两版逐条解码输入隔离、伊莱恩魔法倍率乘后溢出；正常数值和hotfix.3着色器资源不变。新增运行标记`HOTFIX_TRANSACTION_BOUNDARIES_OK`，应与原有完整回归及两个边界标记共同核对。新增加载故障Mixin只属于smoke。详细语义及性能取舍见[稳定性候选](STABILITY_HOTFIX_111.md)末节，本地验证目录`outputs/hotfix-boundaries-2026-10-25/`。
