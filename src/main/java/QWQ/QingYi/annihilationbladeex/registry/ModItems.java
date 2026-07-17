package QWQ.QingYi.annihilationbladeex.registry;

import QWQ.QingYi.annihilationbladeex.annihilation_blade.item.ItemAnnihilationBlade;
import QWQ.QingYi.annihilationbladeex.annihilation_blade.item.ItemAnnihilationCore;
import QWQ.QingYi.annihilationbladeex.annihilation_blade.item.ItemAnnihilationFragment;
import QWQ.QingYi.annihilationbladeex.AnnihilationBladeEX;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
   public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AnnihilationBladeEX.MODID);
   public static final DeferredItem<ItemAnnihilationBlade> ANNIHILATION_BLADE = ITEMS.register("annihilation_blade", ItemAnnihilationBlade::new);
   public static final DeferredItem<ItemAnnihilationFragment> ANNIHILATION_FRAGMENT = ITEMS.register("annihilation_fragment", ItemAnnihilationFragment::new);
   public static final DeferredItem<ItemAnnihilationCore> ANNIHILATION_CORE = ITEMS.register("annihilation_core", ItemAnnihilationCore::new);
}
