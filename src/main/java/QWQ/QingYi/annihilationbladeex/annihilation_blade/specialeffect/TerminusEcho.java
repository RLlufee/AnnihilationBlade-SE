package QWQ.QingYi.annihilationbladeex.annihilation_blade.specialeffect;

import QWQ.QingYi.annihilationbladeex.annihilation_blade.logic.TerminusLogic;
import QWQ.QingYi.annihilationbladeex.annihilation_blade.visual.AnnihilationVisuals;
import QWQ.QingYi.annihilationbladeex.common.SpecialEffectSupport;
import QWQ.QingYi.annihilationbladeex.config.ModConfig;
import QWQ.QingYi.annihilationbladeex.registry.ModSpecialEffects;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.event.SlashBladeEvent.DoSlashEvent;
import mods.flammpfeil.slashblade.registry.specialeffects.SpecialEffect;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = "annihilationbladeex")
public class TerminusEcho extends SpecialEffect {
   private static final Map<UUID, List<TerminusEcho.EchoSequence>> ACTIVE = new HashMap<>();

   public TerminusEcho() {
      super(0, false, false);
   }

   public static void clearPlayer(UUID playerId) {
      ACTIVE.remove(playerId);
   }

   @SubscribeEvent
   public static void onDoingSlash(DoSlashEvent event) {
      if (event.getUser() instanceof ServerPlayer player) {
         if (!Dankong.isActive(player)) {
            ISlashBladeState state = event.getSlashBladeState();
            if (state.hasSpecialEffect(ModSpecialEffects.TERMINUS_ECHO.getId())) {
               ModConfig.TerminusEcho config = ModConfig.COMMON.annihilationBlade.terminusEcho;
               List<TerminusEcho.EchoSequence> sequences = ACTIVE.get(player.getUUID());
               if (sequences == null || sequences.size() < config.maxActiveSequences.get()) {
                  Vec3 start = player.getEyePosition().add(player.getLookAngle().normalize().scale(1.0));
                  Vec3 direction = player.getLookAngle().normalize();
                  Vec3 right = SpecialEffectSupport.rightOf(direction);
                  TerminusEcho.EchoSequence sequence = new TerminusEcho.EchoSequence(start, direction, right);
                  ACTIVE.computeIfAbsent(player.getUUID(), ignored -> new ArrayList<>()).add(sequence);
                  releaseEcho(player.serverLevel(), player, sequence, 0);
                  sequence.nextWave = 1;
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent.Post event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         List<TerminusEcho.EchoSequence> sequences = ACTIVE.get(player.getUUID());
         if (sequences != null && !sequences.isEmpty()) {
            Iterator<TerminusEcho.EchoSequence> iterator = sequences.iterator();

            while (iterator.hasNext()) {
               TerminusEcho.EchoSequence sequence = iterator.next();
               sequence.age++;
               if (sequence.age % ModConfig.COMMON.annihilationBlade.terminusEcho.echoInterval.get() == 0) {
                  if (sequence.nextWave >= ModConfig.COMMON.annihilationBlade.terminusEcho.echoCount.get()) {
                     iterator.remove();
                  } else {
                     releaseEcho(player.serverLevel(), player, sequence, sequence.nextWave);
                     sequence.nextWave++;
                  }
               }
            }

            if (sequences.isEmpty()) {
               ACTIVE.remove(player.getUUID());
            }
         }
      }
   }

   private static void releaseEcho(ServerLevel level, ServerPlayer player, TerminusEcho.EchoSequence sequence, int wave) {
      ModConfig.TerminusEcho config = ModConfig.COMMON.annihilationBlade.terminusEcho;
      double visualScale = config.visualScale.get();
      double side = wave == 0 ? 0.0 : (wave % 2 == 0 ? 1.0 : -1.0) * (1.8 + wave * 0.85);
      double lift = wave * 0.28;
      double range = config.range.get() + wave * 2.5;
      double width = config.width.get() + wave * 0.35;
      Vec3 offset = sequence.right.scale(side).add(0.0, lift, 0.0);
      Vec3 start = sequence.start.add(offset);
      Vec3 end = start.add(sequence.direction.scale(range));
      spawnLine(level, start, end, ParticleTypes.END_ROD, visualCount(42, visualScale), 0.025 + wave * 0.005);
      spawnLine(level, start, end, wave % 2 == 0 ? ParticleTypes.REVERSE_PORTAL : ParticleTypes.PORTAL, visualCount(30, visualScale), 0.08);
      spawnSideCuts(level, sequence, start, range, wave, visualScale);
      spawnEchoRings(level, sequence, start, range, wave, visualScale);
      AnnihilationVisuals.spawnEchoWave(level, start, sequence.direction, sequence.right, range * visualScale, width * visualScale, wave);
      strikeAlong(level, player, start, sequence.direction, range, width, wave);
   }

   private static void spawnSideCuts(ServerLevel level, TerminusEcho.EchoSequence sequence, Vec3 start, double range, int wave, double visualScale) {
      int cuts = visualCount(4 + wave, visualScale);

      for (int i = 0; i < cuts; i++) {
         double distance = 4.0 + (range - 8.0) * (i + 0.5) / cuts;
         Vec3 center = start.add(sequence.direction.scale(distance));
         double tilt = (i % 2 == 0 ? 1.0 : -1.0) * (1.1 + wave * 0.18);
         Vec3 blade = sequence.right.scale((3.5 + wave * 0.8) * visualScale).add(0.0, tilt * visualScale, 0.0);
         ParticleOptions particle = i % 2 == 0 ? ParticleTypes.ELECTRIC_SPARK : ParticleTypes.END_ROD;
         spawnLine(level, center.subtract(blade), center.add(blade), particle, visualCount(10, visualScale), 0.015);
         if (i % 2 == 0) {
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y, center.z, 1, 0.0, 0.0, 0.0, 0.0);
         }
      }
   }

   private static void spawnEchoRings(ServerLevel level, TerminusEcho.EchoSequence sequence, Vec3 start, double range, int wave, double visualScale) {
      Vec3 up = sequence.right.cross(sequence.direction).normalize();

      for (double distance = 7.0; distance < range; distance += 8.0) {
         Vec3 center = start.add(sequence.direction.scale(distance));
         double radius = (1.4 + wave * 0.55 + distance / range * 1.6) * visualScale;
         ParticleOptions particle = wave % 2 == 0 ? ParticleTypes.REVERSE_PORTAL : ParticleTypes.END_ROD;
         spawnRing(level, center, sequence.right, up, radius, particle, visualCount(20, visualScale));
      }
   }

   private static void strikeAlong(ServerLevel level, ServerPlayer player, Vec3 start, Vec3 direction, double range, double width, int wave) {
      double visualScale = ModConfig.COMMON.annihilationBlade.terminusEcho.visualScale.get();
      for (LivingEntity target : SpecialEffectSupport.beamTargets(level, player, start, direction, range, width, ModConfig.COMMON.annihilationBlade.terminusEcho.maxTargetsPerWave.get())) {
         Vec3 targetCenter = SpecialEffectSupport.centerOf(target);
         double projection = targetCenter.subtract(start).dot(direction);
         Vec3 nearest = start.add(direction.scale(projection));
         spawnLine(level, nearest, targetCenter, ParticleTypes.ELECTRIC_SPARK, visualCount(9 + wave, visualScale), 0.025);
         level.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), targetCenter.y, target.getZ(), visualCount(2 + wave, visualScale), 0.35 * visualScale, 0.35 * visualScale, 0.35 * visualScale, 0.0);
         level.sendParticles(ParticleTypes.REVERSE_PORTAL, target.getX(), targetCenter.y, target.getZ(), visualCount(20 + wave * 6, visualScale), 0.55 * visualScale, 0.7 * visualScale, 0.55 * visualScale, 0.25);
         AnnihilationVisuals.spawnExecutionBurst(level, target, player.getRandom());
         TerminusLogic.execute(target, player);
      }
   }

   private static void spawnLine(ServerLevel level, Vec3 start, Vec3 end, ParticleOptions particle, int points, double jitter) {
      if (points <= 0) {
         return;
      }

      for (int i = 0; i <= points; i++) {
         Vec3 pos = start.lerp(end, (double)i / points);
         level.sendParticles(particle, pos.x, pos.y, pos.z, 1, jitter, jitter, jitter, 0.0);
      }
   }

   private static void spawnRing(ServerLevel level, Vec3 center, Vec3 axisA, Vec3 axisB, double radius, ParticleOptions particle, int points) {
      if (points <= 0) {
         return;
      }

      for (int i = 0; i < points; i++) {
         double angle = (Math.PI * 2) * i / points;
         Vec3 pos = center.add(axisA.scale(Math.cos(angle) * radius)).add(axisB.scale(Math.sin(angle) * radius));
         level.sendParticles(particle, pos.x, pos.y, pos.z, 1, 0.015, 0.015, 0.015, 0.0);
      }
   }

   private static int visualCount(int base, double visualScale) {
      return Math.max(1, (int)Math.round(base * visualScale));
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
