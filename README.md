# AnnihilationBladeEX 2.7.0-1.21.1-neoforge

湮灭之刃的 1.21.1 NeoForge 移植版，公开命名空间为 `annihilationbladeex`，目标版本固定为 `2.7.0-1.21.1-neoforge`。

## 环境

- Minecraft `1.21.1`
- NeoForge `21.1.228`
- Java `21`
- SlashBlade Resharped `2.0.3-1.21.1`
- 本地依赖：`libs/SlashBladeResharped-2.0.3-1.21.1.jar`

## 内容

- 主刀：`annihilationbladeex:annihilation_blade`
- 血狱：`annihilationbladeex:blood_prison`
- 湮灭核心：右键生成同一把 canonical 湮灭之刃
- Slash Art：`annihilationbladeex:spatial_fracture`
- Special Effects：
  - `annihilationbladeex:dankong`
  - `annihilationbladeex:world_rift`
  - `annihilationbladeex:terminus_echo`
  - `annihilationbladeex:void_dominion`
  - `annihilationbladeex:causality_collapse`
  - `annihilationbladeex:starless_judgement`
  - `annihilationbladeex:phantom_judgement`
  - `annihilationbladeex:abyssal_decree`

`Phantom Judgement` 使用 40 格索敌范围，触发后生成受限幻影剑雨；剑雨击杀目标后会在地面短暂保留，用于提示击杀来源，同时降低服务器实体与粒子峰值。

湮灭之刃位于背包、主手或副手时，客户端会以光照贴图提供无药水图标的夜视级照明；该效果不写入玩家的药水状态。

所有普攻、SA 与 SE 的目标判定统一遵循 SlashBlade 的 `pvp_enable` 与 `friendly_enable` 配置，默认不会伤及玩家、宠物和非敌对单位。

裂界会在湮灭之刃伤害生效 5 tick 后，以受击位置为中心处决周围所有合法目标，并继续连锁。连锁次数和以初始攻击者为中心的最大连锁范围可在 common config 的 `annihilation_blade.world_rift.chain_count` 与 `chain_range` 调整。

## 构建

PowerShell 下使用 UTF-8 与 Java 21：

```powershell
$OutputEncoding=[Console]::OutputEncoding=[Text.UTF8Encoding]::new($false)
$env:JAVA_HOME='C:\Program Files\Zulu\zulu-21'
$env:JAVA_TOOL_OPTIONS='-Dfile.encoding=UTF-8'
.\gradlew.bat --no-daemon build --console=plain
```

产物位于 `build/libs/`，文件名应包含版本 `2.7.0-1.21.1-neoforge`。

## Changelog
### v2.7.0
- **无尽星空 (Infinity Stellaris) 移植适配**：
  - 迁移了 `infinity_stellaris.obj` 模型贴图以及 GUI 资源。
  - 对 1.21.1 目标工程的 `zh_cn.json`、`zh_tw.json`、`zh_hk.json` 及 `en_us.json` 进行了非破坏性追加，将无尽星空的全部翻译改写为 `annihilationbladeex` 命名空间并合并。
  - 对 `NamedBladeStacks` 数据配置进行适配，新增 `creativeGroup` 和 `item` 组件定义，契合 1.21.1 命名刀规范。
  - 新建 `ModEntities.java` 注册实体，在主类注册其总线。
  - 在 `ModSlashArts`、`ModSpecialEffects`、`ModComboStates`、`ModConfig` 中，合入了无尽星空对应的 `vacuum_decay_collapse` SA、四个特效、绝对湮灭圈状态及对应 Config 配置。
  - 重构了 `InfinityStellarisDefinitions.java`，完全采用 1.21.1 的 `BladeStateData` 组件和 `Enchantment` Holder 模式对无尽星空基础属性和附魔进行运行时绑定。
  - 重写了 `InfinityStellarisTooltipRenderer.java`，完成了 Tesselator begin/buildOrThrow 及 VertexBuilder 格式在 1.21 渲染系统中的重构。
  - 修复并适配了 `CurvatureRuptureLogic.java` 与 `InfinityStellarisLogic.java` 中的 `PlayerTickEvent.Post` 等事件总线签名。
  - 在 `EntropyDissolutionLogic.java` 与 `GammaThunderburstLogic.java` 中，将事件监听更改为兼容 1.21 取消机制的 `LivingIncomingDamageEvent`。
