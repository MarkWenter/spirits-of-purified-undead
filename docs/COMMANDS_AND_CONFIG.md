# 配置与管理指令

适用 0.55.0，Forge 1.20.1 与 NeoForge 1.21.1。指令根为 `/purifiedundead`，可用 Tab 补全。普通玩家仅能查询自己的进度；所有管理操作及查看其他玩家进度要求权限等级 2，服务器控制台也可使用。目标只接受当前在线玩家，支持 `@s`、`@a` 等玩家选择器，不修改离线存档。

## 指令表

| 指令 | 用途 |
| --- | --- |
| `/purifiedundead` | 简要帮助 |
| `/purifiedundead status` | 查询自身护身符等级、战士／契约获取与待补发状态、伊莱恩溺尸计数 |
| `/purifiedundead status <玩家>` | 管理员查询一名在线玩家 |
| `/purifiedundead warrior grant <玩家或选择器> <战士>` | 标记获取并发放对应饰品；已获取不重复发放，满背包进入待补发 |
| `/purifiedundead warrior reset <玩家或选择器> <战士>` | 清除该战士的获取及待补发记录；伊莱恩同时清除溺尸计数。保留已有物品与进度成就 |
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

## 配置文件

配置仍在游戏／服务器的 `config` 中，文件名和既有键不改名，不覆盖自定义数值。新增中文分组说明，保留原有边界校验与历史迁移逻辑。全键、默认值和范围见[配置索引](CONFIG_REFERENCE.md)。

| 文件 | 内容 | 应用方式 |
| --- | --- | --- |
| `purified_undead-common.toml` | 契约、获取路线、护身符、八组战士、遗物及可选兼容 | 两端保持一致，重启 |
| `purified_undead-slate.toml` | 追忆和石板数值 | 两端保持一致，重启 |
| `purified_undead-foundry.json` | 铸造／灌注配方、每次原料和燃料消耗、加工时间 | 指令安全重载，或重启 |

前两份 TOML 建议退出游戏／停止服务器后编辑并重启。普通配置不是完整的服务器配置同步系统，尤其饰品槽数、移动预测和客户端说明，应保持客户端与服务端一致；本次没有提供可能造成两端不一致的通用运行时改值指令。`revision` 与 `balanceRevision` 是历史迁移标记，不应作为平衡数值随意修改。关闭自然路线不会删除已持有饰品、已获取记录或已挂起的奖励。

## 铸造配方重载

上槽 `top`，下槽 `bottom`，成品 `output`；`consumeTop=false` 为不消耗上槽的灌注。数量与左右燃料字段均为每个配方单位的消耗；`ticks=400` 为 20 秒。示例：

```json
{"recipes":[{"top":"minecraft:coal","topCount":1,"bottom":"minecraft:coal","bottomCount":1,"output":"minecraft:diamond","outputCount":2,"consumeTop":true,"leftFuel":1,"rightFuel":1,"ticks":400}]}
```

文件最多 8 MiB、4096 条配方，并须满足同步数据大小上限；物品必须已经注册，数量及时间须合法。手动重载采用整份验证：任意一条有误时整份拒绝，当前配方不变，原文件保留供修正。显式空 `recipes` 数组可停用全部铸造配方。启动时仍沿用旧有加载规则，坏条目会报告并跳过。

成功后向在线玩家同步配方；后续登录仍通过原登录同步接收。进行中的机器每刻核对配方，配方发生改变则重新计时，不会提前消耗原料；已经完成的成品保留。离线／未加载机器无需扫描。JEI 的现有配方刷新继续使用原有客户端同步入口。
