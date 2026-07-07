package org.examplea.annihilationblade.specialeffect;

import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import mods.flammpfeil.slashblade.registry.specialeffects.SpecialEffect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.examplea.annihilationblade.Annihilationblade;
import org.examplea.annihilationblade.logic.TerminusLogic;
import org.examplea.annihilationblade.registry.ModSpecialEffects;
import org.examplea.annihilationblade.visual.AnnihilationVisuals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Annihilationblade.MODID)
public class StarlessJudgement extends SpecialEffect {
    private static final double RANGE = 56.0D;
    private static final double WIDTH = 8.5D;
    private static final int COOLDOWN_TICKS = 34;
    private static final int MAX_TARGETS = 80;
    private static final Map<UUID, Long> LAST_TRIGGER = new HashMap<>();

    public StarlessJudgement() {
        super(0, false, false);
    }

    @SubscribeEvent
    public static void onDoingSlash(SlashBladeEvent.DoSlashEvent event) {
        if (!(event.getUser() instanceof ServerPlayer player)) return;
        if (Dankong.isActive(player)) return;

        ISlashBladeState state = event.getSlashBladeState();
        if (!state.hasSpecialEffect(ModSpecialEffects.STARLESS_JUDGEMENT.getId())) return;
        if (!SpecialEffectSupport.tryStartCooldown(LAST_TRIGGER, player, player.level().getGameTime(), COOLDOWN_TICKS)) return;

        ServerLevel level = player.serverLevel();
        Vec3 direction = player.getLookAngle().normalize();
        Vec3 right = SpecialEffectSupport.rightOf(direction);
        Vec3 start = player.getEyePosition().add(direction.scale(1.4D));
        Vec3 end = start.add(direction.scale(RANGE));

        AnnihilationVisuals.spawnStarlessJudgementCast(level, start, direction, right, RANGE, WIDTH);

        List<LivingEntity> targets = SpecialEffectSupport.beamTargets(level, player, start, direction, RANGE, WIDTH, MAX_TARGETS);
        for (int index = 0; index < targets.size(); index++) {
            LivingEntity target = targets.get(index);
            Vec3 targetCenter = SpecialEffectSupport.centerOf(target);
            double projection = targetCenter.subtract(start).dot(direction);
            Vec3 nearest = start.add(direction.scale(Math.max(0.0D, Math.min(RANGE, projection))));

            AnnihilationVisuals.spawnSlashBridge(level, nearest, targetCenter, 1.1D + index * 0.01D, player.getRandom());
            SpecialEffectSupport.pullToward(target, nearest, 0.1D);
            AnnihilationVisuals.spawnExecutionBurst(level, target, player.getRandom());
            TerminusLogic.execute(target, player);
        }

        AnnihilationVisuals.spawnCollapsePulse(level, end, WIDTH, targets.size());
    }
}
