# 费林更新 · 架构接入约定

依据：两版 1.1.1hotfix 实际源码、统一开发指南、CODE_STANDARDS、ADDON_API，以及 Git b54784b 中污秽之雨 ARCHITECTURE 的准备模式。此文描述接入边界，不预先承诺具体功能。

## 最小入口与职责

两版各有 `dev.purifiedundead.ferinupdate.FerinUpdateContent`，参数分别使用 Forge / NeoForge 自己的 `IEventBus`。入口由本体构造器在现有模块注册区调用一次。当前无状态、无事件订阅、无资源访问；不保存事件总线或服务器引用，不引入第二个 @Mod、模块管理器、服务定位器或重复事件总线。

未来确认的更新专属注册与事件安装由此入口组织。已有费林类继续保留各自职责；修改原技能应直接修改其真实拥有者，不能另装一套同义监听器造成重复结算。若将来确需迁移监听器，必须同一变更移除旧安装点并验证只注册一次。

## 现有费林调用链

| 层 | 实际入口 | 接入约束 |
| --- | --- | --- |
| 获取与装备 | AncientContractItem / WarriorRewardService / FerinWarriorItem / FerinEquipment | 获取记录、当前佩戴和反转资格分别判断；不得直接赠物绕过已领取、pending 与保存通知 |
| 合格攻击 | combat/FerinCombatEvents：近战事件、最终生命伤害确认、同刻末大型/多部件补触发 | 保留合法目标、取消/免疫/未扣血过滤及同刻去重；旧原型只描述普通事件路径，不能删补触发 |
| 连段与快照 | FerinComboState / FerinComboTimings / FerinContinuationBuffer / FerinPlayerState | 服务端持有段数、时间、朝向、伤害和暴击快照；续段请求不能启动新召唤或提供可信伤害 |
| 命中 | FerinBladeTrajectory / FerinSweptBlade / FerinCombatEvents.processBlade | 实际剑路命中，单段目标去重；保留队友、宠物和 PVP 过滤；临时无敌帧用 finally 恢复 |
| 伤害与联动 | FerinDamageModel / FerinBlightModel / MemoryEffects / HoenirCombatEvents / GuardianWaveCombat | 不重复套用触发伤害加成；协同斩击不递归触发自身；外部数值及运算后校验有限性 |
| 生命周期 | FerinPlayerStateStorage / FerinCombatEvents 的 clone、换维、登出、关服处理 | 保留旧键和格式；死亡/换维取消活动攻击与排队输入但不刷新召唤冷却；缓存按生命周期清理 |
| 同步与表现 | FerinEntity / client/FerinRenderer / FerinSlashMesh | 实体是表现载体，当前只画刀光；客户端时间/网格不决定真实命中，视觉余辉不增加伤害 |
| 输入 | client/FerinInputEvents、AbilityKeys / FerinContinuePacket / AbilityPreferences | 复用现有技能偏好、续段意图和 ModNetwork；保持服务端资格、窗口和速率检查 |

Forge 与 NeoForge 的伤害事件时点不同，特别是生命扣除和吸收；保留各自实现，不能整类机械互拷。旧 GeckoLib 模型资源与类不是当前活动角色渲染入口。

## 后续内容出现时如何接入

- 新物品/实体只在需求明确后创建 deferred register，使用 `purified_undead` 命名空间；旧 ID、属性修饰符身份、伤害类型标签、资源路径和注册顺序无需求不迁移。玩家可获取内容补创造栏、中文和英文。
- 原有战士奖励走 WarriorRewardService；需要公开显式奖励语义时使用 ProgressApi。API 主线程、非重入约束仍有效。新独立进度确有需求再设计所有者、缺省、迁移、死亡/换维策略，不随意扩充八战士枚举。
- 纯规则沿用现有 Model / State 分层，有实际复杂度才抽取；事件适配处理加载器对象与上下文，注册入口不承担战斗、库存或存档业务。
- 新网络仅在有真实同步需要时加到 ModNetwork；只传动作意图，服务端检查装备、生命状态、目标、距离、冷却、数量和请求频率。需要协议变更时两版一起处理，不占空包编号。
- 客户端入口放在现有 client 生命周期；公共初始化禁止引用 Minecraft、屏幕、渲染器等客户端类。角色模型恢复需独立提取封存资料并重新检查动作与服务器剑路，不能整包覆盖当前代码。
- 配置复用合适的现有所有者；新配置按实际功能定义范围和迁移。配方/标签沿用数据驱动入口，铸造复用 FoundryRecipes 的严格重载、余料及同步语义，JEI 保持可选。
- 运行逻辑不每刻全世界扫描、不重复反射、不引入无界重试；临时上下文 finally 清理。保持 hotfix 已有逐项提交、未知条目保留、真实插入回滚和同步退避保护。

## 附属边界

`spirits_of_expand` 是独立模组，只能由附属依赖本体，本体不得反向依赖附属或预备第三方目标。API v1 当前是进度 API，不是动态战士或费林连段注册 API；本次不扩展其契约。

附属 0.2.0 实际还依赖本体内部 `BlightedRainData.model`、`BlightedRainModel.phase()` 与 `RainExposure.exposedAt`。它是缓存反射和直接调用的集中兼容桥，不是稳定公开 API。费林框架不改这些实现；未来涉及雨模块变更时，必须同时复核桥接与息金 199/200 刻、遮蔽、中断、卸载和保存边界，或另行设计公开雨查询接口及迁移。不要因最低版本为 1.1 就声称未来版本都兼容。

## 验证门槛

空入口不添加复述实现的单测。准备阶段验证双版构建、现有单测、独立服务端完整回归、故障边界/包装链、生产包测试类隔离及附属与本轮本体的加载/息金检查。所有游戏检查读取本轮 _OK / _FAILED，不以 Gradle 退出码代替。

具体内容阶段按实际触发面增加规则与边界测试；客户端视觉、真实配方/JEI、多人或网络、正式加载器/Mixin 验证在相应改动时执行。未跑项目明确记录，旧日志不算本轮结果。开发服务器和测试存档与正式游戏隔离。


## 第一部分增量（2026-11-07）

原唯一战斗事件入口保留。`FerinFlightMotion`/`FerinSlashFlight`分离运动与每发上下文；`FerinWispAnchor`维护逻辑锚点，`FerinWispMotion`双端共用。命中实际入口由`processBlade`改为`processFlight`，原伤害处理保留。同步协议14、客户端有界外推、单发生命周期独立于连段。具体边界与未测内容以[第一部分](PART_1.md)及[外部测试清单](TEST_PLAN_PART_1.md)为准；上方准备阶段的“无配置、协议不变”仅适用于历史空入口。
