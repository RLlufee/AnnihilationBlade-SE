package com.qingyi.annihilationbladeex;

import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TerminusLogic {
    private static final float EXECUTION_DAMAGE = 1_000_000_000.0F;

    public static void markForDeath(LivingEntity target) {
        if (target.getHealth() > 0.0F) {
            target.setHealth(0.1F);
            target.invulnerableTime = 0;
        }
    }

    public static boolean isMarkedForDeath(LivingEntity target) {
        return target.getHealth() <= 0.1F && target.invulnerableTime == 0;
    }

    public static void execute(LivingEntity target, Player attacker) {
        if (target.level().isClientSide() || !SlashBladeTargeting.canAttack(attacker, target)) {
            return;
        }
        target.invulnerableTime = 0;
        DamageSource source = target.level().damageSources().playerAttack(attacker);
        ItemStack blade = attacker.getMainHandItem();
        int killCountBefore = getKillCount(blade);

        target.hurt(source, Math.max(EXECUTION_DAMAGE, target.getMaxHealth() * 1000.0F));
        if (target.isAlive()) {
            target.setHealth(0.0F);
            target.die(source);
        }
        if (target.isAlive()) {
            target.discard();
        }
        if (!target.isAlive()) {
            ensureKillCountIncremented(blade, killCountBefore);
        }
    }

    private static int getKillCount(ItemStack blade) {
        if (blade.isEmpty() || !(blade.getItem() instanceof ItemSlashBlade)) {
            return -1;
        }
        return BladeStateAccess.of(blade)
                .map(state -> state.getKillCount())
                .orElse(-1);
    }

    private static void ensureKillCountIncremented(ItemStack blade, int killCountBefore) {
        if (killCountBefore < 0) {
            return;
        }

        BladeStateAccess.of(blade).ifPresent(state -> {
            if (state.getKillCount() == killCountBefore) {
                state.setKillCount(killCountBefore + 1);
            }
        });
    }
}
