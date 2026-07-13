package QWQ.QingYi.annihilationblade.annihilation_blade.specialeffect;

import QWQ.QingYi.annihilationblade.annihilation_blade.logic.TerminusLogic;
import QWQ.QingYi.annihilationblade.annihilation_blade.visual.AnnihilationVisuals;
import QWQ.QingYi.annihilationblade.common.SpecialEffectSupport;
import QWQ.QingYi.annihilationblade.registry.ModSpecialEffects;
import java.util.List;
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
public class WorldRift extends SpecialEffect {
   private static final double RADIUS = 8.0;
   private static final int MAX_TARGETS = 24;

   public WorldRift() {
      super(0, false, false);
   }

   @SubscribeEvent
   public static void onSlashBladeHit(HitEvent event) {
      ISlashBladeState state = event.getSlashBladeState();
      if (state.hasSpecialEffect(ModSpecialEffects.WORLD_RIFT.getId())) {
         if (event.getUser() instanceof Player player) {
            LivingEntity target = event.getTarget();
            if (target != null) {
               if (target.level() instanceof ServerLevel level) {
                  Vec3 var12 = SpecialEffectSupport.centerOf(target);
                  List<LivingEntity> targets = SpecialEffectSupport.radialTargets(level, player, var12, 8.0);
                  if (!targets.isEmpty()) {
                     AnnihilationVisuals.spawnWorldRiftOpening(level, var12, 8.0);
                     int count = 0;

                     for (LivingEntity entity : targets) {
                        if (count >= 24) {
                           break;
                        }

                        Vec3 entityCenter = SpecialEffectSupport.centerOf(entity);
                        AnnihilationVisuals.spawnWorldRiftThread(level, var12, entityCenter, count, player.getRandom());
                        SpecialEffectSupport.pullToward(entity, var12, 0.18);
                        AnnihilationVisuals.spawnExecutionBurst(level, entity, player.getRandom());
                        TerminusLogic.execute(entity, player);
                        count++;
                     }

                     AnnihilationVisuals.spawnCollapsePulse(level, var12, 5.76, count);
                  }
               }
            }
         }
      }
   }
}
