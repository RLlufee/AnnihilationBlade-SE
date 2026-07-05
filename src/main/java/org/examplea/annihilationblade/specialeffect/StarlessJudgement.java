package org.examplea.annihilationblade.specialeffect;

import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import mods.flammpfeil.slashblade.registry.specialeffects.SpecialEffect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

import java.util.HashMap;
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

        long gameTime = player.level().getGameTime();
        long last = LAST_TRIGGER.getOrDefault(player.getUUID(), -COOLDOWN_TICKS * 2L);
        if (gameTime - last < COOLDOWN_TICKS) return;
        LAST_TRIGGER.put(player.getUUID(), gameTime);

        ServerLevel level = player.serverLevel();
        Vec3 direction = player.getLookAngle().normalize();
        Vec3 right = rightOf(direction);
        Vec3 start = player.getEyePosition().add(direction.scale(1.4D));

        for (int lane = -1; lane <= 1; lane++) {
            Vec3 laneStart = start.add(right.scale(lane * WIDTH * 0.45D)).add(0.0D, Math.abs(lane) * 0.35D, 0.0D);
            AnnihilationVisuals.spawnEchoWave(level, laneStart, direction, right, RANGE + Math.abs(lane) * 7.0D, WIDTH, lane + 1);
        }

        Vec3 end = start.add(direction.scale(RANGE));
        int count = 0;
        AABB area = new AABB(start, end).inflate(WIDTH);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area, entity -> canTarget(player, entity))) {
            if (count >= MAX_TARGETS) break;

            Vec3 targetCenter = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
            double projection = targetCenter.subtract(start).dot(direction);
            if (projection < 0.0D || projection > RANGE) continue;

            Vec3 nearest = start.add(direction.scale(projection));
            double allowedWidth = WIDTH + projection / RANGE * 4.0D;
            if (targetCenter.distanceToSqr(nearest) > allowedWidth * allowedWidth) continue;

            AnnihilationVisuals.spawnSlashBridge(level, nearest, targetCenter, 1.1D, player.getRandom());
            AnnihilationVisuals.spawnExecutionBurst(level, target, player.getRandom());
            TerminusLogic.execute(target, player);
            count++;
        }

        AnnihilationVisuals.spawnCollapsePulse(level, end, WIDTH, count);
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

    private static Vec3 rightOf(Vec3 direction) {
        Vec3 right = direction.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (right.lengthSqr() < 0.001D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        }
        return right.normalize();
    }
}
