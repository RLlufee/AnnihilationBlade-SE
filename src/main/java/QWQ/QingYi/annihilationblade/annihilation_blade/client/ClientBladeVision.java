package QWQ.QingYi.annihilationblade.annihilation_blade.client;

import QWQ.QingYi.annihilationblade.Annihilationblade;
import QWQ.QingYi.annihilationblade.common.AnnihilationBladeItemSupport;
import QWQ.QingYi.annihilationblade.infinity_stellaris.item.InfinityStellarisItemSupport;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = Annihilationblade.MODID, value = Dist.CLIENT)
public final class ClientBladeVision {
   private static final int INVENTORY_SCAN_INTERVAL = 10;
   private static final double FULL_BRIGHT_GAMMA = 15.0D;
   private static UUID lastPlayerId;
   private static int lastScanTick = Integer.MIN_VALUE;
   private static boolean cachedHasBlade;
   private static boolean gammaBoostActive;
   private static double savedGamma = -1.0D;
   private static Field gammaValueField;
   private static boolean gammaFieldWarningLogged;

   private ClientBladeVision() {
   }

   public static boolean hasBladeInInventory() {
      Player player = Minecraft.getInstance().player;
      if (player == null) {
         clearCache();
         return false;
      }

      if (!AnnihilationBladeItemSupport.isHoldingAnnihilationBlade(player)) {
         UUID playerId = player.getUUID();
         if (playerId.equals(lastPlayerId) && player.tickCount - lastScanTick < INVENTORY_SCAN_INTERVAL) {
            return cachedHasBlade;
         }

         for (ItemStack stack : player.getInventory().items) {
            if (AnnihilationBladeItemSupport.isAnnihilationBlade(stack)) {
               cache(player, true);
               return true;
            }
         }

         cache(player, false);
         return false;
      } else {
         cache(player, true);
         return true;
      }
   }

   @SubscribeEvent
   public static void onClientTick(ClientTickEvent event) {
      if (event.phase != Phase.END) {
         return;
      }

      Minecraft minecraft = Minecraft.getInstance();
      boolean shouldBoost = minecraft.player != null
         && (hasBladeInInventory() || InfinityStellarisItemSupport.hasInfinityStellarisInInventory(minecraft.player));
      applyGammaBoost(minecraft, shouldBoost);
   }

   private static void applyGammaBoost(Minecraft minecraft, boolean shouldBoost) {
      OptionInstance<Double> gamma = minecraft.options.gamma();
      double currentGamma = gamma.get();
      if (shouldBoost) {
         if (!gammaBoostActive) {
            savedGamma = currentGamma;
            gammaBoostActive = true;
         } else if (currentGamma != FULL_BRIGHT_GAMMA && currentGamma != savedGamma) {
            savedGamma = currentGamma;
         }

         if (currentGamma != FULL_BRIGHT_GAMMA) {
            setGammaValue(gamma, FULL_BRIGHT_GAMMA);
         }
      } else if (gammaBoostActive) {
         if (savedGamma >= 0.0D && currentGamma == FULL_BRIGHT_GAMMA) {
            setGammaValue(gamma, savedGamma);
         }

         gammaBoostActive = false;
         savedGamma = -1.0D;
      }
   }

   private static void setGammaValue(OptionInstance<Double> gamma, double value) {
      Field valueField = getGammaValueField(gamma);
      if (valueField == null) {
         gamma.set(Math.max(0.0D, Math.min(1.0D, value)));
         return;
      }

      try {
         valueField.set(gamma, value);
      } catch (IllegalAccessException exception) {
         if (!gammaFieldWarningLogged) {
            Annihilationblade.LOGGER.warn("Failed to write gamma option value reflectively", exception);
            gammaFieldWarningLogged = true;
         }
      }
   }

   private static Field getGammaValueField(OptionInstance<Double> gamma) {
      if (gammaValueField != null) {
         return gammaValueField;
      }

      for (Field field : gamma.getClass().getDeclaredFields()) {
         int modifiers = field.getModifiers();
         if (!Modifier.isStatic(modifiers) && !Modifier.isFinal(modifiers)) {
            field.setAccessible(true);
            gammaValueField = field;
            return field;
         }
      }

      if (!gammaFieldWarningLogged) {
         Annihilationblade.LOGGER.warn("Could not locate mutable OptionInstance value field for gamma fullbright");
         gammaFieldWarningLogged = true;
      }
      return null;
   }

   private static void cache(Player player, boolean hasBlade) {
      lastPlayerId = player.getUUID();
      lastScanTick = player.tickCount;
      cachedHasBlade = hasBlade;
   }

   private static void clearCache() {
      lastPlayerId = null;
      lastScanTick = Integer.MIN_VALUE;
      cachedHasBlade = false;
   }

}
