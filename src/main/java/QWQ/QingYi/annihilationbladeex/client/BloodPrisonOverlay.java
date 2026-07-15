package QWQ.QingYi.annihilationbladeex.client;

import QWQ.QingYi.annihilationbladeex.AnnihilationBladeEX;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

@EventBusSubscriber(modid = AnnihilationBladeEX.MODID, value = Dist.CLIENT)
public final class BloodPrisonOverlay {
    private BloodPrisonOverlay() {
    }

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiLayersEvent event) {
        event.registerAboveAll(AnnihilationBladeEX.prefix("blood_prison_domain"), BloodPrisonClientEffects::renderOverlay);
    }
}
