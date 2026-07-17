package QWQ.QingYi.annihilationbladeex.annihilation_blade.client;

import QWQ.QingYi.annihilationbladeex.AnnihilationBladeEX;
import QWQ.QingYi.annihilationbladeex.common.AnnihilationBladeItemSupport;
import QWQ.QingYi.annihilationbladeex.network.DankongBlinkModePacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = AnnihilationBladeEX.MODID, value = Dist.CLIENT)
public final class DankongBlinkKeyHandler {
   private static final String KEY_NAME = "key.annihilationbladeex.toggle_dankong_blink";
   private static final String KEY_CATEGORY = "key.categories.annihilationbladeex";
   private static final String MESSAGE_TOGGLE = "message.annihilationbladeex.dankong_blink_toggle";
   private static final String MESSAGE_STATE_ON = "message.annihilationbladeex.dankong_blink.on";
   private static final String MESSAGE_STATE_OFF = "message.annihilationbladeex.dankong_blink.off";
   private static final KeyMapping TOGGLE_DANKONG_BLINK = new KeyMapping(
      KEY_NAME, KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_CONTROL, KEY_CATEGORY
   );
   private static boolean blinkEnabled = true;

   private DankongBlinkKeyHandler() {
   }

   @SubscribeEvent
   public static void onClientTick(ClientTickEvent.Post event) {
      Minecraft minecraft = Minecraft.getInstance();
      Player player = minecraft.player;
      if (player == null) {
         return;
      }

      while (TOGGLE_DANKONG_BLINK.consumeClick()) {
         if (!AnnihilationBladeItemSupport.isHoldingAnnihilationBlade(player)) {
            continue;
         }

         blinkEnabled = !blinkEnabled;
         PacketDistributor.sendToServer(new DankongBlinkModePacket(blinkEnabled));
         player.displayClientMessage(Component.translatable(MESSAGE_TOGGLE, Component.translatable(blinkEnabled ? MESSAGE_STATE_ON : MESSAGE_STATE_OFF)), true);
      }
   }

   public static void resetBlinkMode() {
      blinkEnabled = true;
   }

   @SubscribeEvent
   public static void onLoggingOut(LoggingOut event) {
      resetBlinkMode();
   }

   @EventBusSubscriber(modid = AnnihilationBladeEX.MODID, bus = Bus.MOD, value = Dist.CLIENT)
   public static final class ModBusEvents {
      private ModBusEvents() {
      }

      @SubscribeEvent
      public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
         event.register(TOGGLE_DANKONG_BLINK);
      }
   }
}
