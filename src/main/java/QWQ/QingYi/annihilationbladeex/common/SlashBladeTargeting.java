package QWQ.QingYi.annihilationbladeex.common;

import mods.flammpfeil.slashblade.util.TargetSelector.AttackablePredicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class SlashBladeTargeting {
   private static final AttackablePredicate ATTACKABLE = new AttackablePredicate();

   private SlashBladeTargeting() {
   }

   public static boolean canAttack(Player attacker, LivingEntity target) {
      return attacker != target && target.isAlive() && ATTACKABLE.test(target);
   }
}
