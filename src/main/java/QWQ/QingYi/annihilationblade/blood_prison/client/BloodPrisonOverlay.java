package QWQ.QingYi.annihilationblade.blood_prison.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = "annihilationblade", bus = Bus.MOD, value = Dist.CLIENT)
public final class BloodPrisonOverlay {
   private BloodPrisonOverlay() {
   }

   @SubscribeEvent
   public static void registerOverlays(RegisterGuiOverlaysEvent event) {
      event.registerAboveAll("blood_prison_domain", BloodPrisonClientEffects::renderOverlay);
   }
}
