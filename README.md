# Annihilation Blade · Terminus 2.6.1-1.20.1-forge

> 基于 SlashBlade / SlashBlade Resharped 的 Forge 1.20.1 拔刀剑扩展模组。模组围绕“终焉、裂界、坍缩、审判、血狱”主题，提供两把命名刀、两套 SA、完整 SE 链路、可配置低风险参数，以及面向实战可读性的视觉与按键控制。

作者：青衣_璃

## 概览

当前版本包含：

- 主刀 `annihilationblade:annihilation_blade`
- 血狱刀 `annihilationblade:blood_prison`
- 2 个 SA：`spatial_fracture`、`infernal_slaughter`
- 11 个 SE 注册项，其中湮灭之刃使用 8 个终焉系 SE，血狱使用 3 个血狱系 SE
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

## SA

### `空间破碎` / `Spatial Fracture`

湮灭之刃绑定的主 SA。触发后会沿玩家视线寻找裂隙中心，生成空间裂环、裂界蛛网、传送门粒子、闪电散射和剑雨演出。

逻辑上会优先锁定视线前方落点；如果准星路径上存在合适目标，则以目标中心作为裂隙焦点。命中实体会逐个执行终焉处决，并尽量走 SlashBlade 的真实击杀路径来维持击杀计数。

已开放配置项包括最大距离、裂隙半径、视线扫描步长、采样半径、锁定半径、备用搜索半径、目标上限、可视化目标数、斩击线数量和视觉倍率。

### `炼狱杀戮` / `Infernal Slaughter`

血狱刀绑定的 SA。触发后展开血狱领域，并同步客户端领域覆盖效果。领域持续期间，玩家斩击会在领域内选取敌对单位进行穿梭打击，并记录领域内造成的伤害，用于结束时的治疗反馈。

已开放配置项包括领域持续时间、领域半径、边界粒子刷新间隔、玩家血气粒子间隔、领域脉冲间隔和视觉倍率。

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

每个配置项都带有中文和英文注释，并写明建议最小 / 最大值。Forge 也会通过 `defineInRange` 对配置值做硬范围限制，避免新玩家填入极端数值导致卡顿或逻辑异常。

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
- SA 描述键：`slashblade.slash_art.annihilationblade.*.desc`
- SE 描述键：`se.annihilationblade.*.desc`

安装 JEI SlashBlade 后，可以在 JEI 中查看两把命名刀的简介，并在 SA / SE 分类里阅读湮灭之刃和血狱相关效果说明。

## 注册与源码路径

SA / SE 注册位置：

- `src/main/java/QWQ/QingYi/annihilationblade/registry/ModSlashArts.java`
- `src/main/java/QWQ/QingYi/annihilationblade/registry/ModSpecialEffects.java`

主要实现路径：

- `src/main/java/QWQ/QingYi/annihilationblade/annihilation_blade/`
- `src/main/java/QWQ/QingYi/annihilationblade/blood_prison/`
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

`build/libs/annihilationblade-2.6.1-1.20.1-forge.jar`
