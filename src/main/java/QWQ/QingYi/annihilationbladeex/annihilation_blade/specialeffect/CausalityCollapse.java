package QWQ.QingYi.annihilationbladeex.annihilation_blade.specialeffect;

import QWQ.QingYi.annihilationbladeex.annihilation_blade.logic.TerminusLogic;
import QWQ.QingYi.annihilationbladeex.annihilation_blade.visual.AnnihilationVisuals;
import QWQ.QingYi.annihilationbladeex.common.SpecialEffectSupport;
import QWQ.QingYi.annihilationbladeex.config.ModConfig;
import QWQ.QingYi.annihilationbladeex.registry.ModSpecialEffects;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.event.SlashBladeEvent.HitEvent;
import mods.flammpfeil.slashblade.registry.specialeffects.SpecialEffect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = "annihilationbladeex")
public class CausalityCollapse extends SpecialEffect {
   private static final Map<UUID, Long> LAST_TRIGGER = new HashMap<>();

   public CausalityCollapse() {
      super(0, false, false);
   }

   public static void clearPlayer(UUID playerId) {
      LAST_TRIGGER.remove(playerId);
   }

   @SubscribeEvent
   public static void onSlashBladeHit(HitEvent event) {
      ISlashBladeState state = event.getSlashBladeState();
      if (state.hasSpecialEffect(ModSpecialEffects.CAUSALITY_COLLAPSE.getId())) {
         if (event.getUser() instanceof Player player) {
            LivingEntity firstTarget = event.getTarget();
            if (firstTarget != null) {
               if (firstTarget.level() instanceof ServerLevel level) {
                  ModConfig.CausalityCollapse config = ModConfig.COMMON.annihilationBlade.causalityCollapse;
                  if (SpecialEffectSupport.tryStartCooldown(LAST_TRIGGER, player, level.getGameTime(), config.cooldownTicks.get())) {
                     List<LivingEntity> chain = SpecialEffectSupport.nearestChain(level, player, firstTarget, config.chainRadius.get(), config.maxChain.get());
                     if (!chain.isEmpty()) {
                        Vec3 previous = player.getEyePosition();

                        for (int index = 0; index < chain.size(); index++) {
                           LivingEntity target = chain.get(index);
                           Vec3 targetCenter = SpecialEffectSupport.centerOf(target);
                           if (index == 0) {
                              AnnihilationVisuals.spawnCausalityAnchor(level, targetCenter, chain.size());
                           }

                           AnnihilationVisuals.spawnCausalityStep(level, previous, targetCenter, index, player.getRandom());
                           SpecialEffectSupport.pullToward(target, previous, 0.12);
                           AnnihilationVisuals.spawnExecutionBurst(level, target, player.getRandom());
                           TerminusLogic.execute(target, player);
                           previous = targetCenter;
                        }

                        AnnihilationVisuals.spawnCollapsePulse(level, previous, config.chainRadius.get() * 0.55 * config.visualScale.get(), chain.size());
                     }
                  }
               }
            }
         }
      }
   }
}
