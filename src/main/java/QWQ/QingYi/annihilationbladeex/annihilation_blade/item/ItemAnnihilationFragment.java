package QWQ.QingYi.annihilationbladeex.annihilation_blade.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import org.jetbrains.annotations.NotNull;

public class ItemAnnihilationFragment extends Item {
   public ItemAnnihilationFragment() {
      super(new Properties());
   }

   public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      super.appendHoverText(stack, context, tooltip, flag);
      tooltip.add(Component.translatable("item.annihilationbladeex.annihilation_fragment.tip"));
   }
}
