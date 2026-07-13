package QWQ.QingYi.annihilationblade.blood_prison.client;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = "annihilationblade", value = Dist.CLIENT)
public final class BloodPrisonClientEffects {
   private static final ResourceLocation RED_OVERLAY = ResourceLocation.fromNamespaceAndPath("annihilationblade", "textures/screens/red.png");
   private static int domainTicks;
   private static int domainAgeTicks;

   private BloodPrisonClientEffects() {
   }

   public static void setDomainTicks(int ticks) {
      domainTicks = Math.max(0, ticks);
      domainAgeTicks = 0;
   }

   @SubscribeEvent
   public static void onClientTick(ClientTickEvent event) {
      if (event.phase == Phase.END && !Minecraft.getInstance().isPaused()) {
         if (domainTicks > 0) {
            domainTicks--;
            domainAgeTicks++;
         } else {
            domainAgeTicks = 0;
         }
      }
   }

   public static void renderOverlay(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
      if (domainTicks > 0 && screenWidth > 0 && screenHeight > 0) {
         float enter = Math.min(1.0F, (domainAgeTicks + partialTick) / 10.0F);
         float exit = Math.min(1.0F, (domainTicks + partialTick) / 26.0F);
         float fade = Math.min(enter, exit);
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.enableBlend();
         RenderSystem.setShader(GameRenderer::getPositionTexShader);
         RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.92F * fade);
         graphics.blit(RED_OVERLAY, 0, 0, 0.0F, 0.0F, screenWidth, screenHeight, 200, 5);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.depthMask(true);
         RenderSystem.defaultBlendFunc();
         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();
      }
   }
}
