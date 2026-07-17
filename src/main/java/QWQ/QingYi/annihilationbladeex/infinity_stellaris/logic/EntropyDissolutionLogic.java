package QWQ.QingYi.annihilationbladeex.infinity_stellaris.logic;

import QWQ.QingYi.annihilationbladeex.common.ServerTickScheduler;
import QWQ.QingYi.annihilationbladeex.common.SlashBladeTargeting;
import QWQ.QingYi.annihilationbladeex.config.ModConfig;
import QWQ.QingYi.annihilationbladeex.infinity_stellaris.item.InfinityStellarisItemSupport;
import QWQ.QingYi.annihilationbladeex.infinity_stellaris.visual.InfinityStellarisVisuals;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = "annihilationbladeex")
public final class EntropyDissolutionLogic {
   private static final float EXECUTION_DAMAGE = 1.0E9F;
   private static final Map<UUID, Map<UUID, Integer>> MARKS = new HashMap<>();
   private static final Map<UUID, Long> BLACKLISTED_UNTIL = new HashMap<>();
   private static final Set<UUID> INTERNAL_EXECUTION = new HashSet<>();

   private EntropyDissolutionLogic() {
   }

   @SubscribeEvent(priority = EventPriority.HIGH)
   public static void onHurt(LivingIncomingDamageEvent event) {
      LivingEntity target = event.getEntity();
      if (target.level().isClientSide || isInternalExecution(target)) {
         return;
      }

      purgeBlacklist(target.level().getGameTime());
      Entity source = event.getSource().getEntity();
      Entity directSource = event.getSource().getDirectEntity();
      if (!(source instanceof Player player) || !isInfinityDamage(player, event.getSource(), directSource)) {
         return;
      }

      if (!SlashBladeTargeting.canAttack(player, target)) {
         return;
      }

      double percent = ModConfig.COMMON.infinityStellaris.entropyPercent.get();
      float entropyDamage = (float)(target.getMaxHealth() * percent);
      float newHealth = Math.max(1.0F, target.getHealth() - entropyDamage);
      target.setHealth(newHealth);
      int marks = addMark(player, target);
      if (marks >= ModConfig.COMMON.infinityStellaris.entropyMarks.get()) {
         clearMark(player, target);
         executeFinal(target, player);
      } else if (target.level() instanceof ServerLevel level) {
         InfinityStellarisVisuals.spawnDamageChain(level, player, target);
         spawnEntropyTrace(level, target, marks);
      }
   }

   public static boolean isInfinityDamage(Player player, DamageSource source, Entity directSource) {
      return InfinityStellarisItemSupport.isDirectInfinityAttack(player, source)
         || InfinityStellarisItemSupport.isInfinitySlashEntityAttack(player, directSource);
   }

   public static boolean isInternalExecution(LivingEntity target) {
      return target != null && INTERNAL_EXECUTION.contains(target.getUUID());
   }

   public static void executeFinal(LivingEntity target, Player attacker) {
      if (target.level().isClientSide || !SlashBladeTargeting.canAttack(attacker, target)) {
         return;
      }

      target.invulnerableTime = 0;
      DamageSource source = target.level().damageSources().playerAttack(attacker);
      if (target.level() instanceof ServerLevel level) {
         InfinityStellarisVisuals.spawnDamageChain(level, attacker, target);
         spawnHeatDeath(level, target);
      }

      INTERNAL_EXECUTION.add(target.getUUID());
      try {
         if (target instanceof Player) {
            target.hurt(source, EXECUTION_DAMAGE);
            if (target.isAlive()) {
               target.invulnerableTime = 0;
               target.setHealth(0.0F);
               target.die(source);
            }
         } else {
            disableNonPlayerTarget(target);
            target.hurt(source, EXECUTION_DAMAGE);
            if (target.isAlive()) {
               target.invulnerableTime = 0;
               target.setHealth(0.0F);
               target.die(source);
            }

            eraseNonPlayerTarget(target);
            blacklist(target);
            ServerTickScheduler.schedule(1, () -> eraseNonPlayerTarget(target));
         }
      } finally {
         INTERNAL_EXECUTION.remove(target.getUUID());
      }
   }

   public static void clearPlayer(UUID playerId) {
      MARKS.remove(playerId);
   }

   private static int addMark(Player player, LivingEntity target) {
      Map<UUID, Integer> playerMarks = MARKS.computeIfAbsent(player.getUUID(), ignored -> new HashMap<>());
      int marks = playerMarks.getOrDefault(target.getUUID(), 0) + 1;
      playerMarks.put(target.getUUID(), marks);
      return marks;
   }

   private static void clearMark(Player player, LivingEntity target) {
      Map<UUID, Integer> playerMarks = MARKS.get(player.getUUID());
      if (playerMarks != null) {
         playerMarks.remove(target.getUUID());
         if (playerMarks.isEmpty()) {
            MARKS.remove(player.getUUID());
         }
      }
   }

   private static void disableNonPlayerTarget(LivingEntity target) {
      target.stopRiding();
      target.ejectPassengers();
      target.setDeltaMovement(Vec3.ZERO);
      target.hasImpulse = true;
      if (target instanceof Mob mob) {
         mob.setTarget(null);
         mob.getNavigation().stop();
         mob.setNoAi(true);
      }
   }

   private static void eraseNonPlayerTarget(LivingEntity target) {
      if (target instanceof Player) {
         return;
      }

      disableNonPlayerTarget(target);
      target.remove(Entity.RemovalReason.KILLED);
      target.discard();
   }

   private static void blacklist(LivingEntity target) {
      long expiry = target.level().getGameTime() + ModConfig.COMMON.infinityStellaris.entropyBlacklistTicks.get();
      BLACKLISTED_UNTIL.put(target.getUUID(), expiry);
   }

   private static void purgeBlacklist(long gameTime) {
      Iterator<Map.Entry<UUID, Long>> iterator = BLACKLISTED_UNTIL.entrySet().iterator();
      while (iterator.hasNext()) {
         if (iterator.next().getValue() <= gameTime) {
            iterator.remove();
         }
      }
   }

   private static void spawnEntropyTrace(ServerLevel level, LivingEntity target, int marks) {
      Vec3 center = target.position().add(0.0, target.getBbHeight() * 0.55, 0.0);
      double radius = Math.max(0.7, target.getBbWidth() * (1.0 + marks * 0.04));
      level.sendParticles(ParticleTypes.END_ROD, center.x, center.y, center.z, 6 + marks, radius * 0.35, radius * 0.35, radius * 0.35, 0.01);
      level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y, center.z, 10 + marks, radius * 0.45, radius * 0.45, radius * 0.45, 0.08);
   }

   private static void spawnHeatDeath(ServerLevel level, LivingEntity target) {
      Vec3 center = target.position().add(0.0, target.getBbHeight() * 0.55, 0.0);
      double radius = Math.max(1.0, target.getBbWidth() * 2.0);
      level.playSound(null, center.x, center.y, center.z, SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 2.0F, 0.45F);
      level.playSound(null, center.x, center.y, center.z, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.5F, 0.8F);
      level.playSound(null, center.x, center.y, center.z, SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 1.0F, 1.0F);
      level.sendParticles(ParticleTypes.FLASH, center.x, center.y, center.z, 2, 0.0, 0.0, 0.0, 0.0);
      level.sendParticles(ParticleTypes.END_ROD, center.x, center.y, center.z, 80, radius * 0.5, radius * 0.8, radius * 0.5, 0.1);
      level.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y, center.z, 64, radius * 1.0, radius * 1.0, radius * 1.0, 0.2);
      level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y, center.z, 160, radius * 1.2, radius * 1.2, radius * 1.2, 0.5);
      level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, center.x, center.y, center.z, 100, radius * 0.8, radius * 0.8, radius * 0.8, 0.05);
   }
}
