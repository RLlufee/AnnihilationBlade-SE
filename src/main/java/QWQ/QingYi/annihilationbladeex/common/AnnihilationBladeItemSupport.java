package QWQ.QingYi.annihilationbladeex.common;

import QWQ.QingYi.annihilationbladeex.annihilation_blade.AnnihilationBladeDefinitions;
import QWQ.QingYi.annihilationbladeex.registry.ModItems;
import mods.flammpfeil.slashblade.capability.slashblade.BladeStateAccess;
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
            || BladeStateAccess.getData(stack).map(data -> AnnihilationBladeDefinitions.DESCRIPTION_ID.equals(data.translationKey())).orElse(false));
   }
}
