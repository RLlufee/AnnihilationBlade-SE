package QWQ.QingYi.annihilationblade.annihilation_blade.specialeffect;

import QWQ.QingYi.annihilationblade.annihilation_blade.logic.TerminusLogic;
import QWQ.QingYi.annihilationblade.annihilation_blade.visual.AnnihilationVisuals;
import QWQ.QingYi.annihilationblade.common.SpecialEffectSupport;
import QWQ.QingYi.annihilationblade.registry.ModSpecialEffects;
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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = "annihilationblade")
public class CausalityCollapse extends SpecialEffect {
   private static final double CHAIN_RADIUS = 14.0;
   private static final int MAX_CHAIN = 18;
   private static final int COOLDOWN_TICKS = 10;
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
                  if (SpecialEffectSupport.tryStartCooldown(LAST_TRIGGER, player, level.getGameTime(), 10)) {
                     List<LivingEntity> chain = SpecialEffectSupport.nearestChain(level, player, firstTarget, 14.0, 18);
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

                        AnnihilationVisuals.spawnCollapsePulse(level, previous, 7.700000000000001, chain.size());
                     }
                  }
               }
            }
         }
      }
   }
}
