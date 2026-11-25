# 费林更新预备 · 本次验证

日期：2026-11-06。对象：Forge1.20.1、NeoForge1.21.1的1.1.1hotfix工作区 + 费林空入口；附属0.2.0。不是新内容发布。

## 结果

| 检查 | Forge | NeoForge |
| --- | --- | --- |
| 离线 build / stageMods | 通过 | 通过 |
| 现有单元测试（本轮执行 test，非UP-TO-DATE） | 140项，零失败/错误/跳过 | 141项，零失败/错误/跳过 |
| 新建独立开发世界完整服务器回归 | 33个_OK，无_FAILED | 33个_OK，无_FAILED |
| 费林生命周期/大型目标 | FERIN_LIFECYCLE_OK、FERIN_LARGE_MOB_OK | 同左 |
| 既有hotfix边界和Mixin包装链 | FAILURE_BOUNDARIES_OK、HOTFIX_COST_BOUNDARIES_OK、HOTFIX_TRANSACTION_BOUNDARIES_OK、MIXIN_CHAIN_OK | 同左 |
| 本轮本体 + 附属0.2.0真实加载/API | EXPAND_FRAMEWORK_SERVER_OK，明确base=1.1.1hotfix、api=1 | 同左 |
| 附属息金服务器检查 | BREATHGOLD_SERVER_OK，132项 | 同左 |
| 可选目标全部缺失的检测/延迟闸门 | EXPAND_OPTIONAL_BASELINE_OK，256种合成组合 | 128种合成组合 |
| 生产JAR包含新入口且无smoke/测试注入 | 通过；完整jarJar内含MixinExtras | 通过；使用加载器内置库 |
| 与正式1.1.1hotfix的生产class差异 | 仅PurifiedUndead改变、新增FerinUpdateContent | 同左 |
| 与正式1.1.1hotfix的全部assets/data比较 | 内容一致，4份文本仅换行差异 | 逐字节一致 |
| 附属工程清点范围内源码/资源/文档/构建配置 | 指纹保持不变 | 指纹保持不变 |

Forge与正式包对比的4份纯换行差异为en_us.json、zh_cn.json、blighted_precipitation.fsh和vsh；与本轮开始时源码资源指纹相比没有改变，不为凑字节一致覆盖用户工作区。

33个游戏标记与2026-10-25正式复验日志的标记集逐一对比，没有缺项；包含奖励、进度、净化、铸造、追忆、污秽雨与三个故障边界。故障注入会刻意打印异常，已按预期库存呈现失败、转换拒绝、极值伤害、移除后监听失败等场景区分，不能以日志存在ERROR直接判失败，也不能忽略真正的_FAILED。

构建出现既有弃用警告（Forge ModLoadingContext.get、NeoForge FerinModel API），本轮未为消除警告而改动无关代码。没有添加镜像空入口实现的单测。

## 可复核命令与位置

在本体根目录执行：

```powershell
& ./scripts/gradle.ps1 --offline build stageMods
& './1.21.1neoforge版本/scripts/gradle.ps1' --offline build stageMods
# 两版分别使用各自launcher，下面为Forge示例：
& ./scripts/gradle.ps1 --offline runServer -I scripts/audit-server.gradle -I scripts/audit-chain.gradle -I outputs/ferin-preparation-2026-11-06/server.gradle
```

附属使用原工程各自launcher与framework-smoke.gradle，命令参数覆盖`-Pbase_mod_version=1.1.1hotfix`，不修改默认1.1配置；附加本轮addon-server.gradle改变测试目录。两版原脚本都会将expectedBaseVersion设置为实际选择版本，因此日志明确验证本轮本体，不误用旧JAR。

本地证据：`outputs/ferin-preparation-2026-11-06/` 下的四份server日志、两份build日志、verification.json、SHA256SUMS.txt、inventory-before.json。新本体开发世界在该目录server-forge/server-neo；附属新开发世界在各自工程`run/ferin-preparation-2026-11-06/`。复用既有开发EULA的接受记录，仅创建测试配置和新世界，没有操作正式存档。

构建包位于根工程`build/libs/purified_undead-1.1.1hotfix.jar`及NeoForge工程`build/libs/purified_undead-1.21.1-1.1.1hotfix.jar`；版本号保持原值，属于含预备入口的开发构建，不替换版本交付文件夹中的正式JAR。

## 未覆盖与证据索引

本轮没有具体玩法或美术变化，因此未重复客户端画面、JEI界面、正式客户端加载器、光影和AMD硬件检查；上述服务端结果不冒充客户端视觉或多人TPS压测。附属可选组合为检测逻辑的合成存在状态，不是第三方模组实物全安装组合。正式生产JAR已作为附属依赖实际加载，但附属开发加载器仍不等同独立正式客户端启动器测试。

开始时机制证据CURRENT；接入后检查按预期报告两版入口、新文件和指南变化。复核实际源码差异、生产class、资源与服务器回归后，向CURRENT_CONTENT追加本阶段说明，并仅登记已审核变化；不重置其他玩法结论或盲目刷新未知指纹。完整复核范围与判断见PROJECT_STATE和本轮verification。
