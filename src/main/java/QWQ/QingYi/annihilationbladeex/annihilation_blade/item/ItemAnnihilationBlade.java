package QWQ.QingYi.annihilationbladeex.annihilation_blade.item;

import QWQ.QingYi.annihilationbladeex.annihilation_blade.AnnihilationBladeDefinitions;
import java.util.List;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemAnnihilationBlade extends ItemSlashBlade {
   public ItemAnnihilationBlade() {
      super(Tiers.NETHERITE, 100, -2.4F, new Properties().fireResistant().stacksTo(1));
   }

   public boolean isDamageable(@NotNull ItemStack stack) {
      return false;
   }

   public @NotNull ItemStack getDefaultInstance() {
      return AnnihilationBladeDefinitions.createStack();
   }

   public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
      super.inventoryTick(stack, level, entity, slotId, isSelected);
      if (!level.isClientSide && entity.tickCount % 20 == 0) {
         AnnihilationBladeDefinitions.ensureStats(stack, level);
      }
   }

   public void releaseUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entityLiving, int timeLeft) {
      AnnihilationBladeDefinitions.refreshHeldStats(stack, entityLiving);
      super.releaseUsing(stack, level, entityLiving, timeLeft);
   }

   public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      super.appendHoverText(stack, context, tooltip, flag);
      tooltip.add(Component.empty());
      tooltip.add(Component.translatable("item.annihilationbladeex.passive").withStyle(ChatFormatting.GOLD));
      tooltip.add(Component.translatable("item.annihilationbladeex.desc.line1").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
      tooltip.add(Component.translatable("item.annihilationbladeex.desc.line2").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
      tooltip.add(Component.translatable("item.annihilationbladeex.desc.line3").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
      tooltip.add(Component.translatable("item.annihilationbladeex.desc.line4").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
      tooltip.add(Component.empty());
      tooltip.add(Component.translatable("item.annihilationbladeex.infinite_damage").withStyle(ChatFormatting.LIGHT_PURPLE));
   }
}
