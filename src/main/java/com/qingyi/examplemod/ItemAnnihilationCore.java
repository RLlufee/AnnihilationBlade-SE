package com.qingyi.examplemod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemAnnihilationCore extends Item {
    public ItemAnnihilationCore() {
        super(new Properties().fireResistant().stacksTo(1));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player,
                                                           @NotNull InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            ItemStack blade = AnnihilationBladeEX.createGodBlade(level, player.registryAccess());
            if (!player.getInventory().add(blade)) {
                player.drop(blade, false);
            }
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
        }
        return InteractionResultHolder.success(held);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.annihilationbladeex.annihilation_core.tip"));
    }
}
