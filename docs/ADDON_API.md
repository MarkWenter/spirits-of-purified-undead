# 扩展模组接口 v1

0.55.0 起提供 `dev.purifiedundead.api` 下的公开接口。Forge 1.20.1 与 NeoForge 1.21.1 使用相同的类名和方法语义，但必须分别编译对应加载器／Minecraft 版本，不能共用二进制。附属模组应声明本模组为前置，最低版本 `0.55.0-dev`（Forge）或 `0.55.0-neoforge-dev`（NeoForge）。可选兼容模组须延迟加载适配类，避免在本体不存在时直接引用 API 类。

## 玩家进度与奖励

`ProgressApi.VERSION == 1`。以下方法只允许服务端主线程访问；从异步任务发起时，先使用 `server.execute(...)` 调度。API 不检查附属模组自己的任务条件或权限，由调用方负责。

```java
import dev.purifiedundead.api.ProgressApi;
import dev.purifiedundead.api.WarriorId;

var before = ProgressApi.snapshot(player);
var result = ProgressApi.grantWarrior(player, WarriorId.GROTH);
// result: DELIVERED / PENDING / ALREADY_OBTAINED
// 自然获取开关不限制明确的任务奖励；重复调用不会复制奖励。
```

| 方法 | 契约 |
| --- | --- |
| `snapshot(ServerPlayer)` | 不可变快照：每个战士的已获取／待补发状态、契约状态、护身符等级及伊莱恩溺尸计数；不暴露可变 NBT |
| `grantWarrior(ServerPlayer, WarriorId)` | 一次性发放；满背包挂起，沿用登录／每秒恢复机制；死亡换体保留记录 |
| `grantContract(ServerPlayer)` | 同样的一次性契约发放 |
| `resetWarrior(ServerPlayer, WarriorId)` | 重置指定获取记录；保留饰品与成就，不应在普通奖励流程中调用 |
| `setTalismanLevel(ServerPlayer, int)` | 设置配置范围内的护身符等级；越界拒绝，不能借机覆盖其他玩家数据 |

`WarriorId` 包含八组现有战士；`id()` 是固定小写标识，`item()` 返回对应已注册饰品（仅在注册完成后调用）。快照表示获取记录，不代表玩家当前拥有或佩戴物品，不可将其当作额外伤害效果开关。

## 进度变化事件

在加载器的游戏事件总线上监听 `ProgressChangedEvent`：Forge 使用 `MinecraftForge.EVENT_BUS`，NeoForge 使用 `NeoForge.EVENT_BUS`。事件为非可取消通知，在玩家数据保存后发出，包含 `player()`、`before()`、`after()`。自然获取、待补发完成、护身符升级以及指令/API 修改共用通知入口；无实际变化时不发事件。

```java
// 注册到游戏事件总线的监听方法；SubscribeEvent 使用对应加载器的注解。
public static void onProgressChanged(ProgressChangedEvent event) {
    var id = WarriorId.GROTH;
    if (!event.before().warriors().get(id).obtained()
            && event.after().warriors().get(id).obtained()) {
        // 更新附属模组自己的任务状态；满背包时仍可能处于 pending。
    }
}
```

不要在事件回调内同步修改本体进度：API 会拒绝这种重入，防止奖励递归。需要后续修改时由附属模组排队到后续服务端任务，且必须自己防止循环。通知不是发奖事务的取消点，回调失败不撤销已保存的奖励。正常注册的监听器无需扫描玩家或每刻反射；API 只在既有进度保存发生时通知。不要依赖 `progress` 包内部实现、私有存档键或网络包编号作为稳定接口。

## 数据驱动的兼容入口

- 战利品／手记的亡灵识别继续使用现有实体类型标签，详见 [亡灵联动](UNDEAD_COMPATIBILITY.md)。添加标签时使用 `replace:false`，可选实体使用 `required:false`。
- 铸造 JSON 支持其他模组的已注册物品 ID，保留 `consumeTop` 两类配方与严格重载，详见 [配置与指令](COMMANDS_AND_CONFIG.md)。无需附属模组替换方块实体或容器代码。
- 原版合成／锻造配方和标签仍可以数据包添加；Forge 1.20.1 使用复数 `recipes`／`tags/entity_types` 等目录，NeoForge 1.21.1 使用对应单数目录。

此 v1 提供任务奖励、进度观察和既有数据兼容入口；没有开放动态扩充八重污秽、契约槽位定义或费林连段注册器。新增战士角色及全新战斗行为仍应由附属模组注册自己的物品和事件，并在后续接口版本中单独设计；不要修改现有战士枚举或覆盖存档格式。升级本体前建议用独立开发实例验证附属模组。
