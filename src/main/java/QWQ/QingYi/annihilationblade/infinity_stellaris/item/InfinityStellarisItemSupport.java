package QWQ.QingYi.annihilationblade.infinity_stellaris.item;

import QWQ.QingYi.annihilationblade.infinity_stellaris.InfinityStellarisDefinitions;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class InfinityStellarisItemSupport {
   private InfinityStellarisItemSupport() {
   }

   public static boolean isInfinityStellaris(ItemStack stack) {
      return !stack.isEmpty()
         && stack.getItem() instanceof ItemSlashBlade
         && (InfinityStellarisDefinitions.DESCRIPTION_ID.equals(stack.getDescriptionId())
            || stack.hasTag() && stack.getTag().getBoolean(InfinityStellarisDefinitions.IDENTITY_TAG));
   }

   public static boolean isHoldingInfinityStellaris(LivingEntity entity) {
      return isInfinityStellaris(entity.getMainHandItem()) || isInfinityStellaris(entity.getOffhandItem());
   }

   public static boolean hasInfinityStellarisInInventory(Player player) {
      for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
         if (isInfinityStellaris(player.getInventory().getItem(i))) {
            return true;
         }
      }
      return false;
   }

   public static ItemStack heldInfinityStellaris(LivingEntity entity) {
      ItemStack mainHand = entity.getMainHandItem();
      if (isInfinityStellaris(mainHand)) {
         return mainHand;
      }

      ItemStack offHand = entity.getOffhandItem();
      return isInfinityStellaris(offHand) ? offHand : ItemStack.EMPTY;
   }

   public static boolean isDirectInfinityAttack(Player player, net.minecraft.world.damagesource.DamageSource source) {
      return isHoldingInfinityStellaris(player) && source.getEntity() == player;
   }

   public static boolean isInfinitySlashEntityAttack(Player player, net.minecraft.world.entity.Entity directSource) {
      return directSource != null && directSource.getType().toString().contains("slashblade") && isHoldingInfinityStellaris(player);
   }
}
