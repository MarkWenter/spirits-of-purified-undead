# 污秽之雨预备 · 2026-10-14 验证

## 最终结果

|检查|Forge 1.20.1|NeoForge 1.21.1|
|---|---|---|
|主工程离线 `build stageMods`|通过|通过|
|现有单元测试|127 项，0 失败/错误|128 项，0 失败/错误|
|独立开发服务端完整回归|通过|通过|
|独立包装注入串联|`MIXIN_CHAIN_OK`|`MIXIN_CHAIN_OK`|
|附属与新本体共同加载|`EXPAND_FRAMEWORK_SERVER_OK`|`EXPAND_FRAMEWORK_SERVER_OK`|
|开发客户端基本加载|`PURIFIED_UNDEAD_CLIENT_SMOKE_OK`|`PURIFIED_UNDEAD_CLIENT_SMOKE_OK`（隔离基本探针）|
|生产包检查|新入口存在，测试类未打包|新入口存在，测试类未打包|
|新增 Java 固定格式检查|通过|通过|

服务端使用专用 `run/blighted-rain-preparation/`，世界名 `blighted-rain-framework-test`，本机随机端口与默认配置。运行现有 `audit-server.gradle` 和各版 `audit-chain.gradle`；另用本地 init 脚本设置独立目录，不修改生产构建文件。

两版均核对 16 项必需控制台标记，并检查真实游戏日志正常启动/停止且无失败标记，包括：`PURIFICATION_SERVER_OK`、`LILY_DIARY_OK`、`ADVANCEMENTS_OK`、`PROJECT_AUDIT_OK`、`FERIN_LIFECYCLE_OK`、`HARDCORE_OK`、`ADMIN_API_OK`、`FOUNDRY_SERVER_OK`、`SLATE_SERVER_OK`、`MIXIN_CHAIN_OK`、`AUDIT_BOUNDARIES_OK`、`GUARDIAN_WAVE_OK`、`UNDEAD_COMPAT_OK`、`PURIFIED_UNDEAD_BALANCE_SMOKE_OK`、`PURIFIED_UNDEAD_RELIC_SMOKE_OK` 和 `PURIFIED_UNDEAD_DIARY_SERVER_OK`。其他已输出专项标记也保存在本地日志。

附属检查仅运行自己的隔离世界，验证 API v1、零物品/方块/实体注册。另检查 Forge 缓存中的本体映射依赖确实包含 `BlightedRainContent.class`，排除误用旧同版本 JAR；NeoForge 直接引用新本体完整 JAR。附属源码及资源指纹未改变。

## 首次运行中发现的开发环境问题

1. Forge 原 `run/server/` 的 `acquisition.contractEnabled=false`。进度探针要求契约自然获取开启，因此报 `ADVANCEMENT_FAILED: actual death/clone reward grants root`，Gradle 仍返回成功。保留该失败日志与原配置；没有改生产逻辑，也没有开启原实例路线。改在全新专用目录用默认配置运行后全部通过。
2. NeoForge 默认 `runClientSmoke` 自动扫描全部客户端开发订阅类；`AdministrationClientSmoke` 的私有方法直接引用 `IJeiRuntime`，在未安装 JEI 时，加载器反射方法签名早于测试开关判断，产生 `NoClassDefFoundError`。报错来自原有开发探针；生产包中没有该类。本次基本加载使用本地 `basic-client.gradle` 只编译/装载 `ClientSmoke`，未添加 JEI 必需依赖，也未修改原专项探针或生产兼容代码。检查输出目录只有基本探针，客户端取得真实主菜单标记并正常退出。以后运行原全部客户端探针时仍需处理这个开发工具限制，不能仅关掉 opt-in 属性就认为不会扫描该类。

以上首次失败不计为通过；最终通过对应的是后续隔离运行日志。

## 改动保护与产物

与准备前逐文件 SHA-256 比较，旧文件只改变两版 `PurifiedUndead.java`（各增加一条新模块注册调用）、根 `.gitignore`（放行新更新文档）和统一交接指南（追加入口）。新增两版 `blightedrain` 的注册类/包文档及本更新记录。其余原源码、测试、资源、配置元数据与附属内容均保持原指纹，用户既有修改保留。

开发构建产物：

|产物|SHA-256|
|---|---|
|`build/libs/purified_undead-1.0.jar`（Forge 完整包）|`D3178E6416FE844D1773C651A173B7CE3D972B479FFCCBD77B18827CC25B44D2`|
|`1.21.1neoforge版本/build/libs/purified_undead-1.21.1-1.0.jar`|`9F1E0404F005820FEAA19FD7736F260BD21BC2FF5649D054463F91C9C8626503`|

版本仍为 1.0；这些是更新预备的本地开发构建，最终交付目录、安装包 ZIP、发布记录及源码交付快照未替换。没有操作正式玩家存档。

本地证据统一保存于 `docs/local-audit/blighted-rain-preparation/`：基线指纹、两版 build、首次运行日志、最终 `*-server-isolated.log`、游戏日志、附属加载日志、基本客户端日志及 `results.json`。该目录沿用项目本地审查资料的忽略规则，不进入公开仓库。新增文档链接和 Git 差异检查通过。

## 验证范围

客户端只验证主菜单及资源加载；此次没有新视觉，不宣称检查了新的游戏内画面。未重跑 JEI 专项客户端、所有第三方组合、长期多人压力或真实发布 JAR 的正式加载器实例；没有修改 Mixin，已有正式版启动验证可作为历史背景，不冒充本轮结果。后续实际玩法需增加相应规则与游戏内验证。
