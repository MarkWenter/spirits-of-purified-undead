# 0.51.0 · 整合包收魂名单兼容

## 证据与修复

只读检查用户实例确认本模组 JAR SHA-256 与 0.50.0 发布一致，日志确实加载该版本；Malum 配置允许刷怪笼生物并开启默认精魂数据。实际原因位于 KubeJS server_scripts/other_mod/soul_hunter_weapon.js：removeAll 清空 soul_hunter_weapon，随后只放回三件法杖和三件镰刀。

开发测试先移除秽鬼之守护的原生收魂标签：0.50.0 真实剑刃击杀无法产生精魂，成功复现。0.51.0 新增 GuardianMalumSoulCompatibility：仅在服务端死亡事件、来源归属 ServerPlayer、主手为本武器、缺少原生标签且配置启用时处理。在 Malum 自身死亡处理之前，调用其原生暴露精魂接口；并不自行执行掉落，也不改 soulless / spawner 状态。费林仍保留协同伤害类型，不改成玩家近战，不引入递归协同。

Forge 使用 SoulDataHandler.exposeSoul；NeoForge 使用 LIVING_SOUL_INFO 的 LivingSoulData.setExposed。通过可选、缓存的反射接入，避免没有 Malum 时类加载失败。只缓存接口，不缓存实体或世界；不兼容 API 仅记录一次并停止补救。

## 性能与控制

额外代码只运行于死亡事件，主体是若干条件判断。标签正常时不调用 Malum 补救接口；接口查找仅首次需要时执行。没有每刻扫描、全球实体查询、延迟任务或无上限缓存。此为代码路径审查结论，不宣称完成大型整合包性能基准或长期压力测试。

配置 `[compatibility] guardianMalumSoulHarvest = true` 控制本补救，关闭后保留 0.50.0 的标签方式。

如果只维护单个整合包，也可在其清空名单的脚本最后追加：

```javascript
event.add('malum:soul_hunter_weapon', ['purified_undead:blighted_guardian'])
```

该行必须位于原有 ServerEvents.tags 回调内、removeAll 之后。此方案没有额外收魂补救调用，但需要整合包更新时保留。未修改用户实例脚本或正式存档。NeoForge 原生标签为 soul_shatter_capable_weapon，不能机械复用 Forge 标签名。

## 验证范围

两版使用真实 Malum/Lodestone，在测试中移除本武器收魂标签，验证附魔武器剑刃、剑气、费林伤害击杀实际生成精魂实体；关闭补救后不掉落，普通铁剑不会被授予收魂能力。标记为 GUARDIAN_SOUL_DROPS_OK。此前曾因只测铁砧而漏掉真实收魂，此处继续要求观察真实掉落。

同时运行不安装 Malum 的基础回归、可选环境回归、两版客户端与单元测试。测试复现了整合包的名单移除条件，并非启动整套神秘启旅、也未读写正式世界。实例原始日志与诊断只保存在本地，不进入公开仓库。
