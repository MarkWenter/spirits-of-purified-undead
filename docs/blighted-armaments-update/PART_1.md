# 秽鬼武装第一部分 · 息金

开发版本 1.3.0-armaments.1。从 Spirits of Expand 0.2.0 移植息金矿、息金及剑、斧、锄、镐、锹。七件物品进入涤净亡魂创造栏，注册名沿用原后缀，命名空间改为 `purified_undead`。

在主世界放置普通金矿石，露天连续经历 200 个已加载的污秽降水刻后转化。沿用本体降水与遮蔽判定，包括污秽之雪。背包内物品、天然矿石和深层金矿石不参与。离开有效降水或区块卸载会清除部分进度，重启时恢复已记录的位置。

矿石掉落、时运、精准采集、挖掘疲劳、五种有序配方、375 耐久与修复材料沿用附属实现。两张新图在 32×32 整数网格上精修；五件工具沿用原贴图。旧附属拥有独立注册名；本轮没有自动改写旧方块、库存或旧附属的位置记录。

运行开发回归时为独立服务端启用 `purified_undead.purificationSmoke` 与 `purified_undead.breathgoldSmoke`，检查 `BREATHGOLD_SERVER_OK` 和 `PURIFICATION_SERVER_OK`。独立客户端启用 `purified_undead.breathgoldClient`，检查 `BREATHGOLD_CLIENT_OK` 并查看截图。测试类位于 smoke 源集。
