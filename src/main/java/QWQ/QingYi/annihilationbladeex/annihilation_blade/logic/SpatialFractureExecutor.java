package QWQ.QingYi.annihilationbladeex.annihilation_blade.logic;

import QWQ.QingYi.annihilationbladeex.annihilation_blade.visual.AnnihilationVisuals;
import QWQ.QingYi.annihilationbladeex.common.SlashBladeTargeting;
import QWQ.QingYi.annihilationbladeex.config.ModConfig;
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
   private SpatialFractureExecutor() {
   }

   public static void unleash(LivingEntity entity) {
      if (entity instanceof Player player) {
         if (!entity.level().isClientSide) {
            ServerLevel level = (ServerLevel)entity.level();
            ModConfig.SpatialFracture config = ModConfig.COMMON.annihilationBlade.spatialFracture;
            double radius = config.fractureRadius.get();
            double visualScale = config.visualScale.get();
            Vec3 center = getFractureCenter(level, player, config);
            Set<LivingEntity> targets = gatherTargets(level, player, center, config);
            playOpeningRupture(level, player, center);
            AnnihilationVisuals.spawnOpeningHalo(level, center, radius * visualScale);
            spawnFractureField(level, player, center, config);
            spawnBladeStorm(level, player, center, config);
            int visualized = 0;

            for (LivingEntity target : targets) {
               if (visualized < config.maxVisualizedTargets.get()) {
                  spawnSlash(level, player, target);
               }

               TerminusLogic.execute(target, player);
               visualized++;
            }

            playDetonation(level, player, center, targets.size());
         }
      }
   }

   private static Vec3 getFractureCenter(ServerLevel level, Player player, ModConfig.SpatialFracture config) {
      double maxDistance = config.maxDistance.get();
      Vec3 eye = player.getEyePosition();
      Vec3 look = player.getLookAngle().normalize();
      Vec3 end = eye.add(look.scale(maxDistance));
      HitResult hit = level.clip(new ClipContext(eye, end, Block.COLLIDER, Fluid.NONE, player));
      Vec3 blockOrAir = hit.getType() == Type.MISS ? end : hit.getLocation();
      Vec3 aimedTarget = findAimedTargetCenter(level, player, eye, look, eye.distanceTo(blockOrAir), config.entityLockRadius.get());
      return aimedTarget == null ? blockOrAir : aimedTarget;
   }

   private static Vec3 findAimedTargetCenter(ServerLevel level, Player player, Vec3 eye, Vec3 look, double maxDistance, double entityLockRadius) {
      Vec3 end = eye.add(look.scale(maxDistance));
      AABB rayArea = new AABB(eye, end).inflate(entityLockRadius);
      Vec3 bestCenter = null;
      double bestProjection = Double.MAX_VALUE;

      for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, rayArea, entity -> canTarget(player, entity))) {
         Vec3 candidateCenter = candidate.position().add(0.0, candidate.getBbHeight() * 0.5, 0.0);
         double projection = candidateCenter.subtract(eye).dot(look);
         if (!(projection < 0.0) && !(projection > maxDistance)) {
            Vec3 nearestPoint = eye.add(look.scale(projection));
            double lockRadius = entityLockRadius + candidate.getBbWidth() * 0.5;
            if (candidateCenter.distanceToSqr(nearestPoint) <= lockRadius * lockRadius && projection < bestProjection) {
               bestProjection = projection;
               bestCenter = candidateCenter;
            }
         }
      }

      return bestCenter;
   }

   private static Set<LivingEntity> gatherTargets(ServerLevel level, Player player, Vec3 center, ModConfig.SpatialFracture config) {
      double radius = config.fractureRadius.get();
      int maxTargets = config.maxTargets.get();
      Vec3 eye = player.getEyePosition();
      Vec3 look = player.getLookAngle().normalize();
      Set<LivingEntity> targets = new LinkedHashSet<>();
      AABB fracture = new AABB(center, center).inflate(radius);

      for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, fracture, entity -> canTarget(player, entity))) {
         if (candidate.position().distanceToSqr(center) <= radius * radius) {
            targets.add(candidate);
            if (targets.size() >= maxTargets) {
               return targets;
            }
         }
      }

      double pathLength = Math.min(config.maxDistance.get(), eye.distanceTo(center));

      for (double distance = 2.0; distance <= pathLength && targets.size() < maxTargets; distance += config.rayStep.get()) {
         Vec3 sampleCenter = eye.add(look.scale(distance));
         AABB sample = new AABB(sampleCenter, sampleCenter).inflate(config.raySampleRadius.get());

         for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, sample, entity -> canTarget(player, entity))) {
            targets.add(candidate);
            if (targets.size() < maxTargets) {
               continue;
            }
         }
      }

      if (targets.isEmpty()) {
         AABB fallback = player.getBoundingBox().inflate(config.backupRadius.get());
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

   private static void spawnFractureField(ServerLevel level, Player player, Vec3 center, ModConfig.SpatialFracture config) {
      double radius = config.fractureRadius.get();
      double visualScale = config.visualScale.get();
      double visualRadius = radius * visualScale;
      level.sendParticles(ParticleTypes.FLASH, center.x, center.y + 1.0, center.z, 5, 0.2, 0.2, 0.2, 0.0);
      level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 1.0, center.z, visualCount(140, visualScale), visualRadius * 0.4, 4.0 * visualScale, visualRadius * 0.4, 0.25);
      level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 1.0, center.z, visualCount(200, visualScale), visualRadius * 0.6, 5.0 * visualScale, visualRadius * 0.6, 0.7);
      level.sendParticles(ParticleTypes.PORTAL, center.x, center.y + 1.0, center.z, visualCount(260, visualScale), visualRadius, 5.0 * visualScale, visualRadius, 1.0);
      level.sendParticles(ParticleTypes.DRAGON_BREATH, center.x, center.y + 1.0, center.z, visualCount(110, visualScale), visualRadius * 0.35, 3.0 * visualScale, visualRadius * 0.35, 0.15);
      spawnRing(level, center.add(0.0, 0.25, 0.0), visualRadius, ParticleTypes.ELECTRIC_SPARK, visualCount(96, visualScale));
      spawnRing(level, center.add(0.0, 2.4, 0.0), visualRadius * 0.7, ParticleTypes.END_ROD, visualCount(72, visualScale));
      spawnRing(level, center.add(0.0, 4.2, 0.0), visualRadius * 0.42, ParticleTypes.REVERSE_PORTAL, visualCount(54, visualScale));
      spawnVerticalRing(level, center.add(0.0, 1.5, 0.0), visualRadius * 0.9, ParticleTypes.END_ROD, visualCount(72, visualScale), true);
      spawnVerticalRing(level, center.add(0.0, 1.5, 0.0), visualRadius * 0.9, ParticleTypes.ELECTRIC_SPARK, visualCount(72, visualScale), false);
      RandomSource random = player.getRandom();
      AnnihilationVisuals.spawnWorldRiftBloom(level, center.add(0.0, 1.0, 0.0), visualRadius * 0.58);
      AnnihilationVisuals.spawnFractureWeb(level, center, visualRadius, random);

      for (int i = 0; i < visualCount(18, visualScale); i++) {
         Vec3 end = center.add(randomUnit(random).scale(6.0 * visualScale + random.nextDouble() * visualRadius));
         spawnParticleLine(level, center, end, ParticleTypes.REVERSE_PORTAL, visualCount(12, visualScale), 0.05);
      }
   }

   private static void spawnBladeStorm(ServerLevel level, Player player, Vec3 center, ModConfig.SpatialFracture config) {
      RandomSource random = player.getRandom();
      double radius = config.fractureRadius.get();
      double visualScale = config.visualScale.get();

      int centerSlashes = config.centerSlashes.get();
      for (int i = 0; i < centerSlashes; i++) {
         AttackManager.doSlash(player, 360.0F * i / Math.max(1, centerSlashes), Vec3.ZERO, true, true, 9999.0);
      }

      for (int i = 0; i < config.fractureSlashes.get(); i++) {
         Vec3 direction = randomUnit(random);
         Vec3 offset = randomUnit(random).scale(random.nextDouble() * radius * 0.65 * visualScale);
         double length = (12.0 + random.nextDouble() * 32.0) * visualScale;
         Vec3 middle = center.add(offset).add(0.0, (1.0 + random.nextDouble() * 5.0) * visualScale, 0.0);
         Vec3 start = middle.add(direction.scale(-length * 0.5));
         Vec3 end = middle.add(direction.scale(length * 0.5));
         spawnParticleLine(level, start, end, ParticleTypes.END_ROD, visualCount(18, visualScale), 0.02);
         spawnParticleLine(level, start, end, ParticleTypes.ELECTRIC_SPARK, visualCount(10, visualScale), 0.04);
         if (i % 4 == 0) {
            AnnihilationVisuals.spawnSlashBridge(level, start, end, 1.2 + random.nextDouble() * 0.8, random);
         }

         if (i % 3 == 0) {
            spawnParticleLine(level, start, end, ParticleTypes.SWEEP_ATTACK, visualCount(6, visualScale), 0.0);
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
      if (points <= 0) {
         return;
      }

      for (int i = 0; i < points; i++) {
         double angle = (Math.PI * 2) * i / points;
         double x = center.x + Math.cos(angle) * radius;
         double z = center.z + Math.sin(angle) * radius;
         level.sendParticles(particle, x, center.y, z, 1, 0.02, 0.02, 0.02, 0.0);
      }
   }

   private static void spawnVerticalRing(ServerLevel level, Vec3 center, double radius, ParticleOptions particle, int points, boolean alongX) {
      if (points <= 0) {
         return;
      }

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
      if (points <= 0) {
         return;
      }

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

   private static int visualCount(int base, double visualScale) {
      return Math.max(1, (int)Math.round(base * visualScale));
   }
}
