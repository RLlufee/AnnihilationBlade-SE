package QWQ.QingYi.annihilationblade.annihilation_blade.specialeffect;

import QWQ.QingYi.annihilationblade.annihilation_blade.logic.TerminusLogic;
import QWQ.QingYi.annihilationblade.annihilation_blade.visual.AnnihilationVisuals;
import QWQ.QingYi.annihilationblade.common.SpecialEffectSupport;
import QWQ.QingYi.annihilationblade.config.ModConfig;
import QWQ.QingYi.annihilationblade.registry.ModSpecialEffects;
import java.util.HashMap;
import java.util.List;
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
public class StarlessJudgement extends SpecialEffect {
   private static final Map<UUID, Long> LAST_TRIGGER = new HashMap<>();

   public StarlessJudgement() {
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
            if (state.hasSpecialEffect(ModSpecialEffects.STARLESS_JUDGEMENT.getId())) {
               ModConfig.StarlessJudgement config = ModConfig.COMMON.annihilationBlade.starlessJudgement;
               if (SpecialEffectSupport.tryStartCooldown(LAST_TRIGGER, player, player.level().getGameTime(), config.cooldownTicks.get())) {
                  ServerLevel level = player.serverLevel();
                  double range = config.range.get();
                  double width = config.width.get();
                  double visualScale = config.visualScale.get();
                  Vec3 direction = player.getLookAngle().normalize();
                  Vec3 right = SpecialEffectSupport.rightOf(direction);
                  Vec3 start = player.getEyePosition().add(direction.scale(1.4));
                  Vec3 end = start.add(direction.scale(range));
                  AnnihilationVisuals.spawnStarlessJudgementCast(level, start, direction, right, range * visualScale, width * visualScale);
                  List<LivingEntity> targets = SpecialEffectSupport.beamTargets(level, player, start, direction, range, width, config.maxTargets.get());

                  for (int index = 0; index < targets.size(); index++) {
                     LivingEntity target = targets.get(index);
                     Vec3 targetCenter = SpecialEffectSupport.centerOf(target);
                     double projection = targetCenter.subtract(start).dot(direction);
                     Vec3 nearest = start.add(direction.scale(Math.max(0.0, Math.min(range, projection))));
                     AnnihilationVisuals.spawnSlashBridge(level, nearest, targetCenter, (1.1 + index * 0.01) * visualScale, player.getRandom());
                     SpecialEffectSupport.pullToward(target, nearest, 0.1);
                     AnnihilationVisuals.spawnExecutionBurst(level, target, player.getRandom());
                     TerminusLogic.execute(target, player);
                  }

                  AnnihilationVisuals.spawnCollapsePulse(level, end, width * visualScale, targets.size());
               }
            }
         }
      }
   }
}
