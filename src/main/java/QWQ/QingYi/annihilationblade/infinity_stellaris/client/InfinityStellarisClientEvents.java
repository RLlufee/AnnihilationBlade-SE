package QWQ.QingYi.annihilationblade.infinity_stellaris.client;

import QWQ.QingYi.annihilationblade.Annihilationblade;
import QWQ.QingYi.annihilationblade.registry.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = Annihilationblade.MODID, bus = Bus.MOD, value = Dist.CLIENT)
public final class InfinityStellarisClientEvents {
   private InfinityStellarisClientEvents() {
   }

   @SubscribeEvent
   public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
      event.registerEntityRenderer(ModEntities.GAMMA_THUNDERBOLT.get(), GammaThunderboltRenderer::new);
   }
}
