package QWQ.QingYi.annihilationbladeex.infinity_stellaris.logic;

import QWQ.QingYi.annihilationbladeex.infinity_stellaris.InfinityStellarisDefinitions;
import QWQ.QingYi.annihilationbladeex.infinity_stellaris.item.InfinityStellarisItemSupport;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = "annihilationbladeex")
public final class InfinityStellarisLogic {
   private static final int BLADE_REFRESH_INTERVAL = 20;
   private static final Set<UUID> PLAYERS_WITH_FLIGHT = new HashSet<>();

   private InfinityStellarisLogic() {
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onLivingAttack(LivingIncomingDamageEvent event) {
      if (!event.getEntity().level().isClientSide && event.getEntity() instanceof Player player
            && InfinityStellarisItemSupport.hasInfinityStellarisInInventory(player)) {
         event.setCanceled(true);
         restoreFinalWeaponBearer(player);
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onLivingHurt(LivingIncomingDamageEvent event) {
      if (!event.getEntity().level().isClientSide && event.getEntity() instanceof Player player
            && InfinityStellarisItemSupport.hasInfinityStellarisInInventory(player)) {
         event.setCanceled(true);
         restoreFinalWeaponBearer(player);
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onLivingDeath(LivingDeathEvent event) {
      if (!event.getEntity().level().isClientSide && event.getEntity() instanceof Player player
            && InfinityStellarisItemSupport.hasInfinityStellarisInInventory(player)) {
         event.setCanceled(true);
         restoreFinalWeaponBearer(player);
         player.deathTime = 0;
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent.Post event) {
      Player player = event.getEntity();
      if (player.level().isClientSide) {
         return;
      }
      UUID id = player.getUUID();
      boolean holding = InfinityStellarisItemSupport.isHoldingInfinityStellaris(player);
      boolean inInventory = InfinityStellarisItemSupport.hasInfinityStellarisInInventory(player);

      if (holding) {
         if (player.tickCount % BLADE_REFRESH_INTERVAL == 0) {
            refreshHeldStats(player);
         }

         // 顶尖常驻视觉特效 (Held visual effects)
         if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            if (player.tickCount % 2 == 0) {
               double px = player.getX();
               double py = player.getY() + player.getBbHeight() / 2.0;
               double pz = player.getZ();
               // 虚空粒子环绕
               serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.DRAGON_BREATH, px, py, pz, 3, 0.5,
                     0.5, 0.5, 0.02);
               serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT, px, py, pz, 5, 0.8, 0.8,
                     0.8, 0.1);
               serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD, px, player.getY(), pz, 1,
                     0.6, 0.1, 0.6, 0.01);
            }
         }
      }

      if (inInventory) {
         if (player.getHealth() < player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
         }

         player.getFoodData().setFoodLevel(20);
         player.getFoodData().setSaturation(20.0F);
         player.fallDistance = 0.0F;
         if (!player.isCreative() && !player.isSpectator() && !player.getAbilities().mayfly) {
            player.getAbilities().mayfly = true;
            PLAYERS_WITH_FLIGHT.add(id);
            player.onUpdateAbilities();
         }

         if (player.getY() < -64.0) {
            player.teleportTo(player.getX(), 320.0, player.getZ());
            player.setDeltaMovement(0.0, 0.0, 0.0);
            if (!player.getAbilities().flying) {
               player.getAbilities().flying = true;
               player.onUpdateAbilities();
            }
         }
      } else if (!player.isCreative() && !player.isSpectator() && PLAYERS_WITH_FLIGHT.remove(id)) {
         player.getAbilities().mayfly = false;
         player.getAbilities().flying = false;
         player.onUpdateAbilities();
      }
   }

   @SubscribeEvent
   public static void onPlayerLogout(PlayerLoggedOutEvent event) {
      clearPlayer(event.getEntity());
   }

   @SubscribeEvent
   public static void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
      clearPlayer(event.getEntity());
   }

   private static void refreshHeldStats(Player player) {
      if (InfinityStellarisItemSupport.isInfinityStellaris(player.getMainHandItem())) {
         InfinityStellarisDefinitions.ensureStats(player.getMainHandItem(), player.level());
      }

      if (InfinityStellarisItemSupport.isInfinityStellaris(player.getOffhandItem())) {
         InfinityStellarisDefinitions.ensureStats(player.getOffhandItem(), player.level());
      }
   }

   private static void clearPlayer(Player player) {
      UUID id = player.getUUID();
      if (!player.isCreative() && !player.isSpectator() && PLAYERS_WITH_FLIGHT.remove(id)) {
         player.getAbilities().mayfly = false;
         player.getAbilities().flying = false;
         player.onUpdateAbilities();
      }

      EntropyDissolutionLogic.clearPlayer(id);
      CosmicStringCutLogic.clearPlayer(id);
      CurvatureRuptureLogic.clearPlayer(player);
   }

   private static void restoreFinalWeaponBearer(Player player) {
      player.invulnerableTime = Math.max(player.invulnerableTime, 20);
      player.setHealth(player.getMaxHealth());
      player.getFoodData().setFoodLevel(20);
      player.getFoodData().setSaturation(20.0F);
      player.deathTime = 0;
   }
}
