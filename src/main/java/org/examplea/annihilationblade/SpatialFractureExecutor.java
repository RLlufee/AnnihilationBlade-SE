package org.examplea.annihilationblade;

import mods.flammpfeil.slashblade.util.AttackManager;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Executes the Spatial Fracture slash arts by tearing open a large fracture field at the player's gaze.
 */
public final class SpatialFractureExecutor {
    private static final double MAX_DISTANCE = 160.0D;
    private static final double FRACTURE_RADIUS = 20.0D;
    private static final double RAY_STEP = 4.0D;
    private static final double RAY_SAMPLE_RADIUS = 5.0D;
    private static final double ENTITY_LOCK_RADIUS = 3.0D;
    private static final double BACKUP_RADIUS = 48.0D;
    private static final int MAX_TARGETS = 512;
    private static final int FRACTURE_SLASHES = 36;
    private static final int CENTER_SLASHES = 18;

    private SpatialFractureExecutor() {
    }

    @SuppressWarnings("resource")
    public static void unleash(LivingEntity entity) {
        if (!(entity instanceof Player player)) return;
        if (entity.level().isClientSide) return;

        ServerLevel level = (ServerLevel) entity.level();
        Vec3 center = getFractureCenter(level, player);
        Set<LivingEntity> targets = gatherTargets(level, player, center);

        playOpeningRupture(level, player, center);
        AnnihilationVisuals.spawnOpeningHalo(level, center, FRACTURE_RADIUS);
        spawnFractureField(level, player, center);
        spawnBladeStorm(level, player, center);
        targets.forEach(target -> {
            spawnSlash(level, player, target);
            TerminusLogic.execute(target, player);
        });

        playDetonation(level, player, center, targets.size());
    }

    private static Vec3 getFractureCenter(ServerLevel level, Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        Vec3 end = eye.add(look.scale(MAX_DISTANCE));
        HitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 blockOrAir = hit.getType() == HitResult.Type.MISS ? end : hit.getLocation();
        Vec3 aimedTarget = findAimedTargetCenter(level, player, eye, look, eye.distanceTo(blockOrAir));
        return aimedTarget == null ? blockOrAir : aimedTarget;
    }

    private static Vec3 findAimedTargetCenter(ServerLevel level, Player player, Vec3 eye, Vec3 look, double maxDistance) {
        Vec3 end = eye.add(look.scale(maxDistance));
        AABB rayArea = new AABB(eye, end).inflate(ENTITY_LOCK_RADIUS);
        Vec3 bestCenter = null;
        double bestProjection = Double.MAX_VALUE;

        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, rayArea, entity -> canTarget(player, entity))) {
            Vec3 candidateCenter = candidate.position().add(0.0D, candidate.getBbHeight() * 0.5D, 0.0D);
            double projection = candidateCenter.subtract(eye).dot(look);
            if (projection < 0.0D || projection > maxDistance) continue;

            Vec3 nearestPoint = eye.add(look.scale(projection));
            double lockRadius = ENTITY_LOCK_RADIUS + candidate.getBbWidth() * 0.5D;
            if (candidateCenter.distanceToSqr(nearestPoint) <= lockRadius * lockRadius && projection < bestProjection) {
                bestProjection = projection;
                bestCenter = candidateCenter;
            }
        }

        return bestCenter;
    }

    private static Set<LivingEntity> gatherTargets(ServerLevel level, Player player, Vec3 center) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        Set<LivingEntity> targets = new LinkedHashSet<>();

        AABB fracture = new AABB(center, center).inflate(FRACTURE_RADIUS);
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, fracture, entity -> canTarget(player, entity))) {
            if (candidate.position().distanceToSqr(center) <= FRACTURE_RADIUS * FRACTURE_RADIUS) {
                targets.add(candidate);
                if (targets.size() >= MAX_TARGETS) return targets;
            }
        }

        double pathLength = Math.min(MAX_DISTANCE, eye.distanceTo(center));
        for (double distance = 2.0D; distance <= pathLength && targets.size() < MAX_TARGETS; distance += RAY_STEP) {
            Vec3 sampleCenter = eye.add(look.scale(distance));
            AABB sample = new AABB(sampleCenter, sampleCenter).inflate(RAY_SAMPLE_RADIUS);
            for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, sample, entity -> canTarget(player, entity))) {
                targets.add(candidate);
                if (targets.size() >= MAX_TARGETS) break;
            }
        }

        if (targets.isEmpty()) {
            AABB fallback = player.getBoundingBox().inflate(BACKUP_RADIUS);
            targets.addAll(level.getEntitiesOfClass(LivingEntity.class, fallback, entity -> canTarget(player, entity)));
        }

        return targets;
    }

    private static boolean canTarget(Player player, LivingEntity candidate) {
        if (candidate == player) return false;
        if (!candidate.isAlive()) return false;
        if (candidate.isAlliedTo(player)) return false;
        if (candidate instanceof Player other) {
            if (other.isCreative() || other.isSpectator()) return false;
            return !hasAnnihilationBlade(other);
        }
        return true;
    }

    private static boolean hasAnnihilationBlade(Player player) {
        if (isAnnihilationBlade(player.getMainHandItem())) return true;
        if (isAnnihilationBlade(player.getOffhandItem())) return true;
        for (ItemStack stack : player.getInventory().items) {
            if (isAnnihilationBlade(stack)) return true;
        }
        return false;
    }

    private static boolean isAnnihilationBlade(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof ItemAnnihilationBlade) return true;
        if (stack.getDescriptionId().equals("item.annihilationblade.annihilation_blade")) return true;
        return stack.hasTag() && stack.getTag().getBoolean("IsAnnihilationBlade");
    }

    private static void playOpeningRupture(ServerLevel level, Player player, Vec3 center) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 2.5F, 0.35F);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 4.0F, 1.4F);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 2.0F, 0.55F);
    }

    private static void spawnFractureField(ServerLevel level, Player player, Vec3 center) {
        level.sendParticles(ParticleTypes.FLASH, center.x, center.y + 1.0D, center.z, 5, 0.2D, 0.2D, 0.2D, 0.0D);
        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 1.0D, center.z, 220, 8.0D, 4.0D, 8.0D, 0.25D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 1.0D, center.z, 320, 12.0D, 5.0D, 12.0D, 0.7D);
        level.sendParticles(ParticleTypes.PORTAL, center.x, center.y + 1.0D, center.z, 420, FRACTURE_RADIUS, 5.0D, FRACTURE_RADIUS, 1.0D);
        level.sendParticles(ParticleTypes.DRAGON_BREATH, center.x, center.y + 1.0D, center.z, 180, 7.0D, 3.0D, 7.0D, 0.15D);

        spawnRing(level, center.add(0.0D, 0.25D, 0.0D), FRACTURE_RADIUS, ParticleTypes.ELECTRIC_SPARK, 128);
        spawnRing(level, center.add(0.0D, 2.4D, 0.0D), FRACTURE_RADIUS * 0.7D, ParticleTypes.END_ROD, 96);
        spawnRing(level, center.add(0.0D, 4.2D, 0.0D), FRACTURE_RADIUS * 0.42D, ParticleTypes.REVERSE_PORTAL, 72);
        spawnVerticalRing(level, center.add(0.0D, 1.5D, 0.0D), FRACTURE_RADIUS * 0.9D, ParticleTypes.END_ROD, 96, true);
        spawnVerticalRing(level, center.add(0.0D, 1.5D, 0.0D), FRACTURE_RADIUS * 0.9D, ParticleTypes.ELECTRIC_SPARK, 96, false);

        RandomSource random = player.getRandom();
        AnnihilationVisuals.spawnWorldRiftBloom(level, center.add(0.0D, 1.0D, 0.0D), FRACTURE_RADIUS * 0.58D);
        AnnihilationVisuals.spawnFractureWeb(level, center, FRACTURE_RADIUS, random);
        for (int i = 0; i < 28; i++) {
            Vec3 end = center.add(randomUnit(random).scale(6.0D + random.nextDouble() * FRACTURE_RADIUS));
            spawnParticleLine(level, center, end, ParticleTypes.REVERSE_PORTAL, 18, 0.05D);
        }
    }

    private static void spawnBladeStorm(ServerLevel level, Player player, Vec3 center) {
        RandomSource random = player.getRandom();
        for (int i = 0; i < CENTER_SLASHES; i++) {
            AttackManager.doSlash(player, (360.0F / CENTER_SLASHES) * i, Vec3.ZERO, true, true, 9999.0F);
        }

        for (int i = 0; i < FRACTURE_SLASHES; i++) {
            Vec3 direction = randomUnit(random);
            Vec3 offset = randomUnit(random).scale(random.nextDouble() * FRACTURE_RADIUS * 0.65D);
            double length = 12.0D + random.nextDouble() * 32.0D;
            Vec3 middle = center.add(offset).add(0.0D, 1.0D + random.nextDouble() * 5.0D, 0.0D);
            Vec3 start = middle.add(direction.scale(-length * 0.5D));
            Vec3 end = middle.add(direction.scale(length * 0.5D));
            spawnParticleLine(level, start, end, ParticleTypes.END_ROD, 24, 0.02D);
            spawnParticleLine(level, start, end, ParticleTypes.ELECTRIC_SPARK, 14, 0.04D);
            if (i % 4 == 0) {
                AnnihilationVisuals.spawnSlashBridge(level, start, end, 1.2D + random.nextDouble() * 0.8D, random);
            }
            if (i % 3 == 0) {
                spawnParticleLine(level, start, end, ParticleTypes.SWEEP_ATTACK, 6, 0.0D);
            }
        }
    }

    private static void spawnSlash(ServerLevel level, Player player, LivingEntity target) {
        double y = target.getY() + target.getBbHeight() * 0.5D;
        Vec3 center = new Vec3(target.getX(), y, target.getZ());
        level.sendParticles(ParticleTypes.FLASH, target.getX(), y, target.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.END_ROD, target.getX(), y, target.getZ(), 35, 0.8D, 0.8D, 0.8D, 0.08D);
        level.sendParticles(ParticleTypes.PORTAL, target.getX(), target.getY() + 1.0D, target.getZ(), 60, 1.2D, 1.2D, 1.2D, 0.45D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, target.getX(), target.getY() + 1.0D, target.getZ(), 45, 0.9D, 0.9D, 0.9D, 0.35D);
        spawnParticleLine(level, center.add(-2.5D, 1.8D, -2.5D), center.add(2.5D, -1.4D, 2.5D), ParticleTypes.END_ROD, 18, 0.0D);
        spawnParticleLine(level, center.add(2.5D, 1.8D, -2.5D), center.add(-2.5D, -1.4D, 2.5D), ParticleTypes.ELECTRIC_SPARK, 18, 0.0D);
        AnnihilationVisuals.spawnExecutionBurst(level, target, player.getRandom());
        AttackManager.doSlash(player, player.getRandom().nextInt(360), Vec3.ZERO, true, true, 9999.0F);
    }

    private static void playDetonation(ServerLevel level, Player player, Vec3 center, int count) {
        float volume = Math.min(4.0F, 1.0F + count / 20.0F);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, volume, 0.35F);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, volume, 0.65F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, Math.min(2.5F, volume), 1.7F);
        level.sendParticles(ParticleTypes.SONIC_BOOM, center.x, center.y + 1.0D, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.DRAGON_BREATH, center.x, center.y + 1.0D, center.z, 180, 5.0D, 2.0D, 5.0D, 0.25D);
        AnnihilationVisuals.spawnCollapsePulse(level, center, FRACTURE_RADIUS, count);
    }

    private static void spawnRing(ServerLevel level, Vec3 center, double radius, ParticleOptions particle, int points) {
        for (int i = 0; i < points; i++) {
            double angle = Math.PI * 2.0D * i / points;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            level.sendParticles(particle, x, center.y, z, 1, 0.02D, 0.02D, 0.02D, 0.0D);
        }
    }

    private static void spawnVerticalRing(ServerLevel level, Vec3 center, double radius, ParticleOptions particle, int points, boolean alongX) {
        for (int i = 0; i < points; i++) {
            double angle = Math.PI * 2.0D * i / points;
            double horizontal = Math.cos(angle) * radius;
            double y = center.y + Math.sin(angle) * radius * 0.65D;
            double x = center.x + (alongX ? horizontal : 0.0D);
            double z = center.z + (alongX ? 0.0D : horizontal);
            level.sendParticles(particle, x, y, z, 1, 0.02D, 0.02D, 0.02D, 0.0D);
        }
    }

    private static void spawnParticleLine(ServerLevel level, Vec3 start, Vec3 end, ParticleOptions particle, int points, double jitter) {
        for (int i = 0; i <= points; i++) {
            double progress = i / (double) points;
            Vec3 pos = start.lerp(end, progress);
            level.sendParticles(particle, pos.x, pos.y, pos.z, 1, jitter, jitter, jitter, 0.0D);
        }
    }

    private static Vec3 randomUnit(RandomSource random) {
        double yaw = random.nextDouble() * Math.PI * 2.0D;
        double pitch = (random.nextDouble() - 0.5D) * Math.PI;
        double horizontal = Math.cos(pitch);
        return new Vec3(Math.cos(yaw) * horizontal, Math.sin(pitch), Math.sin(yaw) * horizontal).normalize();
    }
}
