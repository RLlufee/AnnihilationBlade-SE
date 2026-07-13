package QWQ.QingYi.annihilationblade.registry;

import QWQ.QingYi.annihilationblade.annihilation_blade.AnnihilationBladeDefinitions;
import QWQ.QingYi.annihilationblade.blood_prison.BloodPrisonDefinitions;
import QWQ.QingYi.annihilationblade.client.ClientBladeLookup;
import mods.flammpfeil.slashblade.init.SBItems;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
   public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "annihilationblade");
   public static final RegistryObject<CreativeModeTab> SLASHBLADE_TAB = CREATIVE_MODE_TABS.register(
      "slashblade_tab",
      () -> CreativeModeTab.builder()
         .title(Component.translatable("item.annihilationblade.tab_title"))
         .icon(ModCreativeTabs::createIcon)
         .displayItems((parameters, output) -> {
            output.accept(getNamedBladeStack("annihilation_blade"));
            output.accept(getNamedBladeStack("blood_prison"));
            output.accept(new ItemStack((ItemLike)ModItems.ANNIHILATION_FRAGMENT.get()));
            output.accept(new ItemStack((ItemLike)ModItems.ANNIHILATION_CORE.get()));
         })
         .build()
   );

   private ModCreativeTabs() {
   }

   private static ItemStack createIcon() {
      ItemStack stack = new ItemStack(SBItems.slashblade);
      stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(state -> {
         state.setModel(ResourceLocation.fromNamespaceAndPath("annihilationblade", "model/blade.obj"));
         state.setTexture(ResourceLocation.fromNamespaceAndPath("annihilationblade", "model/blade.png"));
      });
      return stack;
   }

   private static ItemStack getNamedBladeStack(String bladeName) {
      ItemStack clientStack = getClientNamedBladeStack(bladeName);
      if (!clientStack.isEmpty()) {
         if ("blood_prison".equals(bladeName)) {
            BloodPrisonDefinitions.ensureStats(clientStack);
         } else if ("annihilation_blade".equals(bladeName)) {
            AnnihilationBladeDefinitions.applyStats(clientStack);
         }

         return clientStack;
      } else if ("annihilation_blade".equals(bladeName)) {
         return AnnihilationBladeDefinitions.createStack();
      } else {
         return "blood_prison".equals(bladeName) ? BloodPrisonDefinitions.createStack() : ItemStack.EMPTY;
      }
   }

   private static ItemStack getClientNamedBladeStack(String bladeName) {
      ItemStack stack = (ItemStack)DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> ClientBladeLookup.getNamedBladeFromManager(bladeName));
      return stack == null ? ItemStack.EMPTY : stack;
   }
}
