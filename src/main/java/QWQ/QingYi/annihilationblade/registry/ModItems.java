package QWQ.QingYi.annihilationblade.registry;

import QWQ.QingYi.annihilationblade.annihilation_blade.item.ItemAnnihilationBlade;
import QWQ.QingYi.annihilationblade.annihilation_blade.item.ItemAnnihilationCore;
import QWQ.QingYi.annihilationblade.annihilation_blade.item.ItemAnnihilationFragment;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
   public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "annihilationblade");
   public static final RegistryObject<Item> ANNIHILATION_BLADE = ITEMS.register("annihilation_blade", ItemAnnihilationBlade::new);
   public static final RegistryObject<Item> ANNIHILATION_FRAGMENT = ITEMS.register("annihilation_fragment", ItemAnnihilationFragment::new);
   public static final RegistryObject<Item> ANNIHILATION_CORE = ITEMS.register("annihilation_core", ItemAnnihilationCore::new);
}
