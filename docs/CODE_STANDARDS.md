# 代码规范与复查

适用于 Forge 1.20.1 与 NeoForge 1.21.1 实际工程。保留各自加载器事件、网络、数据组件差异，不用跨版本复制替代验证。

## 格式

Java 使用 google-java-format **1.24.0** AOSP 四空格样式。保留导入、字符串内容和 Javadoc 原文；格式调整不得夹带玩法修改。工具仅用于开发，不加入游戏依赖。

官方工具：https://github.com/google/google-java-format/releases/tag/v1.24.0
下载 `google-java-format-1.24.0-all-deps.jar` 到 `.tools/format/`；SHA-256：
`812f805f58112460edf01bf202a8e61d0fd1f35c0d4fabd54220640776ec57a1`

使用 Java 21 运行格式工具（Forge 编译仍使用 Java 17）：

```powershell
./scripts/format-java.ps1 -Java '<Java21>/bin/java.exe'
./scripts/format-java.ps1 -Fix -Java '<Java21>/bin/java.exe'
```

NeoForge 的脚本位于其工程 scripts 目录，可用 `-Formatter` 指向同一份校验过的工具。不自动下载或接受其他版本。脚本默认只检查，加 `-Fix` 才改文件。

## 实现边界

- 注册 ID、存档键、属性修饰符身份和网络编号属于兼容契约；更名须显式迁移。
- 世界、实体、库存和进度写入在游戏主线程执行；客户端只预测表现，不提供可信伤害或奖励。
- 高频事件先检查作用对象，不扫描整个世界，不在每刻重新查找反射接口；缓存必须按玩家退出或服务器停止清理。
- 可选模组访问经明确的桥接类，查找缓存，失配记录并退回已有原生行为。不要在整个 tick 或伤害流程外捕获 Throwable。
- 有作用域的额外伤害身份、递归防护使用 finally 恢复；不能改写真实 DamageSource 来伪装击杀要求。
- 新增包装式 Mixin 优先使用可串联的 WrapOperation，调用传入的 original，不能绕过其他包装器直接调用原方法。无理由不降低注入匹配要求来隐藏错误。
- 资源、配方、配置有数量及长度边界；非法重载保留旧配置。数据恢复不擅自删除玩家物品。
- 第三方代码不能从反编译文件复制进业务源码。使用公开 API、注册标签或明确许可的库；注明库版本与许可。参考行为后由项目独立实现。

## 验证

`build stageMods` 后运行独立服务器与客户端。服务器完整回归使用 `-I scripts/audit-server.gradle`；附加 `-I scripts/audit-chain.gradle` 可验证两个独立包装器修改同一随机调用仍共存。测试注入器、假玩家和探针只属于 src/smoke，禁止打入发布包。

Mixin 修改后必须再运行真实发布 JAR 的独立正式加载器启动检查；开发环境映射不能代替正式验证。检查游戏内 `_OK` 标记、失败断言和实际退出状态，不能只看 Gradle 成功。
