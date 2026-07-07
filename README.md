# Annihilation Blade · Terminus 2.4.5-1.20.1-forge

> 基于 SlashBlade 的拔刀剑扩展模组，围绕“终焉”“裂界”“坍缩”“审判”主题构建一把主刀和一组风格统一的 SA / SE 组合。

作者：青衣_璃

## 概览

Annihilation Blade 提供了一把主武器 `annihilationblade:annihilation_blade`，以及围绕这把刀设计的完整效果链路：

- 1 个主 SA
- 8 个 SE
- 命名刀配置
- 粒子和音效演出
- 召唤剑、裂界牵引、因果连锁与范围处决逻辑

## 特色

- 主刀拥有鲜明的终焉主题视觉风格
- 每个 SE 都有独立触发节奏、目标筛选和表现方式
- 处决逻辑统一接入 SlashBlade 的击杀路径
- `Phantom Judgement` 使用真实召唤剑实体表现
- `World Rift`、`Causality Collapse`、`Starless Judgement` 已拆出共用支撑逻辑，导包更轻，效果主体更清晰
- `Spatial Fracture` 作为主 SA，承担大范围终结演出

## 武器

### `annihilationblade:annihilation_blade`

主武器的命名刀配置位于 `data/annihilationblade/slashblade/named_blades/annihilation_blade.json`。

| 项目 | 内容 |
| --- | --- |
| 攻击力基础值 | `50.0` |
| 耐久 | `2000` |
| SA | `annihilationblade:spatial_fracture` |
| SE 数量 | `8` |

SE 列表：

- `annihilationblade:dankong`
- `annihilationblade:world_rift`
- `annihilationblade:terminus_echo`
- `annihilationblade:void_dominion`
- `annihilationblade:causality_collapse`
- `annihilationblade:starless_judgement`
- `annihilationblade:phantom_judgement`
- `annihilationblade:abyssal_decree`

### `annihilationblade:blood_prison`

另一把红色「血狱」刀未完成，仅仅展示基础用途。

## SA

### `空间破碎` / `Spatial Fracture`

主刀绑定的 SA，属于大范围终结型斩击。

触发后会先播放开场音效，并在玩家视线方向上寻找裂隙中心。随后生成一整套裂界演出，包括：

- 空间裂环
- 裂界蛛网
- 传送门粒子
- 闪电与能量散射
- 终焉感的爆发式剑雨效果

逻辑上，它会优先锁定玩家视线前方的落点；如果视线中存在合适目标，也会把目标中心作为裂隙焦点。命中的实体会被逐个执行终焉处决，并尽量走 SlashBlade 的真实击杀路径来维持击杀计数。

## SE

| 名称 | 类型 | 表现 |
| --- | --- | --- |
| `断空` / `Dankong` | 瞬移连斩 | 在多个目标之间连续闪现并逐个斩杀，最后返回原位 |
| `裂界` / `World Rift` | 范围裂隙 | 以被命中目标为中心打开裂界，牵引并处决半径内敌对单位 |
| `归墟回响` / `Terminus Echo` | 前向回声 | 沿面朝方向连续释放多波回响斩击 |
| `虚无权域` / `Void Dominion` | 大范围领域 | 在前方区域展开裂界并逐个清场 |
| `因果坍缩` / `Causality Collapse` | 连锁审判 | 从首个目标开始按最近目标续接，生成因果锚点与桥接斩线 |
| `星寂裁决` / `Starless Judgement` | 直线裁决 | 在前方展开三重裁决波带，按投影判定沿途处决 |
| `幻影审判` / `Phantom Judgement` | 召剑审判 | 先环绕搜索，再以召唤剑落下集中打击 |
| `归墟天诏` / `Abyssal Decree` | 高位裁定 | 在头顶构筑冠冕后，从高空逐个降下审判 |

## 效果细节

### `断空` / `Dankong`

- 触发于斩击动作
- 抓取一定范围内的目标列表
- 玩家在目标之间连续瞬移斩击
- 有冷却限制，避免过快重复触发
- 适合表现高速收割

### `裂界` / `World Rift`

- 命中型范围 SE
- 以被击中的目标为中心打开裂界
- 周围半径内的敌对单位会被裂界丝线捕获并向中心牵引
- 每个目标都有独立的裂界桥接、处决爆发和最终坍缩收束
- 视觉上更接近“世界被撕开后整片区域被拉入终焉”

### `归墟回响` / `Terminus Echo`

- 前方多波推进
- 按波次释放回响斩击
- 形成层层推进的横向切割效果
- 可持续打击路径上的多个敌对目标

### `虚无权域` / `Void Dominion`

- 以玩家前方区域为中心展开
- 同步叠加多层裂界与坍缩视觉
- 对区域内多个目标逐个处决
- 适合表现“领域展开后直接清空”

### `因果坍缩` / `Causality Collapse`

- 首个命中后继续寻找附近最近目标
- 每一跳都会生成因果锚点、桥接斩线和局部处决爆发
- 目标会被轻微拉向上一段因果节点，让连锁更有“被命运拽回去”的感觉
- 末尾会以坍缩脉冲收束整条因果链

### `星寂裁决` / `Starless Judgement`

- 面向前方的直线审判
- 起手生成三道并行裁决波带，并附带贯穿光线与终点闪爆
- 沿玩家视线方向投影判定目标，越远判定区越宽
- 命中目标会被拉回裁决轨迹并触发处决爆发
- 适合中远距离清场和压制

### `幻影审判` / `Phantom Judgement`

- 多把召唤剑在玩家周围搜索与盘旋
- 先进入预备阶段，再进入落下阶段
- 使用真实召唤剑实体来表现
- 命中时带有明显的剑雨和冲击痕迹

### `归墟天诏` / `Abyssal Decree`

- 先在玩家头顶构筑冠冕效果
- 再按优先级逐个从高空降下审判
- 目标选择会参考血量、护甲和距离
- 适合表现高位裁定和最终宣判

## 注册

源码里把 SA / SE 分别注册在两个位置：

- `ModSlashArts` 负责注册 `Spatial Fracture`
- `ModSpecialEffects` 负责注册全部 8 个 SE

对应实现位于：

- `src/main/java/org/examplea/annihilationblade/specialeffect/`
- `src/main/java/org/examplea/annihilationblade/logic/`
- `src/main/java/org/examplea/annihilationblade/visual/`

## 资源

项目的视觉和听觉表现主要由以下内容组成：

- 粒子：`ENCHANT`、`END_ROD`、`REVERSE_PORTAL`、`PORTAL`、`FLASH`、`ELECTRIC_SPARK`
- 音效：末地传送门、信标、雷击、诡异坍缩等音色
- 召唤剑：`Phantom Judgement` 使用召唤剑实体进行演出
- 处决：多个 SE 最终都会调用统一的终焉执行逻辑

## 构建

本项目面向 Forge 1.20.1 环境，建议使用 Java 17 进行编译与调试。
