package QWQ.QingYi.annihilationblade.annihilation_blade.specialeffect;

import QWQ.QingYi.annihilationblade.annihilation_blade.logic.TerminusLogic;
import QWQ.QingYi.annihilationblade.annihilation_blade.visual.AnnihilationVisuals;
import QWQ.QingYi.annihilationblade.common.SpecialEffectSupport;
import QWQ.QingYi.annihilationblade.registry.ModSpecialEffects;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import mods.flammpfeil.slashblade.SlashBlade.RegistryEvents;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.entity.EntityAbstractSummonedSword;
import mods.flammpfeil.slashblade.event.SlashBladeEvent.DoSlashEvent;
import mods.flammpfeil.slashblade.registry.specialeffects.SpecialEffect;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = "annihilationblade")
public class PhantomJudgement extends SpecialEffect {
   private static final double RANGE = 40.0;
   private static final int SEARCH_TICKS = 20;
   private static final int SWORD_COUNT = 8;
   private static final int RAIN_SWORDS_PER_TARGET = 6;
   private static final int LINGER_TICKS = 60;
   private static final int MAX_TARGETS = 24;
   private static final int MAX_LINGERING_SWORDS = 96;
   private static final int COOLDOWN_TICKS = 44;
   private static final double TAU = Math.PI * 2;
   private static final Map<UUID, PhantomJudgement.Sequence> ACTIVE = new HashMap<>();
   private static final Map<UUID, List<PhantomJudgement.LingeringSword>> LINGERING = new HashMap<>();
   private static final Map<UUID, Long> LAST_TRIGGER = new HashMap<>();

   public PhantomJudgement() {
      super(0, false, false);
   }

   public static void clearPlayer(Level level, UUID playerId) {
      ServerLevel serverLevel = level instanceof ServerLevel server ? server : null;
      PhantomJudgement.Sequence sequence = ACTIVE.remove(playerId);
      if (serverLevel != null && sequence != null) {
         discardSearchSwords(serverLevel, sequence);
      }

      List<PhantomJudgement.LingeringSword> lingering = LINGERING.remove(playerId);
      if (serverLevel != null && lingering != null) {
         for (PhantomJudgement.LingeringSword sword : lingering) {
            discardLingering(serverLevel, sword);
         }
      }

      LAST_TRIGGER.remove(playerId);
   }

   @SubscribeEvent
   public static void onDoingSlash(DoSlashEvent event) {
      if (event.getUser() instanceof ServerPlayer player) {
         if (!Dankong.isActive(player)) {
            ISlashBladeState state = event.getSlashBladeState();
            if (state.hasSpecialEffect(ModSpecialEffects.PHANTOM_JUDGEMENT.getId())) {
               if (!ACTIVE.containsKey(player.getUUID())) {
                  long gameTime = player.level().getGameTime();
                  long last = LAST_TRIGGER.getOrDefault(player.getUUID(), -88L);
                  if (gameTime - last >= 44L) {
                     ServerLevel level = player.serverLevel();
                     List<PhantomJudgement.TargetLock> locks = collectTargets(level, player);
                     if (!locks.isEmpty()) {
                        LAST_TRIGGER.put(player.getUUID(), gameTime);
                        ACTIVE.put(player.getUUID(), new PhantomJudgement.Sequence(state.getColorCode(), locks));
                        Vec3 center = player.position().add(0.0, player.getBbHeight() * 0.58, 0.0);
                        level.playSound(null, center.x, center.y, center.z, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.5F, 1.8F);
                        level.playSound(null, center.x, center.y, center.z, SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.1F, 0.65F);
                        level.sendParticles(ParticleTypes.ENCHANT, center.x, center.y, center.z, 130, 2.4, 1.2, 2.4, 0.08);
                     }
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         if (event.player instanceof ServerPlayer player) {
            ServerLevel level = player.serverLevel();
            tickLingeringSwords(level, player);
            PhantomJudgement.Sequence sequence = ACTIVE.get(player.getUUID());
            if (sequence != null) {
               sequence.age++;
               spawnSearchingBlades(level, player, sequence);
               if (sequence.age >= 20) {
                  discardSearchSwords(level, sequence);
                  executeJudgement(level, player, sequence);
                  ACTIVE.remove(player.getUUID());
               }
            }
         }
      }
   }

   private static void spawnSearchingBlades(ServerLevel level, ServerPlayer player, PhantomJudgement.Sequence sequence) {
      Vec3 center = player.position().add(0.0, player.getBbHeight() * 0.58, 0.0);
      int age = sequence.age;
      double radius = 2.1 + Math.sin(age * 0.45) * 0.22;

      for (int i = 0; i < 8; i++) {
         double angle = (Math.PI * 2) * i / 8.0 + age * 0.34;
         double next = angle + 0.42;
         Vec3 orbit = center.add(Math.cos(angle) * radius, 0.25 + Math.sin(age * 0.28 + i) * 0.65, Math.sin(angle) * radius);
         Vec3 tangent = new Vec3(-Math.sin(next), 0.16 * Math.cos(age * 0.2 + i), Math.cos(next)).normalize();
         EntityAbstractSummonedSword sword = getOrCreateSearchSword(level, player, sequence, i);
         placeSummonedSword(sword, orbit, tangent, sequence.color);
         level.sendParticles(ParticleTypes.ENCHANT, orbit.x, orbit.y, orbit.z, 2, 0.06, 0.06, 0.06, 0.0);
      }

      if (age % 4 == 0) {
         level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 0.3, center.z, 10, radius * 0.55, 0.28, radius * 0.55, 0.02);
      }
   }

   private static void executeJudgement(ServerLevel level, ServerPlayer player, PhantomJudgement.Sequence sequence) {
      Vec3 center = player.position().add(0.0, player.getBbHeight() * 0.55, 0.0);
      int count = 0;

      for (PhantomJudgement.TargetLock lock : sequence.targets) {
         if (count >= 24) {
            break;
         }

         LivingEntity target = findTarget(level, lock.targetId);
         if (target != null && SpecialEffectSupport.canTarget(player, target)) {
            Vec3 targetCenter = SpecialEffectSupport.centerOf(target);
            Vec3 source = center.add(Math.cos((Math.PI * 2) * (count % 8) / 8.0) * 2.4, 0.8, Math.sin((Math.PI * 2) * (count % 8) / 8.0) * 2.4);
            Vec3 split = targetCenter.add(0.0, target.getBbHeight() * 0.75 + 4.2, 0.0);
            AnnihilationVisuals.spawnSlashBridge(level, source, split, 0.85, player.getRandom());
            spawnSwordRain(level, player, target, split, sequence.color, count);
            AnnihilationVisuals.spawnExecutionBurst(level, target, player.getRandom());
            TerminusLogic.execute(target, player);
            count++;
         }
      }

      level.playSound(null, center.x, center.y, center.z, SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 1.4F, 1.65F);
      AnnihilationVisuals.spawnCollapsePulse(level, center, Math.min(12.0, 3.2 + count * 0.18), count);
   }

   private static List<PhantomJudgement.TargetLock> collectTargets(ServerLevel level, ServerPlayer player) {
      List<LivingEntity> entities = SpecialEffectSupport.radialTargets(level, player, player.position(), 40.0);
      List<PhantomJudgement.TargetLock> locks = new ArrayList<>();

      for (LivingEntity entity : entities) {
         locks.add(new PhantomJudgement.TargetLock(entity.getUUID(), SpecialEffectSupport.centerOf(entity)));
         if (locks.size() >= 24) {
            break;
         }
      }

      return locks;
   }

   private static EntityAbstractSummonedSword getOrCreateSearchSword(ServerLevel level, ServerPlayer player, PhantomJudgement.Sequence sequence, int index) {
      if (index < sequence.swords.size() && level.getEntity(sequence.swords.get(index)) instanceof EntityAbstractSummonedSword sword && sword.isAlive()) {
         return sword;
      }

      EntityAbstractSummonedSword sword = new EntityAbstractSummonedSword(RegistryEvents.SummonedSword, level);
      sword.setOwner(player);
      sword.setShooter(player);
      sword.setNoClip(true);
      sword.setDamage(0.0);
      sword.setPierce((byte)0);
      sword.setDelay(32);
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
      Vec3 forward = direction.lengthSqr() < 1.0E-6 ? new Vec3(0.0, -1.0, 0.0) : direction.normalize();
      sword.setColor(color);
      sword.setNoClip(true);
      sword.setDeltaMovement(Vec3.ZERO);
      sword.moveTo(pos.x, pos.y, pos.z, yawToFace(forward), pitchToFace(forward));
      sword.hasImpulse = true;
   }

   private static void discardSearchSwords(ServerLevel level, PhantomJudgement.Sequence sequence) {
      for (UUID uuid : sequence.swords) {
         if (uuid != null && level.getEntity(uuid) instanceof EntityAbstractSummonedSword sword) {
            sword.discard();
         }
      }

      sequence.swords.clear();
   }

   private static void spawnSwordRain(ServerLevel level, ServerPlayer player, LivingEntity target, Vec3 split, int color, int targetIndex) {
      Vec3 center = SpecialEffectSupport.centerOf(target);
      double radius = Math.max(1.35, target.getBbWidth() * 1.95);
      level.playSound(null, center.x, center.y, center.z, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 1.2F, 0.55F);
      level.sendParticles(ParticleTypes.FLASH, split.x, split.y, split.z, 1, 0.0, 0.0, 0.0, 0.0);
      level.sendParticles(ParticleTypes.END_ROD, split.x, split.y, split.z, 28, 0.36, 0.22, 0.36, 0.03);

      for (int i = 0; i < 6; i++) {
         double angle = (Math.PI * 2) * i / 6.0 + targetIndex * 0.43;
         double distance = radius * (0.35 + i % 3 * 0.32);
         Vec3 offset = new Vec3(Math.cos(angle) * distance, 0.0, Math.sin(angle) * distance);
         Vec3 start = split.add(offset.scale(0.72)).add(0.0, i % 2 * 0.45, 0.0);
         Vec3 end = target.position().add(offset).add(0.0, 0.08, 0.0);
         spawnFallingSummonedSword(level, player, start, end, color, targetIndex * 6 + i);
         spawnFallingTrail(level, start, end, i);
         addLingeringSword(player, end, end.subtract(start), color, targetIndex * 6 + i);
      }
   }

   private static void spawnFallingSummonedSword(ServerLevel level, ServerPlayer player, Vec3 start, Vec3 end, int color, int index) {
      EntityAbstractSummonedSword sword = new EntityAbstractSummonedSword(RegistryEvents.SummonedSword, level);
      sword.setOwner(player);
      sword.setShooter(player);
      sword.setColor(color);
      sword.setDamage(0.0);
      sword.setNoClip(true);
      sword.setPierce((byte)0);
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
      level.sendParticles(ParticleTypes.FLASH, end.x, end.y, end.z, 1, 0.0, 0.0, 0.0, 0.0);
      level.sendParticles(ParticleTypes.ENCHANT, end.x, end.y + 0.18, end.z, 30, 0.34, 0.25, 0.34, 0.04);
      if (index % 2 == 0) {
         level.sendParticles(ParticleTypes.ELECTRIC_SPARK, end.x, end.y + 0.12, end.z, 10, 0.24, 0.16, 0.24, 0.03);
      }
   }

   private static void spawnLine(ServerLevel level, Vec3 start, Vec3 end, int points) {
      for (int i = 0; i <= points; i++) {
         Vec3 pos = start.lerp(end, (double)i / points);
         level.sendParticles(ParticleTypes.ENCHANT, pos.x, pos.y, pos.z, 1, 0.012, 0.012, 0.012, 0.0);
      }
   }

   private static LivingEntity findTarget(ServerLevel level, UUID uuid) {
      return SpecialEffectSupport.findLivingEntity(level, uuid);
   }

   private static void addLingeringSword(ServerPlayer player, Vec3 ground, Vec3 fallDirection, int color, int index) {
      Vec3 direction = fallDirection.lengthSqr() < 1.0E-6 ? new Vec3(0.0, -1.0, 0.0) : fallDirection.normalize();
      List<PhantomJudgement.LingeringSword> swords = LINGERING.computeIfAbsent(player.getUUID(), ignored -> new ArrayList<>());
      if (swords.size() < 96) {
         swords.add(new PhantomJudgement.LingeringSword(ground, direction, color, index * 27.0F));
      }
   }

   private static void tickLingeringSwords(ServerLevel level, ServerPlayer player) {
      List<PhantomJudgement.LingeringSword> swords = LINGERING.get(player.getUUID());
      if (swords != null) {
         Iterator<PhantomJudgement.LingeringSword> iterator = swords.iterator();

         while (iterator.hasNext()) {
            PhantomJudgement.LingeringSword lingering = iterator.next();
            if (lingering.age++ >= 60) {
               discardLingering(level, lingering);
               iterator.remove();
            } else {
               EntityAbstractSummonedSword sword = getOrCreateLingeringSword(level, player, lingering);
               Vec3 direction = lingering.direction;
               sword.setColor(lingering.color);
               sword.setNoClip(true);
               sword.setDamage(0.0);
               sword.setPierce((byte)0);
               sword.setDelay(60);
               sword.setDeltaMovement(Vec3.ZERO);
               sword.moveTo(lingering.position.x, lingering.position.y, lingering.position.z, yawToFace(direction), pitchToFace(direction));
               sword.setRoll(lingering.roll);
               sword.hasImpulse = true;
               if (lingering.age % 10 == 0) {
                  level.sendParticles(ParticleTypes.ENCHANT, lingering.position.x, lingering.position.y + 0.18, lingering.position.z, 3, 0.08, 0.04, 0.08, 0.0);
               }
            }
         }

         if (swords.isEmpty()) {
            LINGERING.remove(player.getUUID());
         }
      }
   }

   private static EntityAbstractSummonedSword getOrCreateLingeringSword(ServerLevel level, ServerPlayer player, PhantomJudgement.LingeringSword lingering) {
      if (lingering.entityId != null && level.getEntity(lingering.entityId) instanceof EntityAbstractSummonedSword sword && sword.isAlive()) {
         return sword;
      }

      EntityAbstractSummonedSword sword = new EntityAbstractSummonedSword(RegistryEvents.SummonedSword, level);
      sword.setOwner(player);
      sword.setShooter(player);
      sword.setColor(lingering.color);
      sword.setNoClip(true);
      sword.setDamage(0.0);
      sword.setPierce((byte)0);
      sword.setDelay(60);
      sword.setRoll(lingering.roll);
      sword.setPos(lingering.position.x, lingering.position.y, lingering.position.z);
      level.addFreshEntity(sword);
      lingering.entityId = sword.getUUID();
      return sword;
   }

   private static void discardLingering(ServerLevel level, PhantomJudgement.LingeringSword lingering) {
      if (lingering.entityId != null) {
         if (level.getEntity(lingering.entityId) instanceof EntityAbstractSummonedSword sword) {
            sword.discard();
         }
      }
   }

   private static float yawToFace(Vec3 direction) {
      return (float)(Mth.atan2(direction.x, direction.z) * 180.0F / (float)Math.PI);
   }

   private static float pitchToFace(Vec3 direction) {
      double horizontal = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
      return (float)(Mth.atan2(direction.y, horizontal) * 180.0F / (float)Math.PI);
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

   private static class Sequence {
      private final int color;
      private final List<PhantomJudgement.TargetLock> targets;
      private final List<UUID> swords = new ArrayList<>();
      private int age;

      private Sequence(int color, List<PhantomJudgement.TargetLock> targets) {
         this.color = color;
         this.targets = targets;
      }
   }

   private record TargetLock(UUID targetId, Vec3 center) {
   }
}
