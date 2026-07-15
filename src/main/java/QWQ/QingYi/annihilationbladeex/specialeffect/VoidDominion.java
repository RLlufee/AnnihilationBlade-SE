package QWQ.QingYi.annihilationbladeex.specialeffect;

import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import mods.flammpfeil.slashblade.registry.specialeffects.SpecialEffect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import QWQ.QingYi.annihilationbladeex.AnnihilationBladeEX;
import QWQ.QingYi.annihilationbladeex.TerminusLogic;
import QWQ.QingYi.annihilationbladeex.ModSpecialEffects;
import QWQ.QingYi.annihilationbladeex.visual.AnnihilationVisuals;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = AnnihilationBladeEX.MODID)
public class VoidDominion extends SpecialEffect {
    private static final double RANGE = 26.0D;
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
    public static void onDoingSlash(SlashBladeEvent.DoSlashEvent event) {
        if (!(event.getUser() instanceof ServerPlayer player)) return;
        if (Dankong.isActive(player)) return;

        ISlashBladeState state = event.getSlashBladeState();
        if (!state.hasSpecialEffect(ModSpecialEffects.VOID_DOMINION.getId())) return;

        long gameTime = player.level().getGameTime();
        long last = LAST_TRIGGER.getOrDefault(player.getUUID(), -COOLDOWN_TICKS * 2L);
        if (gameTime - last < COOLDOWN_TICKS) return;
        LAST_TRIGGER.put(player.getUUID(), gameTime);

        ServerLevel level = player.serverLevel();
        Vec3 direction = player.getLookAngle().normalize();
        Vec3 center = player.getEyePosition().add(direction.scale(10.0D));

        AnnihilationVisuals.spawnOpeningHalo(level, center, RANGE * 0.62D);
        AnnihilationVisuals.spawnWorldRiftBloom(level, center, RANGE * 0.42D);
        AnnihilationVisuals.spawnFractureWeb(level, center, RANGE * 0.78D, player.getRandom());

        int count = 0;
        for (LivingEntity target : SpecialEffectSupport.radialTargets(level, player, center, RANGE)) {
            if (count >= MAX_TARGETS) break;

            Vec3 targetCenter = SpecialEffectSupport.centerOf(target);

            AnnihilationVisuals.spawnSlashBridge(level, center, targetCenter, 1.4D, player.getRandom());
            AnnihilationVisuals.spawnExecutionBurst(level, target, player.getRandom());
            TerminusLogic.execute(target, player);
            count++;
        }

        AnnihilationVisuals.spawnCollapsePulse(level, center, RANGE * 0.72D, count);
    }
}
