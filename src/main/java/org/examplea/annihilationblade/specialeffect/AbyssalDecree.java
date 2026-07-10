package org.examplea.annihilationblade.specialeffect;

import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import mods.flammpfeil.slashblade.registry.specialeffects.SpecialEffect;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.examplea.annihilationblade.Annihilationblade;
import org.examplea.annihilationblade.logic.TerminusLogic;
import org.examplea.annihilationblade.registry.ModSpecialEffects;
import org.examplea.annihilationblade.visual.AnnihilationVisuals;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Annihilationblade.MODID)
public class AbyssalDecree extends SpecialEffect {
    private static final double RANGE = 34.0D;
    private static final int MAX_TARGETS = 16;
    private static final int STRIKE_INTERVAL = 3;
    private static final int COOLDOWN_TICKS = 82;
    private static final double TAU = Math.PI * 2.0D;
    private static final Map<UUID, Sequence> ACTIVE = new HashMap<>();
    private static final Map<UUID, Long> LAST_TRIGGER = new HashMap<>();

    public AbyssalDecree() {
        super(0, false, false);
    }

    public static void clearPlayer(UUID playerId) {
        ACTIVE.remove(playerId);
        LAST_TRIGGER.remove(playerId);
    }

    @SubscribeEvent
    public static void onDoingSlash(SlashBladeEvent.DoSlashEvent event) {
        if (!(event.getUser() instanceof ServerPlayer player)) return;
        if (Dankong.isActive(player)) return;

        ISlashBladeState state = event.getSlashBladeState();
        if (!state.hasSpecialEffect(ModSpecialEffects.ABYSSAL_DECREE.getId())) return;
        if (ACTIVE.containsKey(player.getUUID())) return;

        long gameTime = player.level().getGameTime();
        long last = LAST_TRIGGER.getOrDefault(player.getUUID(), -COOLDOWN_TICKS * 2L);
        if (gameTime - last < COOLDOWN_TICKS) return;

        List<UUID> targets = collectTargets(player);
        if (targets.isEmpty()) return;

        LAST_TRIGGER.put(player.getUUID(), gameTime);
        ACTIVE.put(player.getUUID(), new Sequence(targets));

        Vec3 crown = player.position().add(0.0D, player.getBbHeight() + 3.2D, 0.0D);
        ServerLevel level = player.serverLevel();
        spawnCrown(level, crown, 3.6D, 0);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2F, 0.55F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 1.1F, 1.45F);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        Sequence sequence = ACTIVE.get(player.getUUID());
        if (sequence == null) return;

        sequence.age++;
        ServerLevel level = player.serverLevel();
        Vec3 crown = player.position().add(0.0D, player.getBbHeight() + 3.2D, 0.0D);
        spawnCrown(level, crown, 3.6D + Math.sin(sequence.age * 0.25D) * 0.35D, sequence.age);

        if (sequence.age % STRIKE_INTERVAL != 0) return;

        while (sequence.index < sequence.targets.size()) {
            LivingEntity target = findTarget(level, sequence.targets.get(sequence.index++));
            if (target == null || !SpecialEffectSupport.canTarget(player, target)) continue;

            strike(level, player, target, crown, sequence.index);
            return;
        }

        AnnihilationVisuals.spawnCollapsePulse(level, crown, 5.0D, sequence.targets.size());
        level.playSound(null, crown.x, crown.y, crown.z, SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.1F, 0.6F);
        ACTIVE.remove(player.getUUID());
    }

    private static List<UUID> collectTargets(ServerPlayer player) {
        List<LivingEntity> entities = SpecialEffectSupport.radialTargets(player.serverLevel(), player, player.position(), RANGE);
        entities.sort(Comparator
                .comparingDouble((LivingEntity entity) -> entity.getHealth() + entity.getArmorValue() * 2.0D)
                .reversed()
                .thenComparingDouble(entity -> entity.distanceToSqr(player)));

        List<UUID> result = new ArrayList<>();
        for (LivingEntity entity : entities) {
            result.add(entity.getUUID());
            if (result.size() >= MAX_TARGETS) break;
        }
        return result;
    }

    private static void strike(ServerLevel level, ServerPlayer player, LivingEntity target, Vec3 crown, int index) {
        Vec3 head = target.position().add(0.0D, target.getBbHeight() + 0.35D, 0.0D);
        Vec3 sky = head.add(0.0D, 8.0D, 0.0D);
        double radius = 3.4D + (index % 4) * 0.35D;
        double angle = TAU * index / 7.0D;
        Vec3 seal = crown.add(Math.cos(angle) * radius, Math.sin(index * 0.7D) * 0.45D, Math.sin(angle) * radius);

        AnnihilationVisuals.spawnSlashBridge(level, seal, sky, 1.05D, player.getRandom());
        spawnVerticalSentence(level, sky, head);
        AnnihilationVisuals.spawnExecutionBurst(level, target, player.getRandom());
        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.9F, 1.9F);
        TerminusLogic.execute(target, player);
    }

    private static LivingEntity findTarget(ServerLevel level, UUID uuid) {
        return SpecialEffectSupport.findLivingEntity(level, uuid);
    }

    private static void spawnCrown(ServerLevel level, Vec3 center, double radius, int tick) {
        for (int i = 0; i < 14; i++) {
            double angle = TAU * i / 14.0D + tick * 0.045D;
            Vec3 point = center.add(Math.cos(angle) * radius, Math.sin(angle * 3.0D + tick * 0.08D) * 0.28D, Math.sin(angle) * radius);
            level.sendParticles(ParticleTypes.ENCHANT, point.x, point.y, point.z, 2, 0.025D, 0.025D, 0.025D, 0.0D);
            if (i % 2 == 0) {
                level.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z, 1, 0.01D, 0.01D, 0.01D, 0.0D);
            }
        }
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y, center.z, 10, radius * 0.25D, 0.2D, radius * 0.25D, 0.08D);
    }

    private static void spawnVerticalSentence(ServerLevel level, Vec3 start, Vec3 end) {
        for (int i = 0; i <= 24; i++) {
            Vec3 pos = start.lerp(end, i / 24.0D);
            level.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 1, 0.015D, 0.015D, 0.015D, 0.0D);
            if (i % 3 == 0) {
                level.sendParticles(ParticleTypes.ENCHANT, pos.x, pos.y, pos.z, 3, 0.08D, 0.03D, 0.08D, 0.0D);
            }
        }
        level.sendParticles(ParticleTypes.FLASH, end.x, end.y, end.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.SONIC_BOOM, end.x, end.y, end.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    private static class Sequence {
        private final List<UUID> targets;
        private int age;
        private int index;

        private Sequence(List<UUID> targets) {
            this.targets = targets;
        }
    }
}
