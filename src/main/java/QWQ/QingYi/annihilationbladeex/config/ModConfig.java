package QWQ.QingYi.annihilationbladeex.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ModConfig {
   public static final ModConfigSpec COMMON_SPEC;
   public static final Common COMMON;

   static {
      ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
      COMMON = new Common(builder);
      COMMON_SPEC = builder.build();
   }

   private ModConfig() {
   }

   public static final class Common {
      public final AnnihilationBlade annihilationBlade;
      public final BloodPrison bloodPrison;
      public final InfinityStellaris infinityStellaris;

      private Common(ModConfigSpec.Builder builder) {
         builder.push("annihilation_blade");
         this.annihilationBlade = new AnnihilationBlade(builder);
         builder.pop();
         builder.push("blood_prison");
         this.bloodPrison = new BloodPrison(builder);
         builder.pop();
         builder.push("infinity_stellaris");
         this.infinityStellaris = new InfinityStellaris(builder);
         builder.pop();
      }
   }

   public static final class AnnihilationBlade {
      public final SpatialFracture spatialFracture;
      public final Dankong dankong;
      public final WorldRift worldRift;
      public final TerminusEcho terminusEcho;
      public final VoidDominion voidDominion;
      public final CausalityCollapse causalityCollapse;
      public final StarlessJudgement starlessJudgement;
      public final PhantomJudgement phantomJudgement;
      public final AbyssalDecree abyssalDecree;

      private AnnihilationBlade(ModConfigSpec.Builder builder) {
         builder.push("spatial_fracture");
         this.spatialFracture = new SpatialFracture(builder);
         builder.pop();
         builder.push("dankong");
         this.dankong = new Dankong(builder);
         builder.pop();
         builder.push("world_rift");
         this.worldRift = new WorldRift(builder);
         builder.pop();
         builder.push("terminus_echo");
         this.terminusEcho = new TerminusEcho(builder);
         builder.pop();
         builder.push("void_dominion");
         this.voidDominion = new VoidDominion(builder);
         builder.pop();
         builder.push("causality_collapse");
         this.causalityCollapse = new CausalityCollapse(builder);
         builder.pop();
         builder.push("starless_judgement");
         this.starlessJudgement = new StarlessJudgement(builder);
         builder.pop();
         builder.push("phantom_judgement");
         this.phantomJudgement = new PhantomJudgement(builder);
         builder.pop();
         builder.push("abyssal_decree");
         this.abyssalDecree = new AbyssalDecree(builder);
         builder.pop();
      }
   }

   public static final class SpatialFracture {
      public final ModConfigSpec.DoubleValue maxDistance;
      public final ModConfigSpec.DoubleValue fractureRadius;
      public final ModConfigSpec.DoubleValue rayStep;
      public final ModConfigSpec.DoubleValue raySampleRadius;
      public final ModConfigSpec.DoubleValue entityLockRadius;
      public final ModConfigSpec.DoubleValue backupRadius;
      public final ModConfigSpec.IntValue maxTargets;
      public final ModConfigSpec.IntValue maxVisualizedTargets;
      public final ModConfigSpec.IntValue fractureSlashes;
      public final ModConfigSpec.IntValue centerSlashes;
      public final ModConfigSpec.DoubleValue visualScale;

      private SpatialFracture(ModConfigSpec.Builder builder) {
         this.maxDistance = doubleValue(builder, "max_distance", 160.0, 32.0, 256.0, "最大施法距离，建议 64-192。        还有，我他妈也忘记了这哪个SE对应啥技能效果了，我记忆力不好我是废物qwq", "Maximum cast distance. Suggested: 64-192.");
         this.fractureRadius = doubleValue(builder, "fracture_radius", 20.0, 4.0, 64.0, "空间破碎主判定半径，建议 12-32。", "Main Spatial Fracture target radius. Suggested: 12-32.");
         this.rayStep = doubleValue(builder, "ray_step", 4.0, 1.0, 12.0, "沿视线补充扫描步长，越小越密，建议 2-6。", "Backup ray scan step; lower means denser scan. Suggested: 2-6.");
         this.raySampleRadius = doubleValue(builder, "ray_sample_radius", 5.0, 1.0, 16.0, "沿视线每个采样点的扫描半径，建议 3-8。", "Backup ray sample radius. Suggested: 3-8.");
         this.entityLockRadius = doubleValue(builder, "entity_lock_radius", 3.0, 0.5, 10.0, "准星锁定实体的宽容半径，建议 2-5。", "Aim lock tolerance around entities. Suggested: 2-5.");
         this.backupRadius = doubleValue(builder, "backup_radius", 48.0, 8.0, 96.0, "没有命中目标时的备用搜索半径，建议 24-64。", "Fallback search radius when no target is found. Suggested: 24-64.");
         this.maxTargets = intValue(builder, "max_targets", 128, 1, 256, "最大影响目标数，建议 32-160。", "Maximum affected targets. Suggested: 32-160.");
         this.maxVisualizedTargets = intValue(builder, "max_visualized_targets", 32, 0, 128, "最多播放单体斩击视觉的目标数，建议 16-48。", "Targets with individual slash visuals. Suggested: 16-48.");
         this.fractureSlashes = intValue(builder, "fracture_slashes", 24, 0, 96, "裂隙风暴斩击线数量，建议 12-36。", "Fracture storm visual slash count. Suggested: 12-36.");
         this.centerSlashes = intValue(builder, "center_slashes", 12, 0, 48, "中心 SlashBlade 斩击数量，建议 6-18。", "Center SlashBlade slash count. Suggested: 6-18.");
         this.visualScale = visualScale(builder, 1.0);
      }
   }

   public static final class Dankong {
      public final ModConfigSpec.DoubleValue range;
      public final ModConfigSpec.IntValue maxTargets;
      public final ModConfigSpec.IntValue stepInterval;
      public final ModConfigSpec.IntValue cooldownTicks;
      public final ModConfigSpec.DoubleValue visualScale;

      private Dankong(ModConfigSpec.Builder builder) {
         this.range = doubleValue(builder, "range", 72.0, 8.0, 256.0, "断空搜索范围，建议 32-96。", "Severed Space target search range. Suggested: 32-96.");
         this.maxTargets = intValue(builder, "max_targets", 48, 1, 128, "连续闪现最多目标数，建议 16-64。", "Maximum blink targets. Suggested: 16-64.");
         this.stepInterval = intValue(builder, "step_interval_ticks", 3, 1, 12, "连续闪现间隔 tick，20 tick=1秒，建议 3-6。", "Ticks between blink steps; 20 ticks = 1 second. Suggested: 3-6.");
         this.cooldownTicks = intValue(builder, "cooldown_ticks", 12, 0, 100, "触发冷却 tick，建议 8-24。", "Trigger cooldown in ticks. Suggested: 8-24.");
         this.visualScale = visualScale(builder, 1.0);
      }
   }

   public static final class WorldRift {
      public final ModConfigSpec.DoubleValue radius;
      public final ModConfigSpec.IntValue maxTargets;
      public final ModConfigSpec.IntValue chainCount;
      public final ModConfigSpec.DoubleValue chainRange;
      public final ModConfigSpec.DoubleValue visualScale;

      private WorldRift(ModConfigSpec.Builder builder) {
         this.radius = doubleValue(builder, "radius", 8.0, 2.0, 32.0, "裂界扩散半径，建议 5-14。", "World Rift spread radius. Suggested: 5-14.");
         this.maxTargets = intValue(builder, "max_targets", 24, 1, 96, "裂界最多影响目标数，建议 12-36。", "Maximum World Rift targets. Suggested: 12-36.");
         this.chainCount = intValue(builder, "chain_count", 3, 1, 10, "裂界处决最多连锁次数，建议 3-5。", "Maximum World Rift execution chain depth. Suggested: 3-5.");
         this.chainRange = doubleValue(builder, "chain_range", 256.0, 1.0, 1024.0, "裂界连锁距离上限，以最初攻击者为中心，建议 128-256。", "World Rift chain range cap from the original attacker. Suggested: 128-256.");
         this.visualScale = visualScale(builder, 1.0);
      }
   }

   public static final class TerminusEcho {
      public final ModConfigSpec.DoubleValue range;
      public final ModConfigSpec.DoubleValue width;
      public final ModConfigSpec.IntValue echoCount;
      public final ModConfigSpec.IntValue echoInterval;
      public final ModConfigSpec.IntValue cooldownTicks;
      public final ModConfigSpec.IntValue maxActiveSequences;
      public final ModConfigSpec.IntValue maxTargetsPerWave;
      public final ModConfigSpec.DoubleValue visualScale;

      private TerminusEcho(ModConfigSpec.Builder builder) {
         this.range = doubleValue(builder, "range", 36.0, 8.0, 96.0, "归墟回响基础射程，建议 24-48。", "Base Terminus Echo range. Suggested: 24-48.");
         this.width = doubleValue(builder, "width", 4.4, 1.0, 16.0, "归墟回响基础宽度，建议 3-7。", "Base Terminus Echo width. Suggested: 3-7.");
         this.echoCount = intValue(builder, "echo_count", 5, 1, 12, "回响波次数量，建议 3-6。", "Echo wave count. Suggested: 3-6.");
         this.echoInterval = intValue(builder, "echo_interval_ticks", 3, 1, 12, "回响波次间隔 tick，建议 2-5。", "Ticks between echo waves. Suggested: 2-5.");
         this.cooldownTicks = intValue(builder, "cooldown_ticks", 0, 0, 120, "归墟回响冷却已取消，保留此项仅兼容旧配置。", "Terminus Echo cooldown is disabled; this value is kept only for config compatibility.");
         this.maxActiveSequences = intValue(builder, "max_active_sequences", 2, 1, 8, "同一玩家最多并存回响序列，建议 1-3。", "Maximum simultaneous echo sequences per player. Suggested: 1-3.");
         this.maxTargetsPerWave = intValue(builder, "max_targets_per_wave", 32, 1, 128, "每波最多影响目标数，建议 16-48。", "Maximum targets per echo wave. Suggested: 16-48.");
         this.visualScale = visualScale(builder, 1.0);
      }
   }

   public static final class VoidDominion {
      public final ModConfigSpec.DoubleValue range;
      public final ModConfigSpec.IntValue maxTargets;
      public final ModConfigSpec.IntValue cooldownTicks;
      public final ModConfigSpec.DoubleValue visualScale;

      private VoidDominion(ModConfigSpec.Builder builder) {
         this.range = doubleValue(builder, "range", 26.0, 4.0, 64.0, "虚无权域半径，建议 16-36。", "Void Dominion radius. Suggested: 16-36.");
         this.maxTargets = intValue(builder, "max_targets", 64, 1, 160, "虚无权域最多影响目标数，建议 32-96。", "Maximum Void Dominion targets. Suggested: 32-96.");
         this.cooldownTicks = intValue(builder, "cooldown_ticks", 70, 0, 200, "触发冷却 tick，建议 50-100。", "Trigger cooldown in ticks. Suggested: 50-100.");
         this.visualScale = visualScale(builder, 1.0);
      }
   }

   public static final class CausalityCollapse {
      public final ModConfigSpec.DoubleValue chainRadius;
      public final ModConfigSpec.IntValue maxChain;
      public final ModConfigSpec.IntValue cooldownTicks;
      public final ModConfigSpec.DoubleValue visualScale;

      private CausalityCollapse(ModConfigSpec.Builder builder) {
         this.chainRadius = doubleValue(builder, "chain_radius", 14.0, 2.0, 48.0, "因果链搜索半径，建议 8-20。", "Causality chain search radius. Suggested: 8-20.");
         this.maxChain = intValue(builder, "max_chain", 18, 1, 96, "因果链最大目标数，建议 8-30。", "Maximum causality chain targets. Suggested: 8-30.");
         this.cooldownTicks = intValue(builder, "cooldown_ticks", 10, 0, 100, "触发冷却 tick，建议 8-20。", "Trigger cooldown in ticks. Suggested: 8-20.");
         this.visualScale = visualScale(builder, 1.0);
      }
   }

   public static final class StarlessJudgement {
      public final ModConfigSpec.DoubleValue range;
      public final ModConfigSpec.DoubleValue width;
      public final ModConfigSpec.IntValue cooldownTicks;
      public final ModConfigSpec.IntValue maxTargets;
      public final ModConfigSpec.DoubleValue visualScale;

      private StarlessJudgement(ModConfigSpec.Builder builder) {
         this.range = doubleValue(builder, "range", 56.0, 8.0, 128.0, "星寂裁决射程，建议 36-72。", "Starless Judgement beam range. Suggested: 36-72.");
         this.width = doubleValue(builder, "width", 8.5, 1.0, 24.0, "星寂裁决宽度，建议 5-12。", "Starless Judgement beam width. Suggested: 5-12.");
         this.cooldownTicks = intValue(builder, "cooldown_ticks", 34, 0, 160, "触发冷却 tick，建议 24-60。", "Trigger cooldown in ticks. Suggested: 24-60.");
         this.maxTargets = intValue(builder, "max_targets", 80, 1, 200, "星寂裁决最多影响目标数，建议 40-120。", "Maximum Starless Judgement targets. Suggested: 40-120.");
         this.visualScale = visualScale(builder, 1.0);
      }
   }

   public static final class PhantomJudgement {
      public final ModConfigSpec.DoubleValue range;
      public final ModConfigSpec.IntValue searchTicks;
      public final ModConfigSpec.IntValue swordCount;
      public final ModConfigSpec.IntValue rainSwordsPerTarget;
      public final ModConfigSpec.IntValue fallingSwordDelayTicks;
      public final ModConfigSpec.IntValue lingerTicks;
      public final ModConfigSpec.IntValue maxTargets;
      public final ModConfigSpec.IntValue maxLingeringSwords;
      public final ModConfigSpec.IntValue cooldownTicks;
      public final ModConfigSpec.DoubleValue visualScale;

      private PhantomJudgement(ModConfigSpec.Builder builder) {
         this.range = doubleValue(builder, "range", 40.0, 8.0, 96.0, "幻影审判搜索范围，建议 24-56。", "Phantom Judgement search range. Suggested: 24-56.");
         this.searchTicks = intValue(builder, "search_ticks", 20, 4, 80, "锁定阶段持续 tick，建议 12-30。", "Target lock phase duration in ticks. Suggested: 12-30.");
         this.swordCount = intValue(builder, "search_sword_count", 8, 1, 24, "锁定阶段环绕剑数量，建议 6-12。", "Orbiting search sword count. Suggested: 6-12.");
         this.rainSwordsPerTarget = intValue(builder, "rain_swords_per_target", 6, 1, 16, "每个目标落剑数量，建议 4-8。", "Falling swords per target. Suggested: 4-8.");
         this.fallingSwordDelayTicks = intValue(builder, "falling_sword_delay_ticks", 24, 0, 80, "落剑命中延迟 tick，建议 18-30。", "Falling sword impact delay in ticks. Suggested: 18-30.");
         this.lingerTicks = intValue(builder, "linger_ticks", 60, 0, 200, "落剑残留 tick，建议 30-80。", "Lingering sword duration in ticks. Suggested: 30-80.");
         this.maxTargets = intValue(builder, "max_targets", 24, 1, 96, "幻影审判最多目标数，建议 12-36。", "Maximum Phantom Judgement targets. Suggested: 12-36.");
         this.maxLingeringSwords = intValue(builder, "max_lingering_swords", 96, 0, 256, "同一玩家最多残留剑数量，建议 48-128。", "Maximum lingering swords per player. Suggested: 48-128.");
         this.cooldownTicks = intValue(builder, "cooldown_ticks", 44, 0, 200, "触发冷却 tick，建议 34-80。", "Trigger cooldown in ticks. Suggested: 34-80.");
         this.visualScale = visualScale(builder, 1.0);
      }
   }

   public static final class AbyssalDecree {
      public final ModConfigSpec.DoubleValue range;
      public final ModConfigSpec.IntValue maxTargets;
      public final ModConfigSpec.IntValue strikeInterval;
      public final ModConfigSpec.IntValue cooldownTicks;
      public final ModConfigSpec.DoubleValue visualScale;

      private AbyssalDecree(ModConfigSpec.Builder builder) {
         this.range = doubleValue(builder, "range", 34.0, 6.0, 96.0, "归墟天诏搜索范围，建议 20-48。", "Abyssal Decree search range. Suggested: 20-48.");
         this.maxTargets = intValue(builder, "max_targets", 16, 1, 80, "归墟天诏最多目标数，建议 8-24。", "Maximum Abyssal Decree targets. Suggested: 8-24.");
         this.strikeInterval = intValue(builder, "strike_interval_ticks", 3, 1, 12, "逐个裁决间隔 tick，建议 2-5。", "Ticks between decree strikes. Suggested: 2-5.");
         this.cooldownTicks = intValue(builder, "cooldown_ticks", 82, 0, 240, "触发冷却 tick，建议 70-120。", "Trigger cooldown in ticks. Suggested: 70-120.");
         this.visualScale = visualScale(builder, 1.0);
      }
   }

   public static final class BloodPrison {
      public final Domain domain;
      public final PhantomBurst phantomBurst;

      private BloodPrison(ModConfigSpec.Builder builder) {
         builder.push("domain");
         this.domain = new Domain(builder);
         builder.pop();
         builder.push("phantom_burst");
         this.phantomBurst = new PhantomBurst(builder);
         builder.pop();
      }
   }

   public static final class Domain {
      public final ModConfigSpec.IntValue durationTicks;
      public final ModConfigSpec.DoubleValue radius;
      public final ModConfigSpec.IntValue borderIntervalTicks;
      public final ModConfigSpec.IntValue playerAuraIntervalTicks;
      public final ModConfigSpec.IntValue pulseIntervalTicks;
      public final ModConfigSpec.DoubleValue visualScale;

      private Domain(ModConfigSpec.Builder builder) {
         this.durationTicks = intValue(builder, "duration_ticks", 400, 40, 1200, "血狱领域持续 tick，20 tick=1秒，建议 200-600。", "Blood Prison domain duration in ticks. Suggested: 200-600.");
         this.radius = doubleValue(builder, "radius", 10.0, 3.0, 32.0, "血狱领域半径，建议 8-14。", "Blood Prison domain radius. Suggested: 8-14.");
         this.borderIntervalTicks = intValue(builder, "border_interval_ticks", 10, 2, 40, "领域边界粒子刷新间隔，建议 8-16。", "Domain border particle interval. Suggested: 8-16.");
         this.playerAuraIntervalTicks = intValue(builder, "player_aura_interval_ticks", 4, 1, 40, "领域内玩家血气粒子间隔，建议 4-10。", "Player aura particle interval inside domain. Suggested: 4-10.");
         this.pulseIntervalTicks = intValue(builder, "pulse_interval_ticks", 20, 4, 80, "领域脉冲视觉间隔，建议 16-30。", "Domain pulse visual interval. Suggested: 16-30.");
         this.visualScale = visualScale(builder, 1.0);
      }
   }

   public static final class PhantomBurst {
      public final ModConfigSpec.IntValue swordCount;
      public final ModConfigSpec.IntValue swordDelayTicks;
      public final ModConfigSpec.DoubleValue burstRadiusScale;
      public final ModConfigSpec.DoubleValue visualScale;

      private PhantomBurst(ModConfigSpec.Builder builder) {
         this.swordCount = intValue(builder, "sword_count", 10, 1, 32, "血狱幻影爆发剑数量，建议 6-14。", "Blood Prison phantom burst sword count. Suggested: 6-14.");
         this.swordDelayTicks = intValue(builder, "sword_delay_ticks", 18, 0, 80, "幻影剑命中延迟 tick，建议 12-24。", "Phantom sword impact delay in ticks. Suggested: 12-24.");
         this.burstRadiusScale = doubleValue(builder, "burst_radius_scale", 2.2, 0.5, 5.0, "爆发视觉半径与目标宽度倍率，建议 1.5-2.8。", "Burst visual radius scale against target width. Suggested: 1.5-2.8.");
         this.visualScale = visualScale(builder, 1.0);
      }
   }

   private static ModConfigSpec.DoubleValue visualScale(ModConfigSpec.Builder builder, double defaultValue) {
      return doubleValue(builder, "visual_scale", defaultValue, 0.25, 2.0, "视觉倍率，只影响低风险视觉半径/粒子密度，建议 0.5-1.25。", "Visual scale; only affects low-risk visual radius/particle density. Suggested: 0.5-1.25.");
   }

   private static ModConfigSpec.IntValue intValue(
      ModConfigSpec.Builder builder, String name, int defaultValue, int min, int max, String chineseComment, String englishComment
   ) {
      return builder.comment(chineseComment + " 推荐最小/最大值: " + min + "-" + max + ".", englishComment + " Suggested min/max: " + min + "-" + max + ".")
         .defineInRange(name, defaultValue, min, max);
   }

   private static ModConfigSpec.DoubleValue doubleValue(
      ModConfigSpec.Builder builder, String name, double defaultValue, double min, double max, String chineseComment, String englishComment
   ) {
      return builder.comment(chineseComment + " 推荐最小/最大值: " + min + "-" + max + ".", englishComment + " Suggested min/max: " + min + "-" + max + ".")
         .defineInRange(name, defaultValue, min, max);
   }

   public static final class InfinityStellaris {
      public final ModConfigSpec.DoubleValue entropyPercent;
      public final ModConfigSpec.IntValue entropyMarks;
      public final ModConfigSpec.IntValue entropyBlacklistTicks;
      public final ModConfigSpec.DoubleValue curvatureRadius;

      private InfinityStellaris(ModConfigSpec.Builder builder) {
         this.entropyPercent = doubleValue(builder, "entropy_percent", 0.10, 0.0, 1.0, "熵增蚀解每次追加的最大生命百分比。", "Max-health percentage added by Entropy Dissolution.");
         this.entropyMarks = intValue(builder, "entropy_marks", 10, 1, 100, "触发热寂归零所需累计次数。", "Marks required before heat-death zeroing.");
         this.entropyBlacklistTicks = intValue(builder, "entropy_blacklist_ticks", 40, 0, 400, "非玩家目标最终阶段后的短期黑名单 tick。", "Temporary blacklist ticks after final non-player execution.");
         this.curvatureRadius = doubleValue(builder, "curvature_radius", 25.0, 1.0, 128.0, "曲率撕裂冻结半径。", "Curvature rupture freeze radius.");
      }
   }
}
