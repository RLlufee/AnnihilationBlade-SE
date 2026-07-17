# Annihilation Blade · Terminus 2.7.0-1.20.1-forge

> 基于 SlashBlade / SlashBlade Resharped 的 Forge 1.20.1 拔刀剑扩展模组。模组围绕“终焉、裂界、坍缩、审判、血狱、宇宙法则”主题，提供三把命名刀、三套 SA、完整 SE 链路、可配置低风险参数，以及面向实战可读性的视觉与按键控制。

作者：青衣_璃

## 概览

当前版本包含：

- 主刀 `annihilationblade:annihilation_blade`
- 血狱刀 `annihilationblade:blood_prison`
- 无尽星空 `annihilationblade:infinity_stellaris`
- 3 个 SA：`spatial_fracture`、`infernal_slaughter`、`vacuum_decay_collapse`
- 15 个 SE 注册项，其中湮灭之刃使用 8 个终焉系 SE，血狱使用 3 个血狱系 SE，无尽星空使用 4 个宇宙法则系 SE
- 命名刀 datapack 定义
- Forge common 配置文件
- 中英及繁中语言资源
- JEI SlashBlade 联动描述资源
- 断空闪现模式热键与动作栏提示
- SlashBlade 原生友伤 / PVP 判定统一接入

## 需求

| 项目 | 版本 |
| --- | --- |
| Minecraft | `1.20.1` |
| Forge | `47.4.21` |
| Java | `17` |
| 前置 | SlashBlade / SlashBlade Resharped |

## 武器

### `annihilationblade:annihilation_blade`

主武器“湮灭之刃 · 终焉”。命名刀定义位于：

`src/main/resources/data/annihilationblade/slashblade/named_blades/annihilation_blade.json`

| 项目 | 内容 |
| --- | --- |
| 基础攻击力 | `50.0` |
| 耐久 | `2000` |
| SA | `annihilationblade:spatial_fracture` |
| SE 数量 | `8` |
| 额外被动 | 绝对庇护、虚空飞行、终焉处决、永昼视界 |

湮灭之刃位于背包、主手或副手时，客户端会获得永昼视界效果；战斗判定统一遵循 SlashBlade 的原生攻击规则，默认不会误伤玩家、宠物或非敌对单位。

裂界会在湮灭之刃伤害生效 5 tick 后，以受击位置为中心处决周围所有合法目标，并继续连锁。连锁次数和以初始攻击者为中心的最大连锁范围可在 common config 的 `annihilation_blade.world_rift.chain_count` 与 `chain_range` 调整。

### `annihilationblade:blood_prison`

血狱刀“魔刀 · 血狱”。命名刀定义位于：

`src/main/resources/data/annihilationblade/slashblade/named_blades/blood_prison.json`

| 项目 | 内容 |
| --- | --- |
| 基础攻击力 | `16.0` |
| 耐久 | `2400` |
| SA | `annihilationblade:infernal_slaughter` |
| SE | `blood_leech`、`spirit_shield`、`phantom_mark` |

血狱围绕低血量风险、吸血、护盾、领域与幻影爆发构建。伤害、吸血、护盾触发和处决类逻辑保持写死，不开放到配置文件，避免破坏平衡或造成服务端误用。

### `annihilationblade:infinity_stellaris`

无尽星空是一把不可合成、不可平衡化的最终兵器命名刀。命名刀定义位于：

`src/main/resources/data/annihilationblade/slashblade/named_blades/infinity_stellaris.json`

| 项目 | 内容 |
| --- | --- |
| 基础攻击力 | `1000000.0` |
| 耐久 | `2147483647` |
| SA | `annihilationblade:vacuum_decay_collapse` |
| SE | `entropy_dissolution`、`curvature_rupture`、`gamma_thunderburst`、`cosmic_string_cut` |
| 额外被动 | 飞行、无敌、死亡无效、kill 兜底、虚空坠落保护、永昼视界 |

无尽星空只在主手或副手持有时授予主动战斗权能；背包内也会提供基础生存兜底。它拥有 10 级剑、弓、弩相关附魔，但按设定排除火焰附加与火矢，不提供合成配方。它拥有专属旗舰 tooltip renderer：收束后的外扩黑洞背景、外侧旋转白色魔法阵、动态白色绕框、绕框自转六芒星符号、星图叠层、宇宙谱线标题、权能芯片、属性记录与附魔回路会在客户端完整重绘。

战斗层面分为多套最终兵器逻辑：熵增蚀解按命中叠加热寂归零，所有无尽星空伤害会拉出 5 tick 粒子锁链，伽马霆爆会在玩家周围 128 格内连续 3 tick 落下自定义彩色闪电，曲率撕裂永久关闭周围合法目标 AI。宇宙弦切现在作为无尽星空自带 SE，通过 SlashBlade 原生斩击事件触发，不再占用潜行右键。它们位于 `QWQ.QingYi.annihilationblade.infinity_stellaris` 包下，不复用湮灭之刃的终焉处决体系。

## SA

### `空间破碎` / `Spatial Fracture`

湮灭之刃绑定的主 SA。触发后会沿玩家视线寻找裂隙中心，生成空间裂环、裂界蛛网、传送门粒子、闪电散射和剑雨演出。

逻辑上会优先锁定视线前方落点；如果准星路径上存在合适目标，则以目标中心作为裂隙焦点。命中实体会逐个执行终焉处决，并尽量走 SlashBlade 的真实击杀路径来维持击杀计数。

已开放配置项包括最大距离、裂隙半径、视线扫描步长、采样半径、锁定半径、备用搜索半径、目标上限、可视化目标数、斩击线数量和视觉倍率。

### `炼狱杀戮` / `Infernal Slaughter`

血狱刀绑定的 SA。触发后展开血狱领域，并同步客户端领域覆盖效果。领域持续期间，玩家斩击会在领域内选取敌对单位进行穿梭打击，并记录领域内造成的伤害，用于结束时的治疗反馈。

已开放配置项包括领域持续时间、领域半径、边界粒子刷新间隔、玩家血气粒子间隔、领域脉冲间隔和视觉倍率。

### `绝对湮灭圈` / `Absolute Annihilation Zone`

无尽星空绑定的 SA，注册 ID 仍为 `annihilationblade:vacuum_decay_collapse` 以保持旧命名刀定义兼容。释放时从玩家视线 raycast，优先在目视方块上展开领域；若没有命中方块，则在视线前方生成备用领域。

领域为 `128×128` 水平正方形，高度约 `64` 格，持续 `100 tick`。领域内每 tick 扫描 SlashBlade 合法目标，进入者直接热寂处决并压制掉落物和经验；方块不再被替换为空气。视觉上使用正方形边界、角柱、中心坍缩粒子与每 tick 12 道自定义彩色闪电模拟真空衰变的剧烈幻化感。

## SE

| 名称 | 类型 | 表现 |
| --- | --- | --- |
| `断空` / `Dankong` | 瞬移连斩 | 在多个目标之间连续闪现并逐个斩杀，最后返回原位 |
| `裂界` / `World Rift` | 范围裂隙 | 以被命中目标为中心打开裂界，牵引并处决半径内敌对单位 |
| `归墟回响` / `Terminus Echo` | 前向回声 | 沿面朝方向连续释放多波回响斩击 |
| `虚无权域` / `Void Dominion` | 大范围领域 | 在前方区域展开裂界并逐个清场 |
| `因果坍缩` / `Causality Collapse` | 连锁审判 | 从首个目标开始按最近目标续接，生成因果锚点与桥接斩线 |
| `星寂裁决` / `Starless Judgement` | 直线裁决 | 在前方展开裁决波带，按投影判定沿途处决 |
| `幻影审判` / `Phantom Judgement` | 召剑审判 | 先环绕搜索，再以召唤剑落下集中打击 |
| `归墟天诏` / `Abyssal Decree` | 高位裁定 | 在头顶构筑冠冕后，从高空逐个降下审判 |
| `嗜血` / `Blood Leech` | 血狱被动 | 配合血狱主逻辑提供吸血与风险收益 |
| `源流灵盾` / `Spirit Shield` | 血狱被动 | 低血量时提供护盾与短时增益 |
| `幻影印记` / `Phantom Mark` | 血狱被动 | 累积标记后触发幻影剑爆发 |
| `熵增蚀解` / `Entropy Dissolution` | 无尽星空被动 | 每次合法伤害追加扣除 10% 最大生命，同目标十层后触发热寂归零与移除兜底 |
| `曲率撕裂` / `Curvature Rupture` | 无尽星空控场 | 持刀者周围 25 格合法目标强制静止，Mob AI、寻路和目标被永久关闭 |
| `gamma_thunderburst` / `Gamma Thunderburst` | 无尽星空爆发 | 无尽星空伤害触发，玩家周围 128 格随机 12 道自定义彩色闪电，每 tick 一轮，持续 3 tick |
| `宇宙弦切` / `Cosmic String Cut` | 无尽星空斩击 SE | 通过 SlashBlade 原生斩击事件触发，只在玩家周围生成 5×5×5 局部星线，并处决 128 格立方内合法目标 |

## 断空控制

`断空` 是高速连续闪现 SE。为了避免日常杀怪时不断闪现导致视野混乱，当前版本提供两层保险：

- 按住 Shift 时，断空不会开始新的闪现序列。
- 断空序列进行中按住 Shift，会中断并返回起点。
- 新增可配置按键“切换断空闪现模式”，默认 `Left Ctrl`。
- 热键只在玩家主手或副手手持湮灭之刃时生效。
- 按下热键后，屏幕下方动作栏会显示本地化提示，例如“切换断空闪现模式：当前闪现：开 / 关”。

提示文本已经使用语言文件，不再硬编码中文，便于其他语言翻译和样式调整。

## 配置文件

首次启动后，Forge 会生成：

`config/annihilationblade-common.toml`

配置文件只开放低风险参数：

- 范围：例如搜索范围、领域半径、裁决宽度。
- 间隔：例如连续闪现间隔、回响波次间隔、领域粒子刷新间隔。
- 冷却：例如各 SE 的触发冷却。
- 数量：例如最大目标数、召唤剑数量、可视化目标数。
- 视觉倍率：例如粒子数量、视觉半径或演出密度。

不会开放的内容：

- 伤害倍率
- 终焉处决逻辑
- 血狱吸血与护盾核心数值
- 无敌、庇护、飞行等安全相关逻辑
- SlashBlade 真实击杀路径

每个配置项都带有中文 and 英文注释，并写明建议最小 / 最大值。Forge 也会通过 `defineInRange` 对配置值做硬范围限制，避免新玩家填入极端数值导致卡顿或逻辑异常。

### 配置分组

主要分组如下：

```toml
[annihilation_blade.spatial_fracture]
[annihilation_blade.dankong]
[annihilation_blade.world_rift]
[annihilation_blade.terminus_echo]
[annihilation_blade.void_dominion]
[annihilation_blade.causality_collapse]
[annihilation_blade.starless_judgement]
[annihilation_blade.phantom_judgement]
[annihilation_blade.abyssal_decree]
[blood_prison.domain]
[blood_prison.phantom_burst]
[infinity_stellaris]
```

## 本地化

语言资源位于：

`src/main/resources/assets/annihilationblade/lang/`

当前包含：

- `zh_cn.json`
- `zh_tw.json`
- `zh_hk.json`
- `en_us.json`

断空按键名称、动作栏提示、物品名、SA / SE 名称、物品描述和 JEI 说明文案均已接入语言文件。

## JEI SlashBlade 联动

当前版本为 `jei_slashblade` 添加了资源级联动，并补充了两把命名刀、SA 与 SE 的本地化说明：

- `assets/annihilationblade/blade_desc/annihilation_blade.json`
- `assets/annihilationblade/blade_desc/blood_prison.json`
- `assets/annihilationblade/blade_desc/infinity_stellaris.json`
- SA 描述键：`slashblade.slash_art.annihilationblade.*.desc`
- SE 描述键：`se.annihilationblade.*.desc`

安装 JEI SlashBlade 后，可以在 JEI 中查看三把命名刀的简介，并在 SA / SE 分类里阅读湮灭之刃、血狱和无尽星空相关效果说明。

## 注册与源码路径

SA / SE 注册位置：

- `src/main/java/QWQ/QingYi/annihilationblade/registry/ModSlashArts.java`
- `src/main/java/QWQ/QingYi/annihilationblade/registry/ModSpecialEffects.java`

主要实现路径：

- `src/main/java/QWQ/QingYi/annihilationblade/annihilation_blade/`
- `src/main/java/QWQ/QingYi/annihilationblade/blood_prison/`
- `src/main/java/QWQ/QingYi/annihilationblade/infinity_stellaris/`
- `src/main/java/QWQ/QingYi/annihilationblade/common/`
- `src/main/java/QWQ/QingYi/annihilationblade/config/ModConfig.java`
- `src/main/java/QWQ/QingYi/annihilationblade/network/`

## 构建

建议使用 Java 17：

```powershell
$env:JAVA_HOME='C:\Program Files\Zulu\zulu-17'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
$env:JAVA_TOOL_OPTIONS='-Dfile.encoding=UTF-8'
./gradlew.bat --no-daemon clean build --console=plain
```

构建产物位于：

`build/libs/annihilationblade-2.7.0-1.20.1-forge.jar`

## Changelog

### 2.7.0-1.20.1-forge

**新增与重构功能 (Features & Refactoring)**
- **新增最终兵器拔刀剑「无尽星空」 (`annihilationblade:infinity_stellaris`)**：
  - 最终兵器定位：基础攻击力提升至 `1,000,000.0`，耐久提升至 `2,147,483,647` (不可合成、不可平衡化)。
  - 被动权能：手持或背包内可获得飞行、无敌、死亡兜底、虚空坠落保护及永昼视界等基础生存保障。
  - 附魔设定：保留剑、弓、弩相关 10 级附魔，按背景设定移除了火焰附加与火矢附魔。
  - 移除旧有的麦克斯韦妖虹吸与终极协议机制，将语言文件、Tooltip、JEI 描述均更新为最终兵器设定。
- **重构专属 SE 特性**：
  - `entropy_dissolution` (熵增蚀解)：每次伤害可叠加层数，10 层后触发热寂归零处决，并剥夺目标的所有免死与保护。
  - `curvature_rupture` (曲率撕裂)：使持刀者周围 25 格内的合法目标完全静止，永久锁死 Mob AI、寻路和行动速度；删除了此前可能导致卡顿的引力拉扯位移逻辑。
  - `gamma_thunderburst` (伽马霆爆)：无尽星空伤害触发，在玩家周围 128 格内连续 3 tick 落雷，每 tick 落下 12 道自定义彩色闪电。将其改为自定义实体与专用渲染器以提升效率，移除旧版拦截原版闪电渲染的 Mixin。
  - `cosmic_string_cut` (宇宙弦切)：重构为由原生斩击事件 (`DoSlashEvent`) 触发，删除了会抢占普通右键与长按 SA 的潜行右键入口。视觉上精简了远距大面积星线，仅保留玩家周围 5x5x5 的局部星线以保证视野。
- **重构专属 SA 技能**：
  - `vacuum_decay_collapse` (重构为「绝对湮灭圈」)：以目视方块或视线前方为中心展开 `128×128` (高约 64 格) 的正方领域，持续 100 tick。领域内合法目标入圈即死，压制掉落物与经验，且不再替换方块，保证游戏流畅度。

**视觉与 GUI 重构 (Visuals & UI Aesthetics)**
- **设计了顶级「无尽星空」专属 Tooltip 悬停渲染器**：
  - **背景与魔法阵**：
    - 采用黑洞专属暗色背景面板与星图叠层线框纹理。
    - 接入动态分段光带边框与沿边框公转加自转的白色六芒星/菱形符号。
    - 新增手绘白色大型复杂多维魔法阵（包含多层圆环、多边轨道、星形连线与刻度），由 GUI 渲染代码实时实时差速绘制。
    - 限制了外扩黑洞背景的露边范围，避免在背包或 Creative/JEI 视图中铺满过宽。
  - **流光标题与内容分区**：
    - 标题 `最终兵器：无尽星空` 改为基于安全采样的运行时逐字流动 RGB 宇宙谱线渲染，彻底修复浮点越界导致创造栏或背包渲染崩溃的问题。
    - 界面模块化排版：权能核心、刀体记录、宇宙法则芯片和附魔回路分区展示，优化不同分区的动态指示条为短轨短脉冲微动效。
    - 将 Tooltip 追加逻辑完全解耦并移至客户端专用事件类中，使核心战斗逻辑不再直接依赖客户端渲染 API。

**优化与修复 (Improvements & Bug Fixes)**
- **热键与右键修复**：修复了宇宙弦切拦截普通右键长按的问题，将蓄力/释放链路完全归还给 SA。
- **渲染性能优化**：移除了先前打包进 GUI 的大体积外扩背景贴图（`infinity_stellaris_cosmic_backdrop.png`），完全依靠实时代码进行魔法阵的手绘。
- **本地化与配置同步**：同步更新了简中、繁中、香港繁中和英文的本地化文案，重构了命名刀 JSON、JEI 联动说明 JSON 与 README，保证描述与当前版本统一。
