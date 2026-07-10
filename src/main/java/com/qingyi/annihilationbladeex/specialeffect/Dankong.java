package com.qingyi.annihilationbladeex.specialeffect;

import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import mods.flammpfeil.slashblade.registry.specialeffects.SpecialEffect;
import mods.flammpfeil.slashblade.util.AttackManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import com.qingyi.annihilationbladeex.AnnihilationBladeEX;
import com.qingyi.annihilationbladeex.TerminusLogic;
import com.qingyi.annihilationbladeex.ModSpecialEffects;
import com.qingyi.annihilationbladeex.visual.AnnihilationVisuals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = AnnihilationBladeEX.MODID)
public class Dankong extends SpecialEffect {
    private static final double RANGE = 72.0D;
    private static final int MAX_TARGETS = 48;
    private static final int STEP_INTERVAL = 2;
    private static final int COOLDOWN_TICKS = 12;
    private static final Map<UUID, Sequence> ACTIVE = new HashMap<>();
    private static final Map<UUID, Long> LAST_TRIGGER = new HashMap<>();

    public Dankong() {
        super(0, false, false);
    }

    public static boolean isActive(Player player) {
        return ACTIVE.containsKey(player.getUUID());
    }

    public static void clearPlayer(UUID playerId) {
        ACTIVE.remove(playerId);
        LAST_TRIGGER.remove(playerId);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDoingSlash(SlashBladeEvent.DoSlashEvent event) {
        if (!(event.getUser() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;

        ISlashBladeState state = event.getSlashBladeState();
        if (!state.hasSpecialEffect(ModSpecialEffects.DANKONG.getId())) return;
        if (player.isShiftKeyDown()) return;
        if (ACTIVE.containsKey(player.getUUID())) return;

        long gameTime = player.level().getGameTime();
        if (!SpecialEffectSupport.tryStartCooldown(LAST_TRIGGER, player, gameTime, COOLDOWN_TICKS)) return;

        Vec3 origin = player.position();
        List<UUID> targets = collectTargets(player, origin);
        if (targets.isEmpty()) return;

        ACTIVE.put(player.getUUID(), new Sequence(origin, player.getYRot(), player.getXRot(), targets));
        AnnihilationVisuals.spawnBlinkGate(player.serverLevel(), origin.add(0.0D, 1.0D, 0.0D), 2.0D);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.2F, 1.8F);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        Sequence sequence = ACTIVE.get(player.getUUID());
        if (sequence == null) return;

        sequence.age++;
        if (sequence.age % STEP_INTERVAL != 0) return;

        ServerLevel level = player.serverLevel();
        while (sequence.index < sequence.targets.size()) {
            LivingEntity target = findTarget(level, sequence.targets.get(sequence.index++));
            if (target == null || !canTarget(player, target)) continue;

            strikeTarget(level, player, target, sequence.origin);
            return;
        }

        player.teleportTo(level, sequence.origin.x, sequence.origin.y, sequence.origin.z, sequence.yRot, sequence.xRot);
        player.setDeltaMovement(Vec3.ZERO);
        level.playSound(null, sequence.origin.x, sequence.origin.y, sequence.origin.z,
                SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.PLAYERS, 1.4F, 0.6F);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, sequence.origin.x, sequence.origin.y + 1.0D, sequence.origin.z,
                90, 1.4D, 0.9D, 1.4D, 0.35D);
        AnnihilationVisuals.spawnBlinkGate(level, sequence.origin.add(0.0D, 1.0D, 0.0D), 2.4D);
        ACTIVE.remove(player.getUUID());
    }

    private static List<UUID> collectTargets(ServerPlayer player, Vec3 origin) {
        AABB area = new AABB(origin, origin).inflate(RANGE);
        List<LivingEntity> entities = player.serverLevel().getEntitiesOfClass(
                LivingEntity.class,
                area,
                entity -> SpecialEffectSupport.canTarget(player, entity)
                        && SpecialEffectSupport.distanceToBoxSqr(origin, entity.getBoundingBox()) <= RANGE * RANGE
        );
        entities.sort((a, b) -> Double.compare(
                SpecialEffectSupport.distanceToBoxSqr(origin, b.getBoundingBox()),
                SpecialEffectSupport.distanceToBoxSqr(origin, a.getBoundingBox())
        ));

        List<UUID> result = new ArrayList<>();
        for (LivingEntity entity : entities) {
            result.add(entity.getUUID());
            if (result.size() >= MAX_TARGETS) break;
        }
        return result;
    }

    private static LivingEntity findTarget(ServerLevel level, UUID uuid) {
        return SpecialEffectSupport.findLivingEntity(level, uuid);
    }

    private static void strikeTarget(ServerLevel level, ServerPlayer player, LivingEntity target, Vec3 origin) {
        Vec3 targetCenter = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
        Vec3 away = target.position().subtract(origin);
        if (away.lengthSqr() < 0.01D) {
            away = player.getLookAngle();
        }
        Vec3 attackPos = target.position().add(away.normalize().scale(1.8D)).add(0.0D, 0.15D, 0.0D);
        float yaw = yawToFace(attackPos, targetCenter);
        float pitch = pitchToFace(attackPos, targetCenter);
        Vec3 travelStart = player.position().add(0.0D, player.getBbHeight() * 0.5D, 0.0D);

        player.teleportTo(level, attackPos.x, attackPos.y, attackPos.z, yaw, pitch);
        player.setDeltaMovement(Vec3.ZERO);

        AnnihilationVisuals.spawnBlinkTrail(level, travelStart, targetCenter, player.getRandom());
        level.sendParticles(ParticleTypes.FLASH, targetCenter.x, targetCenter.y, targetCenter.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.END_ROD, targetCenter.x, targetCenter.y, targetCenter.z, 55, 0.8D, 0.8D, 0.8D, 0.12D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, targetCenter.x, targetCenter.y, targetCenter.z, 80, 1.2D, 1.0D, 1.2D, 0.45D);
        AnnihilationVisuals.spawnExecutionBurst(level, target, player.getRandom());
        level.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.9F);
        AttackManager.doSlash(player, player.getRandom().nextInt(360), Vec3.ZERO, true, true, 9999.0F);
        TerminusLogic.execute(target, player);
    }

    private static boolean canTarget(Player player, LivingEntity candidate) {
        return SpecialEffectSupport.canTarget(player, candidate);
    }

    private static float yawToFace(Vec3 from, Vec3 to) {
        Vec3 diff = to.subtract(from);
        return (float) (Mth.atan2(diff.z, diff.x) * Mth.RAD_TO_DEG) - 90.0F;
    }

    private static float pitchToFace(Vec3 from, Vec3 to) {
        Vec3 diff = to.subtract(from);
        double horizontal = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        return (float) (-(Mth.atan2(diff.y, horizontal) * Mth.RAD_TO_DEG));
    }

    private static class Sequence {
        private final Vec3 origin;
        private final float yRot;
        private final float xRot;
        private final List<UUID> targets;
        private int index;
        private int age;

        private Sequence(Vec3 origin, float yRot, float xRot, List<UUID> targets) {
            this.origin = origin;
            this.yRot = yRot;
            this.xRot = xRot;
            this.targets = targets;
        }
    }
}
