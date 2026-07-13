package QWQ.QingYi.annihilationblade.annihilation_blade.specialeffect;

import QWQ.QingYi.annihilationblade.annihilation_blade.logic.TerminusLogic;
import QWQ.QingYi.annihilationblade.annihilation_blade.visual.AnnihilationVisuals;
import QWQ.QingYi.annihilationblade.common.SpecialEffectSupport;
import QWQ.QingYi.annihilationblade.registry.ModSpecialEffects;
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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = "annihilationblade")
public class VoidDominion extends SpecialEffect {
   private static final double RANGE = 26.0;
   private static final int MAX_TARGETS = 64;
   private static final int COOLDOWN_TICKS = 70;
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
               long gameTime = player.level().getGameTime();
               long last = LAST_TRIGGER.getOrDefault(player.getUUID(), -140L);
               if (gameTime - last >= 70L) {
                  LAST_TRIGGER.put(player.getUUID(), gameTime);
                  ServerLevel level = player.serverLevel();
                  Vec3 direction = player.getLookAngle().normalize();
                  Vec3 center = player.getEyePosition().add(direction.scale(10.0));
                  AnnihilationVisuals.spawnOpeningHalo(level, center, 16.12);
                  AnnihilationVisuals.spawnWorldRiftBloom(level, center, 10.92);
                  AnnihilationVisuals.spawnFractureWeb(level, center, 20.28, player.getRandom());
                  int count = 0;

                  for (LivingEntity target : SpecialEffectSupport.radialTargets(level, player, center, 26.0)) {
                     if (count >= 64) {
                        break;
                     }

                     Vec3 targetCenter = SpecialEffectSupport.centerOf(target);
                     AnnihilationVisuals.spawnSlashBridge(level, center, targetCenter, 1.4, player.getRandom());
                     AnnihilationVisuals.spawnExecutionBurst(level, target, player.getRandom());
                     TerminusLogic.execute(target, player);
                     count++;
                  }

                  AnnihilationVisuals.spawnCollapsePulse(level, center, 18.72, count);
               }
            }
         }
      }
   }
}
