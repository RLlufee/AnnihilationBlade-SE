package QWQ.QingYi.annihilationbladeex.common;

import java.util.Optional;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.world.item.ItemStack;

public final class SlashBladeStateSupport {
   private SlashBladeStateSupport() {
   }

   public static boolean isSlashBlade(ItemStack stack) {
      return !stack.isEmpty() && stack.getItem() instanceof ItemSlashBlade;
   }

   public static Optional<ISlashBladeState> state(ItemStack stack) {
      return !isSlashBlade(stack) ? Optional.empty() : BladeStateAccess.of(stack);
   }

   public static int killCount(ItemStack stack) {
      return state(stack).<Integer>map(ISlashBladeState::getKillCount).orElse(0);
   }

   public static int proudSoul(ItemStack stack) {
      return state(stack).<Integer>map(ISlashBladeState::getProudSoulCount).orElse(0);
   }

   public static int refine(ItemStack stack) {
      return state(stack).<Integer>map(ISlashBladeState::getRefine).orElse(0);
   }
}
