package QWQ.QingYi.annihilationblade.common;

import QWQ.QingYi.annihilationblade.annihilation_blade.AnnihilationBladeDefinitions;
import QWQ.QingYi.annihilationblade.registry.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class AnnihilationBladeItemSupport {
   private AnnihilationBladeItemSupport() {
   }

   public static boolean isHoldingAnnihilationBlade(Player player) {
      return isAnnihilationBlade(player.getMainHandItem()) || isAnnihilationBlade(player.getOffhandItem());
   }

   public static boolean isAnnihilationBlade(ItemStack stack) {
      return !stack.isEmpty()
         && (stack.is(ModItems.ANNIHILATION_BLADE.get())
            || AnnihilationBladeDefinitions.DESCRIPTION_ID.equals(stack.getDescriptionId())
            || stack.hasTag() && stack.getTag().getBoolean("IsAnnihilationBlade"));
   }
}
