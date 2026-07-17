package QWQ.QingYi.annihilationbladeex.annihilation_blade.specialeffect;

import QWQ.QingYi.annihilationbladeex.annihilation_blade.logic.TerminusLogic;
import QWQ.QingYi.annihilationbladeex.annihilation_blade.visual.AnnihilationVisuals;
import QWQ.QingYi.annihilationbladeex.common.SpecialEffectSupport;
import QWQ.QingYi.annihilationbladeex.config.ModConfig;
import QWQ.QingYi.annihilationbladeex.registry.ModSpecialEffects;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.event.SlashBladeEvent.DoSlashEvent;
import mods.flammpfeil.slashblade.registry.specialeffects.SpecialEffect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = "annihilationbladeex")
public class VoidDominion extends SpecialEffect {
   private static final Map<UUID, Long> LAST_TRIGGER = new HashMap<>();

   public VoidDominion() {
      super(0, false, false);
   }

   public static void clearPlayer(UUID playerId) {
      LAST_TRIGGER.remove(playerId);
   }

   @SubscribeEvent
   public static void onDoingSlash(DoSlashEvent event) {
      if (event.getUser() instanceof ServerPlayer player) {
         if (!Dankong.isActive(player)) {
            ISlashBladeState state = event.getSlashBladeState();
            if (state.hasSpecialEffect(ModSpecialEffects.VOID_DOMINION.getId())) {
               ModConfig.VoidDominion config = ModConfig.COMMON.annihilationBlade.voidDominion;
               long gameTime = player.level().getGameTime();
               long last = LAST_TRIGGER.getOrDefault(player.getUUID(), -config.cooldownTicks.get() * 2L);
               if (gameTime - last >= config.cooldownTicks.get()) {
                  LAST_TRIGGER.put(player.getUUID(), gameTime);
                  ServerLevel level = player.serverLevel();
                  double range = config.range.get();
                  double visualScale = config.visualScale.get();
                  Vec3 direction = player.getLookAngle().normalize();
                  Vec3 center = player.getEyePosition().add(direction.scale(10.0));
                  AnnihilationVisuals.spawnOpeningHalo(level, center, range * 0.62 * visualScale);
                  AnnihilationVisuals.spawnWorldRiftBloom(level, center, range * 0.42 * visualScale);
                  AnnihilationVisuals.spawnFractureWeb(level, center, range * 0.78 * visualScale, player.getRandom());
                  int count = 0;

                  for (LivingEntity target : SpecialEffectSupport.radialTargets(level, player, center, range)) {
                     if (count >= config.maxTargets.get()) {
                        break;
                     }

                     Vec3 targetCenter = SpecialEffectSupport.centerOf(target);
                     AnnihilationVisuals.spawnSlashBridge(level, center, targetCenter, 1.4, player.getRandom());
                     AnnihilationVisuals.spawnExecutionBurst(level, target, player.getRandom());
                     TerminusLogic.execute(target, player);
                     count++;
                  }

                  AnnihilationVisuals.spawnCollapsePulse(level, center, range * 0.72 * visualScale, count);
               }
            }
         }
      }
   }
}
