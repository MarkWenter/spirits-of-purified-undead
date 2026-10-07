# 秽鬼之守护 · 0.48.0

## 实现与边界

剑气由服务器创建短命实体；客户端只发送挥剑意图，不发送伤害和目标。同玩家每游戏刻仅接受一次请求，检查存活、主手武器、旁观、使用物品与眩晕状态。没有全世界实体扫描；只查询路径附近目标。实体不写入存档，结束、阻挡、主人死亡或换维度后清理。

状态由挥剑时捕获；命中使用作用域内的状态上下文，异常时也会清理，不临时篡改玩家的落地或疾跑字段。标准玩家近战事件可被其他模组监听；并执行附魔命中回调、火焰/击退等武器效果。仅监听“直接点击实体”而非伤害事件的第三方技能，仍可能需要该模组专门适配，不能承诺所有模组的非标准战斗系统。

费林自身协同斩击继续使用原辅助伤害类型，不递归产生协同连段。剑气作为玩家近战伤害会触发正常协同判定；原有连段时窗、同刻去重和目标过滤不变。主手护甲属性不在副手生效。

## 实测版本

| 平台 | 外部模组 | 结果 |
| --- | --- | --- |
| Forge 1.20.1 | Apotheosis 7.4.8 | 4 项参考剑可用附魔通过适用性及真实铁砧结果检查 |
| Forge 1.20.1 | Celestial Enchantments 1.3.7 / Celestial Core 1.6.0 / L2Library 2.5.3 | 24 项通过；同时修正吸收生命与费林吸血事件顺序差异 |
| Forge 1.20.1 | Goety 2.5.57.3 | 1 项通过 |
| NeoForge 1.21.1 | Apothic Enchanting 1.6.3 | 2 项通过真实铁砧检查 |
| NeoForge 1.21.1 | Goety 3.2.0 | 1 项通过真实铁砧检查 |

这些数量是本次参考武器规则匹配的附魔数，不代表任意互斥附魔可同时存在。自动经验修补规则保持不变；已经附魔的武器仍须遵守附魔台/铁砧本身的规则。1.21.1 的星月附魔入口已实现，但未获得对应成品进行实测；不将 1.20.1 的 JAR 装入 NeoForge。

外部测试模组仅用于独立开发环境，不进入交付依赖，不操作正式玩家世界。

## 验证

GUARDIAN_WAVE_OK 检查主手护甲、副手限制、剑刃/剑气独立伤害、120% 倍率、独立暴击倍率、三格范围、不穿墙、队友过滤、到期清理、下落状态保留、格洛特眩晕、伊莱恩魔弹、海尼尔标记、费林触发与同刻请求去重。

GUARDIAN_ENCHANT_OK 在实际安装上述可选模组后遍历相应附魔并执行真实铁砧菜单合成。GUARDIAN_CLIENT_OK 通过客户端输入事件发送网络请求，核对同步实体、倾角、渲染及到期清理，并保存游戏截图。完整旧功能回归及单元测试随两版交付日志保存。

源码入口：combat/GuardianWaveCombat、entity/GuardianWaveEntity、client/GuardianWaveInput 与 GuardianWaveRenderer、network/GuardianWavePacket、compat/GuardianEnchantments。

参考版本来源：[Goety 官方发布页](https://www.curseforge.com/minecraft/mc-mods/goety/files/all)、[Apothic Enchanting 官方发布页](https://www.curseforge.com/minecraft/mc-mods/apothic-enchanting)、[星月附魔作者发布页](https://www.curseforge.com/minecraft/mc-mods/celestial-enchantments)。Forge 具体附魔规则另以用户本机 JAR 核对。
