package QWQ.QingYi.annihilationbladeex;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AnnihilationBladeEX.MODID);

    public static final DeferredItem<ItemAnnihilationBlade> ANNIHILATION_BLADE =
            ITEMS.register("annihilation_blade", ItemAnnihilationBlade::new);

    public static final DeferredItem<ItemAnnihilationFragment> ANNIHILATION_FRAGMENT =
            ITEMS.register("annihilation_fragment", ItemAnnihilationFragment::new);

    public static final DeferredItem<ItemAnnihilationCore> ANNIHILATION_CORE =
            ITEMS.register("annihilation_core", ItemAnnihilationCore::new);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
