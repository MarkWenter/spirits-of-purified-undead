# 配置与管理指令

适用 1.1.0-blighted-rain.5，Forge 1.20.1 与 NeoForge 1.21.1。指令根为 `/purifiedundead`，可用 Tab 补全。普通玩家仅能查询自己的进度；所有管理操作及查看其他玩家进度要求权限等级 2，服务器控制台也可使用。目标只接受当前在线玩家，支持 `@s`、`@a` 等玩家选择器，不修改离线存档。

## 指令表

| 指令 | 用途 |
| --- | --- |
| `/purifiedundead` | 简要帮助 |
| `/purifiedundead status` | 查询自身护身符等级、战士／契约获取与待补发状态、伊莱恩符合条件的混合击杀计数 |
| `/purifiedundead status <玩家>` | 管理员查询一名在线玩家 |
| `/purifiedundead warrior grant <玩家或选择器> <战士>` | 标记获取并发放对应饰品；已获取不重复发放，满背包进入待补发 |
| `/purifiedundead warrior reset <玩家或选择器> <战士>` | 清除该战士的获取及待补发记录；伊莱恩同时清除符合条件的混合击杀计数。保留已有物品与进度成就 |
| `/purifiedundead warrior collect <玩家或选择器> <战士>` | 回收背包、主副手、光标及 Curios 实际／外观槽中的对应饰品，并标记已获取、取消待补发 |
| `/purifiedundead contract grant <玩家或选择器>` | 一次性发放古老民族之契，沿用满背包补发 |
| `/purifiedundead talisman set <玩家或选择器> <等级>` | 设置护身符等级，范围为 0 至配置的最大等级；0 即重置等级 |
| `/purifiedundead rewards retry <玩家或选择器>` | 立即尝试发放所有已有待补发奖励，不生成新的奖励资格 |
| `/purifiedundead config common` | 列出普通配置键 |
| `/purifiedundead config slate` | 列出石板配置键 |
| `/purifiedundead config common <键>` | 查询该普通配置的服务端当前值，例如 `acquisition.ferinEnabled` |
| `/purifiedundead config slate <键>` | 查询石板当前值，例如 `slate.ferinStageDamageBonus` |
| `/purifiedundead config reload foundry` | 严格验证并重载铸造 JSON 配方，同步在线客户端与 JEI |

战士标识：`ferin` 费林、`groth` 格洛特、`julius` 尤里乌斯、`guardians` 西丽亚与西丽德、`ulv` 狼、`eleine` 伊莱恩、`hoenir` 海尼尔、`faden` 法腾。姐妹是一组奖励。

例如：`/purifiedundead warrior grant @s groth`；`/purifiedundead talisman set @s 10`。

战士能力仍取决于实际佩戴饰品，获取记录只管理一次性奖励。`reset` 不回收物品，满足自然条件时可重新获得；尤其佩戴契约时，费林可能很快重新发放。`collect` 保留“已获取”以防下一刻自动发回来，且不会扫描末影箱、嵌套背包或世界容器。需要重新授予已经回收的战士时，先 `reset` 再 `grant`。若希望长期关闭自然获取，应同时关闭对应 `acquisition` 开关。以上操作不回滚原版进度成就；需要时可单独使用原版 `/advancement revoke`。

管理员授予与 API 授予不受自然获取开关限制，适合整合包任务奖励。授予契约不会强行装备；新获得的物品仍须正常佩戴。游戏已有五刻一次的契约提示更新和饰品属性刷新继续使用原流程。


## 污秽之雨指令

以下均要求权限等级2，且执行环境为主世界。

| 指令 | 作用 |
| --- | --- |
| `/purifiedundead rain status` | 查看事件阶段、剩余秒数、永雨锁定状态 |
| `/purifiedundead rain start [秒数]` | 在正在下雨、无事件/冷却且无生效信标时开启预警；指定1–600秒，省略随机300–600秒；不绕过这些条件 |
| `/purifiedundead rain stop` | 停止正在预警/进行的事件，进入冷却 |
| `/purifiedundead rain reset` | 停止事件并重置冷却 |
| `/purifiedundead rain weather locked` | 开启永雨锁定 |
| `/purifiedundead rain weather vanilla` | 取消永雨锁定，恢复自然天气；不关闭雨中随机事件 |

原版游戏规则命令新增 `/gamerule purifiedUndeadEternalRain true` 或 `false`，控制永雨。创建世界时也能设置；后来切换不会给旧世界追加初始纹章。

配置及完整键列表见 [当前配置清单](PART_4_CONFIG.md)。模组没有通用 config set 指令，也没有忽略所有条件的 rain force 指令；无视冷却/信标的玩法入口是秽雨纹章。
