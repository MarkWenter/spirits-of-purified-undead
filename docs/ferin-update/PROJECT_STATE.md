# 费林更新预备 · 项目现状核对

核对日期：2026-11-06；本体 Git 起点 caf889ea30a51abc73afd42ca0c320b5d0118bf7。本文记录工作区实际状态，不将历史验证自动视为本轮验证。

## 工程与已有修改

| 项目 | 实际入口 | 当前版本 | 依赖边界 |
| --- | --- | --- | --- |
| 本体 Forge | 根目录 src、build.gradle | 1.1.1hotfix；MC1.20.1、Forge47.3.22、Java17 | purified_undead；完整 jarJar 内嵌 MixinExtras，slim 不是安装包 |
| 本体 NeoForge | 1.21.1neoforge版本/ | 1.1.1hotfix；MC1.21.1、NeoForge21.1.248、Java21 | 独立实际源码，被根 Git 忽略，不能仅凭 git status 判断同步 |
| 附属 Forge | I:/spirits of expand 根目录 | 0.2.0 | spirits_of_expand；本体最低1.1，开发默认引用1.1 JAR |
| 附属 NeoForge | I:/spirits of expand/neoforge-1.21.1/ | 0.2.0 | 同义实现、独立JAR；本体默认引用1.1 JAR |

Forge 的 `1.20.1forge版本/工程源码/` 是交付快照。本次只改变实际工程，不刷新历史交付。附属目录没有自己的 Git 仓库，不能以 Git 状态证明其干净。

开始时本体已有 `.gitignore`、`AGENTS.md`、统一指南修改，以及未跟踪的 CURRENT_CONTENT、content-evidence、核对脚本、outputs 和验证记录。均保留。本轮只在 .gitignore 与指南追加新入口；没有提交、切分支、推送或改写既有记录。

## 内容与架构地图

- content：契约、八组战士、遗物、材料、药水、注册和创造栏。progress：获取事件、护身符、去重奖励、pending、克隆、死亡保留和进度迁移。
- combat：各战士事件与纯规则模型、服务器费林剑路、姐妹移动、秽鬼之守护剑气；entity/client/network 分别承担载体、表现/输入与动作/状态同步。
- purification：净化装备、消耗品、莉莉手记、书本桥接。slate：八槽追忆、效果和奖励。foundry：容器、余料、批量加工、JSON配方及重载。
- blightedrain：雨阶段模型、维度存档、曝光/免疫/归属、转换与避雨、物品及新增秽鬼；不是早期的空模块。
- api：v1进度快照、授予和变更通知；command/config 提供管理与配置。compat/mixin：亡灵标签、可选JEI/附魔/属性/掉落桥接和限定条件包装。
- 两版资源、注册、存储和事件各自适配；NeoForge 还包含版本独有生物。不能把不同文件数视为漏移植，也不能将两个源码目录盲拷同步。

准备前清点：主Forge 1101个工程文件（356 Java，539资源），主NeoForge 972个（351 Java，568资源）；附属Forge 66个（13 Java，33资源），附属NeoForge 53个（13 Java，33资源）。数字含开发测试和所选文档/脚本，不含缓存、构建产物、世界、交付快照、美术归档及docs/local-audit/reference；不是生产类数量。

完整路径、字节数、修改前SHA-256与Java模块地图见本地 `outputs/ferin-preparation-2026-11-06/inventory-before.json` 和 `*-source-map.txt`。已读取所选工程树全部文件用于指纹/文本结构/JSON解析；人工语义阅读重点覆盖指南、机制索引、主要更新链、费林全链与附属核心实现。此清点不声称逐行审计所有业务代码、查看所有图片，或解包阅读所有历史二进制。后续具体需求仍须深入涉及的实际调用链。

## 历史与当前状态的区别

| 历史材料 | 当前核对结论 |
| --- | --- |
| PROJECT_SCOPE / DESIGN_HANDOFF 的启动阶段、仅Forge、未加入玩法 | 历史设计背景；当前双版已有正式1.1.1hotfix，不是本次新功能指令 |
| FERIN_CORE_PROTOTYPE 的角色显现、旧位置/时序、锁定不排队 | 由后续刀光V2/V3/V4、生命周期修复和当前代码覆盖；尤其已有一次缓存和宽松续段 |
| FERIN_FORMAL_MODEL 与 art 中后期工程 | 模型动作未获认可；2026-09-23封存，v22资料在历史ZIP中。本轮核对封存说明与压缩包路径，不恢复旧模型 |
| FerinModel / FerinSwordLayer / 模型动画文件仍存在 | 不代表活动渲染。实际 FerinRenderer 继承 EntityRenderer，直接绘制 FerinSlashMesh |
| 契约成长旧文“满级不消耗”、早期碎片概率 | 后续净化/文本/配置更新已覆盖，查询当前机制须沿实际代码和CURRENT_CONTENT复核 |
| 指南早期“莉莉手记不可翻阅” | 不作Patchouli教程，但当前主手可打开八槽追忆界面，不可据此删交互 |
| 污秽雨阶段“仅更新分支/不合并” | 1.1和1.1.1hotfix已正式合并；历史授权不适用于本次Git操作 |
| docs/blighted-rain-update 的指南链接 | 当前目录缺失；已从固定Git对象读取架构/入口，机制索引另保存25份历史证据引用，不擅自恢复整目录 |
| hotfix候选的不同单测数、AMD结论 | 最新正式基线记录140/141单测；本轮数量另报。NVIDIA验证不能证明AMD实机已解决 |
| 附属 FRAMEWORK_001 / OPTIONAL_PREPARATION | 历史空框架。当前0.2.0已有息金矿、材料、五工具、配方和资源 |
| 附属 BREATHGOLD_020_PENDING | 顶部已注明随后恢复并完成；不能再当成当前未验证状态 |
| 附属可选目标 PRESENT | 仅检测存在；尚无实际第三方玩法适配，不等于组合实测 |

开始时 `py -3 scripts/check-content-evidence.py` 得到 CONTENT_EVIDENCE_CURRENT，记录证据未变化。未修改指纹manifest。此结果只检查新鲜度，不替代语义核对或本轮游戏回归。

## 费林当前接轨基线

现有基础协同来自契约，饰品负责对应反转资格；不能将基础协同改为必须佩戴费林战士。首段有真实服务端攻击确认，后续由已激活连段接收续段意图。当前支持大型/多部件受伤事件不完整时的健康变化观察，保持同刻去重。

每段使用独立快照和暴击，服务端实际剑路决定命中；追忆和海尼尔联动在现有结算点叠加。FerinEntity同步段数、起时和锚点；客户端仅显示刀光。死亡、换维、失去资格取消空间攻击但保留冷却；登出/关服清理运行引用。旧存档键 `purified_undead:ferin_state` 及当前格式均未改。

费林更新入口只组织未来新增注册；不搬迁这套已验证的代码，不重复安装 FerinCombatEvents，也不添加没有用途的服务接口或静态状态。

## 附属实际状态与需要保护的接点

0.2.0已实现息金：追踪玩家放置的普通金矿，用附属自己的PlacedGoldData保存位置，按本体污秽降水规则累计已加载曝光，最终转化为息金矿；没有新自然生成。工具、掉落、疲劳、时运/精准与配方由附属自己维护。

公共进度访问按API v1；雨查询目前是内部实现桥接：BlightedRainBridge反射缓存model字段，读取phase；PlacedGoldData直接调用RainExposure。桥接失配会明确失败，不是静默使用原版雨。新本体对它的兼容需要用本轮JAR验证，附属默认base_mod_version=1.1不能证明测试了1.1.1hotfix。

本次不改附属工程源码或默认依赖版本；测试时用命令参数选择本轮本体。可选目标仍全部非必需，不向本体增加反向依赖。

## 本阶段完成边界

具体玩法、美术、模型、平衡、配方、额外状态、协议和版本规划等待用户给出内容。本阶段的完整性指可继续开发的接入边界、可追溯现状和本轮验证，不等于“费林内容更新已完成”。验证结果见VALIDATION。
