package QWQ.QingYi.annihilationbladeex.annihilation_blade.item;

import QWQ.QingYi.annihilationbladeex.annihilation_blade.AnnihilationBladeDefinitions;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemAnnihilationCore extends Item {
   public ItemAnnihilationCore() {
      super(new Properties().stacksTo(1));
   }

   public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
      ItemStack itemStack = player.getItemInHand(hand);
      if (!level.isClientSide) {
         ItemStack godSword = getGodBladeFromManager(level);
         if (!godSword.isEmpty() && player.getInventory().add(godSword)) {
            itemStack.shrink(1);
            return InteractionResultHolder.success(itemStack);
         }
      }

      return InteractionResultHolder.fail(itemStack);
   }

   public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      super.appendHoverText(stack, context, tooltip, flag);
      tooltip.add(Component.translatable("item.annihilationbladeex.annihilation_core.tip"));
   }

   private static ItemStack getGodBladeFromManager(Level level) {
      return AnnihilationBladeDefinitions.createStack(level);
   }
}
