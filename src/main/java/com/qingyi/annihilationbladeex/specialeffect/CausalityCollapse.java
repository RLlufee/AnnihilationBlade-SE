package com.qingyi.annihilationbladeex.specialeffect;

import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import mods.flammpfeil.slashblade.registry.specialeffects.SpecialEffect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import com.qingyi.annihilationbladeex.AnnihilationBladeEX;
import com.qingyi.annihilationbladeex.TerminusLogic;
import com.qingyi.annihilationbladeex.ModSpecialEffects;
import com.qingyi.annihilationbladeex.visual.AnnihilationVisuals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = AnnihilationBladeEX.MODID)
public class CausalityCollapse extends SpecialEffect {
    private static final double CHAIN_RADIUS = 14.0D;
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
    public static void onSlashBladeHit(SlashBladeEvent.HitEvent event) {
        ISlashBladeState state = event.getSlashBladeState();
        if (!state.hasSpecialEffect(ModSpecialEffects.CAUSALITY_COLLAPSE.getId())) return;
        if (!(event.getUser() instanceof Player player)) return;
        LivingEntity firstTarget = event.getTarget();
        if (firstTarget == null) return;
        if (!(firstTarget.level() instanceof ServerLevel level)) return;
        if (!SpecialEffectSupport.tryStartCooldown(LAST_TRIGGER, player, level.getGameTime(), COOLDOWN_TICKS)) return;

        List<LivingEntity> chain = SpecialEffectSupport.nearestChain(level, player, firstTarget, CHAIN_RADIUS, MAX_CHAIN);
        if (chain.isEmpty()) return;

        Vec3 previous = player.getEyePosition();
        for (int index = 0; index < chain.size(); index++) {
            LivingEntity target = chain.get(index);
            Vec3 targetCenter = SpecialEffectSupport.centerOf(target);

            if (index == 0) {
                AnnihilationVisuals.spawnCausalityAnchor(level, targetCenter, chain.size());
            }
            AnnihilationVisuals.spawnCausalityStep(level, previous, targetCenter, index, player.getRandom());
            SpecialEffectSupport.pullToward(target, previous, 0.12D);
            AnnihilationVisuals.spawnExecutionBurst(level, target, player.getRandom());
            TerminusLogic.execute(target, player);
            previous = targetCenter;
        }

        AnnihilationVisuals.spawnCollapsePulse(level, previous, CHAIN_RADIUS * 0.55D, chain.size());
    }
}
