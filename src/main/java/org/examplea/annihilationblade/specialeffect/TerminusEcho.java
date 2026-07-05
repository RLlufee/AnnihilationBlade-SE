package org.examplea.annihilationblade.specialeffect;

import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import mods.flammpfeil.slashblade.registry.specialeffects.SpecialEffect;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.examplea.annihilationblade.Annihilationblade;
import org.examplea.annihilationblade.item.ItemAnnihilationBlade;
import org.examplea.annihilationblade.logic.TerminusLogic;
import org.examplea.annihilationblade.registry.ModSpecialEffects;
import org.examplea.annihilationblade.visual.AnnihilationVisuals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Annihilationblade.MODID)
public class TerminusEcho extends SpecialEffect {
    private static final double RANGE = 36.0D;
    private static final double WIDTH = 4.4D;
    private static final int ECHO_COUNT = 5;
    private static final int ECHO_INTERVAL = 3;
    private static final Map<UUID, List<EchoSequence>> ACTIVE = new HashMap<>();

    public TerminusEcho() {
        super(0, false, false);
    }

    @SubscribeEvent
    public static void onDoingSlash(SlashBladeEvent.DoSlashEvent event) {
        if (!(event.getUser() instanceof ServerPlayer player)) return;
        if (Dankong.isActive(player)) return;

        ISlashBladeState state = event.getSlashBladeState();
        if (!state.hasSpecialEffect(ModSpecialEffects.TERMINUS_ECHO.getId())) return;

        Vec3 start = player.getEyePosition().add(player.getLookAngle().normalize().scale(1.0D));
        Vec3 direction = player.getLookAngle().normalize();
        Vec3 right = rightOf(direction);
        EchoSequence sequence = new EchoSequence(start, direction, right);
        ACTIVE.computeIfAbsent(player.getUUID(), ignored -> new ArrayList<>()).add(sequence);
        releaseEcho(player.serverLevel(), player, sequence, 0);
        sequence.nextWave = 1;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        List<EchoSequence> sequences = ACTIVE.get(player.getUUID());
        if (sequences == null || sequences.isEmpty()) return;

        Iterator<EchoSequence> iterator = sequences.iterator();
        while (iterator.hasNext()) {
            EchoSequence sequence = iterator.next();
            sequence.age++;
            if (sequence.age % ECHO_INTERVAL != 0) continue;

            if (sequence.nextWave >= ECHO_COUNT) {
                iterator.remove();
                continue;
            }

            releaseEcho(player.serverLevel(), player, sequence, sequence.nextWave);
            sequence.nextWave++;
        }

        if (sequences.isEmpty()) {
            ACTIVE.remove(player.getUUID());
        }
    }

    private static void releaseEcho(ServerLevel level, ServerPlayer player, EchoSequence sequence, int wave) {
        double side = wave == 0 ? 0.0D : ((wave % 2 == 0 ? 1.0D : -1.0D) * (1.8D + wave * 0.85D));
        double lift = wave * 0.28D;
        double range = RANGE + wave * 2.5D;
        double width = WIDTH + wave * 0.35D;

        Vec3 offset = sequence.right.scale(side).add(0.0D, lift, 0.0D);
        Vec3 start = sequence.start.add(offset);
        Vec3 end = start.add(sequence.direction.scale(range));

        spawnLine(level, start, end, ParticleTypes.END_ROD, 54, 0.025D + wave * 0.005D);
        spawnLine(level, start, end, wave % 2 == 0 ? ParticleTypes.REVERSE_PORTAL : ParticleTypes.PORTAL, 42, 0.08D);
        spawnSideCuts(level, sequence, start, range, wave);
        spawnEchoRings(level, sequence, start, range, wave);
        AnnihilationVisuals.spawnEchoWave(level, start, sequence.direction, sequence.right, range, width, wave);
        strikeAlong(level, player, start, sequence.direction, range, width, wave);
    }

    private static void spawnSideCuts(ServerLevel level, EchoSequence sequence, Vec3 start, double range, int wave) {
        int cuts = 4 + wave;
        for (int i = 0; i < cuts; i++) {
            double distance = 4.0D + (range - 8.0D) * (i + 0.5D) / cuts;
            Vec3 center = start.add(sequence.direction.scale(distance));
            double tilt = (i % 2 == 0 ? 1.0D : -1.0D) * (1.1D + wave * 0.18D);
            Vec3 blade = sequence.right.scale(3.5D + wave * 0.8D).add(0.0D, tilt, 0.0D);
            ParticleOptions particle = i % 2 == 0 ? ParticleTypes.ELECTRIC_SPARK : ParticleTypes.END_ROD;
            spawnLine(level, center.subtract(blade), center.add(blade), particle, 14, 0.015D);
            if (i % 2 == 0) {
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    private static void spawnEchoRings(ServerLevel level, EchoSequence sequence, Vec3 start, double range, int wave) {
        Vec3 up = sequence.right.cross(sequence.direction).normalize();
        for (double distance = 7.0D; distance < range; distance += 8.0D) {
            Vec3 center = start.add(sequence.direction.scale(distance));
            double radius = 1.4D + wave * 0.55D + distance / range * 1.6D;
            ParticleOptions particle = wave % 2 == 0 ? ParticleTypes.REVERSE_PORTAL : ParticleTypes.END_ROD;
            spawnRing(level, center, sequence.right, up, radius, particle, 28);
        }
    }

    private static void strikeAlong(ServerLevel level, ServerPlayer player, Vec3 start, Vec3 direction, double range, double width, int wave) {
        Vec3 end = start.add(direction.scale(range));
        AABB area = new AABB(start, end).inflate(width);
        double maxDistanceSqr = width * width;

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area, entity -> canTarget(player, entity))) {
            Vec3 targetCenter = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
            double projection = targetCenter.subtract(start).dot(direction);
            if (projection < 0.0D || projection > range) continue;

            Vec3 nearest = start.add(direction.scale(projection));
            if (targetCenter.distanceToSqr(nearest) > maxDistanceSqr) continue;

            spawnLine(level, nearest, targetCenter, ParticleTypes.ELECTRIC_SPARK, 9 + wave, 0.025D);
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), targetCenter.y, target.getZ(),
                    2 + wave, 0.35D, 0.35D, 0.35D, 0.0D);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, target.getX(), targetCenter.y, target.getZ(),
                    20 + wave * 6, 0.55D, 0.7D, 0.55D, 0.25D);
            AnnihilationVisuals.spawnExecutionBurst(level, target, player.getRandom());
            TerminusLogic.execute(target, player);
        }
    }

    private static boolean canTarget(Player player, LivingEntity candidate) {
        if (candidate == player) return false;
        if (!candidate.isAlive()) return false;
        if (candidate.isAlliedTo(player)) return false;
        if (candidate instanceof Player other) {
            if (other.isCreative() || other.isSpectator()) return false;
            return !(other.getMainHandItem().getItem() instanceof ItemAnnihilationBlade);
        }
        return true;
    }

    private static Vec3 rightOf(Vec3 direction) {
        Vec3 right = direction.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (right.lengthSqr() < 0.001D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        }
        return right.normalize();
    }

    private static void spawnLine(ServerLevel level, Vec3 start, Vec3 end, ParticleOptions particle, int points, double jitter) {
        for (int i = 0; i <= points; i++) {
            Vec3 pos = start.lerp(end, i / (double) points);
            level.sendParticles(particle, pos.x, pos.y, pos.z, 1, jitter, jitter, jitter, 0.0D);
        }
    }

    private static void spawnRing(ServerLevel level, Vec3 center, Vec3 axisA, Vec3 axisB, double radius, ParticleOptions particle, int points) {
        for (int i = 0; i < points; i++) {
            double angle = Math.PI * 2.0D * i / points;
            Vec3 pos = center
                    .add(axisA.scale(Math.cos(angle) * radius))
                    .add(axisB.scale(Math.sin(angle) * radius));
            level.sendParticles(particle, pos.x, pos.y, pos.z, 1, 0.015D, 0.015D, 0.015D, 0.0D);
        }
    }

    private static class EchoSequence {
        private final Vec3 start;
        private final Vec3 direction;
        private final Vec3 right;
        private int age;
        private int nextWave;

        private EchoSequence(Vec3 start, Vec3 direction, Vec3 right) {
            this.start = start;
            this.direction = direction;
            this.right = right;
        }
    }
}
