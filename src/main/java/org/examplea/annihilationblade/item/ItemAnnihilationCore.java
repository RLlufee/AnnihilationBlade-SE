package org.examplea.annihilationblade.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.examplea.annihilationblade.Annihilationblade;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemAnnihilationCore extends Item {
    public ItemAnnihilationCore() {
        super(new Properties().stacksTo(1));
    }

    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            ItemStack godSword = getGodBladeFromManager();
            if (!godSword.isEmpty() && player.getInventory().add(godSword)) {
                itemStack.shrink(1);
                return InteractionResultHolder.success(itemStack);
            }
        }

        return InteractionResultHolder.fail(itemStack);
    }

    @Override
    public void appendHoverText(
            @Nonnull ItemStack stack,
            @Nullable Level level,
            @Nonnull List<Component> tooltip,
            @Nonnull TooltipFlag flag
    ) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item.annihilationblade.annihilation_core.tip"));
    }

    private static ItemStack getGodBladeFromManager() {
        ItemStack stack = Annihilationblade.getNamedBladeFromManager("annihilation_blade");
        if (!stack.isEmpty()) {
            Annihilationblade.applyGodStats(stack);
        }
        return stack;
    }
}
