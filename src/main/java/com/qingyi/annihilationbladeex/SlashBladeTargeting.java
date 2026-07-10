package com.qingyi.annihilationbladeex;

import mods.flammpfeil.slashblade.util.TargetSelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * 湮灭之刃所有伤害与索敌共用的目标规则。
 *
 * <p>直接委托 SlashBlade 的 {@link TargetSelector.AttackablePredicate}，因此严格遵循
 * SlashBlade 配置中的 {@code pvp_enable} 与 {@code friendly_enable}。</p>
 */
public final class SlashBladeTargeting {
    private static final TargetSelector.AttackablePredicate ATTACKABLE = new TargetSelector.AttackablePredicate();

    private SlashBladeTargeting() {
    }

    public static boolean canAttack(Player attacker, LivingEntity target) {
        return attacker != target && target.isAlive() && ATTACKABLE.test(target);
    }
}
