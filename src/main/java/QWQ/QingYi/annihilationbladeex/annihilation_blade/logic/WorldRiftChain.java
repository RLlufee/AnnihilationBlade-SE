package QWQ.QingYi.annihilationbladeex.annihilation_blade.logic;

import QWQ.QingYi.annihilationbladeex.annihilation_blade.visual.AnnihilationVisuals;
import QWQ.QingYi.annihilationbladeex.common.ServerTickScheduler;
import QWQ.QingYi.annihilationbladeex.common.SpecialEffectSupport;
import QWQ.QingYi.annihilationbladeex.config.ModConfig;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class WorldRiftChain {
   private static final int DELAY_TICKS = 5;
   private static boolean executingChain;

   private WorldRiftChain() {
   }

   public static boolean isExecutingChain() {
      return executingChain;
   }

   public static void scheduleFromDamage(ServerLevel level, Player attacker, LivingEntity damaged) {
      if (!executingChain) {
         schedule(level, attacker.getUUID(), SpecialEffectSupport.centerOf(attacker), SpecialEffectSupport.centerOf(damaged), 1);
      }
   }

   private static void schedule(ServerLevel level, UUID attackerId, Vec3 attackerOrigin, Vec3 center, int depth) {
      int maxDepth = ModConfig.COMMON.annihilationBlade.worldRift.chainCount.get();
      if (depth > maxDepth) {
         return;
      }

      ServerTickScheduler.schedule(DELAY_TICKS, () -> execute(level, attackerId, attackerOrigin, center, depth));
   }

   private static void execute(ServerLevel level, UUID attackerId, Vec3 attackerOrigin, Vec3 center, int depth) {
      ServerPlayer attacker = level.getServer().getPlayerList().getPlayer(attackerId);
      if (attacker == null || attacker.level() != level) {
         return;
      }

      ModConfig.WorldRift config = ModConfig.COMMON.annihilationBlade.worldRift;
      double chainRange = config.chainRange.get();
      double chainRangeSqr = chainRange * chainRange;
      if (center.distanceToSqr(attackerOrigin) > chainRangeSqr) {
         return;
      }

      double radius = config.radius.get();
      double visualScale = config.visualScale.get();
      List<LivingEntity> targets = SpecialEffectSupport.radialTargets(
         level,
         attacker,
         center,
         radius,
         entity -> SpecialEffectSupport.centerOf(entity).distanceToSqr(attackerOrigin) <= chainRangeSqr
      );
      if (targets.isEmpty()) {
         return;
      }

      AnnihilationVisuals.spawnWorldRiftOpening(level, center, radius * visualScale);
      int count = 0;
      for (LivingEntity target : targets) {
         Vec3 targetCenter = SpecialEffectSupport.centerOf(target);
         AnnihilationVisuals.spawnWorldRiftThread(level, center, targetCenter, count, attacker.getRandom());
         SpecialEffectSupport.pullToward(target, center, 0.18);
         AnnihilationVisuals.spawnExecutionBurst(level, target, attacker.getRandom());
         executingChain = true;
         try {
            TerminusLogic.execute(target, attacker);
         } finally {
            executingChain = false;
         }

         if (depth < config.chainCount.get()) {
            schedule(level, attackerId, attackerOrigin, targetCenter, depth + 1);
         }
         count++;
      }

      AnnihilationVisuals.spawnCollapsePulse(level, center, radius * 0.72 * visualScale, count);
   }
}
