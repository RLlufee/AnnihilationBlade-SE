package QWQ.QingYi.annihilationblade.mixin;

import QWQ.QingYi.annihilationblade.annihilation_blade.client.ClientBladeVision;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightTexture.class)
public abstract class LightTextureMixin {
   @Redirect(
      method = "updateLightTexture",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/world/effect/MobEffect;)Z", ordinal = 0)
   )
   private boolean annihilationblade$hasNightVision(LivingEntity entity, MobEffect effect) {
      return entity.hasEffect(effect) || effect == MobEffects.NIGHT_VISION && ClientBladeVision.hasBladeInInventory();
   }

   @Redirect(
      method = "updateLightTexture",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;getNightVisionScale(Lnet/minecraft/world/entity/LivingEntity;F)F")
   )
   private float annihilationblade$fullNightVisionScale(LivingEntity entity, float partialTick) {
      return ClientBladeVision.hasBladeInInventory() ? 1.0F : GameRenderer.getNightVisionScale(entity, partialTick);
   }
}
