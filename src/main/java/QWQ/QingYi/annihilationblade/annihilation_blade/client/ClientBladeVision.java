package QWQ.QingYi.annihilationblade.annihilation_blade.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientBladeVision {
   private static final String BLADE_DESCRIPTION_ID = "item.annihilationblade.annihilation_blade";

   private ClientBladeVision() {
   }

   public static boolean hasBladeInInventory() {
      Player player = Minecraft.getInstance().player;
      if (player == null) {
         return false;
      }

      if (!isAnnihilationBlade(player.getMainHandItem()) && !isAnnihilationBlade(player.getOffhandItem())) {
         for (ItemStack stack : player.getInventory().items) {
            if (isAnnihilationBlade(stack)) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   private static boolean isAnnihilationBlade(ItemStack stack) {
      return !stack.isEmpty()
         && ("item.annihilationblade.annihilation_blade".equals(stack.getDescriptionId()) || stack.hasTag() && stack.getTag().getBoolean("IsAnnihilationBlade"));
   }
}
