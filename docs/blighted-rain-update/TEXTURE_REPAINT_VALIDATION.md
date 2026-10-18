# 贴图重绘验证 · 1.1.0-blighted-rain.2.1

日期：2026-10-15。实际验证根目录 Forge 1.20.1 与 `1.21.1neoforge版本/` NeoForge 1.21.1。

- 两版 `build stageMods runClientSmoke` 完成；单元测试结果 Forge 133、NeoForge 134，失败与错误均为 0（未变化的任务允许 Gradle 复用）。
- 最终增强贴图的游戏内标记：Forge `BLIGHTED_MOBS_CLIENT_OK: all 42`，NeoForge `BLIGHTED_MOBS_CLIENT_OK: all 43`，随后均正常结束构建。此处数字是展示场景数：包括 15/16 类生物、幼年僵尸系、背面、侧面、村民职业衣层、羊毛状态和七种马毛色，并非新增生物数量。
- 实际检查村民、图书管理员、掠夺者、女巫、流浪者等截图；外层衣物与头部纹路生效，未发现缺失贴图。截图保存在各版 `run/blighted-rain-part2-client/screenshots/`，成品预览位于 `outputs/blighted-repaint/humanoid-preview.png`。
- 测试使用独立开发世界；沿用历史 JVM 临时设置 `-XX:TieredStopAtLevel=1`，Forge 开发实例关闭早期窗口。此轮没有重新验证默认 JIT 或所有正式启动器组合。NeoForge 日志仍有第三方 apothic_attributes 闪避音效的字幕翻译缺失提示，不影响此次贴图加载。
- 所有目标 PNG 尺寸、透明通道逐像素匹配各版原版资源：Forge 71、NeoForge 73。公共像素重建脚本重建后与安装包内资源逐字节一致。
- 对照工作前基线，两版正式 Java 源码均未变化；每版 119 个教程书文件及 2 张旧秽鬼傀儡/国王贴图哈希不变。测试辅助类不进入正式 JAR。
- 本轮只改画面资源与验证辅助代码，未重复独立服务端全套玩法回归；沿用第二部分既有功能验证，网络协议仍为 12。

## 安装包

本地成品位于 `outputs/blighted-repaint/`；校验文件为 `SHA256SUMS.txt`。

| 版本 | 文件 | SHA-256 |
| --- | --- | --- |
| Forge 1.20.1 | `purified_undead-1.20.1-1.1.0-blighted-rain.2.1.jar` | `d374a3d1e09c7ddeb0149343249f741778fae09811ece8a262730a67ffdbef0a` |
| NeoForge 1.21.1 | `purified_undead-1.21.1-1.1.0-blighted-rain.2.1.jar` | `fa441369456b5bda992080bd953d8f3893637ac0409594415842a071bd9b7dd3` |

最终运行日志位于 `docs/local-audit/blighted-repaint/forge-strengthened.log` 与 `neo-strengthened.log`（本地审计文件，不进入发布包）。
