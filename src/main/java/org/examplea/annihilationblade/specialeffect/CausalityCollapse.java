package org.examplea.annihilationblade.specialeffect;

import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import mods.flammpfeil.slashblade.registry.specialeffects.SpecialEffect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.examplea.annihilationblade.Annihilationblade;
import org.examplea.annihilationblade.item.ItemAnnihilationBlade;
import org.examplea.annihilationblade.logic.TerminusLogic;
import org.examplea.annihilationblade.registry.ModSpecialEffects;
import org.examplea.annihilationblade.visual.AnnihilationVisuals;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Annihilationblade.MODID)
public class CausalityCollapse extends SpecialEffect {
    private static final double CHAIN_RADIUS = 14.0D;
    private static final int MAX_CHAIN = 18;
    private static final int COOLDOWN_TICKS = 10;
    private static final Map<UUID, Long> LAST_TRIGGER = new HashMap<>();

    public CausalityCollapse() {
        super(0, false, false);
    }

    @SubscribeEvent
    public static void onSlashBladeHit(SlashBladeEvent.HitEvent event) {
        ISlashBladeState state = event.getSlashBladeState();
        if (!state.hasSpecialEffect(ModSpecialEffects.CAUSALITY_COLLAPSE.getId())) return;
        if (!(event.getUser() instanceof Player player)) return;

        Entity rawTarget = event.getTarget();
        if (!(rawTarget instanceof LivingEntity firstTarget)) return;
        if (!(firstTarget.level() instanceof ServerLevel level)) return;

        long gameTime = level.getGameTime();
        long last = LAST_TRIGGER.getOrDefault(player.getUUID(), -COOLDOWN_TICKS * 2L);
        if (gameTime - last < COOLDOWN_TICKS) return;
        LAST_TRIGGER.put(player.getUUID(), gameTime);

        List<LivingEntity> chain = buildChain(level, player, firstTarget);
        Vec3 previous = player.getEyePosition();
        int index = 0;

        for (LivingEntity target : chain) {
            Vec3 targetCenter = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
            AnnihilationVisuals.spawnSlashBridge(level, previous, targetCenter, 0.9D + index * 0.06D, player.getRandom());
            AnnihilationVisuals.spawnExecutionBurst(level, target, player.getRandom());
            TerminusLogic.execute(target, player);
            previous = targetCenter;
            index++;
        }

        if (!chain.isEmpty()) {
            AnnihilationVisuals.spawnCollapsePulse(level, previous, CHAIN_RADIUS * 0.55D, chain.size());
        }
    }

    private static List<LivingEntity> buildChain(ServerLevel level, Player player, LivingEntity firstTarget) {
        List<LivingEntity> chain = new ArrayList<>();
        Set<UUID> used = new HashSet<>();
        LivingEntity current = firstTarget;

        while (current != null && chain.size() < MAX_CHAIN) {
            chain.add(current);
            used.add(current.getUUID());

            Vec3 currentCenter = current.position().add(0.0D, current.getBbHeight() * 0.5D, 0.0D);
            AABB area = new AABB(currentCenter, currentCenter).inflate(CHAIN_RADIUS);
            List<LivingEntity> candidates = level.getEntitiesOfClass(
                    LivingEntity.class,
                    area,
                    entity -> canTarget(player, entity) && !used.contains(entity.getUUID())
            );
            candidates.sort(Comparator.comparingDouble(entity -> entity.position().distanceToSqr(currentCenter)));

            current = null;
            for (LivingEntity candidate : candidates) {
                Vec3 candidateCenter = candidate.position().add(0.0D, candidate.getBbHeight() * 0.5D, 0.0D);
                if (candidateCenter.distanceToSqr(currentCenter) <= CHAIN_RADIUS * CHAIN_RADIUS) {
                    current = candidate;
                    break;
                }
            }
        }

        return chain;
    }

    private static boolean canTarget(Player player, LivingEntity candidate) {
        if (candidate == player) return false;
        if (!candidate.isAlive()) return false;
        if (candidate.isAlliedTo(player)) return false;
        if (candidate instanceof Player other) {
            if (other.isCreative() || other.isSpectator()) return false;
            return !(other.getMainHandItem().getItem() instanceof ItemAnnihilationBlade);
        }
        return true;
    }
}
