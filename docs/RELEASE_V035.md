# v0.35.0 — 白巫女手记

新增 Patchouli（帕秋莉手册）可选联动：同时安装两款模组时，用一个污秽碎片和一本书无序合成「白巫女手记」。不安装 Patchouli 时，原有玩法照常运行，手记配方不加载。Patchouli 不捆绑在本模组或安装包内，也不是强制前置。

## 内容与外观

- 按作者提供的《白巫女手记》原稿整理为四章、25 个条目、47 页：接受契约、古老民族之契、白巫女遗物、其他内容。
- 保留原稿叙述、获取方式和灰色引文；长段落按语义分页，不靠缩小字号或截断正文塞入页面。
- 使用现有物品作为条目图标，并显示对应合成配方；秽鬼灵药补充「怨念污秽 + 粗制的药水」的酿造说明。
- 64×64 透明日记物品贴图：旧白皮封面、暗红书脊、金属扣和少量陈旧血迹。书页使用原版界面布局，在页角增添暗红血迹。
- 正文以作者提供的中文为准；其他语言环境回退到同一中文内容。

## 两个版本分别实现

| 版本 | 帕秋莉验证版本 | 合成结果格式 |
| --- | --- | --- |
| Minecraft 1.20.1 / Forge 47.4.23 | 1.20.1-85-FORGE | Patchouli 无序书本配方与 NBT |
| Minecraft 1.21.1 / NeoForge 21.1.248 | 1.21.1-93-NEOFORGE | 原版无序配方与 `patchouli:book` 数据组件 |

书本定义位于 `data/purified_undead/patchouli_books/white_witch_diary/book.json`，正文位于 `assets/purified_undead/patchouli_books/white_witch_diary/`。配方分别使用 Forge / NeoForge 的模组存在条件。

## 安装

替换旧版涤净亡魂 JAR，并按游戏版本选择相应 Patchouli；联机时客户端和服务端都应安装相应版本，才能合成和阅读手记。已有前置及 NeoForge 最低版本要求不变。

书本资源按 [Patchouli 官方格式](https://vazkiimods.github.io/Patchouli/docs/reference/book-json/) 制作。GUI 衍生纹理的署名和许可见 `docs/licenses/PATCHOULI_TEXTURE.md`。
