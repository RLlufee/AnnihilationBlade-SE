package QWQ.QingYi.annihilationblade.client;

import java.util.Map.Entry;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientBladeLookup {
   private ClientBladeLookup() {
   }

   public static ItemStack getNamedBladeFromManager(String bladeName) {
      return getNamedBladeFromManager(bladeName, null);
   }

   public static ItemStack getNamedBladeFromManager(String bladeName, @Nullable Item bladeItem) {
      if (Minecraft.getInstance().getConnection() == null) {
         return ItemStack.EMPTY;
      }

      Registry<SlashBladeDefinition> registry = BladeModelManager.getClientSlashBladeRegistry();

      for (Entry<ResourceKey<SlashBladeDefinition>, SlashBladeDefinition> entry : registry.entrySet()) {
         if (entry.getKey() != null) {
            ResourceLocation id = entry.getKey().location();
            if (id.getNamespace().equals("annihilationblade") && id.getPath().equals(bladeName)) {
               ItemStack stack = bladeItem == null ? entry.getValue().getBlade() : entry.getValue().getBlade(bladeItem);
               return stack.copy();
            }
         }
      }

      return ItemStack.EMPTY;
   }
}
