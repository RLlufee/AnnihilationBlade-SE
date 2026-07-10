package org.examplea.annihilationblade.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 客户端本地的湮灭之刃持有判定。
 *
 * <p>由光照贴图 Mixin 调用：不向玩家添加夜视效果，因此不会产生药水图标或状态效果提示。</p>
 */
@OnlyIn(Dist.CLIENT)
public final class ClientBladeVision {
    private static final String BLADE_DESCRIPTION_ID = "item.annihilationblade.annihilation_blade";

    private ClientBladeVision() {
    }

    public static boolean hasBladeInInventory() {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }

        if (isAnnihilationBlade(player.getMainHandItem()) || isAnnihilationBlade(player.getOffhandItem())) {
            return true;
        }

        for (ItemStack stack : player.getInventory().items) {
            if (isAnnihilationBlade(stack)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAnnihilationBlade(ItemStack stack) {
        return !stack.isEmpty() && (BLADE_DESCRIPTION_ID.equals(stack.getDescriptionId())
                || stack.hasTag() && stack.getTag().getBoolean("IsAnnihilationBlade"));
    }
}
