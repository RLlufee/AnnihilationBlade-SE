package org.examplea.annihilationblade.specialeffect;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.entity.EntityAbstractSummonedSword;
import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import mods.flammpfeil.slashblade.registry.specialeffects.SpecialEffect;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.examplea.annihilationblade.Annihilationblade;
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
public class PhantomJudgement extends SpecialEffect {
    private static final double RANGE = 40.0D;
    private static final int SEARCH_TICKS = 20;
    private static final int SWORD_COUNT = 8;
    private static final int RAIN_SWORDS_PER_TARGET = 6;
    private static final int LINGER_TICKS = 60;
    private static final int MAX_TARGETS = 24;
    private static final int MAX_LINGERING_SWORDS = 96;
    private static final int COOLDOWN_TICKS = 44;
    private static final double TAU = Math.PI * 2.0D;
    private static final Map<UUID, Sequence> ACTIVE = new HashMap<>();
    private static final Map<UUID, List<LingeringSword>> LINGERING = new HashMap<>();
    private static final Map<UUID, Long> LAST_TRIGGER = new HashMap<>();

    public PhantomJudgement() {
        super(0, false, false);
    }

    public static void clearPlayer(Level level, UUID playerId) {
        ServerLevel serverLevel = level instanceof ServerLevel server ? server : null;
        Sequence sequence = ACTIVE.remove(playerId);
        if (serverLevel != null && sequence != null) {
            discardSearchSwords(serverLevel, sequence);
        }

        List<LingeringSword> lingering = LINGERING.remove(playerId);
        if (serverLevel != null && lingering != null) {
            for (LingeringSword sword : lingering) {
                discardLingering(serverLevel, sword);
            }
        }

        LAST_TRIGGER.remove(playerId);
    }

    @SubscribeEvent
    public static void onDoingSlash(SlashBladeEvent.DoSlashEvent event) {
        if (!(event.getUser() instanceof ServerPlayer player)) return;
        if (Dankong.isActive(player)) return;

        ISlashBladeState state = event.getSlashBladeState();
        if (!state.hasSpecialEffect(ModSpecialEffects.PHANTOM_JUDGEMENT.getId())) return;
        if (ACTIVE.containsKey(player.getUUID())) return;

        long gameTime = player.level().getGameTime();
        long last = LAST_TRIGGER.getOrDefault(player.getUUID(), -COOLDOWN_TICKS * 2L);
        if (gameTime - last < COOLDOWN_TICKS) return;

        ServerLevel level = player.serverLevel();
        List<TargetLock> locks = collectTargets(level, player);
        if (locks.isEmpty()) return;

        LAST_TRIGGER.put(player.getUUID(), gameTime);
        ACTIVE.put(player.getUUID(), new Sequence(state.getColorCode(), locks));

        Vec3 center = player.position().add(0.0D, player.getBbHeight() * 0.58D, 0.0D);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.5F, 1.8F);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.1F, 0.65F);
        level.sendParticles(ParticleTypes.ENCHANT, center.x, center.y, center.z, 130, 2.4D, 1.2D, 2.4D, 0.08D);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        ServerLevel level = player.serverLevel();
        tickLingeringSwords(level, player);

        Sequence sequence = ACTIVE.get(player.getUUID());
        if (sequence == null) return;

        sequence.age++;
        spawnSearchingBlades(level, player, sequence);

        if (sequence.age < SEARCH_TICKS) return;

        discardSearchSwords(level, sequence);
        executeJudgement(level, player, sequence);
        ACTIVE.remove(player.getUUID());
    }

    private static void spawnSearchingBlades(ServerLevel level, ServerPlayer player, Sequence sequence) {
        Vec3 center = player.position().add(0.0D, player.getBbHeight() * 0.58D, 0.0D);
        int age = sequence.age;
        double radius = 2.1D + Math.sin(age * 0.45D) * 0.22D;
        for (int i = 0; i < SWORD_COUNT; i++) {
            double angle = TAU * i / SWORD_COUNT + age * 0.34D;
            double next = angle + 0.42D;
            Vec3 orbit = center.add(Math.cos(angle) * radius, 0.25D + Math.sin(age * 0.28D + i) * 0.65D, Math.sin(angle) * radius);
            Vec3 tangent = new Vec3(-Math.sin(next), 0.16D * Math.cos(age * 0.2D + i), Math.cos(next)).normalize();
            EntityAbstractSummonedSword sword = getOrCreateSearchSword(level, player, sequence, i);
            placeSummonedSword(sword, orbit, tangent, sequence.color);
            level.sendParticles(ParticleTypes.ENCHANT, orbit.x, orbit.y, orbit.z, 2, 0.06D, 0.06D, 0.06D, 0.0D);
        }

        if (age % 4 == 0) {
            level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 0.3D, center.z, 10, radius * 0.55D, 0.28D, radius * 0.55D, 0.02D);
        }
    }

    private static void executeJudgement(ServerLevel level, ServerPlayer player, Sequence sequence) {
        Vec3 center = player.position().add(0.0D, player.getBbHeight() * 0.55D, 0.0D);

        int count = 0;
        for (TargetLock lock : sequence.targets) {
            if (count >= MAX_TARGETS) break;

            LivingEntity target = findTarget(level, lock.targetId);
            if (target == null || !SpecialEffectSupport.canTarget(player, target)) continue;

            Vec3 targetCenter = SpecialEffectSupport.centerOf(target);
            Vec3 source = center.add(Math.cos(TAU * (count % SWORD_COUNT) / SWORD_COUNT) * 2.4D, 0.8D, Math.sin(TAU * (count % SWORD_COUNT) / SWORD_COUNT) * 2.4D);
            Vec3 split = targetCenter.add(0.0D, target.getBbHeight() * 0.75D + 4.2D, 0.0D);

            AnnihilationVisuals.spawnSlashBridge(level, source, split, 0.85D, player.getRandom());
            spawnSwordRain(level, player, target, split, sequence.color, count);
            AnnihilationVisuals.spawnExecutionBurst(level, target, player.getRandom());
            TerminusLogic.execute(target, player);
            count++;
        }

        level.playSound(null, center.x, center.y, center.z, SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 1.4F, 1.65F);
        AnnihilationVisuals.spawnCollapsePulse(level, center, Math.min(12.0D, 3.2D + count * 0.18D), count);
    }

    private static List<TargetLock> collectTargets(ServerLevel level, ServerPlayer player) {
        List<LivingEntity> entities = SpecialEffectSupport.radialTargets(level, player, player.position(), RANGE);
        List<TargetLock> locks = new ArrayList<>();
        for (LivingEntity entity : entities) {
            locks.add(new TargetLock(entity.getUUID(), SpecialEffectSupport.centerOf(entity)));
            if (locks.size() >= MAX_TARGETS) break;
        }
        return locks;
    }

    private static EntityAbstractSummonedSword getOrCreateSearchSword(ServerLevel level, ServerPlayer player, Sequence sequence, int index) {
        if (index < sequence.swords.size()) {
            Entity existing = level.getEntity(sequence.swords.get(index));
            if (existing instanceof EntityAbstractSummonedSword sword && sword.isAlive()) {
                return sword;
            }
        }

        EntityAbstractSummonedSword sword = new EntityAbstractSummonedSword(SlashBlade.RegistryEvents.SummonedSword, level);
        sword.setOwner(player);
        sword.setShooter(player);
        sword.setNoClip(true);
        sword.setDamage(0.0D);
        sword.setPierce((byte) 0);
        sword.setDelay(SEARCH_TICKS + 12);
        sword.setColor(sequence.color);
        sword.setRoll(index * 40.0F);
        level.addFreshEntity(sword);

        while (sequence.swords.size() <= index) {
            sequence.swords.add(null);
        }
        sequence.swords.set(index, sword.getUUID());
        return sword;
    }

    private static void placeSummonedSword(EntityAbstractSummonedSword sword, Vec3 pos, Vec3 direction, int color) {
        Vec3 forward = direction.lengthSqr() < 1.0E-6D ? new Vec3(0.0D, -1.0D, 0.0D) : direction.normalize();
        sword.setColor(color);
        sword.setNoClip(true);
        sword.setDeltaMovement(Vec3.ZERO);
        sword.moveTo(pos.x, pos.y, pos.z, yawToFace(forward), pitchToFace(forward));
        sword.hasImpulse = true;
    }

    private static void discardSearchSwords(ServerLevel level, Sequence sequence) {
        for (UUID uuid : sequence.swords) {
            if (uuid == null) continue;
            Entity entity = level.getEntity(uuid);
            if (entity instanceof EntityAbstractSummonedSword sword) {
                sword.discard();
            }
        }
        sequence.swords.clear();
    }

    private static void spawnSwordRain(ServerLevel level, ServerPlayer player, LivingEntity target, Vec3 split, int color, int targetIndex) {
        Vec3 center = SpecialEffectSupport.centerOf(target);
        double radius = Math.max(1.35D, target.getBbWidth() * 1.95D);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 1.2F, 0.55F);
        level.sendParticles(ParticleTypes.FLASH, split.x, split.y, split.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.END_ROD, split.x, split.y, split.z, 28, 0.36D, 0.22D, 0.36D, 0.03D);

        for (int i = 0; i < RAIN_SWORDS_PER_TARGET; i++) {
            double angle = TAU * i / RAIN_SWORDS_PER_TARGET + targetIndex * 0.43D;
            double distance = radius * (0.35D + (i % 3) * 0.32D);
            Vec3 offset = new Vec3(Math.cos(angle) * distance, 0.0D, Math.sin(angle) * distance);
            Vec3 start = split.add(offset.scale(0.72D)).add(0.0D, (i % 2) * 0.45D, 0.0D);
            Vec3 end = target.position().add(offset).add(0.0D, 0.08D, 0.0D);
            spawnFallingSummonedSword(level, player, start, end, color, targetIndex * RAIN_SWORDS_PER_TARGET + i);
            spawnFallingTrail(level, start, end, i);
            addLingeringSword(player, end, end.subtract(start), color, targetIndex * RAIN_SWORDS_PER_TARGET + i);
        }
    }

    private static void spawnFallingSummonedSword(ServerLevel level, ServerPlayer player, Vec3 start, Vec3 end, int color, int index) {
        EntityAbstractSummonedSword sword = new EntityAbstractSummonedSword(SlashBlade.RegistryEvents.SummonedSword, level);
        sword.setOwner(player);
        sword.setShooter(player);
        sword.setColor(color);
        sword.setDamage(0.0D);
        sword.setNoClip(true);
        sword.setPierce((byte) 0);
        sword.setDelay(24);
        sword.setRoll(index * 27.0F);
        Vec3 direction = end.subtract(start).normalize();
        sword.setPos(start.x, start.y, start.z);
        sword.moveTo(start.x, start.y, start.z, yawToFace(direction), pitchToFace(direction));
        sword.shoot(direction.x, direction.y, direction.z, 2.1F, 0.0F);
        level.addFreshEntity(sword);
    }

    private static void spawnFallingTrail(ServerLevel level, Vec3 start, Vec3 end, int index) {
        spawnLine(level, start, end, 18);
        level.sendParticles(ParticleTypes.FLASH, end.x, end.y, end.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.ENCHANT, end.x, end.y + 0.18D, end.z, 30, 0.34D, 0.25D, 0.34D, 0.04D);
        if (index % 2 == 0) {
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, end.x, end.y + 0.12D, end.z, 10, 0.24D, 0.16D, 0.24D, 0.03D);
        }
    }

    private static void spawnLine(ServerLevel level, Vec3 start, Vec3 end, int points) {
        for (int i = 0; i <= points; i++) {
            Vec3 pos = start.lerp(end, i / (double) points);
            level.sendParticles(ParticleTypes.ENCHANT, pos.x, pos.y, pos.z, 1, 0.012D, 0.012D, 0.012D, 0.0D);
        }
    }

    private static LivingEntity findTarget(ServerLevel level, UUID uuid) {
        return SpecialEffectSupport.findLivingEntity(level, uuid);
    }

    private static void addLingeringSword(ServerPlayer player, Vec3 ground, Vec3 fallDirection, int color, int index) {
        Vec3 direction = fallDirection.lengthSqr() < 1.0E-6D ? new Vec3(0.0D, -1.0D, 0.0D) : fallDirection.normalize();
        List<LingeringSword> swords = LINGERING.computeIfAbsent(player.getUUID(), ignored -> new ArrayList<>());
        if (swords.size() >= MAX_LINGERING_SWORDS) {
            return;
        }
        swords.add(new LingeringSword(ground, direction, color, index * 27.0F));
    }

    private static void tickLingeringSwords(ServerLevel level, ServerPlayer player) {
        List<LingeringSword> swords = LINGERING.get(player.getUUID());
        if (swords == null) return;

        Iterator<LingeringSword> iterator = swords.iterator();
        while (iterator.hasNext()) {
            LingeringSword lingering = iterator.next();
            if (lingering.age++ >= LINGER_TICKS) {
                discardLingering(level, lingering);
                iterator.remove();
                continue;
            }

            EntityAbstractSummonedSword sword = getOrCreateLingeringSword(level, player, lingering);
            Vec3 direction = lingering.direction;
            sword.setColor(lingering.color);
            sword.setNoClip(true);
            sword.setDamage(0.0D);
            sword.setPierce((byte) 0);
            sword.setDelay(LINGER_TICKS);
            sword.setDeltaMovement(Vec3.ZERO);
            sword.moveTo(lingering.position.x, lingering.position.y, lingering.position.z, yawToFace(direction), pitchToFace(direction));
            sword.setRoll(lingering.roll);
            sword.hasImpulse = true;

            if (lingering.age % 10 == 0) {
                level.sendParticles(ParticleTypes.ENCHANT, lingering.position.x, lingering.position.y + 0.18D, lingering.position.z, 3, 0.08D, 0.04D, 0.08D, 0.0D);
            }
        }

        if (swords.isEmpty()) {
            LINGERING.remove(player.getUUID());
        }
    }

    private static EntityAbstractSummonedSword getOrCreateLingeringSword(ServerLevel level, ServerPlayer player, LingeringSword lingering) {
        if (lingering.entityId != null) {
            Entity entity = level.getEntity(lingering.entityId);
            if (entity instanceof EntityAbstractSummonedSword sword && sword.isAlive()) {
                return sword;
            }
        }

        EntityAbstractSummonedSword sword = new EntityAbstractSummonedSword(SlashBlade.RegistryEvents.SummonedSword, level);
        sword.setOwner(player);
        sword.setShooter(player);
        sword.setColor(lingering.color);
        sword.setNoClip(true);
        sword.setDamage(0.0D);
        sword.setPierce((byte) 0);
        sword.setDelay(LINGER_TICKS);
        sword.setRoll(lingering.roll);
        sword.setPos(lingering.position.x, lingering.position.y, lingering.position.z);
        level.addFreshEntity(sword);
        lingering.entityId = sword.getUUID();
        return sword;
    }

    private static void discardLingering(ServerLevel level, LingeringSword lingering) {
        if (lingering.entityId == null) return;
        Entity entity = level.getEntity(lingering.entityId);
        if (entity instanceof EntityAbstractSummonedSword sword) {
            sword.discard();
        }
    }

    private static float yawToFace(Vec3 direction) {
        return (float) (Mth.atan2(direction.x, direction.z) * Mth.RAD_TO_DEG);
    }

    private static float pitchToFace(Vec3 direction) {
        double horizontal = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        return (float) (Mth.atan2(direction.y, horizontal) * Mth.RAD_TO_DEG);
    }

    private static class Sequence {
        private final int color;
        private final List<TargetLock> targets;
        private final List<UUID> swords = new ArrayList<>();
        private int age;

        private Sequence(int color, List<TargetLock> targets) {
            this.color = color;
            this.targets = targets;
        }
    }

    private record TargetLock(UUID targetId, Vec3 center) {
    }

    private static class LingeringSword {
        private final Vec3 position;
        private final Vec3 direction;
        private final int color;
        private final float roll;
        private UUID entityId;
        private int age;

        private LingeringSword(Vec3 position, Vec3 direction, int color, float roll) {
            this.position = position;
            this.direction = direction;
            this.color = color;
            this.roll = roll;
        }
    }
}
