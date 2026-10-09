# 石板更新 · 0.52.1 启动修复

两份用户报告的根因相同：Forge 正式环境应用 MemoryKillCriteriaMixin 时，未将 matches 映射为 m_48130_，因此在进入菜单前崩溃。此前开发环境启用 SRG→官方名称重映射，未暴露安装包映射键错误。

修复 purified_undead.refmap.json 的 mappings 与 data.searge 中全部类名键：使用 JVM 内部名称 dev/purifiedundead/mixin/...，替代 Java 点分隔名称。原字段、方法 SRG 值不变。没有禁用追忆掉落/击杀兼容，也没有降低注入失败检查级别。

新增 ProductionRefmapTest，检查正式运行时类名格式、两套上下文一致、类资源存在及关键击杀/字段映射。NeoForge 无该 SRG 映射文件，维持其正确的官方名称实现，版本同步为 0.52.1。

本轮验证以实际发布 JAR 为启动对象，使用独立游戏目录、临时离线测试身份与原始前置 JAR；启动探针仅放入该测试实例，不进入交付包。Forge 47.4.23 旧包复现原错误；修复包和 NeoForge 21.1.248 均产生 PRODUCTION_STARTUP_OK，确认到达 TitleScreen，全部新增注入目标已加载。Forge 47.3.22 / NeoForge 21.1.248 开发服务端全套回归通过；Forge 可选联动回归包括真实星月遗物 AddItemModifier 条件测试并通过 CELESTIAL_ETCHING_CRITERIA_OK。

未运行用户存档；未声称整个整合包所有组合通过。NeoForge 实际星月遗物联动未新增实模组验证。本次不更改玩法、原有白巫女手记正文或已有资源。

后续新增或修改 Mixin 时，开发环境通过不能替代生产 JAR 的正式加载测试；须同时核对真实游戏通过标记。
