# 秽鬼生物独有特征 · 1.1.0-blighted-rain.3

适用：Forge 1.20.1 与 NeoForge 1.21.1。仅发布在 `污秽之雨更新` 分支；更新仍未定稿。白巫女手记正文不变。

## 数值与行为

以下倍率以对应原版生物为基准，替代先前的统一双倍生命；攻击频率未特别指定的生物沿用此前规则。

| 生物 | 最大生命 | 移动速度 | 攻击变化 |
| --- | --- | --- | --- |
| 秽鬼僵尸 | 30 | 原版 70% | 伤害 120% |
| 秽鬼溺尸 | 30 | 原版 70% | 伤害 120%，成功近战/远程命中附加缓慢 I，200 刻 |
| 秽鬼尸壳 | 30 | 原版 70% | 伤害先乘 1.2，再额外 +1；原版饥饿保留 |
| 秽鬼骷髅、流浪者 | 30 | 原版 70% | 攻击频率约为原版 70%，伤害 120%；流浪者仍射缓慢箭 |
| 秽鬼沼骸（仅 1.21.1） | 24 | 原版 70% | 同骷髅；仍射中毒箭 |
| 秽鬼村民 | 30 | 普通村民 50%（基础值 0.25） | 敌对行为保留，可用纯净灵药净化 |

攻击频率通过拉弓进度和攻击间隔一起调整，按游戏刻取整，并非将间隔简单加 30%。近战与投射物伤害均经过统一规则，护甲、抗性等防御仍按原版计算。暴击等既有系统仍正常叠加。

秽鬼掠夺者、卫道士、女巫、鸡、马、驴维持原有特征。秽鬼猪、牛在原生物掉落外增加原版僵尸的腐肉掉落池（通常 0–2，抢夺规则相同），独立污秽碎片掉落继续保留。

秽鬼羊剪毛一律产出红羊毛，数量仍是原版的 1–3。保留羊本身的颜色与剪毛状态，手持剪刀和发射器路径均适用；死亡掉落并未统一改为红色。

## 秽鬼村民净化

手持普通瓶装纯净灵药右键秽鬼村民，普通/延长/增强型均可。喷溅与滞留型不能启动；虚弱配合金苹果不作为此变种的净化入口。

复用原版僵尸村民的 3600–6000 刻等待、抖动/音效、床与铁栏杆加速、保存进度、恢复村民及玩家治愈归属。恢复职业等原版支持的村民信息，恢复后的生命与移动属性回到普通村民。生存模式消耗一瓶并返还玻璃瓶；已经净化中的重复点击不再扣药。

## 秽鬼蜘蛛

新增 `purified_undead:blighted_spider`，仅原版蜘蛛作为来源，不将洞穴蜘蛛混入。遵循此前共性：32 点最大生命、双倍攻击频率、亡灵/秽鬼分类、无日晒燃烧、免污秽之雨伤害、原版蜘蛛掉落加独立污秽碎片。

可在污秽之雨中累计 200 刻转化；离开暴露位置时按原规则衰减，也接入原有受契约约束的怨念污秽点击转化。骑乘分离、装备掉落、拥有者保护均复用现有入口。包含独立刷怪蛋、中英文名称、创造栏展示及原版蜘蛛渲染器。

贴图由内置 imagegen 生成后，用像素脚本校准为原版 64×32 UV，严格恢复原图透明区域，并将红眼对齐原版眼部像素。保留深褐色蜘蛛辨识度，加入暗红裂纹与骨灰色节纹。两版资源：`assets/purified_undead/textures/entity/blighted/spider/spider.png`。可重建素材在 `scripts/blighted-textures/masks/spider.png`，使用现有 `rebuild.py` 与合法安装的原版客户端 JAR 重建。

生成提示：Edit this Minecraft spider UV texture atlas, not a portrait or model. Preserve exact UV island boundaries, transparency and 64×32 pixel grid. Retain vanilla dark brown charcoal identity; add burgundy parasitic erosion veins edged with nearly black cracks, subtle bone-grey segmented rib marks, and red eyes at original positions. Sparse readable pixel clusters; no new geometry, lighting, gradients or text; flat UV atlas only.

## 存档与架构

- 数值规则集中在 `BlightedMobRules`；实际生物属性注册和转化入口同时应用，覆盖刷怪蛋与环境转化。
- 旧版受影响生物加载时一次性迁移最大生命与移动属性，保持生命比例，写入 `BlightedTraitsVersion` 避免重复缩放；Forge/NeoForge 分别处理其属性 NBT 字段名。
- 所有旧注册 ID 保留，新增蜘蛛 ID。附属 API 不变；两端需同时更新，网络协议保持 12。
- 村民净化通过原版完整附加存档数据入口启动原版状态机，不复制一套计时/恢复逻辑。
- 蜘蛛使用既有派生类、标签、转化、渲染重定向与刷怪蛋注册方式；未修改旧秽鬼傀儡、秽鬼国王贴图。

验证与安装包校验见 [本次验证记录](TRAITS_VALIDATION.md)。
