package org.examplea.annihilationblade.specialeffect;

import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import mods.flammpfeil.slashblade.registry.specialeffects.SpecialEffect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.examplea.annihilationblade.Annihilationblade;
import org.examplea.annihilationblade.logic.TerminusLogic;
import org.examplea.annihilationblade.registry.ModSpecialEffects;
import org.examplea.annihilationblade.visual.AnnihilationVisuals;

import java.util.List;

@Mod.EventBusSubscriber(modid = Annihilationblade.MODID)
public class WorldRift extends SpecialEffect {
    private static final double RADIUS = 8.0D;
    private static final int MAX_TARGETS = 24;

    public WorldRift() {
        super(0, false, false);
    }

    @SubscribeEvent
    public static void onSlashBladeHit(SlashBladeEvent.HitEvent event) {
        ISlashBladeState state = event.getSlashBladeState();
        if (!state.hasSpecialEffect(ModSpecialEffects.WORLD_RIFT.getId())) return;
        if (!(event.getUser() instanceof Player player)) return;
        LivingEntity target = event.getTarget();
        if (target == null) return;
        if (!(target.level() instanceof ServerLevel level)) return;

        Vec3 center = SpecialEffectSupport.centerOf(target);
        List<LivingEntity> targets = SpecialEffectSupport.radialTargets(level, player, center, RADIUS);
        if (targets.isEmpty()) return;

        AnnihilationVisuals.spawnWorldRiftOpening(level, center, RADIUS);

        int count = 0;
        for (LivingEntity entity : targets) {
            if (count >= MAX_TARGETS) break;

            Vec3 entityCenter = SpecialEffectSupport.centerOf(entity);
            AnnihilationVisuals.spawnWorldRiftThread(level, center, entityCenter, count, player.getRandom());
            SpecialEffectSupport.pullToward(entity, center, 0.18D);
            AnnihilationVisuals.spawnExecutionBurst(level, entity, player.getRandom());
            TerminusLogic.execute(entity, player);
            count++;
        }

        AnnihilationVisuals.spawnCollapsePulse(level, center, RADIUS * 0.72D, count);
    }
}
