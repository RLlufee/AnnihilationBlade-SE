package com.qingyi.annihilationbladeex.mixin;

import com.qingyi.annihilationbladeex.client.TerminusTooltipRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {
    @Inject(
            method = "renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;Lnet/minecraft/world/item/ItemStack;II)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void annihilationbladeex$renderTerminusTooltip(Font font, List<Component> textComponents,
                                                           Optional<TooltipComponent> tooltipComponent, ItemStack stack,
                                                           int mouseX, int mouseY, CallbackInfo ci) {
        if (!TerminusTooltipRenderer.shouldRender(stack)) {
            return;
        }

        TerminusTooltipRenderer.render((GuiGraphics) (Object) this, font, stack, textComponents, mouseX, mouseY);
        ci.cancel();
    }
}
