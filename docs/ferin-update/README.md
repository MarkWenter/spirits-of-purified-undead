# 费林更新

当前候选为 **1.2.0-ferin.20**，适用于 Forge 1.20.1 与 NeoForge 1.21.1，协议 16。ferin.20 为纯化铸造器的灰色石壳补了少量深浅石纹，覆盖立柱、顶沿和底座，待机与工作状态共用这层石纹。

ferin.17 重绘材料，ferin.18 重绘饰品与追忆，ferin.19 更新无垢护甲、纯化铸造器和秽鬼刷怪蛋。各批次图像见[旧贴图与重绘成品](../../旧贴图/README.md)。费林相关实现见[骑士寂夜女仆虚影与骨架接入](PART_3.md)、[饰品与绑定](PART_2.md)和[动态刀光](DYNAMIC_SLASH.md)。

纯化铸造器的[正、侧、背三视图](purification_foundry_three_views.png)对应[可编辑的 GeckoLib Block 工程](purification_foundry.bbmodel)，模型内嵌待机图集。放置时使用 `geo/block/purification_foundry.geo.json`，按方块朝向旋转，按原有 `LIT` 状态选择待机或工作图集。物品栏、掉落物与手持沿用 Java 方块物品的显示变换，使用相同尺寸与 UV 的方块模型。修改几何时应同时更新两份导出，`FoundryModelResourcesTest` 核对两个显示入口的方体位置与尺寸。

- [当前交接、架构复核与验证](HANDOFF.md)
- [第一部分实现与边界](PART_1.md)
- [外部测试步骤和回传模板](TEST_PLAN_PART_1.md)
- [架构与接入约定](ARCHITECTURE.md)
- [准备阶段项目现状](PROJECT_STATE.md)
- [准备阶段历史验证](VALIDATION.md)

以下为2026-11-06准备阶段历史记录，不描述第一部分后的当前状态：

# 费林更新 · 更新预备

日期：2026-11-06。阶段：框架已接入，具体内容等待用户后续指示。主模组两版版本仍为 **1.1.1hotfix**，网络协议 **13**，公开进度 API **v1**。

本次沿用污秽之雨更新的准备模式：独立更新目录、最小模块入口、现状核对与验证记录。两版 `PurifiedUndead` 各调用一次 `ferinupdate/FerinUpdateContent.register(modBus)`。当前入口仅校验事件总线参数，没有注册新物品、事件监听器、配置、数据、网络包或客户端行为。

- [架构与接入约定](ARCHITECTURE.md)
- [项目现状与历史甄别](PROJECT_STATE.md)
- [本次验证与适用范围](VALIDATION.md)

更新名称不构成玩法设计，也不自动授权恢复未完成角色模型。既有费林战斗、刀光、获取、追忆、按键、存档与附属依赖保持原位。下一阶段收到实际需求后，按受影响的链路增量实现，不提前设计新技能、数值、配方或存档格式。

本阶段没有发布新版本、覆盖安装目录、刷新交付源码快照或执行 Git 提交/推送。准备源码属于当前开发工作区；历史正式安装包仍代表原发布版本。
