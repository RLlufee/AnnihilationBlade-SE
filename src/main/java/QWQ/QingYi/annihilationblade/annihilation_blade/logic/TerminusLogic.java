package QWQ.QingYi.annihilationblade.annihilation_blade.logic;

import QWQ.QingYi.annihilationblade.common.SlashBladeStateSupport;
import QWQ.QingYi.annihilationblade.common.SlashBladeTargeting;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TerminusLogic {
   private static final float EXECUTION_DAMAGE = 1.0E9F;

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
      if (!target.level().isClientSide && SlashBladeTargeting.canAttack(attacker, target)) {
         target.invulnerableTime = 0;
         DamageSource source = target.level().damageSources().playerAttack(attacker);
         ItemStack blade = attacker.getMainHandItem();
         int killCountBefore = getKillCount(blade);
         target.hurt(source, 1.0E9F);
         if (target.isAlive()) {
            target.invulnerableTime = 0;
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
   }

   private static int getKillCount(ItemStack blade) {
      return SlashBladeStateSupport.isSlashBlade(blade) ? SlashBladeStateSupport.killCount(blade) : -1;
   }

   private static void ensureKillCountIncremented(ItemStack blade, int killCountBefore) {
      if (killCountBefore >= 0) {
         SlashBladeStateSupport.state(blade).ifPresent(state -> {
            if (state.getKillCount() == killCountBefore) {
               state.setKillCount(killCountBefore + 1);
            }
         });
      }
   }
}
