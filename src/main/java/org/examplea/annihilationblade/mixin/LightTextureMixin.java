package org.examplea.annihilationblade.mixin;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.examplea.annihilationblade.client.ClientBladeVision;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 在本地光照贴图中给持有湮灭之刃的玩家提供夜视级别的亮度。
 * 不会把药水效果写入实体状态，因而不会出现药水提示框或图标。
 * 我已经尽力了哈，实在是没办法消除......
 */
@Mixin(LightTexture.class)
public abstract class LightTextureMixin {
    @Redirect(
            method = "updateLightTexture",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/world/effect/MobEffect;)Z",
                    ordinal = 0
            )
    )
    private boolean annihilationblade$hasNightVision(LivingEntity entity, MobEffect effect) {
        return entity.hasEffect(effect) || effect == MobEffects.NIGHT_VISION && ClientBladeVision.hasBladeInInventory();
    }

    @Redirect(
            method = "updateLightTexture",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;getNightVisionScale(Lnet/minecraft/world/entity/LivingEntity;F)F"
            )
    )
    private float annihilationblade$fullNightVisionScale(LivingEntity entity, float partialTick) {
        if (ClientBladeVision.hasBladeInInventory()) {
            return 1.0F;
        }
        return GameRenderer.getNightVisionScale(entity, partialTick);
    }
}
