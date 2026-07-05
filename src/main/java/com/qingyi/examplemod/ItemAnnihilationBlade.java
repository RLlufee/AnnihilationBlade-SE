package com.qingyi.examplemod;

import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.List;

public class ItemAnnihilationBlade extends ItemSlashBlade {
    public ItemAnnihilationBlade() {
        super(Tiers.NETHERITE, 100, -2.4F, new Item.Properties().fireResistant().stacksTo(1));
    }

    @Override
    public boolean isDamageable(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public @NotNull ItemStack getDefaultInstance() {
        return AnnihilationBladeEX.createGodBlade();
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        AnnihilationBladeEX.refreshHeldGodBlade(stack, entity);
    }

    @Override
    public void releaseUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entityLiving, int timeLeft) {
        AnnihilationBladeEX.refreshHeldGodBlade(stack, entityLiving);
        super.releaseUsing(stack, level, entityLiving, timeLeft);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.annihilationbladeex.passive").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.annihilationbladeex.desc.line1").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("item.annihilationbladeex.desc.line2").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("item.annihilationbladeex.desc.line3").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("item.annihilationbladeex.desc.line4").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        tooltip.add(Component.empty());
        tooltip.add(rainbow(Component.translatable("item.annihilationbladeex.infinite_damage").getString()));
    }

    private static MutableComponent rainbow(String text) {
        MutableComponent result = Component.literal(" ");
        long time = System.currentTimeMillis();
        for (int i = 0; i < text.length(); i++) {
            float hue = ((time % 3000L) / 3000.0F + i * 0.08F) % 1.0F;
            int rgb = Color.HSBtoRGB(hue, 1.0F, 1.0F);
            result.append(Component.literal(String.valueOf(text.charAt(i)))
                    .withStyle(style -> style.withColor(rgb)));
        }
        return result;
    }
}
