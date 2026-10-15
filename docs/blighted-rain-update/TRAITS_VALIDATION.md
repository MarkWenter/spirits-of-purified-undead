# 独有特征与秽鬼蜘蛛验证

日期：2026-10-16。版本：1.1.0-blighted-rain.3。两版实际工程均构建、运行，未操作正式存档。

| 检查 | Forge 1.20.1 | NeoForge 1.21.1 |
| --- | --- | --- |
| 构建及原有单元测试 | 133 通过 | 134 通过 |
| 服务端完整回归 | 通过 | 通过 |
| 新增特征行为回归 | 通过 | 通过 |
| 实际刷怪蛋交互 | 18 种通过 | 19 种通过 |
| 客户端针对性检查 | 5 个视角/模型通过 | 5 个视角/模型通过 |
| 最终正式包启动 | Forge 47.4.23 通过 | NeoForge 21.1.248 通过 |

## 实际行为证据

- `BLIGHTED_TRAITS_SERVER_OK`：新生成属性、旧存档迁移、再次保存加载不重复缩放、真实伤害事件、近战和投射物伤害加成、溺尸缓慢时长、原版流浪者缓慢箭/NeoForge 沼骸中毒箭。
- 实际手持纯净灵药交互、喷溅型拒绝、玻璃瓶返还、重复点击不扣第二瓶；净化进度保存加载、缩短测试计时后真实恢复村民、恢复职业和原版属性。
- 16 种羊颜色的实际剪毛掉落均为红色，保留原本颜色；Forge 额外测试其手持剪刀 `onSheared` 路径。
- `BLIGHTED_MOBS_SERVER_OK`：Forge 16 / NeoForge 17 种新派生生物转化，按各自原版基础血量验证倍率，含蜘蛛；骑乘、装备、雨伤免疫及独立碎片原有回归通过。
- 骷髅实际弓 AI：10 刻尚未完成拉弓，29 刻已完成，20 刻原版间隔按 70% 频率放慢为 29 刻。女巫、三叉戟、掠夺者及近战原有加速检查仍通过。
- 猪牛正式 JAR 中掉落池与原版猪牛内容逐项一致，追加池与对应版本原版僵尸腐肉池逐项一致。
- 原有 `BLIGHTED_RAIN_SERVER_OK`、`PROJECT_AUDIT_OK`、`HARDCORE_OK`、`PURIFICATION_SERVER_OK` 等完成标记全部出现，未仅凭 Gradle 返回码判定。

客户端使用既有独立 `blighted-rain-part2-client` 开发实例，检查蜘蛛正面/侧后方、骷髅、村民、羊，以及创造栏 18/19 种刷怪蛋。两版截图均已人工查看，蜘蛛原版形态、纹路与眼部正常，存在 `BLIGHTED_MOBS_CLIENT_OK` / `RAIN_FIX_CLIENT_OK`。本次为受影响范围的针对性检查，未重复所有旧生物的完整外观图集。

两版最终正式 JAR 在独立游戏目录运行原生加载器，核对 `PRODUCTION_STARTUP_OK`，覆盖修改的弓攻击计时注入目标。沿用开发验证的 C1 JVM 启动参数，避免此前无关的本机 JIT 崩溃；未改玩家启动配置。

初轮发现并处理：测试攻击受前置模组随机暴击干扰，测试固定暴击率为零；NeoForge 使用小写 `attributes` NBT 字段，修正迁移逻辑。一次 ForgeGradle 重映射工具失败，独立重新打包后成功；最终包额外通过正式启动验证。

## 产物

两版安装包位于本地 `outputs/blighted-rain-3/`，Forge 为包含 MixinExtras 的完整包，非 slim。检查正式包不含测试类，资源内容与最终源码资源一致。

- `purified_undead-1.20.1-1.1.0-blighted-rain.3.jar`
  - SHA-256：`a396dcae0abc94b46e7899769cdb6252782e878fca57ff7c14aa2482080fc763`
- `purified_undead-1.21.1-1.1.0-blighted-rain.3.jar`
  - SHA-256：`1dbf88f60e94d1a76a419a549092c501551708c8a8c5e27a0725c20e956a5978`

本地详细日志：`docs/local-audit/traits/` 下 `forge-final.log`、`neo-final.log`、两版 `*-client.log` 与 `*-production-final.log`。仅 Forge 源码及公共更新记录推送 GitHub 更新分支；NeoForge 实现与安装包保留在实际工程中。
