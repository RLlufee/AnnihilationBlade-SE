package com.qingyi.annihilationbladeex.client;

import com.qingyi.annihilationbladeex.AnnihilationBladeEX;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * 客户端本地的湮灭之刃持有判定。
 * <p>由光照贴图 Mixin 调用；不向玩家写入夜视药水效果，因此不会产生状态图标或提示</p>
 *  我他妈真的是讨厌这个药水提示框了！！！！！！！！！！！
 */
@OnlyIn(Dist.CLIENT)
public final class ClientBladeVision {
    private ClientBladeVision() {
    }

    public static boolean hasBladeInInventory() {
        var player = Minecraft.getInstance().player;
        return player != null && AnnihilationBladeEX.hasGodBlade(player);
    }
}
