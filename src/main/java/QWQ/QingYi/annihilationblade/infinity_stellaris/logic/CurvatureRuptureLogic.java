package QWQ.QingYi.annihilationblade.infinity_stellaris.logic;

import QWQ.QingYi.annihilationblade.common.SlashBladeTargeting;
import QWQ.QingYi.annihilationblade.common.SpecialEffectSupport;
import QWQ.QingYi.annihilationblade.config.ModConfig;
import QWQ.QingYi.annihilationblade.infinity_stellaris.item.InfinityStellarisItemSupport;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = "annihilationblade")
public final class CurvatureRuptureLogic {
   private CurvatureRuptureLogic() {
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase != Phase.END || event.player.level().isClientSide) {
         return;
      }

      Player player = event.player;
      if (InfinityStellarisItemSupport.isHoldingInfinityStellaris(player) && player.level() instanceof ServerLevel level) {
         freezeNearby(level, player);
      }
   }

   public static void clearPlayer(Player player) {
   }

   private static void freezeNearby(ServerLevel level, Player player) {
      ModConfig.InfinityStellaris config = ModConfig.COMMON.infinityStellaris;
      List<LivingEntity> targets = SpecialEffectSupport.radialTargets(
         level, player, player.position(), config.curvatureRadius.get(), entity -> SlashBladeTargeting.canAttack(player, entity)
      );
      long gameTime = level.getGameTime();
      for (LivingEntity target : targets) {
         if (target instanceof Mob mob) {
            freezeMob(mob);
         } else {
            freezeLiving(target);
         }

         if (gameTime % 2L == 0L) {
            Vec3 center = SpecialEffectSupport.centerOf(target);
            level.sendParticles(ParticleTypes.END_ROD, center.x, center.y, center.z, 24, 0.45, 0.55, 0.45, 0.015);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y, center.z, 36, 0.6, 0.6, 0.6, 0.12);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y, center.z, 12, 0.5, 0.5, 0.5, 0.05);
            if (gameTime % 20L == 0L) {
               level.sendParticles(ParticleTypes.SONIC_BOOM, center.x, center.y + 0.5, center.z, 1, 0.0, 0.0, 0.0, 0.0);
               level.playSound(null, center.x, center.y, center.z, SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 0.7F, 1.5F);
            }
         }
      }
   }

   private static void freezeMob(Mob mob) {
      mob.setTarget(null);
      mob.getNavigation().stop();
      mob.setNoAi(true);
      mob.hasImpulse = true;
      mob.setDeltaMovement(Vec3.ZERO);
      mob.fallDistance = 0.0F;
   }

   private static void freezeLiving(LivingEntity target) {
      target.stopRiding();
      target.ejectPassengers();
      target.setDeltaMovement(Vec3.ZERO);
      target.fallDistance = 0.0F;
      target.hasImpulse = true;
   }
}
