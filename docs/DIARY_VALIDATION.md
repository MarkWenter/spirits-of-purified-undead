# 白巫女手记独立验证

两个工程使用自己的资源、构建与游戏实例，不能把 1.20.1 的配方复制给 1.21.1。

## 资源检查

在各工程根目录执行 `python scripts/validate-diary.py`，或者从主目录执行 `python scripts/validate-diary.py "1.21.1neoforge版本"`。检查章节、条目、分页、配方引用、条件配方格式、贴图尺寸。正文每页不超过 82 个可见字符；客户端检查进一步使用真实字体与 Patchouli 排版结果检测纵向溢出。

## 构建与运行

默认 `./scripts/gradle.ps1 build stageMods` 不下载或安装 Patchouli。追加 `-Pwith_patchouli=true` 只为开发环境加入对应版本的 Patchouli，不打包进成品或安装包。

开发专用的 `DiaryServerSmoke` 在 `purified_undead.diarySmoke=true` 时检查：未安装 Patchouli 时配方不存在；安装时产物绑定正确书本，两种交换位置的 2×2 配方都能匹配。

`DiaryClientSmoke` 在 `purified_undead.diaryClientSmoke=true` 时打开开发存档 `diary-validation`，加载所有条目并逐个展开，每个跨页保存截图，检查文本末行是否超出页面。存档须事先放在该客户端运行目录的 `saves` 中。该检查会在完成后关闭开发客户端。测试代码在独立 smoke source set 中，不进入发布 JAR。

本地独立验证记录保存在两版各自的 `验证记录/0.35.0` 文件夹。专用服务器分别测试有/无 Patchouli；客户端分别测试全部条目和跨页。整合包更换字体、资源包或旧版 Patchouli 后，仍建议重新检查显示效果。
