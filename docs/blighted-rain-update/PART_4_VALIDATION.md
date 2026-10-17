# 第四部分验证

版本 1.1.0-blighted-rain.5。两版独立构建，开发实例测试，不操作正式存档。

## 服务端

最终 build stageMods 和完整服务端回归核对 RAIN_PART4_SERVER_OK、RAIN_PART3_SERVER_OK、BLIGHTED_TRAITS_SERVER_OK、BLIGHTED_MOBS_SERVER_OK、PURIFICATION_SERVER_OK；不以Gradle退出码替代游戏内断言。
新增断言覆盖：世界选项初始化、赠送一次/重载不重复/旧世界不赠送；两种植物实际右键放置、消耗、支撑、掉落及原食用功能；伊莱恩混合击杀；普通僵尸不能获取海尼尔、秽鬼标签可以；真实秽鬼村民治愈后法腾归属和补发去重。

## 客户端

两版 RAIN_PART4_CLIENT_OK、RAIN_PART4_WORLD_OPTION_OK；NeoForge同时 RAIN_PART4_CREATIVE_OK，八件第三部分新增物品位于实际模组创造栏。
检查实际截图：盾牌主副手、格挡、第三人称内外侧、植物、物品模型、原生世界规则编辑器。通过原生按钮切换永雨选项，确认值变化。此前发现的盾牌纹理图集路径问题已修正，最终日志无对应缺失纹理警告。

两版洁白莲花/猩红莲花的原物品PNG逐字节等于本轮前Forge发布基线；物品模型无更改。仅新增放置形态贴图。

## 构建记录

- Minecraft 1.20.1：134项单元测试，失败0，错误0。
  - 文件：`purified_undead-1.20.1-1.1.0-blighted-rain.5.jar`
  - SHA-256：`a8b6b61f7712a562573bc90696e4c1e2270d04b64f12a5501bf8f071d099d197`
- Minecraft 1.21.1：135项单元测试，失败0，错误0。
  - 文件：`purified_undead-1.21.1-1.1.0-blighted-rain.5.jar`
  - SHA-256：`7c164545b6a251974bcd4fcebb482ec86ee7061f07b3580d15df6641801ae237`

安装包逐项核对新增类、方块贴图、盾牌图集文件、全部JSON解析；不包含测试探针/Smoke类。Excel已导出并检查27项及排版。白巫女手记未修改。

两版最终发布JAR均在独立正式加载器客户端启动到主菜单，`PRODUCTION_STARTUP_OK`，退出码0。安装包与此检查使用的stageMods文件哈希一致。
