package QWQ.QingYi.annihilationbladeex.infinity_stellaris.client;

import QWQ.QingYi.annihilationbladeex.AnnihilationBladeEX;
import QWQ.QingYi.annihilationbladeex.registry.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = AnnihilationBladeEX.MODID, bus = Bus.MOD, value = Dist.CLIENT)
public final class InfinityStellarisClientEvents {
   private InfinityStellarisClientEvents() {
   }

   @SubscribeEvent
   public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
      event.registerEntityRenderer(ModEntities.GAMMA_THUNDERBOLT.get(), GammaThunderboltRenderer::new);
   }
}
