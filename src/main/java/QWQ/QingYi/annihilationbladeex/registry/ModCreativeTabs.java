package QWQ.QingYi.annihilationbladeex.registry;

import QWQ.QingYi.annihilationbladeex.annihilation_blade.AnnihilationBladeDefinitions;
import QWQ.QingYi.annihilationbladeex.blood_prison.BloodPrisonDefinitions;
import QWQ.QingYi.annihilationbladeex.AnnihilationBladeEX;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModCreativeTabs {
   public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AnnihilationBladeEX.MODID);
   public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SLASHBLADE_TAB = CREATIVE_MODE_TABS.register(
      "slashblade_tab",
      () -> CreativeModeTab.builder()
         .title(Component.translatable("item.annihilationbladeex.tab_title"))
         .icon(ModCreativeTabs::createIcon)
         .displayItems((parameters, output) -> {
            output.accept(AnnihilationBladeDefinitions.createFromLookup(parameters.holders()));
            output.accept(BloodPrisonDefinitions.createStack(parameters.holders()));
            output.accept(new ItemStack((ItemLike)ModItems.ANNIHILATION_FRAGMENT.get()));
            output.accept(new ItemStack((ItemLike)ModItems.ANNIHILATION_CORE.get()));
         })
         .build()
   );

   private ModCreativeTabs() {
   }

   private static ItemStack createIcon() {
      return AnnihilationBladeDefinitions.createStack();
   }

}
