package com.qingyi.examplemod;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class TerminusLogic {
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
        if (target.level().isClientSide() || !target.isAlive()) {
            return;
        }
        target.invulnerableTime = 0;
        DamageSource source = target.level().damageSources().playerAttack(attacker);
        target.hurt(source, Math.max(10000.0F, target.getMaxHealth() * 1000.0F));
        if (target.isAlive()) {
            target.setHealth(0.0F);
            target.die(source);
        }
        if (target.isAlive()) {
            target.discard();
        }
    }
}
