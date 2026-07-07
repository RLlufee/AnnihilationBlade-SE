package com.qingyi.annihilationbladeex.specialeffect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.qingyi.annihilationbladeex.AnnihilationBladeEX;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

final class SpecialEffectSupport {
    private static final Vec3 UP = new Vec3(0.0D, 1.0D, 0.0D);
    private static final Vec3 FORWARD = new Vec3(0.0D, 0.0D, 1.0D);
    private static final Vec3 RIGHT = new Vec3(1.0D, 0.0D, 0.0D);

    private SpecialEffectSupport() {
    }

    static boolean tryStartCooldown(Map<UUID, Long> cooldowns, Player player, long gameTime, int cooldownTicks) {
        long last = cooldowns.getOrDefault(player.getUUID(), -cooldownTicks * 2L);
        if (gameTime - last < cooldownTicks) {
            return false;
        }
        cooldowns.put(player.getUUID(), gameTime);
        return true;
    }

    static boolean canTarget(Player player, LivingEntity candidate) {
        if (candidate == player || !candidate.isAlive() || candidate.isAlliedTo(player)) {
            return false;
        }
        if (candidate instanceof Player other) {
            if (other.isCreative() || other.isSpectator()) {
                return false;
            }
            return !AnnihilationBladeEX.hasGodBlade(other);
        }
        return true;
    }

    static Vec3 centerOf(LivingEntity entity) {
        return entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
    }

    static Vec3 rightOf(Vec3 direction) {
        Vec3 right = safeNormalize(direction, FORWARD).cross(UP);
        return safeNormalize(right, RIGHT);
    }

    static List<LivingEntity> radialTargets(ServerLevel level, Player player, Vec3 center, double radius) {
        return radialTargets(level, player, center, radius, entity -> true);
    }

    static List<LivingEntity> radialTargets(ServerLevel level, Player player, Vec3 center, double radius, Predicate<LivingEntity> extra) {
        double radiusSqr = radius * radius;
        AABB area = new AABB(center, center).inflate(radius);
        List<LivingEntity> targets = level.getEntitiesOfClass(
                LivingEntity.class,
                area,
                entity -> canTarget(player, entity) && extra.test(entity) && centerOf(entity).distanceToSqr(center) <= radiusSqr
        );
        targets.sort(Comparator.comparingDouble(entity -> centerOf(entity).distanceToSqr(center)));
        return targets;
    }

    static List<LivingEntity> nearestChain(ServerLevel level, Player player, LivingEntity firstTarget, double radius, int maxTargets) {
        List<LivingEntity> chain = new ArrayList<>();
        Set<UUID> used = new HashSet<>();
        LivingEntity current = firstTarget;

        while (current != null && chain.size() < maxTargets) {
            chain.add(current);
            used.add(current.getUUID());

            Vec3 currentCenter = centerOf(current);
            List<LivingEntity> candidates = radialTargets(level, player, currentCenter, radius, entity -> !used.contains(entity.getUUID()));
            current = candidates.isEmpty() ? null : candidates.get(0);
        }

        return chain;
    }

    static List<LivingEntity> beamTargets(ServerLevel level, Player player, Vec3 start, Vec3 direction, double range, double width, int maxTargets) {
        Vec3 forward = safeNormalize(direction, FORWARD);
        Vec3 end = start.add(forward.scale(range));
        AABB area = new AABB(start, end).inflate(width, width * 0.8D, width);
        List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, area, entity -> canTarget(player, entity));
        candidates.sort(Comparator.comparingDouble(entity -> centerOf(entity).subtract(start).dot(forward)));

        List<LivingEntity> result = new ArrayList<>();
        for (LivingEntity target : candidates) {
            Vec3 targetCenter = centerOf(target);
            double projection = targetCenter.subtract(start).dot(forward);
            if (projection < 0.0D || projection > range) {
                continue;
            }

            Vec3 nearest = start.add(forward.scale(projection));
            double allowedWidth = width + projection / range * 4.0D;
            if (targetCenter.distanceToSqr(nearest) > allowedWidth * allowedWidth) {
                continue;
            }

            result.add(target);
            if (result.size() >= maxTargets) {
                break;
            }
        }
        return result;
    }

    static void pullToward(LivingEntity target, Vec3 point, double strength) {
        Vec3 center = centerOf(target);
        Vec3 delta = point.subtract(center);
        if (delta.lengthSqr() < 1.0E-4D) {
            return;
        }
        Vec3 pull = delta.normalize().scale(strength);
        target.push(pull.x, Math.max(0.04D, pull.y * 0.25D + 0.04D), pull.z);
        target.hasImpulse = true;
    }

    private static Vec3 safeNormalize(Vec3 vector, Vec3 fallback) {
        return vector.lengthSqr() < 1.0E-6D ? fallback : vector.normalize();
    }
}
