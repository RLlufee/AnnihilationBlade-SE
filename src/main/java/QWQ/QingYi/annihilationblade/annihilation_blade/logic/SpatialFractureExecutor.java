package QWQ.QingYi.annihilationblade.annihilation_blade.logic;

import QWQ.QingYi.annihilationblade.annihilation_blade.visual.AnnihilationVisuals;
import QWQ.QingYi.annihilationblade.common.SlashBladeTargeting;
import java.util.LinkedHashSet;
import java.util.Set;
import mods.flammpfeil.slashblade.util.AttackManager;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class SpatialFractureExecutor {
   private static final double MAX_DISTANCE = 160.0;
   private static final double FRACTURE_RADIUS = 20.0;
   private static final double RAY_STEP = 4.0;
   private static final double RAY_SAMPLE_RADIUS = 5.0;
   private static final double ENTITY_LOCK_RADIUS = 3.0;
   private static final double BACKUP_RADIUS = 48.0;
   private static final int MAX_TARGETS = 128;
   private static final int MAX_VISUALIZED_TARGETS = 32;
   private static final int FRACTURE_SLASHES = 24;
   private static final int CENTER_SLASHES = 12;

   private SpatialFractureExecutor() {
   }

   public static void unleash(LivingEntity entity) {
      if (entity instanceof Player player) {
         if (!entity.level().isClientSide) {
            ServerLevel level = (ServerLevel)entity.level();
            Vec3 center = getFractureCenter(level, player);
            Set<LivingEntity> targets = gatherTargets(level, player, center);
            playOpeningRupture(level, player, center);
            AnnihilationVisuals.spawnOpeningHalo(level, center, 20.0);
            spawnFractureField(level, player, center);
            spawnBladeStorm(level, player, center);
            int visualized = 0;

            for (LivingEntity target : targets) {
               if (visualized < 32) {
                  spawnSlash(level, player, target);
               }

               TerminusLogic.execute(target, player);
               visualized++;
            }

            playDetonation(level, player, center, targets.size());
         }
      }
   }

   private static Vec3 getFractureCenter(ServerLevel level, Player player) {
      Vec3 eye = player.getEyePosition();
      Vec3 look = player.getLookAngle().normalize();
      Vec3 end = eye.add(look.scale(160.0));
      HitResult hit = level.clip(new ClipContext(eye, end, Block.COLLIDER, Fluid.NONE, player));
      Vec3 blockOrAir = hit.getType() == Type.MISS ? end : hit.getLocation();
      Vec3 aimedTarget = findAimedTargetCenter(level, player, eye, look, eye.distanceTo(blockOrAir));
      return aimedTarget == null ? blockOrAir : aimedTarget;
   }

   private static Vec3 findAimedTargetCenter(ServerLevel level, Player player, Vec3 eye, Vec3 look, double maxDistance) {
      Vec3 end = eye.add(look.scale(maxDistance));
      AABB rayArea = new AABB(eye, end).inflate(3.0);
      Vec3 bestCenter = null;
      double bestProjection = Double.MAX_VALUE;

      for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, rayArea, entity -> canTarget(player, entity))) {
         Vec3 candidateCenter = candidate.position().add(0.0, candidate.getBbHeight() * 0.5, 0.0);
         double projection = candidateCenter.subtract(eye).dot(look);
         if (!(projection < 0.0) && !(projection > maxDistance)) {
            Vec3 nearestPoint = eye.add(look.scale(projection));
            double lockRadius = 3.0 + candidate.getBbWidth() * 0.5;
            if (candidateCenter.distanceToSqr(nearestPoint) <= lockRadius * lockRadius && projection < bestProjection) {
               bestProjection = projection;
               bestCenter = candidateCenter;
            }
         }
      }

      return bestCenter;
   }

   private static Set<LivingEntity> gatherTargets(ServerLevel level, Player player, Vec3 center) {
      Vec3 eye = player.getEyePosition();
      Vec3 look = player.getLookAngle().normalize();
      Set<LivingEntity> targets = new LinkedHashSet<>();
      AABB fracture = new AABB(center, center).inflate(20.0);

      for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, fracture, entity -> canTarget(player, entity))) {
         if (candidate.position().distanceToSqr(center) <= 400.0) {
            targets.add(candidate);
            if (targets.size() >= 128) {
               return targets;
            }
         }
      }

      double pathLength = Math.min(160.0, eye.distanceTo(center));

      for (double distance = 2.0; distance <= pathLength && targets.size() < 128; distance += 4.0) {
         Vec3 sampleCenter = eye.add(look.scale(distance));
         AABB sample = new AABB(sampleCenter, sampleCenter).inflate(5.0);

         for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, sample, entity -> canTarget(player, entity))) {
            targets.add(candidate);
            if (targets.size() < 128) {
               continue;
            }
         }
      }

      if (targets.isEmpty()) {
         AABB fallback = player.getBoundingBox().inflate(48.0);
         targets.addAll(level.getEntitiesOfClass(LivingEntity.class, fallback, entity -> canTarget(player, entity)));
      }

      return targets;
   }

   private static boolean canTarget(Player player, LivingEntity candidate) {
      return SlashBladeTargeting.canAttack(player, candidate);
   }

   private static void playOpeningRupture(ServerLevel level, Player player, Vec3 center) {
      level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 2.5F, 0.35F);
      level.playSound(null, center.x, center.y, center.z, SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 4.0F, 1.4F);
      level.playSound(null, center.x, center.y, center.z, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 2.0F, 0.55F);
   }

   private static void spawnFractureField(ServerLevel level, Player player, Vec3 center) {
      level.sendParticles(ParticleTypes.FLASH, center.x, center.y + 1.0, center.z, 5, 0.2, 0.2, 0.2, 0.0);
      level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 1.0, center.z, 140, 8.0, 4.0, 8.0, 0.25);
      level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 1.0, center.z, 200, 12.0, 5.0, 12.0, 0.7);
      level.sendParticles(ParticleTypes.PORTAL, center.x, center.y + 1.0, center.z, 260, 20.0, 5.0, 20.0, 1.0);
      level.sendParticles(ParticleTypes.DRAGON_BREATH, center.x, center.y + 1.0, center.z, 110, 7.0, 3.0, 7.0, 0.15);
      spawnRing(level, center.add(0.0, 0.25, 0.0), 20.0, ParticleTypes.ELECTRIC_SPARK, 96);
      spawnRing(level, center.add(0.0, 2.4, 0.0), 14.0, ParticleTypes.END_ROD, 72);
      spawnRing(level, center.add(0.0, 4.2, 0.0), 8.4, ParticleTypes.REVERSE_PORTAL, 54);
      spawnVerticalRing(level, center.add(0.0, 1.5, 0.0), 18.0, ParticleTypes.END_ROD, 72, true);
      spawnVerticalRing(level, center.add(0.0, 1.5, 0.0), 18.0, ParticleTypes.ELECTRIC_SPARK, 72, false);
      RandomSource random = player.getRandom();
      AnnihilationVisuals.spawnWorldRiftBloom(level, center.add(0.0, 1.0, 0.0), 11.6);
      AnnihilationVisuals.spawnFractureWeb(level, center, 20.0, random);

      for (int i = 0; i < 18; i++) {
         Vec3 end = center.add(randomUnit(random).scale(6.0 + random.nextDouble() * 20.0));
         spawnParticleLine(level, center, end, ParticleTypes.REVERSE_PORTAL, 12, 0.05);
      }
   }

   private static void spawnBladeStorm(ServerLevel level, Player player, Vec3 center) {
      RandomSource random = player.getRandom();

      for (int i = 0; i < 12; i++) {
         AttackManager.doSlash(player, 30.0F * i, Vec3.ZERO, true, true, 9999.0);
      }

      for (int i = 0; i < 24; i++) {
         Vec3 direction = randomUnit(random);
         Vec3 offset = randomUnit(random).scale(random.nextDouble() * 20.0 * 0.65);
         double length = 12.0 + random.nextDouble() * 32.0;
         Vec3 middle = center.add(offset).add(0.0, 1.0 + random.nextDouble() * 5.0, 0.0);
         Vec3 start = middle.add(direction.scale(-length * 0.5));
         Vec3 end = middle.add(direction.scale(length * 0.5));
         spawnParticleLine(level, start, end, ParticleTypes.END_ROD, 18, 0.02);
         spawnParticleLine(level, start, end, ParticleTypes.ELECTRIC_SPARK, 10, 0.04);
         if (i % 4 == 0) {
            AnnihilationVisuals.spawnSlashBridge(level, start, end, 1.2 + random.nextDouble() * 0.8, random);
         }

         if (i % 3 == 0) {
            spawnParticleLine(level, start, end, ParticleTypes.SWEEP_ATTACK, 6, 0.0);
         }
      }
   }

   private static void spawnSlash(ServerLevel level, Player player, LivingEntity target) {
      double y = target.getY() + target.getBbHeight() * 0.5;
      Vec3 center = new Vec3(target.getX(), y, target.getZ());
      level.sendParticles(ParticleTypes.FLASH, target.getX(), y, target.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
      level.sendParticles(ParticleTypes.END_ROD, target.getX(), y, target.getZ(), 35, 0.8, 0.8, 0.8, 0.08);
      level.sendParticles(ParticleTypes.PORTAL, target.getX(), target.getY() + 1.0, target.getZ(), 60, 1.2, 1.2, 1.2, 0.45);
      level.sendParticles(ParticleTypes.REVERSE_PORTAL, target.getX(), target.getY() + 1.0, target.getZ(), 45, 0.9, 0.9, 0.9, 0.35);
      spawnParticleLine(level, center.add(-2.5, 1.8, -2.5), center.add(2.5, -1.4, 2.5), ParticleTypes.END_ROD, 18, 0.0);
      spawnParticleLine(level, center.add(2.5, 1.8, -2.5), center.add(-2.5, -1.4, 2.5), ParticleTypes.ELECTRIC_SPARK, 18, 0.0);
      AnnihilationVisuals.spawnExecutionBurst(level, target, player.getRandom());
      AttackManager.doSlash(player, player.getRandom().nextInt(360), Vec3.ZERO, true, true, 9999.0);
   }

   private static void playDetonation(ServerLevel level, Player player, Vec3 center, int count) {
      float volume = Math.min(4.0F, 1.0F + count / 20.0F);
      level.playSound(null, center.x, center.y, center.z, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, volume, 0.35F);
      level.playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, volume, 0.65F);
      level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, Math.min(2.5F, volume), 1.7F);
      level.sendParticles(ParticleTypes.SONIC_BOOM, center.x, center.y + 1.0, center.z, 1, 0.0, 0.0, 0.0, 0.0);
      level.sendParticles(ParticleTypes.DRAGON_BREATH, center.x, center.y + 1.0, center.z, 180, 5.0, 2.0, 5.0, 0.25);
      AnnihilationVisuals.spawnCollapsePulse(level, center, 20.0, count);
   }

   private static void spawnRing(ServerLevel level, Vec3 center, double radius, ParticleOptions particle, int points) {
      for (int i = 0; i < points; i++) {
         double angle = (Math.PI * 2) * i / points;
         double x = center.x + Math.cos(angle) * radius;
         double z = center.z + Math.sin(angle) * radius;
         level.sendParticles(particle, x, center.y, z, 1, 0.02, 0.02, 0.02, 0.0);
      }
   }

   private static void spawnVerticalRing(ServerLevel level, Vec3 center, double radius, ParticleOptions particle, int points, boolean alongX) {
      for (int i = 0; i < points; i++) {
         double angle = (Math.PI * 2) * i / points;
         double horizontal = Math.cos(angle) * radius;
         double y = center.y + Math.sin(angle) * radius * 0.65;
         double x = center.x + (alongX ? horizontal : 0.0);
         double z = center.z + (alongX ? 0.0 : horizontal);
         level.sendParticles(particle, x, y, z, 1, 0.02, 0.02, 0.02, 0.0);
      }
   }

   private static void spawnParticleLine(ServerLevel level, Vec3 start, Vec3 end, ParticleOptions particle, int points, double jitter) {
      for (int i = 0; i <= points; i++) {
         double progress = (double)i / points;
         Vec3 pos = start.lerp(end, progress);
         level.sendParticles(particle, pos.x, pos.y, pos.z, 1, jitter, jitter, jitter, 0.0);
      }
   }

   private static Vec3 randomUnit(RandomSource random) {
      double yaw = random.nextDouble() * Math.PI * 2.0;
      double pitch = (random.nextDouble() - 0.5) * Math.PI;
      double horizontal = Math.cos(pitch);
      return new Vec3(Math.cos(yaw) * horizontal, Math.sin(pitch), Math.sin(yaw) * horizontal).normalize();
   }
}
