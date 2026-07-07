package org.examplea.annihilationblade.visual;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class AnnihilationVisuals {
    private static final double TAU = Math.PI * 2.0D;
    private static final Vec3 UP = new Vec3(0.0D, 1.0D, 0.0D);
    private static final Vec3 X_AXIS = new Vec3(1.0D, 0.0D, 0.0D);
    private static final Vec3 Z_AXIS = new Vec3(0.0D, 0.0D, 1.0D);

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
        Vec3 hilt = eye.add(look.scale(0.55D)).add(right.scale(0.34D)).add(0.0D, -0.52D, 0.0D);
        Vec3 tip = hilt.add(look.scale(1.42D)).add(0.0D, -0.42D, 0.0D);

        spawnLine(level, hilt, tip, COSMIC_CYAN, 7, 0.012D);
        spawnLine(level, hilt.add(right.scale(-0.08D)), tip.add(right.scale(0.08D)), VOID_PURPLE, 6, 0.018D);

        if (tick % 8 == 0) {
            Vec3 center = wielder.position().add(0.0D, wielder.getBbHeight() * 0.52D, 0.0D);
            double radius = Math.max(0.75D, wielder.getBbWidth() * 1.65D);
            spawnRing(level, center, X_AXIS, Z_AXIS, radius, VOID_PURPLE, 26, 0.008D);
            spawnRing(level, center.add(0.0D, 0.18D, 0.0D), X_AXIS, Z_AXIS, radius * 0.62D, COSMIC_CYAN, 18, 0.008D);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y, center.z, 8, 0.35D, 0.45D, 0.35D, 0.08D);
            level.sendParticles(ParticleTypes.END_ROD, tip.x, tip.y, tip.z, 4, 0.05D, 0.05D, 0.05D, 0.01D);
        }
    }

    public static void spawnOpeningHalo(ServerLevel level, Vec3 center, double radius) {
        Vec3 c = center.add(0.0D, 1.0D, 0.0D);
        level.sendParticles(ParticleTypes.FLASH, c.x, c.y, c.z, 2, 0.12D, 0.12D, 0.12D, 0.0D);
        spawnRing(level, c, X_AXIS, Z_AXIS, radius, VOID_PURPLE, 168, 0.01D);
        spawnRing(level, c.add(0.0D, 1.2D, 0.0D), X_AXIS, Z_AXIS, radius * 0.64D, COSMIC_CYAN, 116, 0.01D);
        spawnRing(level, c.add(0.0D, 2.6D, 0.0D), X_AXIS, Z_AXIS, radius * 0.36D, TERMINUS_GOLD, 72, 0.008D);
        spawnRing(level, c, X_AXIS, UP, radius * 0.82D, COSMIC_CYAN, 116, 0.012D);
        spawnRing(level, c, Z_AXIS, UP, radius * 0.82D, VOID_PURPLE, 116, 0.012D);

        for (int i = 0; i < 12; i++) {
            double angle = TAU * i / 12.0D;
            Vec3 dir = new Vec3(Math.cos(angle), 0.0D, Math.sin(angle));
            Vec3 inner = c.add(dir.scale(radius * 0.18D));
            Vec3 outer = c.add(dir.scale(radius * 0.95D)).add(0.0D, (i % 3 - 1) * 0.55D, 0.0D);
            spawnLine(level, inner, outer, i % 2 == 0 ? TERMINUS_GOLD : COSMIC_CYAN, 18, 0.02D);
        }
    }

    public static void spawnFractureWeb(ServerLevel level, Vec3 center, double radius, RandomSource random) {
        Vec3 c = center.add(0.0D, 1.4D, 0.0D);
        for (int i = 0; i < 36; i++) {
            double angle = random.nextDouble() * TAU;
            double distance = radius * (0.28D + random.nextDouble() * 0.9D);
            Vec3 dir = new Vec3(Math.cos(angle), (random.nextDouble() - 0.5D) * 0.35D, Math.sin(angle)).normalize();
            Vec3 end = c.add(dir.scale(distance)).add(0.0D, random.nextDouble() * 3.8D - 1.3D, 0.0D);
            ParticleOptions particle = i % 3 == 0 ? COSMIC_CYAN : (i % 3 == 1 ? VOID_PURPLE : ParticleTypes.ELECTRIC_SPARK);
            spawnLine(level, c, end, particle, 13 + random.nextInt(8), 0.035D);

            if (i % 4 == 0) {
                Vec3 side = safeNormalize(dir.cross(UP), X_AXIS).scale((random.nextBoolean() ? 1.0D : -1.0D) * distance * 0.23D);
                spawnLine(level, end, end.add(side), TERMINUS_GOLD, 8, 0.025D);
            }
        }
    }

    public static void spawnCollapsePulse(ServerLevel level, Vec3 center, double radius, int intensity) {
        Vec3 c = center.add(0.0D, 1.0D, 0.0D);
        int sparks = Math.min(240, 80 + intensity * 3);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, c.x, c.y, c.z, sparks, radius * 0.55D, 2.6D, radius * 0.55D, 0.85D);
        level.sendParticles(ParticleTypes.WITCH, c.x, c.y, c.z, 60, radius * 0.35D, 1.4D, radius * 0.35D, 0.08D);
        spawnRing(level, c.add(0.0D, 0.25D, 0.0D), X_AXIS, Z_AXIS, radius * 0.45D, BLACK_CORE, 96, 0.01D);
        spawnRing(level, c.add(0.0D, 0.35D, 0.0D), X_AXIS, Z_AXIS, radius * 0.75D, VOID_PURPLE, 132, 0.012D);
        spawnRing(level, c.add(0.0D, 0.45D, 0.0D), X_AXIS, Z_AXIS, radius, COSMIC_CYAN, 168, 0.014D);
    }

    public static void spawnExecutionBurst(ServerLevel level, LivingEntity target, RandomSource random) {
        Vec3 center = target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D);
        double radius = Math.max(0.9D, target.getBbWidth() * 1.65D);
        spawnWhiteExecutionCircle(level, target, radius);
        level.sendParticles(ParticleTypes.FLASH, center.x, center.y, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y, center.z, 24, radius * 0.38D, radius * 0.55D, radius * 0.38D, 0.22D);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, center.x, center.y, center.z, 10, radius * 0.26D, radius * 0.36D, radius * 0.26D, 0.03D);

        spawnRing(level, center, X_AXIS, Z_AXIS, radius * 1.2D, VOID_PURPLE, 28, 0.006D);
        spawnRing(level, center, X_AXIS, UP, radius * 1.05D, COSMIC_CYAN, 24, 0.006D);
        spawnRing(level, center, Z_AXIS, UP, radius * 1.05D, TERMINUS_GOLD, 24, 0.006D);
        spawnLine(level, center.add(-radius, radius * 0.9D, -radius), center.add(radius, -radius * 0.9D, radius), COSMIC_CYAN, 10, 0.01D);
        spawnLine(level, center.add(radius, radius * 0.9D, -radius), center.add(-radius, -radius * 0.9D, radius), VOID_PURPLE, 10, 0.01D);

        for (int i = 0; i < 6; i++) {
            Vec3 dir = randomUnit(random);
            spawnLine(level, center, center.add(dir.scale(radius * (1.2D + random.nextDouble() * 0.9D))), ParticleTypes.ELECTRIC_SPARK, 5, 0.018D);
        }
    }

    private static void spawnWhiteExecutionCircle(ServerLevel level, LivingEntity target, double targetRadius) {
        Vec3 foot = target.position().add(0.0D, 0.06D, 0.0D);
        double radius = Math.max(1.15D, targetRadius * 1.55D);

        spawnRing(level, foot, X_AXIS, Z_AXIS, radius, ParticleTypes.END_ROD, 96, 0.002D);
        spawnRing(level, foot.add(0.0D, 0.03D, 0.0D), X_AXIS, Z_AXIS, radius * 0.72D, ParticleTypes.FIREWORK, 72, 0.002D);
        spawnRing(level, foot.add(0.0D, 0.06D, 0.0D), X_AXIS, Z_AXIS, radius * 0.42D, ParticleTypes.END_ROD, 48, 0.001D);

        for (int i = 0; i < 6; i++) {
            double a = TAU * i / 6.0D;
            double b = a + TAU / 3.0D;
            Vec3 start = foot.add(Math.cos(a) * radius * 0.88D, 0.08D, Math.sin(a) * radius * 0.88D);
            Vec3 end = foot.add(Math.cos(b) * radius * 0.88D, 0.08D, Math.sin(b) * radius * 0.88D);
            spawnLine(level, start, end, ParticleTypes.END_ROD, 18, 0.001D);
        }

        for (int i = 0; i < 12; i++) {
            double angle = TAU * i / 12.0D;
            Vec3 inner = foot.add(Math.cos(angle) * radius * 0.28D, 0.1D, Math.sin(angle) * radius * 0.28D);
            Vec3 outer = foot.add(Math.cos(angle) * radius, 0.1D, Math.sin(angle) * radius);
            spawnLine(level, inner, outer, i % 2 == 0 ? ParticleTypes.END_ROD : ParticleTypes.FIREWORK, 7, 0.001D);
        }

        level.sendParticles(ParticleTypes.END_ROD, foot.x, foot.y + 0.16D, foot.z, 28, radius * 0.34D, 0.02D, radius * 0.34D, 0.018D);
        level.sendParticles(ParticleTypes.FLASH, foot.x, foot.y + 0.18D, foot.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    public static void spawnBlinkGate(ServerLevel level, Vec3 center, double radius) {
        level.sendParticles(ParticleTypes.FLASH, center.x, center.y, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.PORTAL, center.x, center.y, center.z, 42, radius * 0.45D, radius * 0.28D, radius * 0.45D, 0.35D);
        spawnRing(level, center, X_AXIS, Z_AXIS, radius, VOID_PURPLE, 48, 0.008D);
        spawnRing(level, center, X_AXIS, UP, radius * 0.72D, COSMIC_CYAN, 38, 0.008D);
        spawnRing(level, center, Z_AXIS, UP, radius * 0.72D, TERMINUS_GOLD, 38, 0.008D);
    }

    public static void spawnBlinkTrail(ServerLevel level, Vec3 start, Vec3 end, RandomSource random) {
        spawnLine(level, start, end, VOID_PURPLE, 24, 0.035D);
        spawnLine(level, start, end, COSMIC_CYAN, 18, 0.02D);

        Vec3 direction = safeNormalize(end.subtract(start), Z_AXIS);
        Vec3 right = safeNormalize(direction.cross(UP), X_AXIS);
        for (int i = 0; i < 5; i++) {
            double progress = (i + 1.0D) / 6.0D;
            Vec3 center = start.lerp(end, progress);
            Vec3 slash = right.scale(0.65D + random.nextDouble() * 0.7D).add(0.0D, random.nextDouble() * 0.8D - 0.4D, 0.0D);
            spawnLine(level, center.subtract(slash), center.add(slash), i % 2 == 0 ? ParticleTypes.ELECTRIC_SPARK : TERMINUS_GOLD, 7, 0.012D);
        }
    }

    public static void spawnWorldRiftBloom(ServerLevel level, Vec3 center, double radius) {
        level.sendParticles(ParticleTypes.WITCH, center.x, center.y, center.z, 46, radius * 0.35D, 1.2D, radius * 0.35D, 0.05D);
        spawnRing(level, center, X_AXIS, Z_AXIS, radius, BLACK_CORE, 96, 0.01D);
        spawnRing(level, center.add(0.0D, 0.8D, 0.0D), X_AXIS, Z_AXIS, radius * 0.72D, VOID_PURPLE, 84, 0.012D);
        spawnRing(level, center.add(0.0D, 1.6D, 0.0D), X_AXIS, Z_AXIS, radius * 0.46D, COSMIC_CYAN, 64, 0.012D);
        spawnRing(level, center.add(0.0D, 0.5D, 0.0D), X_AXIS, UP, radius * 0.82D, TERMINUS_GOLD, 72, 0.012D);
        spawnRing(level, center.add(0.0D, 0.5D, 0.0D), Z_AXIS, UP, radius * 0.82D, VOID_PURPLE, 72, 0.012D);

        for (int i = 0; i < 10; i++) {
            double angle = TAU * i / 10.0D;
            Vec3 dir = new Vec3(Math.cos(angle), 0.0D, Math.sin(angle));
            Vec3 inner = center.add(dir.scale(radius * 0.28D)).add(0.0D, 0.5D, 0.0D);
            Vec3 outer = center.add(dir.scale(radius * (0.78D + (i % 3) * 0.08D))).add(0.0D, 0.2D + (i % 2) * 0.8D, 0.0D);
            spawnLine(level, inner, outer, i % 2 == 0 ? COSMIC_CYAN : ParticleTypes.ELECTRIC_SPARK, 14, 0.02D);
        }
    }

    public static void spawnWorldRiftOpening(ServerLevel level, Vec3 center, double radius) {
        level.playSound(null, center.x, center.y, center.z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.8F, 0.45F);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 1.2F, 1.7F);
        level.sendParticles(ParticleTypes.FLASH, center.x, center.y, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.PORTAL, center.x, center.y, center.z, 160, radius * 0.65D, 2.0D, radius * 0.65D, 0.8D);
        spawnWorldRiftBloom(level, center, radius);
        spawnRing(level, center, X_AXIS, Z_AXIS, radius * 1.1D, ParticleTypes.REVERSE_PORTAL, 104, 0.01D);
        spawnRing(level, center.add(0.0D, 1.4D, 0.0D), X_AXIS, Z_AXIS, radius * 0.55D, ParticleTypes.END_ROD, 64, 0.008D);
    }

    public static void spawnWorldRiftThread(ServerLevel level, Vec3 origin, Vec3 target, int index, RandomSource random) {
        double width = 0.72D + (index % 4) * 0.08D;
        spawnSlashBridge(level, origin, target, width, random);
        spawnLine(level, origin, target, ParticleTypes.ELECTRIC_SPARK, 14, 0.025D);
        level.sendParticles(ParticleTypes.END_ROD, target.x, target.y, target.z, 24, 0.5D, 0.7D, 0.5D, 0.08D);
        if (index % 3 == 0) {
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, target.x, target.y, target.z, 18, 0.45D, 0.45D, 0.45D, 0.18D);
        }
    }

    public static void spawnCausalityAnchor(ServerLevel level, Vec3 center, int chainSize) {
        double radius = Math.min(7.0D, 2.4D + chainSize * 0.18D);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.85F);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.2F, 0.7F);
        level.sendParticles(ParticleTypes.FLASH, center.x, center.y, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        spawnRing(level, center.add(0.0D, 0.45D, 0.0D), X_AXIS, Z_AXIS, radius, COSMIC_CYAN, 72, 0.008D);
        spawnRing(level, center.add(0.0D, 1.2D, 0.0D), X_AXIS, Z_AXIS, radius * 0.58D, TERMINUS_GOLD, 52, 0.008D);
    }

    public static void spawnCausalityStep(ServerLevel level, Vec3 previous, Vec3 target, int index, RandomSource random) {
        double width = 0.85D + index * 0.055D;
        spawnSlashBridge(level, previous, target, width, random);
        spawnLine(level, previous, target, index % 2 == 0 ? ParticleTypes.END_ROD : ParticleTypes.ELECTRIC_SPARK, 18, 0.018D);
        spawnRing(level, target, X_AXIS, Z_AXIS, 0.9D + index * 0.04D, index % 2 == 0 ? COSMIC_CYAN : TERMINUS_GOLD, 32, 0.006D);
        level.sendParticles(ParticleTypes.WITCH, target.x, target.y, target.z, 16, 0.36D, 0.42D, 0.36D, 0.04D);
    }

    public static void spawnEchoWave(ServerLevel level, Vec3 start, Vec3 direction, Vec3 right, double range, double width, int wave) {
        Vec3 forward = safeNormalize(direction, Z_AXIS);
        Vec3 side = safeNormalize(right, X_AXIS);
        Vec3 up = safeNormalize(side.cross(forward), UP);
        Vec3 end = start.add(forward.scale(range));

        spawnLine(level, start, end, wave % 2 == 0 ? TERMINUS_GOLD : COSMIC_CYAN, 34, 0.018D);
        for (int lane = -2; lane <= 2; lane++) {
            if (lane == 0) continue;
            double offset = lane * width * 0.24D;
            ParticleOptions particle = Math.abs(lane) == 2 ? VOID_PURPLE : COSMIC_CYAN;
            spawnLine(level, start.add(side.scale(offset)), end.add(side.scale(offset)), particle, 24, 0.025D);
        }

        for (double distance = 5.0D; distance < range; distance += 7.0D) {
            Vec3 center = start.add(forward.scale(distance));
            double radius = width * (0.28D + distance / range * 0.22D) + wave * 0.12D;
            spawnRing(level, center, side, up, radius, wave % 2 == 0 ? VOID_PURPLE : TERMINUS_GOLD, 24, 0.01D);
        }
    }

    public static void spawnStarlessJudgementCast(ServerLevel level, Vec3 start, Vec3 direction, Vec3 right, double range, double width) {
        Vec3 forward = safeNormalize(direction, Z_AXIS);
        Vec3 side = safeNormalize(right, X_AXIS);
        Vec3 end = start.add(forward.scale(range));

        level.playSound(null, start.x, start.y, start.z, SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 1.2F, 1.75F);
        level.playSound(null, start.x, start.y, start.z, SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 0.8F, 1.35F);
        for (int lane = -1; lane <= 1; lane++) {
            Vec3 laneStart = start.add(side.scale(lane * width * 0.45D)).add(0.0D, Math.abs(lane) * 0.35D, 0.0D);
            spawnEchoWave(level, laneStart, forward, side, range + Math.abs(lane) * 7.0D, width, lane + 1);
        }

        spawnLine(level, start, end, ParticleTypes.END_ROD, 44, 0.01D);
        spawnLine(level, start.add(side.scale(width * 0.72D)), end.add(side.scale(-width * 0.38D)), ParticleTypes.ELECTRIC_SPARK, 32, 0.02D);
        spawnLine(level, start.add(side.scale(-width * 0.72D)), end.add(side.scale(width * 0.38D)), TERMINUS_GOLD, 32, 0.02D);
        level.sendParticles(ParticleTypes.FLASH, end.x, end.y, end.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    public static void spawnSlashBridge(ServerLevel level, Vec3 start, Vec3 end, double width, RandomSource random) {
        Vec3 direction = safeNormalize(end.subtract(start), Z_AXIS);
        Vec3 right = safeNormalize(direction.cross(UP), X_AXIS);
        spawnLine(level, start, end, VOID_PURPLE, 16, 0.018D);
        spawnLine(level, start.add(right.scale(width)), end.add(right.scale(-width)), COSMIC_CYAN, 12, 0.018D);
        spawnLine(level, start.add(right.scale(-width)), end.add(right.scale(width)), TERMINUS_GOLD, 12, 0.018D);
        for (int i = 0; i < 4; i++) {
            Vec3 mid = start.lerp(end, random.nextDouble());
            Vec3 slash = right.scale(width * (0.5D + random.nextDouble() * 0.9D));
            spawnLine(level, mid.subtract(slash), mid.add(slash), ParticleTypes.ELECTRIC_SPARK, 6, 0.012D);
        }
    }

    private static void spawnRing(ServerLevel level, Vec3 center, Vec3 axisA, Vec3 axisB, double radius, ParticleOptions particle, int points, double jitter) {
        Vec3 a = safeNormalize(axisA, X_AXIS);
        Vec3 b = safeNormalize(axisB, Z_AXIS);
        for (int i = 0; i < points; i++) {
            double angle = TAU * i / points;
            Vec3 pos = center
                    .add(a.scale(Math.cos(angle) * radius))
                    .add(b.scale(Math.sin(angle) * radius));
            level.sendParticles(particle, pos.x, pos.y, pos.z, 1, jitter, jitter, jitter, 0.0D);
        }
    }

    private static void spawnLine(ServerLevel level, Vec3 start, Vec3 end, ParticleOptions particle, int points, double jitter) {
        for (int i = 0; i <= points; i++) {
            Vec3 pos = start.lerp(end, i / (double) points);
            level.sendParticles(particle, pos.x, pos.y, pos.z, 1, jitter, jitter, jitter, 0.0D);
        }
    }

    private static Vec3 randomUnit(RandomSource random) {
        double yaw = random.nextDouble() * TAU;
        double pitch = (random.nextDouble() - 0.5D) * Math.PI;
        double horizontal = Math.cos(pitch);
        return new Vec3(Math.cos(yaw) * horizontal, Math.sin(pitch), Math.sin(yaw) * horizontal).normalize();
    }

    private static Vec3 safeNormalize(Vec3 vector, Vec3 fallback) {
        if (vector.lengthSqr() < 1.0E-6D) {
            return fallback;
        }
        return vector.normalize();
    }
}
