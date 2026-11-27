# Spirits of Purified Undead · 涤净亡魂

**《ENDER LILIES》非官方同人模组 · An unofficial ENDER LILIES fan mod**

[中文介绍](#中文介绍) · [English](#english)

## 中文介绍

与古老民族缔结契约，净化不死战士的污秽，继承他们的加护。在探索、战斗与成长中收集白巫女的遗物，并迎接污秽之雨带来的变化。

支持 **Minecraft 1.20.1 / Forge** 与 **Minecraft 1.21.1 / NeoForge**。

### 1.2.1 纯净触碰镶嵌修复报告

在《锻造之旅 1.4.4》中，一把已镶嵌纯净触碰的 Tetra 剑没有显示「秽鬼守护」，挥剑也没有剑气。1.2 按物品标签和头部类型判断哪些物品能触发词条，漏掉了整合包的剑 `tetra:sword_renew`。

1.2.1 直接读取 Tetra 已安装部件上的词条。现在主手挥动镶嵌了纯净触碰的 Tetra 改造物品即可发出剑气，剑、镐、铲和切石刃都适用。**升级模组后，已有镶嵌直接生效。**

- **Forge 1.20.1** 在整合包副本中，用存档里已镶嵌的剑对空气和生物各挥击一次，两次都出现了一道剑气。Tetra 工作台和 Tetra View 也能显示词条。
- **NeoForge 1.21.1** 在 Tetra 社区移植版中，分别装上和移除 Better Combat，用镶嵌铁铲对空气挥击，两次都看到了剑气。启用 Better Combat 后，按住攻击键连挥三次，服务端记录到三道剑气。

Forge 的 168 项及 NeoForge 的 169 项自动化测试全部通过。[完整修复说明](docs/RELEASE_V121.md) · [Tetra 联动与实测记录](docs/ferin-update/TETRA.md)

### 1.2 · 费林更新

- **费林的协同斩击**从身旁的光点飞向敌人。五段剑术各有自己的剑路、暖白刃光与紫焰拖尾，最后以翻身斩收势。
- **骑士寂夜**可在车万女仆的祭坛制作。手持饰品蹲下使用以绑定，再让自己的女仆佩戴。她的虚影会在费林出剑时助战，攻击属性也会增强协同斩击。
- **秽鬼之守护**换上像素手持模型和绕刃旋转的半透明气旋。挥剑时，五种战士武器幻影从握剑侧飞出，各有独立光迹。安装 Tetra 可用纯净触碰为近战武器镶嵌同款剑气，Better Combat 支持按住连续挥剑。
- **美术与装备栏**更新了材料、饰品、八枚秽鬼追忆、无垢盔甲和秽鬼刷怪蛋，纯化铸造器也有了立体炉腔。Curios 为古老民族之契、不死战士、白巫女遗物和流浪日志分别显示专属空槽剪影。
- **白巫女手记**补齐了铸造器等配方，并收录骑士寂夜。佩戴古老民族之契时，手持怨念污秽可将符合条件的生物转化为对应秽鬼。

完整变更见 [1.2 费林更新说明](docs/RELEASE_V120.md)。

### 1.0 · 契约与净化

1.0 建立了围绕契约、不死战士与白巫女遗物的完整成长体系。

- **古老民族之契与八组不死战士**：寻找费林、格洛特、尤里乌斯、西丽亚与西丽德、狼、伊莱恩、海尼尔和法腾，完成各自的获取与净化条件，获得战斗和探索能力。
- **协同战斗与移动能力**：费林的连续协同斩击、姐妹的二段跳与空中冲刺，以及各位战士的独特加护。
- **护身符与白巫女遗物**：提升护身符等级，收集提供属性加成、恢复和保命效果的饰品。
- **净化装备与消耗品**：制作纯净灵药、纯净禁果、无垢护甲与秽鬼之守护，探索污秽与净化之间的联系。
- **纯化铸造器与追忆**：铸造材料和装备，将不死战士的追忆装入莉莉手记，进一步强化能力。
- **游戏内指引**：白巫女手记介绍物品、配方和成长路线；莉莉手记作为饰品，记录不同种类亡灵的击杀并提供加成。

### 1.1 · 污秽之雨

1.1 将污秽扩展到天气、生物与世界环境，并带来新的装备和控制方式。

- **污秽之雨与污秽之雪**：主世界出现带有预警音效的天气事件。露天生物可能受伤或转化，村民会寻找避雨地点，屋顶可以提供保护。
- **秽鬼生物**：新增僵尸、骷髅、溺尸、尸壳、流浪者、村民、灾厄村民、女巫、家畜与蜘蛛等秽鬼变体，配有红眼与侵蚀纹路，并具有各自的战斗或掉落特征。1.21.1 额外包含秽鬼沼骸。
- **净化秽鬼村民**：使用普通瓶装纯净灵药，使敌对的秽鬼村民逐渐恢复为普通村民。
- **掌控天气**：静雨信标在紫水晶块上接通红石后，可抑制自然污秽之雨；秽雨纹章则能主动召来污秽之雨。创建世界时也可选择是否启用永雨。
- **材料、食物与装备**：加入墓穴泞尘、污秽红宝石、污秽肉及熟污秽肉，可恢复耐久的投掷物「悬崖村的少年」，以及持有时提供护甲的盾牌「无头的骑士」。
- **种植与收集**：洁白莲花和猩红莲花可以放置为植物；新增秽鬼刷怪蛋与独立创造模式物品栏。
- **饰品与操作设置**：更新伊莱恩、法腾和海尼尔的获取条件，提供独立饰品数值配置，以及费林协同攻击、二段跳与空中冲刺的开关键。两个按键默认未绑定，能力默认开启。
- **手记与配方**：扩充白巫女手记，并提供新物品的合成、烹饪、铸造配方与 JEI 展示。

完整变更见 [1.1 更新报告](docs/RELEASE_V110.md)。

### 安装

请选择与游戏版本和加载器对应的模组文件：

| 游戏版本 | 加载器 | Java |
| --- | --- | --- |
| Minecraft 1.20.1 | Forge 47.3.22 或更高的 47.x 版本 | 17 |
| Minecraft 1.21.1 | NeoForge 21.1.248 | 21 |

**必需前置：** Curios API、GeckoLib、Patchouli。

**建议搭配：** Apothic Attributes 及其前置 Placebo，以获得完整属性效果。JEI 可用于查询配方。

将本体及对应版本的前置放入游戏实例的 `mods` 文件夹。多人游戏的客户端和服务器应安装相同版本。升级时替换旧本体，不要同时安装多个版本，并提前备份存档与配置。

### 开始游玩

首次进入世界时获得的**白巫女手记**是主要指引，也可用一个污秽碎片与一本书无序合成。

收集污秽碎片，合成怨念污秽并酿造秽鬼灵药。在秽鬼化状态下经历死亡与重生，即可踏上与古老民族缔结契约的道路；极限模式提供对应的一次性保命获取机制。随后按照手记和物品提示寻找战士、净化污秽并提升护身符。

古老民族之契在生存模式下不可卸下。饰品数值可在 `config/purified_undead-accessories.toml` 中调整，其他通用规则位于 `config/purified_undead-common.toml`；修改 TOML 配置后请重启游戏或服务器。

### 反馈与致谢

欢迎通过本仓库的 [Issues](https://github.com/MarkWenter/spirits-of-purified-undead/issues) 反馈问题。请提供游戏版本、加载器、模组版本、复现步骤与相关日志，并移除日志中的私人信息。

本项目为非官方同人作品，与 ENDER LILIES 的官方开发者及发行商无隶属关系。素材来源与许可信息见 [NOTICE](NOTICE.md)。

---

## English

Form a pact with the Ancients, purify the blight of fallen warriors, and inherit their blessings. Collect the White Witch's relics as you explore and grow stronger, then face a world changed by Blighted Rain.

Available for **Minecraft 1.20.1 / Forge** and **Minecraft 1.21.1 / NeoForge**.

### 1.2.1 Pure Touch socket fix

In Journey of Forging 1.4.4, Tetra saved the Pure Touch gem in the sword, but Blighted Guardian was missing from the tooltip and swinging the sword produced no wave. Version 1.2 checked item tags and head types to decide which items could trigger the effect. Those checks missed the pack’s `tetra:sword_renew`.

Version 1.2.1 reads the effect directly from the installed Tetra modules. Any Tetra modular item socketed with Pure Touch can now release a wave when swung in the main hand. This includes swords, pickaxes, shovels and stonecutter blades. **Existing gems work as soon as you update.**

- **Forge 1.20.1** was tested in a copy of the modpack with an already-socketed sword from a save. One swing into the air and one at a mob each produced one visible wave. The Tetra workbench and Tetra View both showed the effect.
- **NeoForge 1.21.1** was tested with the community port of Tetra. A socketed iron shovel produced a visible wave when swung into the air both with and without Better Combat. With Better Combat enabled, holding attack for three swings produced three server-recorded waves.

All 168 Forge and 169 NeoForge automated tests passed. [Full patch notes](docs/RELEASE_V121.md) · [Tetra compatibility and test results (Chinese)](docs/ferin-update/TETRA.md)

### 1.2 · The Ferin Update

- **Ferin’s assist slashes** now fly from the wisp beside you. Each of the five cuts has its own path, with a warm white edge and a violet wake. The combo ends in a somersault strike.
- **Knight Nocturne** can be crafted at the Touhou Little Maid altar. Sneak and use the charm to bind it, then equip it on your maid. Her apparition joins Ferin’s attacks, and her attack attributes strengthen his slashes.
- **Blighted Guardian** has a new pixel-art held model with a translucent vortex around its blade. Each swing sends one of five spectral weapons from your sword hand, trailing violet light. Tetra lets you socket Pure Touch into a melee weapon to gain these waves. Better Combat supports continuous swings while the attack button is held.
- **New pixel art** covers materials, accessories, all eight Blighted Memories, Immaculate Armor, and Blighted spawn eggs. The Purification Foundry has a recessed chamber. Four Curios silhouettes distinguish empty Ancient Contract, Undead Warriors, White Witch Relics, and Wanderer Log slots.
- **The White Witch’s Diary** now includes missing recipes and a Knight Nocturne entry. While wearing the Ancient Contract, use a Blighted Spirit on an eligible mob to turn it into its Blighted counterpart.

See the [full 1.2 Ferin Update notes](docs/RELEASE_V120.md).

### 1.0 — Contracts and Purification

Version 1.0 introduces a progression system built around contracts, undead warriors, and the White Witch's relics.

- **The Ancient Contract and eight warrior groups:** discover Ferin, Groth, Julius, the guardian sisters, Ulv, Eleine, Hoenir, and Faden. Fulfill their acquisition and purification requirements to gain combat and exploration abilities.
- **Cooperative combat and movement:** fight alongside Ferin's chained assist slashes, use the sisters' double jump and air dash, and combine the warriors' unique blessings.
- **Talismans and White Witch relics:** level up your talisman and collect accessories that improve attributes, restore health, or protect you from death.
- **Purified equipment and consumables:** craft Pure Elixirs, Pure Forbidden Fruit, armor, and weapons as you explore the connection between blight and purification.
- **The Purification Foundry and memories:** forge materials and equipment, then equip warrior memories in Lily's Diary to strengthen your abilities.
- **In-game guidance:** the White Witch's Diary explains items, recipes, and progression. Lily's Diary is an accessory that records distinct undead types slain and grants bonuses.

### 1.1 — Blighted Rain

Version 1.1 extends the blight to weather, creatures, and the world, with new equipment and controls.

- **Blighted Rain and Blighted Snow:** Overworld weather events arrive with an audible warning. Exposed mobs may take damage or transform, villagers seek shelter, and roofs offer protection.
- **Blighted creatures:** new variants of Zombies, Skeletons, Drowned, Husks, Strays, Villagers, Illagers, Witches, livestock, and Spiders feature red eyes, blight markings, and distinct combat or loot traits. Minecraft 1.21.1 also includes the Blighted Bogged.
- **Cure Blighted Villagers:** use a regular bottled Pure Elixir to begin restoring a hostile Blighted Villager to an ordinary Villager.
- **Control the weather:** power a Still Rain Beacon on an Amethyst Block to suppress naturally triggered Blighted Rain, or use a Blighted Rain Emblem to summon it yourself. Eternal Rain can also be toggled during world creation.
- **Materials, food, and equipment:** discover Grave Mire Dust, Blighted Rubies, Blighted Meat and its cooked form, the durability-recovering throwable Youth of the Cliff Village, and the Headless Knight shield, which grants armor while held.
- **Plants and collections:** place White and Scarlet Lotuses as plants, use Blighted spawn eggs, and browse the mod's dedicated creative inventory tab.
- **Accessory settings and controls:** revised acquisition requirements for Eleine, Faden, and Hoenir; a separate accessory configuration file; and toggles for Ferin's assist attacks and the sisters' double jump and air dash. Both keys are unbound by default, while the abilities remain enabled.
- **Guidebook and recipes:** expanded White Witch's Diary entries, new crafting, cooking, and foundry recipes, and JEI recipe displays.

### Installation

Choose the file matching your Minecraft version and mod loader:

| Minecraft version | Mod loader | Java |
| --- | --- | --- |
| Minecraft 1.20.1 | Forge 47.3.22 or a later 47.x version | 17 |
| Minecraft 1.21.1 | NeoForge 21.1.248 | 21 |

**Required dependencies:** Curios API, GeckoLib, and Patchouli.

**Recommended:** Apothic Attributes and its dependency Placebo for full attribute functionality. JEI can be used to look up recipes.

Place the mod and the matching dependencies in your instance's `mods` folder. Multiplayer clients and servers should use the same version. Replace the old mod JAR when upgrading, avoid installing multiple versions together, and back up your worlds and configuration first.

### Getting Started

The **White Witch's Diary**, given to you when you first enter a world, is your main guide. You can also craft it using one Blighted Fragment and one Book in a shapeless recipe.

Collect fragments, craft Blighted Spirits, and brew a Blighted Elixir. Dying and respawning while blighted begins your journey toward the Ancient Contract; Hardcore mode provides a corresponding one-time rescue mechanism. Follow the diary and item descriptions to find warriors, purify their blight, and strengthen your talisman.

The Ancient Contract cannot be unequipped in Survival mode. Accessory values can be adjusted in `config/purified_undead-accessories.toml`, with general settings in `config/purified_undead-common.toml`. Restart the game or server after changing TOML configuration files.

### Feedback and Credits

Report problems through this repository's [Issues](https://github.com/MarkWenter/spirits-of-purified-undead/issues). Include your Minecraft version, loader, mod version, reproduction steps, and relevant logs. Remove private information before sharing logs.

This is an unofficial fan project and is not affiliated with the developers or publishers of ENDER LILIES. See [NOTICE](NOTICE.md) for asset credits and licensing information.
