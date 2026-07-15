package QWQ.QingYi.annihilationbladeex.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import QWQ.QingYi.annihilationbladeex.AnnihilationBladeEX;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = AnnihilationBladeEX.MODID, value = Dist.CLIENT)
public final class BloodPrisonClientEffects {
    private static final ResourceLocation RED_OVERLAY = AnnihilationBladeEX.prefix("textures/screens/red.png");
    private static int domainTicks;
    private static int domainAgeTicks;

    private BloodPrisonClientEffects() {
    }

    public static void setDomainTicks(int ticks) {
        domainTicks = Math.max(0, ticks);
        domainAgeTicks = 0;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().isPaused()) {
            return;
        }
        if (domainTicks > 0) {
            domainTicks--;
            domainAgeTicks++;
        } else {
            domainAgeTicks = 0;
        }
    }

    public static void renderOverlay(GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (domainTicks <= 0) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
        float enter = Math.min(1.0F, (domainAgeTicks + partialTick) / 10.0F);
        float exit = Math.min(1.0F, (domainTicks + partialTick) / 26.0F);
        float fade = Math.min(enter, exit);

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.92F * fade);
        graphics.blit(RED_OVERLAY, 0, 0, 0, 0, screenWidth, screenHeight, 200, 5);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}
