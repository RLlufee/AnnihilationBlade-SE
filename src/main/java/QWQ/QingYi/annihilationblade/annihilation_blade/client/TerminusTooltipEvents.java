package QWQ.QingYi.annihilationblade.annihilation_blade.client;

import QWQ.QingYi.annihilationblade.Annihilationblade;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = Annihilationblade.MODID, value = Dist.CLIENT)
public final class TerminusTooltipEvents {
   private TerminusTooltipEvents() {
   }

   @SubscribeEvent
   public static void onRenderTooltipPre(RenderTooltipEvent.Pre event) {
      ItemStack stack = event.getItemStack();
      if (!TerminusTooltipRenderer.shouldRender(stack)) {
         return;
      }

      Minecraft minecraft = Minecraft.getInstance();
      TooltipFlag tooltipFlag = minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
      List<Component> textComponents = stack.getTooltipLines(minecraft.player, tooltipFlag);
      TerminusTooltipRenderer.render(event.getGraphics(), event.getFont(), stack, textComponents, event.getX(), event.getY());
      event.setCanceled(true);
   }
}
