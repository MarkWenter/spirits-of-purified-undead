# 1.1.1-hotfix.3：问题1的窄范围着色器兼容候选

本候选继承1.1hotfix上的hotfix.2，不合并main、不创建Release；Forge 1.20.1与NeoForge 1.21.1分别实现，网络协议13。用户提供的GitHub反馈是诊断材料，其中修复建议未经硬件验证，不能把建议直接当作已证实根因。

## 依据与范围

反馈确认自定义blighted_precipitation编译失败打断重载，Oculus 1.8.0的错误处理又以不可取消异常遮蔽了原始驱动编译日志。本地精确版本字节码也显示iris$causeException先执行cancel，再读取glGetShaderInfoLog；因此报告不能确定AMD实际拒绝了哪一行。异常隔离已由问题2修复，本候选保留该代码及所有性能修正。

运行时变更仅限本模组这一份天气程序的json/vsh/fsh，不改Java/Mixin、其他着色器、配置、玩法数据、数值、注册ID、存档、网络或依赖。没有修改Oculus/Iris全局钩子，不尝试修复报告中独立的LootModifierManager异常，不按显卡厂商关闭功能。

## 实现

1. JSON顶点阶段从minecraft:particle换为本模组自己的blighted_precipitation.vsh，保留PARTICLE输入格式、Sampler2光照、矩阵及输出接口，避免继续共享外部粒子程序的名称/缓存入口。
2. 内联该Minecraft版本的fog_distance与linear_fog，只改局部函数名并去掉共享moj_import。两版分别从对应原版资源核对：1.20.1的fog_distance使用ModelViewMat；1.21.1直接使用pos。不能把反馈里的1.20.1矩阵雾公式机械复制到1.21.1。
3. 局部变量patch更名为stainMask。较高GLSL版本中patch作为限定符使用，但这不能证明GLSL150的原名称就是本次驱动拒编原因；此处仅作无视觉变化的防御性更名。
4. 保留原颜色公式、雪花污斑位置/半径、透明度丢弃阈值、混合模式、光照和雾参数；着色器仍为GLSL150，不提升硬件要求，不增加每帧CPU工作或GPU采样次数。

这是一项针对报告触发面的兼容尝试，不代表已证明绕过所有光影补丁。资源包若专门替换全局particle/fog，这一份自定义天气程序将不再继承其全局替换；对默认资源的公式严格等价，资源包仍可通过本模组自己的路径明确覆盖。所有其他原版/模组程序保持原有加载行为。

## 验证与待回测

本机为NVIDIA，不能复现AMD专属驱动问题。实际验证记录、隔离运行日志、两版候选包和真实源码快照见本地outputs/shader-hotfix-2026-10-25/。不能将主菜单可启动视为自定义shader已经启用：本轮探针同时要求首次加载及资源重载后precipitation非null；故障注入则要求明确回退。正式包不含探针。

AMD回测建议使用报告原环境：Oculus 1.8.0、ComplementaryReimagined r5.8.1和本候选，先检查主菜单、按键界面、资源重载，再检查雨雪场景。日志应无weather shader降级警告及原不可取消异常；若仍降级，保留完整日志，说明兼容尝试未解决驱动拒编。不能以“没有崩溃”代替“自定义天气shader编译成功”。


## 本轮实际结果（NVIDIA RTX 4070 Laptop）

- 两版build/stageMods成功。现有139/140项单测报告均无失败/错误/跳过；Java与单测源码未改，Gradle复用了未变化部分的测试结果。
- 两版最终JAR普通环境首载与重载后均确认自定义天气shader实际注册；Oculus 1.8.0 + Embeddium 0.3.31、NeoForge对应Iris + Sodium两组均实际启用官方ComplementaryReimagined r5.8.1并通过相同检查。
- 两版故意无效GLSL后确认precipitation为null且资源重载、主菜单仍成功，测试只修改隔离副本/测试资源包，候选JAR未改。
- 两版专用开发存档实际雨雪场景BLIGHTED_RAIN_CLIENT_OK与RAIN6_TOGGLE_NETWORK_OK通过；本轮active/ended截图已检查，没有把上轮截图当成本轮证据。该视觉检查使用默认天气渲染环境，光影组合另外核对启动/注册/重载。
- 严格范围校验：对比hotfix.2实际源码快照，两版src各仅有上述三份shader资源改变；最终JAR内Forge331个、NeoForge330个生产class逐字节相同。所有问题2保护保持原实现，未重跑独立服务端完整suite；本轮客户端包含实际集成服务器与网络检查。

这些证明本机上正常编译、注册、重载、回退与默认雨雪表现可用，不证明AMD拒编根因已经消除。当前交付应由原报告者在AMD上确认后再判断问题1是否解决。

额外故障注入：Forge在Oculus 1.8.0和r5.8.1均启用时，故意无效GLSL确实触发与报告相同的不可取消异常，原有边界成功隔离，主菜单与资源重载继续完成。这是人工注入的通用编译失败，不是复现AMD原始拒编。
