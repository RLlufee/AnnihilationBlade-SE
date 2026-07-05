package com.qingyi.examplemod.specialeffect;

import com.qingyi.examplemod.AnnihilationBladeEX;
import com.qingyi.examplemod.ModSpecialEffects;
import com.qingyi.examplemod.TerminusLogic;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import mods.flammpfeil.slashblade.registry.specialeffects.SpecialEffect;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = AnnihilationBladeEX.MODID)
public class WorldRift extends SpecialEffect {
    private static final double RADIUS = 8.0D;

    public WorldRift() {
        super(0, false, false);
    }

    @SubscribeEvent
    public static void onSlashBladeHit(SlashBladeEvent.HitEvent event) {
        ISlashBladeState state = event.getSlashBladeState();
        if (!state.hasSpecialEffect(ModSpecialEffects.WORLD_RIFT.getId()) || !(event.getUser() instanceof Player player)) {
            return;
        }

        Entity rawTarget = event.getTarget();
        if (!(rawTarget instanceof LivingEntity target) || !(target.level() instanceof ServerLevel level)) {
            return;
        }

        Vec3 center = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.8F, 0.45F);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 1.2F, 1.7F);
        level.sendParticles(ParticleTypes.FLASH, center.x, center.y, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.PORTAL, center.x, center.y, center.z, 160, RADIUS * 0.65D, 2.0D, RADIUS * 0.65D, 0.8D);
        spawnRing(level, center, RADIUS, ParticleTypes.REVERSE_PORTAL, 72);
        spawnRing(level, center.add(0.0D, 1.4D, 0.0D), RADIUS * 0.55D, ParticleTypes.END_ROD, 56);

        AABB area = new AABB(center, center).inflate(RADIUS);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area, e -> canTarget(player, e))) {
            if (entity.position().distanceToSqr(center) > RADIUS * RADIUS) {
                continue;
            }
            Vec3 entityCenter = entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
            spawnLine(level, center, entityCenter, ParticleTypes.ELECTRIC_SPARK, 12);
            level.sendParticles(ParticleTypes.END_ROD, entityCenter.x, entityCenter.y, entityCenter.z,
                    25, 0.5D, 0.7D, 0.5D, 0.08D);
            TerminusLogic.execute(entity, player);
        }
    }

    private static boolean canTarget(Player player, LivingEntity candidate) {
        if (candidate == player || !candidate.isAlive() || candidate.isAlliedTo(player)) {
            return false;
        }
        if (candidate instanceof Player other) {
            return !other.isCreative() && !other.isSpectator() && !AnnihilationBladeEX.hasGodBlade(other);
        }
        return true;
    }

    private static void spawnRing(ServerLevel level, Vec3 center, double radius, ParticleOptions particle, int points) {
        for (int i = 0; i < points; i++) {
            double angle = Math.PI * 2.0D * i / points;
            level.sendParticles(particle, center.x + Math.cos(angle) * radius, center.y,
                    center.z + Math.sin(angle) * radius, 1, 0.02D, 0.02D, 0.02D, 0.0D);
        }
    }

    private static void spawnLine(ServerLevel level, Vec3 start, Vec3 end, ParticleOptions particle, int points) {
        for (int i = 0; i <= points; i++) {
            Vec3 pos = start.lerp(end, i / (double) points);
            level.sendParticles(particle, pos.x, pos.y, pos.z, 1, 0.03D, 0.03D, 0.03D, 0.0D);
        }
    }
}
