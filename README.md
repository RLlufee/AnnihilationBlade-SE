# AnnihilationBladeEX 2.4.8-1.21.1-neoforge

湮灭之刃的 1.21.1 NeoForge 移植版，公开命名空间为 `annihilationbladeex`，目标版本固定为 `2.4.8-1.21.1-neoforge`。

## 环境

- Minecraft `1.21.1`
- NeoForge `21.1.228`
- Java `21`
- SlashBlade Resharped `2.0.3-1.21.1`
- 本地依赖：`libs/SlashBladeResharped-2.0.3-1.21.1.jar`

## 内容

- 主刀：`annihilationbladeex:annihilation_blade`
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

## 构建

PowerShell 下使用 UTF-8 与 Java 21：

```powershell
$OutputEncoding=[Console]::OutputEncoding=[Text.UTF8Encoding]::new($false)
$env:JAVA_HOME='C:\Program Files\Zulu\zulu-21'
$env:JAVA_TOOL_OPTIONS='-Dfile.encoding=UTF-8'
.\gradlew.bat --no-daemon build --console=plain
```

产物位于 `build/libs/`，文件名应包含版本 `2.4.8-1.21.1-neoforge`。
