package QWQ.QingYi.annihilationblade.annihilation_blade.item;

import QWQ.QingYi.annihilationblade.annihilation_blade.AnnihilationBladeDefinitions;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public class ItemAnnihilationBlade extends ItemSlashBlade {
   public ItemAnnihilationBlade() {
      super(Tiers.NETHERITE, 100, -2.4F, new Properties().fireResistant().stacksTo(1));
   }

   public boolean isDamageable(ItemStack stack) {
      return false;
   }

   public ItemStack getDefaultInstance() {
      ItemStack stack = new ItemStack(this);
      AnnihilationBladeDefinitions.applyStats(stack);
      return stack;
   }

   public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
      super.inventoryTick(stack, level, entity, slotId, isSelected);
      if (!level.isClientSide && entity.tickCount % 20 == 0) {
         AnnihilationBladeDefinitions.ensureStats(stack);
      }
   }
}
