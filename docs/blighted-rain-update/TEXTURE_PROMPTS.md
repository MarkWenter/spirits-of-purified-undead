# Imagegen 提示词与像素校准

使用方式：内置 imagegen，22 次独立 UV 编辑（含反馈后的两次增强）；每次输入相应原版最近邻放大图，透明背景开启。没有使用 CLI/API 回退。以下记录通用提示词与每张图的专属约束；生成后的 UV 精度由像素脚本保障。

## 通用提示词

```text
Use case: precise-object-edit. Asset type: Minecraft production UV texture. Edit supplied vanilla UV sheet IN PLACE, not a character illustration. Preserve EXACT UV island positions, scale, empty areas and native coarse pixel grid. Keep the original species highly recognizable, retain its original coat/clothing/material color with only mild desaturation. Add localized branching charcoal fissures with dark burgundy erosion cores, organic dark-fantasy blight inspired by ENDER LILIES reference art; restrained, asymmetrical, no full-body red noise. Keep 75% of vanilla appearance intact. Existing eyes become vivid red, no extra eyes. Flat pixel art only, no 3D, no text, no frames, no new anatomy, no islands rearranged. Output same aspect ratio, transparent empty areas. Species:
```

## 各图专属约束

| 草稿 | 专属约束（中文整理） |
| --- | --- |
| zombie | 保留绿皮肤、青色破衣和紫裤；面颊、肩膀、躯干和腿部使用炭黑/暗酒红裂纹；64×64 网格。首张提示另明确约 75% 原貌、不得增生新肢体。 |
| skeleton | 保留象牙灰骨骼、肋骨和黑眼窝；头骨与四肢使用窄裂纹。 |
| drowned | 保留深青色水生皮肤、青绿斑点与破衣，原浅色眼改红。 |
| husk | 保留沙色干燥皮肤和棕色破衣；干裂侵蚀纹。 |
| stray | 保留冷灰蓝骨骼；不填充肋骨间透明区域。 |
| villager | 保留绿色皮肤、独立长鼻 UV、眉毛和灰色破衣。 |
| pillager | 保留灰脸、粗眉、青袖、暗红上衣和皮带；不与卫道士外观混同。 |
| vindicator | 保留灰肤、青袖、炭灰外套、扣子和暗红胸部。 |
| witch | 保留浅肤、长鼻、紫袍和帽子区域，局部侵蚀。 |
| pig | 保留粉肤、方鼻和蹄；不增加新器官。 |
| cow | 保留棕白斑块、角和口鼻，维持斑块边界。 |
| sheep | 保留剪毛后的浅色皮肤、白斑、粉色口鼻和原蹄。 |
| chicken | 保留白羽、黄喙/腿、红肉垂；不把肉垂当红眼。 |
| horse | 保留棕色毛发、深鬃与浅鼻；侧面红眼；鞍具保持原样。 |
| donkey | 保留灰褐毛色、白鼻腹、深鬃和长耳；侧面红眼；鞍具保持原样。 |
| bogged | 保留绿色苔藓、浅骨骼与现有蘑菇；不增加蘑菇或新器官。 |
| sheep_fur | 白色羊毛外层；局部炭黑/暗红裂纹；不生成五官。 |
| drowned_outer | 保留青绿藻类、棕色破衣和脸部透明开口；不添加眼睛。 |
| stray_overlay | 保留灰蓝破衣、吊坠、脸部透明开口和破损末端；不添加眼睛。 |
| bogged_overlay | 保留绿色苔藓、浅色地衣和不规则边缘；不添加五官。 |

## 生成后校准

AI 草稿并非直接交付贴图。脚本仅提取本轮暗红纹路与邻接暗色裂纹，依据原版像素坐标合成。清除错位/误生成眼睛（包括沼骸躯干上的错误眼睛），随后按原版瞳孔位置重绘。恢复原版透明通道、装备区域、口鼻和可识别的底纹；不复用上一轮临时贴图。最终遮罩与重建脚本随源码保存。

## 反馈后的增强编辑

- 村民原版基础 UV：要求额头、双侧面颊、头部侧后方及胸袖出现明显炭黑裂纹和暗酒红侵蚀；保持 64×64 原版布局、绿色皮肤、眉眼位置和灰色衣服，至少约 65% 原色辨识度，不添加五官。
- 平原村民服饰原版 UV：独立衣物遮罩，胸前、袖子和裙摆添加原生尺度可读的 1–2 像素裂缝与约 3 像素侵蚀斑；保持原版棕色衣服、裙摆和透明区域，不生成眼睛或身体。
- 生成头部纹路经像素裁切转移到相同村民类头部 UV；衣层纹路依目标 UV 面板对齐。最终直接使用保存的校准遮罩即可复现，不需要重新请求生成。
