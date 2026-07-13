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
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.event.SlashBladeEvent.DoSlashEvent;
import mods.flammpfeil.slashblade.registry.specialeffects.SpecialEffect;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = "annihilationblade")
public class TerminusEcho extends SpecialEffect {
   private static final double RANGE = 36.0;
   private static final double WIDTH = 4.4;
   private static final int ECHO_COUNT = 5;
   private static final int ECHO_INTERVAL = 3;
   private static final int COOLDOWN_TICKS = 16;
   private static final int MAX_ACTIVE_SEQUENCES = 2;
   private static final int MAX_TARGETS_PER_WAVE = 32;
   private static final Map<UUID, List<TerminusEcho.EchoSequence>> ACTIVE = new HashMap<>();
   private static final Map<UUID, Long> LAST_TRIGGER = new HashMap<>();

   public TerminusEcho() {
      super(0, false, false);
   }

   public static void clearPlayer(UUID playerId) {
      ACTIVE.remove(playerId);
      LAST_TRIGGER.remove(playerId);
   }

   @SubscribeEvent
   public static void onDoingSlash(DoSlashEvent event) {
      if (event.getUser() instanceof ServerPlayer player) {
         if (!Dankong.isActive(player)) {
            ISlashBladeState state = event.getSlashBladeState();
            if (state.hasSpecialEffect(ModSpecialEffects.TERMINUS_ECHO.getId())) {
               List<TerminusEcho.EchoSequence> sequences = ACTIVE.get(player.getUUID());
               if (sequences == null || sequences.size() < 2) {
                  if (SpecialEffectSupport.tryStartCooldown(LAST_TRIGGER, player, player.level().getGameTime(), 16)) {
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
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         if (event.player instanceof ServerPlayer player) {
            List<TerminusEcho.EchoSequence> sequences = ACTIVE.get(player.getUUID());
            if (sequences != null && !sequences.isEmpty()) {
               Iterator<TerminusEcho.EchoSequence> iterator = sequences.iterator();

               while (iterator.hasNext()) {
                  TerminusEcho.EchoSequence sequence = iterator.next();
                  sequence.age++;
                  if (sequence.age % 3 == 0) {
                     if (sequence.nextWave >= 5) {
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
   }

   private static void releaseEcho(ServerLevel level, ServerPlayer player, TerminusEcho.EchoSequence sequence, int wave) {
      double side = wave == 0 ? 0.0 : (wave % 2 == 0 ? 1.0 : -1.0) * (1.8 + wave * 0.85);
      double lift = wave * 0.28;
      double range = 36.0 + wave * 2.5;
      double width = 4.4 + wave * 0.35;
      Vec3 offset = sequence.right.scale(side).add(0.0, lift, 0.0);
      Vec3 start = sequence.start.add(offset);
      Vec3 end = start.add(sequence.direction.scale(range));
      spawnLine(level, start, end, ParticleTypes.END_ROD, 42, 0.025 + wave * 0.005);
      spawnLine(level, start, end, wave % 2 == 0 ? ParticleTypes.REVERSE_PORTAL : ParticleTypes.PORTAL, 30, 0.08);
      spawnSideCuts(level, sequence, start, range, wave);
      spawnEchoRings(level, sequence, start, range, wave);
      AnnihilationVisuals.spawnEchoWave(level, start, sequence.direction, sequence.right, range, width, wave);
      strikeAlong(level, player, start, sequence.direction, range, width, wave);
   }

   private static void spawnSideCuts(ServerLevel level, TerminusEcho.EchoSequence sequence, Vec3 start, double range, int wave) {
      int cuts = 4 + wave;

      for (int i = 0; i < cuts; i++) {
         double distance = 4.0 + (range - 8.0) * (i + 0.5) / cuts;
         Vec3 center = start.add(sequence.direction.scale(distance));
         double tilt = (i % 2 == 0 ? 1.0 : -1.0) * (1.1 + wave * 0.18);
         Vec3 blade = sequence.right.scale(3.5 + wave * 0.8).add(0.0, tilt, 0.0);
         ParticleOptions particle = i % 2 == 0 ? ParticleTypes.ELECTRIC_SPARK : ParticleTypes.END_ROD;
         spawnLine(level, center.subtract(blade), center.add(blade), particle, 10, 0.015);
         if (i % 2 == 0) {
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y, center.z, 1, 0.0, 0.0, 0.0, 0.0);
         }
      }
   }

   private static void spawnEchoRings(ServerLevel level, TerminusEcho.EchoSequence sequence, Vec3 start, double range, int wave) {
      Vec3 up = sequence.right.cross(sequence.direction).normalize();

      for (double distance = 7.0; distance < range; distance += 8.0) {
         Vec3 center = start.add(sequence.direction.scale(distance));
         double radius = 1.4 + wave * 0.55 + distance / range * 1.6;
         ParticleOptions particle = wave % 2 == 0 ? ParticleTypes.REVERSE_PORTAL : ParticleTypes.END_ROD;
         spawnRing(level, center, sequence.right, up, radius, particle, 20);
      }
   }

   private static void strikeAlong(ServerLevel level, ServerPlayer player, Vec3 start, Vec3 direction, double range, double width, int wave) {
      for (LivingEntity target : SpecialEffectSupport.beamTargets(level, player, start, direction, range, width, 32)) {
         Vec3 targetCenter = SpecialEffectSupport.centerOf(target);
         double projection = targetCenter.subtract(start).dot(direction);
         Vec3 nearest = start.add(direction.scale(projection));
         spawnLine(level, nearest, targetCenter, ParticleTypes.ELECTRIC_SPARK, 9 + wave, 0.025);
         level.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), targetCenter.y, target.getZ(), 2 + wave, 0.35, 0.35, 0.35, 0.0);
         level.sendParticles(ParticleTypes.REVERSE_PORTAL, target.getX(), targetCenter.y, target.getZ(), 20 + wave * 6, 0.55, 0.7, 0.55, 0.25);
         AnnihilationVisuals.spawnExecutionBurst(level, target, player.getRandom());
         TerminusLogic.execute(target, player);
      }
   }

   private static void spawnLine(ServerLevel level, Vec3 start, Vec3 end, ParticleOptions particle, int points, double jitter) {
      for (int i = 0; i <= points; i++) {
         Vec3 pos = start.lerp(end, (double)i / points);
         level.sendParticles(particle, pos.x, pos.y, pos.z, 1, jitter, jitter, jitter, 0.0);
      }
   }

   private static void spawnRing(ServerLevel level, Vec3 center, Vec3 axisA, Vec3 axisB, double radius, ParticleOptions particle, int points) {
      for (int i = 0; i < points; i++) {
         double angle = (Math.PI * 2) * i / points;
         Vec3 pos = center.add(axisA.scale(Math.cos(angle) * radius)).add(axisB.scale(Math.sin(angle) * radius));
         level.sendParticles(particle, pos.x, pos.y, pos.z, 1, 0.015, 0.015, 0.015, 0.0);
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
