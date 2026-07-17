package QWQ.QingYi.annihilationblade.infinity_stellaris.visual;

import QWQ.QingYi.annihilationblade.common.ServerTickScheduler;
import QWQ.QingYi.annihilationblade.common.SpecialEffectSupport;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class InfinityStellarisVisuals {
   private static final int CHAIN_TICKS = 5;
   private static final int CHAIN_POINTS = 36;

   private InfinityStellarisVisuals() {
   }

   public static void spawnDamageChain(ServerLevel level, Player player, LivingEntity target) {
      Vec3 start = player.getEyePosition().subtract(0.0, 0.25, 0.0);
      Vec3 end = SpecialEffectSupport.centerOf(target);
      spawnDamageChain(level, start, end);
   }

   public static void spawnDamageChain(ServerLevel level, Vec3 start, Vec3 end) {
      for (int tick = 0; tick < CHAIN_TICKS; tick++) {
         int delay = tick;
         ServerTickScheduler.schedule(delay, () -> spawnChainFrame(level, start, end));
      }
   }

   private static void spawnChainFrame(ServerLevel level, Vec3 start, Vec3 end) {
      Vec3 delta = end.subtract(start);
      if (delta.lengthSqr() < 1.0E-6) {
         return;
      }

      Vec3 normal = delta.cross(new Vec3(0.0, 1.0, 0.0));
      if (normal.lengthSqr() < 1.0E-6) {
         normal = new Vec3(1.0, 0.0, 0.0);
      } else {
         normal = normal.normalize();
      }

      for (int i = 0; i <= CHAIN_POINTS; i++) {
         double t = (double)i / CHAIN_POINTS;
         double wave = Math.sin(t * Math.PI * 8.0) * 0.08;
         Vec3 pos = start.lerp(end, t).add(normal.scale(wave));
         level.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 1, 0.01, 0.01, 0.01, 0.0);
         if (i % 2 == 0) {
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y, pos.z, 1, 0.015, 0.015, 0.015, 0.01);
         }
         if (i % 4 == 0) {
            level.sendParticles(ParticleTypes.ENCHANT, pos.x, pos.y, pos.z, 1, 0.02, 0.02, 0.02, 0.0);
         }
      }
   }
}
