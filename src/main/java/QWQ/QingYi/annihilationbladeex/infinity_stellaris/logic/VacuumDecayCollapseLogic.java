package QWQ.QingYi.annihilationbladeex.infinity_stellaris.logic;

import QWQ.QingYi.annihilationbladeex.common.ServerTickScheduler;
import QWQ.QingYi.annihilationbladeex.common.SlashBladeTargeting;
import QWQ.QingYi.annihilationbladeex.infinity_stellaris.InfinityStellarisDefinitions;
import QWQ.QingYi.annihilationbladeex.infinity_stellaris.item.InfinityStellarisItemSupport;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = "annihilationbladeex")
public final class VacuumDecayCollapseLogic {
   private static final String NO_DROPS_TAG = "AnnihilationBladeAbsoluteAnnihilationNoDrops";
   private static final int ZONE_DURATION_TICKS = 100;
   private static final double ZONE_HALF_SIZE = 64.0;
   private static final double ZONE_HEIGHT = 64.0;
   private static final double CAST_RANGE = 512.0;
   private static final double FALLBACK_RANGE = 128.0;
   private static final int BOLTS_PER_TICK = 12;

   private VacuumDecayCollapseLogic() {
   }

   public static void prepareCast(Player player) {
      ItemStack blade = InfinityStellarisItemSupport.heldInfinityStellaris(player);
      if (!blade.isEmpty()) {
         InfinityStellarisDefinitions.ensureStats(blade, player.level());
      }
   }

   public static void unleash(Player player) {
      if (!(player.level() instanceof ServerLevel level) || !InfinityStellarisItemSupport.isHoldingInfinityStellaris(player)) {
         return;
      }

      Vec3 center = findCastCenter(level, player);
      GammaThunderburstLogic.trigger(player);
      spawnZoneOpening(level, center);
      tickZone(new AnnihilationZone(level, player, center));
   }

   @SubscribeEvent
   public static void onLivingDrops(LivingDropsEvent event) {
      if (event.getEntity().getPersistentData().getBoolean(NO_DROPS_TAG)) {
         event.getDrops().clear();
      }
   }

   @SubscribeEvent
   public static void onLivingExperienceDrop(LivingExperienceDropEvent event) {
      if (event.getEntity().getPersistentData().getBoolean(NO_DROPS_TAG)) {
         event.setDroppedExperience(0);
      }
   }

   private static Vec3 findCastCenter(ServerLevel level, Player player) {
      Vec3 start = player.getEyePosition();
      Vec3 look = player.getLookAngle();
      Vec3 end = start.add(look.scale(CAST_RANGE));
      BlockHitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
      if (hit.getType() == HitResult.Type.BLOCK) {
         BlockPos pos = hit.getBlockPos();
         return Vec3.atCenterOf(pos);
      }

      return start.add(look.scale(FALLBACK_RANGE));
   }

   private static void tickZone(AnnihilationZone zone) {
      if (!zone.player.isAlive()) {
         return;
      }

      scanAndExecute(zone);
      spawnZoneFrame(zone.level, zone.center, zone.age);
      GammaThunderburstLogic.spawnRandomBoltsInSquare(zone.level, zone.center, ZONE_HALF_SIZE, BOLTS_PER_TICK, zone.player.getRandom());
      zone.age++;
      if (zone.age < ZONE_DURATION_TICKS) {
         ServerTickScheduler.schedule(1, () -> tickZone(zone));
      }
   }

   private static void scanAndExecute(AnnihilationZone zone) {
      AABB killBox = AABB.ofSize(zone.center, ZONE_HALF_SIZE * 2.0, ZONE_HEIGHT, ZONE_HALF_SIZE * 2.0);
      List<LivingEntity> targets = zone.level.getEntitiesOfClass(LivingEntity.class, killBox, entity -> SlashBladeTargeting.canAttack(zone.player, entity));
      for (LivingEntity target : targets) {
         target.getPersistentData().putBoolean(NO_DROPS_TAG, true);
         EntropyDissolutionLogic.executeFinal(target, zone.player);
      }
   }

   private static void spawnZoneOpening(ServerLevel level, Vec3 center) {
      level.playSound(null, center.x, center.y, center.z, SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 6.0F, 0.35F);
      level.playSound(null, center.x, center.y, center.z, SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.PLAYERS, 5.0F, 0.35F);
      level.playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 5.0F, 0.65F);
      level.playSound(null, center.x, center.y, center.z, SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 4.0F, 0.45F);
      level.sendParticles(ParticleTypes.FLASH, center.x, center.y, center.z, 12, 0.0, 0.0, 0.0, 0.0);
      level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y, center.z, 1600, ZONE_HALF_SIZE * 0.65, ZONE_HEIGHT * 0.35, ZONE_HALF_SIZE * 0.65, 1.1);
      level.sendParticles(ParticleTypes.END_ROD, center.x, center.y, center.z, 700, ZONE_HALF_SIZE * 0.45, ZONE_HEIGHT * 0.25, ZONE_HALF_SIZE * 0.45, 0.28);
      level.sendParticles(ParticleTypes.SONIC_BOOM, center.x, center.y + 0.8, center.z, 8, 0.0, 0.0, 0.0, 0.0);
      level.sendParticles(ParticleTypes.DRAGON_BREATH, center.x, center.y, center.z, 500, ZONE_HALF_SIZE * 0.45, ZONE_HEIGHT * 0.25, ZONE_HALF_SIZE * 0.45, 0.18);
      level.sendParticles(ParticleTypes.SQUID_INK, center.x, center.y, center.z, 360, ZONE_HALF_SIZE * 0.5, ZONE_HEIGHT * 0.2, ZONE_HALF_SIZE * 0.5, 0.16);
   }

   private static void spawnZoneFrame(ServerLevel level, Vec3 center, int age) {
      if (age % 20 == 0) {
         level.playSound(null, center.x, center.y, center.z, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 2.0F, 0.55F + (age % 40) * 0.01F);
      }

      spawnSquare(level, center, ZONE_HALF_SIZE, age);
      spawnCornerPillars(level, center, ZONE_HALF_SIZE, ZONE_HEIGHT, age);
      level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y, center.z, 80, 2.0, 1.2, 2.0, 0.45);
      level.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y + Math.sin(age * 0.25) * 2.0, center.z, 36, 3.0, 1.4, 3.0, 0.18);
      if (age % 5 == 0) {
         level.sendParticles(ParticleTypes.SONIC_BOOM, center.x, center.y + 1.0, center.z, 1, 0.0, 0.0, 0.0, 0.0);
      }
   }

   private static void spawnSquare(ServerLevel level, Vec3 center, double halfSize, int age) {
      double y = center.y + 0.12 + Math.sin(age * 0.35) * 0.35;
      int samples = 32;
      for (int i = 0; i <= samples; i++) {
         double offset = -halfSize + halfSize * 2.0 * i / samples;
         spawnBoundaryPoint(level, center.x + offset, y, center.z - halfSize);
         spawnBoundaryPoint(level, center.x + offset, y, center.z + halfSize);
         spawnBoundaryPoint(level, center.x - halfSize, y, center.z + offset);
         spawnBoundaryPoint(level, center.x + halfSize, y, center.z + offset);
      }
   }

   private static void spawnBoundaryPoint(ServerLevel level, double x, double y, double z) {
      level.sendParticles(ParticleTypes.END_ROD, x, y, z, 1, 0.03, 0.03, 0.03, 0.0);
      level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 1, 0.05, 0.05, 0.05, 0.02);
   }

   private static void spawnCornerPillars(ServerLevel level, Vec3 center, double halfSize, double height, int age) {
      double[] xs = new double[]{center.x - halfSize, center.x + halfSize};
      double[] zs = new double[]{center.z - halfSize, center.z + halfSize};
      for (double x : xs) {
         for (double z : zs) {
            for (int step = 0; step <= 8; step++) {
               double y = center.y - height * 0.5 + height * step / 8.0;
               level.sendParticles(ParticleTypes.REVERSE_PORTAL, x, y, z, 2, 0.08, 0.08, 0.08, 0.1);
               if ((step + age) % 2 == 0) {
                  level.sendParticles(ParticleTypes.END_ROD, x, y, z, 1, 0.04, 0.04, 0.04, 0.0);
               }
            }
         }
      }
   }

   private static final class AnnihilationZone {
      private final ServerLevel level;
      private final Player player;
      private final Vec3 center;
      private int age;

      private AnnihilationZone(ServerLevel level, Player player, Vec3 center) {
         this.level = level;
         this.player = player;
         this.center = center;
      }
   }
}
