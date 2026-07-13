package QWQ.QingYi.annihilationblade.annihilation_blade.specialeffect;

import QWQ.QingYi.annihilationblade.annihilation_blade.logic.TerminusLogic;
import QWQ.QingYi.annihilationblade.annihilation_blade.visual.AnnihilationVisuals;
import QWQ.QingYi.annihilationblade.common.SpecialEffectSupport;
import QWQ.QingYi.annihilationblade.registry.ModSpecialEffects;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.event.SlashBladeEvent.DoSlashEvent;
import mods.flammpfeil.slashblade.registry.specialeffects.SpecialEffect;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = "annihilationblade")
public class AbyssalDecree extends SpecialEffect {
   private static final double RANGE = 34.0;
   private static final int MAX_TARGETS = 16;
   private static final int STRIKE_INTERVAL = 3;
   private static final int COOLDOWN_TICKS = 82;
   private static final double TAU = Math.PI * 2;
   private static final Map<UUID, AbyssalDecree.Sequence> ACTIVE = new HashMap<>();
   private static final Map<UUID, Long> LAST_TRIGGER = new HashMap<>();

   public AbyssalDecree() {
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
            if (state.hasSpecialEffect(ModSpecialEffects.ABYSSAL_DECREE.getId())) {
               if (!ACTIVE.containsKey(player.getUUID())) {
                  long gameTime = player.level().getGameTime();
                  long last = LAST_TRIGGER.getOrDefault(player.getUUID(), -164L);
                  if (gameTime - last >= 82L) {
                     List<UUID> targets = collectTargets(player);
                     if (!targets.isEmpty()) {
                        LAST_TRIGGER.put(player.getUUID(), gameTime);
                        ACTIVE.put(player.getUUID(), new AbyssalDecree.Sequence(targets));
                        Vec3 crown = player.position().add(0.0, player.getBbHeight() + 3.2, 0.0);
                        ServerLevel level = player.serverLevel();
                        spawnCrown(level, crown, 3.6, 0);
                        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2F, 0.55F);
                        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 1.1F, 1.45F);
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
            AbyssalDecree.Sequence sequence = ACTIVE.get(player.getUUID());
            if (sequence != null) {
               sequence.age++;
               ServerLevel level = player.serverLevel();
               Vec3 crown = player.position().add(0.0, player.getBbHeight() + 3.2, 0.0);
               spawnCrown(level, crown, 3.6 + Math.sin(sequence.age * 0.25) * 0.35, sequence.age);
               if (sequence.age % 3 == 0) {
                  while (sequence.index < sequence.targets.size()) {
                     LivingEntity target = findTarget(level, sequence.targets.get(sequence.index++));
                     if (target != null && SpecialEffectSupport.canTarget(player, target)) {
                        strike(level, player, target, crown, sequence.index);
                        return;
                     }
                  }

                  AnnihilationVisuals.spawnCollapsePulse(level, crown, 5.0, sequence.targets.size());
                  level.playSound(null, crown.x, crown.y, crown.z, SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.1F, 0.6F);
                  ACTIVE.remove(player.getUUID());
               }
            }
         }
      }
   }

   private static List<UUID> collectTargets(ServerPlayer player) {
      List<LivingEntity> entities = SpecialEffectSupport.radialTargets(player.serverLevel(), player, player.position(), 34.0);
      entities.sort(
         Comparator.<LivingEntity>comparingDouble(entityx -> entityx.getHealth() + entityx.getArmorValue() * 2.0)
            .reversed()
            .thenComparingDouble(entityx -> entityx.distanceToSqr(player))
      );
      List<UUID> result = new ArrayList<>();

      for (LivingEntity entity : entities) {
         result.add(entity.getUUID());
         if (result.size() >= 16) {
            break;
         }
      }

      return result;
   }

   private static void strike(ServerLevel level, ServerPlayer player, LivingEntity target, Vec3 crown, int index) {
      Vec3 head = target.position().add(0.0, target.getBbHeight() + 0.35, 0.0);
      Vec3 sky = head.add(0.0, 8.0, 0.0);
      double radius = 3.4 + index % 4 * 0.35;
      double angle = (Math.PI * 2) * index / 7.0;
      Vec3 seal = crown.add(Math.cos(angle) * radius, Math.sin(index * 0.7) * 0.45, Math.sin(angle) * radius);
      AnnihilationVisuals.spawnSlashBridge(level, seal, sky, 1.05, player.getRandom());
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
         double angle = (Math.PI * 2) * i / 14.0 + tick * 0.045;
         Vec3 point = center.add(Math.cos(angle) * radius, Math.sin(angle * 3.0 + tick * 0.08) * 0.28, Math.sin(angle) * radius);
         level.sendParticles(ParticleTypes.ENCHANT, point.x, point.y, point.z, 2, 0.025, 0.025, 0.025, 0.0);
         if (i % 2 == 0) {
            level.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z, 1, 0.01, 0.01, 0.01, 0.0);
         }
      }

      level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y, center.z, 10, radius * 0.25, 0.2, radius * 0.25, 0.08);
   }

   private static void spawnVerticalSentence(ServerLevel level, Vec3 start, Vec3 end) {
      for (int i = 0; i <= 24; i++) {
         Vec3 pos = start.lerp(end, i / 24.0);
         level.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 1, 0.015, 0.015, 0.015, 0.0);
         if (i % 3 == 0) {
            level.sendParticles(ParticleTypes.ENCHANT, pos.x, pos.y, pos.z, 3, 0.08, 0.03, 0.08, 0.0);
         }
      }

      level.sendParticles(ParticleTypes.FLASH, end.x, end.y, end.z, 1, 0.0, 0.0, 0.0, 0.0);
      level.sendParticles(ParticleTypes.SONIC_BOOM, end.x, end.y, end.z, 1, 0.0, 0.0, 0.0, 0.0);
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
