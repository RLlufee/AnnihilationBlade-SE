package QWQ.QingYi.annihilationblade.common;

import QWQ.QingYi.annihilationblade.Annihilationblade;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class NamedBladeStacks {
   private static final String MODID = "annihilationblade";

   private NamedBladeStacks() {
   }

   public static ItemStack get(Level level, String bladeName) {
      return get(level, bladeName, null);
   }

   public static ItemStack get(Level level, String bladeName, @Nullable Item bladeItem) {
      if (level == null || bladeName == null || bladeName.isEmpty()) {
         return ItemStack.EMPTY;
      }

      try {
         Registry<SlashBladeDefinition> registry = SlashBlade.getSlashBladeDefinitionRegistry(level);
         SlashBladeDefinition definition = registry.get(ResourceLocation.fromNamespaceAndPath(MODID, bladeName));
         if (definition == null) {
            return ItemStack.EMPTY;
         }

         ItemStack stack = bladeItem == null ? definition.getBlade() : definition.getBlade(bladeItem);
         return stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
      } catch (RuntimeException exception) {
         Annihilationblade.LOGGER.warn("Failed to read SlashBlade named blade definition for {}", bladeName, exception);
         return ItemStack.EMPTY;
      }
   }

   public static boolean copyDefinitionTag(ItemStack target, Level level, String bladeName, @Nullable Item bladeItem) {
      ItemStack definedStack = get(level, bladeName, bladeItem);
      if (definedStack.isEmpty()) {
         return false;
      }

      CompoundTag definedTag = definedStack.getTag();
      target.setTag(definedTag == null ? null : definedTag.copy());
      return true;
   }
}
