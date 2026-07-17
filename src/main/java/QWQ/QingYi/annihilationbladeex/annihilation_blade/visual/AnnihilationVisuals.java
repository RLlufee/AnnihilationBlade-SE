package QWQ.QingYi.annihilationbladeex.annihilation_blade.visual;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class AnnihilationVisuals {
   private static final double TAU = Math.PI * 2;
   private static final Vec3 UP = new Vec3(0.0, 1.0, 0.0);
   private static final Vec3 X_AXIS = new Vec3(1.0, 0.0, 0.0);
   private static final Vec3 Z_AXIS = new Vec3(0.0, 0.0, 1.0);
   private static final ParticleOptions VOID_PURPLE = ParticleTypes.ENCHANT;
   private static final ParticleOptions COSMIC_CYAN = ParticleTypes.ENCHANT;
   private static final ParticleOptions TERMINUS_GOLD = ParticleTypes.ENCHANT;
   private static final ParticleOptions BLACK_CORE = ParticleTypes.ENCHANT;

   private AnnihilationVisuals() {
   }

   public static void spawnHeldBladeAura(ServerLevel level, LivingEntity wielder, int tick) {
      Vec3 look = safeNormalize(wielder.getLookAngle(), Z_AXIS);
      Vec3 right = safeNormalize(look.cross(UP), X_AXIS);
      Vec3 eye = wielder.getEyePosition();
      Vec3 hilt = eye.add(look.scale(0.55)).add(right.scale(0.34)).add(0.0, -0.52, 0.0);
      Vec3 tip = hilt.add(look.scale(1.42)).add(0.0, -0.42, 0.0);
      spawnLine(level, hilt, tip, COSMIC_CYAN, 7, 0.012);
      spawnLine(level, hilt.add(right.scale(-0.08)), tip.add(right.scale(0.08)), VOID_PURPLE, 6, 0.018);
      if (tick % 8 == 0) {
         Vec3 center = wielder.position().add(0.0, wielder.getBbHeight() * 0.52, 0.0);
         double radius = Math.max(0.75, wielder.getBbWidth() * 1.65);
         spawnRing(level, center, X_AXIS, Z_AXIS, radius, VOID_PURPLE, 26, 0.008);
         spawnRing(level, center.add(0.0, 0.18, 0.0), X_AXIS, Z_AXIS, radius * 0.62, COSMIC_CYAN, 18, 0.008);
         level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y, center.z, 8, 0.35, 0.45, 0.35, 0.08);
         level.sendParticles(ParticleTypes.END_ROD, tip.x, tip.y, tip.z, 4, 0.05, 0.05, 0.05, 0.01);
      }
   }

   public static void spawnOpeningHalo(ServerLevel level, Vec3 center, double radius) {
      Vec3 c = center.add(0.0, 1.0, 0.0);
      level.sendParticles(ParticleTypes.FLASH, c.x, c.y, c.z, 2, 0.12, 0.12, 0.12, 0.0);
      spawnRing(level, c, X_AXIS, Z_AXIS, radius, VOID_PURPLE, 168, 0.01);
      spawnRing(level, c.add(0.0, 1.2, 0.0), X_AXIS, Z_AXIS, radius * 0.64, COSMIC_CYAN, 116, 0.01);
      spawnRing(level, c.add(0.0, 2.6, 0.0), X_AXIS, Z_AXIS, radius * 0.36, TERMINUS_GOLD, 72, 0.008);
      spawnRing(level, c, X_AXIS, UP, radius * 0.82, COSMIC_CYAN, 116, 0.012);
      spawnRing(level, c, Z_AXIS, UP, radius * 0.82, VOID_PURPLE, 116, 0.012);

      for (int i = 0; i < 12; i++) {
         double angle = (Math.PI * 2) * i / 12.0;
         Vec3 dir = new Vec3(Math.cos(angle), 0.0, Math.sin(angle));
         Vec3 inner = c.add(dir.scale(radius * 0.18));
         Vec3 outer = c.add(dir.scale(radius * 0.95)).add(0.0, (i % 3 - 1) * 0.55, 0.0);
         spawnLine(level, inner, outer, i % 2 == 0 ? TERMINUS_GOLD : COSMIC_CYAN, 18, 0.02);
      }
   }

   public static void spawnFractureWeb(ServerLevel level, Vec3 center, double radius, RandomSource random) {
      Vec3 c = center.add(0.0, 1.4, 0.0);

      for (int i = 0; i < 36; i++) {
         double angle = random.nextDouble() * (Math.PI * 2);
         double distance = radius * (0.28 + random.nextDouble() * 0.9);
         Vec3 dir = new Vec3(Math.cos(angle), (random.nextDouble() - 0.5) * 0.35, Math.sin(angle)).normalize();
         Vec3 end = c.add(dir.scale(distance)).add(0.0, random.nextDouble() * 3.8 - 1.3, 0.0);
         ParticleOptions particle = (ParticleOptions)(i % 3 == 0 ? COSMIC_CYAN : (i % 3 == 1 ? VOID_PURPLE : ParticleTypes.ELECTRIC_SPARK));
         spawnLine(level, c, end, particle, 13 + random.nextInt(8), 0.035);
         if (i % 4 == 0) {
            Vec3 side = safeNormalize(dir.cross(UP), X_AXIS).scale((random.nextBoolean() ? 1.0 : -1.0) * distance * 0.23);
            spawnLine(level, end, end.add(side), TERMINUS_GOLD, 8, 0.025);
         }
      }
   }

   public static void spawnCollapsePulse(ServerLevel level, Vec3 center, double radius, int intensity) {
      Vec3 c = center.add(0.0, 1.0, 0.0);
      int sparks = Math.min(240, 80 + intensity * 3);
      level.sendParticles(ParticleTypes.REVERSE_PORTAL, c.x, c.y, c.z, sparks, radius * 0.55, 2.6, radius * 0.55, 0.85);
      level.sendParticles(ParticleTypes.WITCH, c.x, c.y, c.z, 60, radius * 0.35, 1.4, radius * 0.35, 0.08);
      spawnRing(level, c.add(0.0, 0.25, 0.0), X_AXIS, Z_AXIS, radius * 0.45, BLACK_CORE, 96, 0.01);
      spawnRing(level, c.add(0.0, 0.35, 0.0), X_AXIS, Z_AXIS, radius * 0.75, VOID_PURPLE, 132, 0.012);
      spawnRing(level, c.add(0.0, 0.45, 0.0), X_AXIS, Z_AXIS, radius, COSMIC_CYAN, 168, 0.014);
   }

   public static void spawnExecutionBurst(ServerLevel level, LivingEntity target, RandomSource random) {
      Vec3 center = target.position().add(0.0, target.getBbHeight() * 0.55, 0.0);
      double radius = Math.max(0.9, target.getBbWidth() * 1.65);
      spawnWhiteExecutionCircle(level, target, radius);
      level.sendParticles(ParticleTypes.FLASH, center.x, center.y, center.z, 1, 0.0, 0.0, 0.0, 0.0);
      level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y, center.z, 16, radius * 0.38, radius * 0.55, radius * 0.38, 0.22);
      level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, center.x, center.y, center.z, 6, radius * 0.26, radius * 0.36, radius * 0.26, 0.03);
      spawnRing(level, center, X_AXIS, Z_AXIS, radius * 1.2, VOID_PURPLE, 20, 0.006);
      spawnRing(level, center, X_AXIS, UP, radius * 1.05, COSMIC_CYAN, 18, 0.006);
      spawnRing(level, center, Z_AXIS, UP, radius * 1.05, TERMINUS_GOLD, 18, 0.006);
      spawnLine(level, center.add(-radius, radius * 0.9, -radius), center.add(radius, -radius * 0.9, radius), COSMIC_CYAN, 7, 0.01);
      spawnLine(level, center.add(radius, radius * 0.9, -radius), center.add(-radius, -radius * 0.9, radius), VOID_PURPLE, 7, 0.01);

      for (int i = 0; i < 4; i++) {
         Vec3 dir = randomUnit(random);
         spawnLine(level, center, center.add(dir.scale(radius * (1.2 + random.nextDouble() * 0.9))), ParticleTypes.ELECTRIC_SPARK, 5, 0.018);
      }
   }

   private static void spawnWhiteExecutionCircle(ServerLevel level, LivingEntity target, double targetRadius) {
      Vec3 foot = target.position().add(0.0, 0.06, 0.0);
      double radius = Math.max(1.15, targetRadius * 1.55);
      spawnRing(level, foot, X_AXIS, Z_AXIS, radius, ParticleTypes.END_ROD, 64, 0.002);
      spawnRing(level, foot.add(0.0, 0.03, 0.0), X_AXIS, Z_AXIS, radius * 0.72, ParticleTypes.FIREWORK, 48, 0.002);
      spawnRing(level, foot.add(0.0, 0.06, 0.0), X_AXIS, Z_AXIS, radius * 0.42, ParticleTypes.END_ROD, 32, 0.001);

      for (int i = 0; i < 6; i++) {
         double a = (Math.PI * 2) * i / 6.0;
         double b = a + (Math.PI * 2.0 / 3.0);
         Vec3 start = foot.add(Math.cos(a) * radius * 0.88, 0.08, Math.sin(a) * radius * 0.88);
         Vec3 end = foot.add(Math.cos(b) * radius * 0.88, 0.08, Math.sin(b) * radius * 0.88);
         spawnLine(level, start, end, ParticleTypes.END_ROD, 12, 0.001);
      }

      for (int i = 0; i < 12; i++) {
         double angle = (Math.PI * 2) * i / 12.0;
         Vec3 inner = foot.add(Math.cos(angle) * radius * 0.28, 0.1, Math.sin(angle) * radius * 0.28);
         Vec3 outer = foot.add(Math.cos(angle) * radius, 0.1, Math.sin(angle) * radius);
         spawnLine(level, inner, outer, i % 2 == 0 ? ParticleTypes.END_ROD : ParticleTypes.FIREWORK, 5, 0.001);
      }

      level.sendParticles(ParticleTypes.END_ROD, foot.x, foot.y + 0.16, foot.z, 18, radius * 0.34, 0.02, radius * 0.34, 0.018);
      level.sendParticles(ParticleTypes.FLASH, foot.x, foot.y + 0.18, foot.z, 1, 0.0, 0.0, 0.0, 0.0);
   }

   public static void spawnBlinkGate(ServerLevel level, Vec3 center, double radius) {
      level.sendParticles(ParticleTypes.FLASH, center.x, center.y, center.z, 1, 0.0, 0.0, 0.0, 0.0);
      level.sendParticles(ParticleTypes.PORTAL, center.x, center.y, center.z, 42, radius * 0.45, radius * 0.28, radius * 0.45, 0.35);
      spawnRing(level, center, X_AXIS, Z_AXIS, radius, VOID_PURPLE, 48, 0.008);
      spawnRing(level, center, X_AXIS, UP, radius * 0.72, COSMIC_CYAN, 38, 0.008);
      spawnRing(level, center, Z_AXIS, UP, radius * 0.72, TERMINUS_GOLD, 38, 0.008);
   }

   public static void spawnBlinkTrail(ServerLevel level, Vec3 start, Vec3 end, RandomSource random) {
      spawnLine(level, start, end, VOID_PURPLE, 24, 0.035);
      spawnLine(level, start, end, COSMIC_CYAN, 18, 0.02);
      Vec3 direction = safeNormalize(end.subtract(start), Z_AXIS);
      Vec3 right = safeNormalize(direction.cross(UP), X_AXIS);

      for (int i = 0; i < 5; i++) {
         double progress = (i + 1.0) / 6.0;
         Vec3 center = start.lerp(end, progress);
         Vec3 slash = right.scale(0.65 + random.nextDouble() * 0.7).add(0.0, random.nextDouble() * 0.8 - 0.4, 0.0);
         spawnLine(level, center.subtract(slash), center.add(slash), (ParticleOptions)(i % 2 == 0 ? ParticleTypes.ELECTRIC_SPARK : TERMINUS_GOLD), 7, 0.012);
      }
   }

   public static void spawnWorldRiftBloom(ServerLevel level, Vec3 center, double radius) {
      level.sendParticles(ParticleTypes.WITCH, center.x, center.y, center.z, 46, radius * 0.35, 1.2, radius * 0.35, 0.05);
      spawnRing(level, center, X_AXIS, Z_AXIS, radius, BLACK_CORE, 96, 0.01);
      spawnRing(level, center.add(0.0, 0.8, 0.0), X_AXIS, Z_AXIS, radius * 0.72, VOID_PURPLE, 84, 0.012);
      spawnRing(level, center.add(0.0, 1.6, 0.0), X_AXIS, Z_AXIS, radius * 0.46, COSMIC_CYAN, 64, 0.012);
      spawnRing(level, center.add(0.0, 0.5, 0.0), X_AXIS, UP, radius * 0.82, TERMINUS_GOLD, 72, 0.012);
      spawnRing(level, center.add(0.0, 0.5, 0.0), Z_AXIS, UP, radius * 0.82, VOID_PURPLE, 72, 0.012);

      for (int i = 0; i < 10; i++) {
         double angle = (Math.PI * 2) * i / 10.0;
         Vec3 dir = new Vec3(Math.cos(angle), 0.0, Math.sin(angle));
         Vec3 inner = center.add(dir.scale(radius * 0.28)).add(0.0, 0.5, 0.0);
         Vec3 outer = center.add(dir.scale(radius * (0.78 + i % 3 * 0.08))).add(0.0, 0.2 + i % 2 * 0.8, 0.0);
         spawnLine(level, inner, outer, (ParticleOptions)(i % 2 == 0 ? COSMIC_CYAN : ParticleTypes.ELECTRIC_SPARK), 14, 0.02);
      }
   }

   public static void spawnWorldRiftOpening(ServerLevel level, Vec3 center, double radius) {
      level.playSound(null, center.x, center.y, center.z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.8F, 0.45F);
      level.playSound(null, center.x, center.y, center.z, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 1.2F, 1.7F);
      level.sendParticles(ParticleTypes.FLASH, center.x, center.y, center.z, 1, 0.0, 0.0, 0.0, 0.0);
      level.sendParticles(ParticleTypes.PORTAL, center.x, center.y, center.z, 160, radius * 0.65, 2.0, radius * 0.65, 0.8);
      spawnWorldRiftBloom(level, center, radius);
      spawnRing(level, center, X_AXIS, Z_AXIS, radius * 1.1, ParticleTypes.REVERSE_PORTAL, 104, 0.01);
      spawnRing(level, center.add(0.0, 1.4, 0.0), X_AXIS, Z_AXIS, radius * 0.55, ParticleTypes.END_ROD, 64, 0.008);
   }

   public static void spawnWorldRiftThread(ServerLevel level, Vec3 origin, Vec3 target, int index, RandomSource random) {
      double width = 0.72 + index % 4 * 0.08;
      spawnSlashBridge(level, origin, target, width, random);
      spawnLine(level, origin, target, ParticleTypes.ELECTRIC_SPARK, 14, 0.025);
      level.sendParticles(ParticleTypes.END_ROD, target.x, target.y, target.z, 24, 0.5, 0.7, 0.5, 0.08);
      if (index % 3 == 0) {
         level.sendParticles(ParticleTypes.REVERSE_PORTAL, target.x, target.y, target.z, 18, 0.45, 0.45, 0.45, 0.18);
      }
   }

   public static void spawnCausalityAnchor(ServerLevel level, Vec3 center, int chainSize) {
      double radius = Math.min(7.0, 2.4 + chainSize * 0.18);
      level.playSound(null, center.x, center.y, center.z, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.85F);
      level.playSound(null, center.x, center.y, center.z, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.2F, 0.7F);
      level.sendParticles(ParticleTypes.FLASH, center.x, center.y, center.z, 1, 0.0, 0.0, 0.0, 0.0);
      spawnRing(level, center.add(0.0, 0.45, 0.0), X_AXIS, Z_AXIS, radius, COSMIC_CYAN, 72, 0.008);
      spawnRing(level, center.add(0.0, 1.2, 0.0), X_AXIS, Z_AXIS, radius * 0.58, TERMINUS_GOLD, 52, 0.008);
   }

   public static void spawnCausalityStep(ServerLevel level, Vec3 previous, Vec3 target, int index, RandomSource random) {
      double width = 0.85 + index * 0.055;
      spawnSlashBridge(level, previous, target, width, random);
      spawnLine(level, previous, target, index % 2 == 0 ? ParticleTypes.END_ROD : ParticleTypes.ELECTRIC_SPARK, 18, 0.018);
      spawnRing(level, target, X_AXIS, Z_AXIS, 0.9 + index * 0.04, index % 2 == 0 ? COSMIC_CYAN : TERMINUS_GOLD, 32, 0.006);
      level.sendParticles(ParticleTypes.WITCH, target.x, target.y, target.z, 16, 0.36, 0.42, 0.36, 0.04);
   }

   public static void spawnEchoWave(ServerLevel level, Vec3 start, Vec3 direction, Vec3 right, double range, double width, int wave) {
      Vec3 forward = safeNormalize(direction, Z_AXIS);
      Vec3 side = safeNormalize(right, X_AXIS);
      Vec3 up = safeNormalize(side.cross(forward), UP);
      Vec3 end = start.add(forward.scale(range));
      spawnLine(level, start, end, wave % 2 == 0 ? TERMINUS_GOLD : COSMIC_CYAN, 34, 0.018);

      for (int lane = -2; lane <= 2; lane++) {
         if (lane != 0) {
            double offset = lane * width * 0.24;
            ParticleOptions particle = Math.abs(lane) == 2 ? VOID_PURPLE : COSMIC_CYAN;
            spawnLine(level, start.add(side.scale(offset)), end.add(side.scale(offset)), particle, 24, 0.025);
         }
      }

      for (double distance = 5.0; distance < range; distance += 7.0) {
         Vec3 center = start.add(forward.scale(distance));
         double radius = width * (0.28 + distance / range * 0.22) + wave * 0.12;
         spawnRing(level, center, side, up, radius, wave % 2 == 0 ? VOID_PURPLE : TERMINUS_GOLD, 24, 0.01);
      }
   }

   public static void spawnStarlessJudgementCast(ServerLevel level, Vec3 start, Vec3 direction, Vec3 right, double range, double width) {
      Vec3 forward = safeNormalize(direction, Z_AXIS);
      Vec3 side = safeNormalize(right, X_AXIS);
      Vec3 end = start.add(forward.scale(range));
      level.playSound(null, start.x, start.y, start.z, SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 1.2F, 1.75F);
      level.playSound(null, start.x, start.y, start.z, SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 0.8F, 1.35F);

      for (int lane = -1; lane <= 1; lane++) {
         Vec3 laneStart = start.add(side.scale(lane * width * 0.45)).add(0.0, Math.abs(lane) * 0.35, 0.0);
         spawnEchoWave(level, laneStart, forward, side, range + Math.abs(lane) * 7.0, width, lane + 1);
      }

      spawnLine(level, start, end, ParticleTypes.END_ROD, 44, 0.01);
      spawnLine(level, start.add(side.scale(width * 0.72)), end.add(side.scale(-width * 0.38)), ParticleTypes.ELECTRIC_SPARK, 32, 0.02);
      spawnLine(level, start.add(side.scale(-width * 0.72)), end.add(side.scale(width * 0.38)), TERMINUS_GOLD, 32, 0.02);
      level.sendParticles(ParticleTypes.FLASH, end.x, end.y, end.z, 1, 0.0, 0.0, 0.0, 0.0);
   }

   public static void spawnSlashBridge(ServerLevel level, Vec3 start, Vec3 end, double width, RandomSource random) {
      Vec3 direction = safeNormalize(end.subtract(start), Z_AXIS);
      Vec3 right = safeNormalize(direction.cross(UP), X_AXIS);
      spawnLine(level, start, end, VOID_PURPLE, 16, 0.018);
      spawnLine(level, start.add(right.scale(width)), end.add(right.scale(-width)), COSMIC_CYAN, 12, 0.018);
      spawnLine(level, start.add(right.scale(-width)), end.add(right.scale(width)), TERMINUS_GOLD, 12, 0.018);

      for (int i = 0; i < 4; i++) {
         Vec3 mid = start.lerp(end, random.nextDouble());
         Vec3 slash = right.scale(width * (0.5 + random.nextDouble() * 0.9));
         spawnLine(level, mid.subtract(slash), mid.add(slash), ParticleTypes.ELECTRIC_SPARK, 6, 0.012);
      }
   }

   public static void spawnBloodPrisonBurst(ServerLevel level, Vec3 center, double radius, RandomSource random) {
      level.sendParticles(ParticleTypes.FLASH, center.x, center.y, center.z, 2, 0.0, 0.0, 0.0, 0.0);
      level.sendParticles(ParticleTypes.CRIMSON_SPORE, center.x, center.y, center.z, 72, radius * 0.42, radius * 0.34, radius * 0.42, 0.08);
      level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, center.x, center.y, center.z, 26, radius * 0.32, radius * 0.26, radius * 0.32, 0.06);
      level.sendParticles(ParticleTypes.DRIPPING_LAVA, center.x, center.y + 0.15, center.z, 18, radius * 0.24, radius * 0.18, radius * 0.24, 0.02);
      spawnRing(level, center, X_AXIS, Z_AXIS, radius * 1.28, ParticleTypes.DRAGON_BREATH, 54, 0.006);
      spawnRing(level, center.add(0.0, 0.18, 0.0), X_AXIS, UP, radius, ParticleTypes.CRIMSON_SPORE, 42, 0.008);
      spawnRing(level, center.add(0.0, 0.18, 0.0), Z_AXIS, UP, radius, ParticleTypes.ELECTRIC_SPARK, 34, 0.01);

      for (int i = 0; i < 8; i++) {
         Vec3 dir = randomUnit(random);
         spawnLine(
            level,
            center,
            center.add(dir.scale(radius * (1.0 + random.nextDouble() * 0.85))),
            i % 2 == 0 ? ParticleTypes.DRAGON_BREATH : ParticleTypes.ELECTRIC_SPARK,
            7,
            0.018
         );
      }
   }

   public static void spawnBloodPrisonDash(ServerLevel level, Vec3 start, Vec3 end, RandomSource random) {
      Vec3 direction = safeNormalize(end.subtract(start), Z_AXIS);
      Vec3 right = safeNormalize(direction.cross(UP), X_AXIS);
      spawnLine(level, start, end, ParticleTypes.DRAGON_BREATH, 18, 0.025);
      spawnLine(level, start.add(right.scale(0.62)), end.add(right.scale(-0.4)), ParticleTypes.CRIMSON_SPORE, 14, 0.02);
      spawnLine(level, start.add(right.scale(-0.62)), end.add(right.scale(0.4)), ParticleTypes.ELECTRIC_SPARK, 10, 0.014);

      for (int i = 0; i < 3; i++) {
         Vec3 mid = start.lerp(end, 0.25 + random.nextDouble() * 0.55);
         Vec3 slash = right.scale(0.6 + random.nextDouble() * 0.75).add(0.0, random.nextDouble() * 0.7 - 0.25, 0.0);
         spawnLine(level, mid.subtract(slash), mid.add(slash), ParticleTypes.DAMAGE_INDICATOR, 5, 0.012);
      }

      level.sendParticles(ParticleTypes.CRIMSON_SPORE, end.x, end.y, end.z, 34, 0.45, 0.34, 0.45, 0.08);
   }

   public static void spawnBloodPrisonDomainPulse(ServerLevel level, Vec3 center, double radius) {
      Vec3 foot = center.add(0.0, 0.08, 0.0);
      spawnRing(level, foot, X_AXIS, Z_AXIS, radius, ParticleTypes.DRAGON_BREATH, 64, 0.004);
      spawnRing(level, foot.add(0.0, 0.04, 0.0), X_AXIS, Z_AXIS, radius * 0.72, ParticleTypes.CRIMSON_SPORE, 42, 0.006);
      spawnRing(level, foot.add(0.0, 0.08, 0.0), X_AXIS, Z_AXIS, radius * 0.38, ParticleTypes.DRIPPING_LAVA, 22, 0.002);

      for (int i = 0; i < 4; i++) {
         double angle = (Math.PI * 2) * i / 4.0 + (Math.PI / 4);
         Vec3 edge = foot.add(Math.cos(angle) * radius, 0.08, Math.sin(angle) * radius);
         spawnLine(level, foot, edge, i % 2 == 0 ? ParticleTypes.CRIMSON_SPORE : ParticleTypes.ELECTRIC_SPARK, 12, 0.008);
      }
   }

   private static void spawnRing(ServerLevel level, Vec3 center, Vec3 axisA, Vec3 axisB, double radius, ParticleOptions particle, int points, double jitter) {
      Vec3 a = safeNormalize(axisA, X_AXIS);
      Vec3 b = safeNormalize(axisB, Z_AXIS);

      for (int i = 0; i < points; i++) {
         double angle = (Math.PI * 2) * i / points;
         Vec3 pos = center.add(a.scale(Math.cos(angle) * radius)).add(b.scale(Math.sin(angle) * radius));
         level.sendParticles(particle, pos.x, pos.y, pos.z, 1, jitter, jitter, jitter, 0.0);
      }
   }

   private static void spawnLine(ServerLevel level, Vec3 start, Vec3 end, ParticleOptions particle, int points, double jitter) {
      for (int i = 0; i <= points; i++) {
         Vec3 pos = start.lerp(end, (double)i / points);
         level.sendParticles(particle, pos.x, pos.y, pos.z, 1, jitter, jitter, jitter, 0.0);
      }
   }

   private static Vec3 randomUnit(RandomSource random) {
      double yaw = random.nextDouble() * (Math.PI * 2);
      double pitch = (random.nextDouble() - 0.5) * Math.PI;
      double horizontal = Math.cos(pitch);
      return new Vec3(Math.cos(yaw) * horizontal, Math.sin(pitch), Math.sin(yaw) * horizontal).normalize();
   }

   private static Vec3 safeNormalize(Vec3 vector, Vec3 fallback) {
      return vector.lengthSqr() < 1.0E-6 ? fallback : vector.normalize();
   }
}
