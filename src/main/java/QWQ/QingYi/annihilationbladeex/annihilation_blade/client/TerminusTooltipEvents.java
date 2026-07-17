package QWQ.QingYi.annihilationbladeex.annihilation_blade.client;

import QWQ.QingYi.annihilationbladeex.AnnihilationBladeEX;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;

@EventBusSubscriber(modid = AnnihilationBladeEX.MODID, value = Dist.CLIENT)
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
      TooltipFlag tooltipFlag = minecraft.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL;
      Item.TooltipContext context = minecraft.level == null ? Item.TooltipContext.EMPTY : Item.TooltipContext.of(minecraft.level);
      List<Component> textComponents = stack.getTooltipLines(context, minecraft.player, tooltipFlag);
      TerminusTooltipRenderer.render(event.getGraphics(), event.getFont(), stack, textComponents, event.getX(), event.getY());
      event.setCanceled(true);
   }
}
