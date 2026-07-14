package QWQ.QingYi.annihilationblade.network;

import QWQ.QingYi.annihilationblade.annihilation_blade.specialeffect.Dankong;
import QWQ.QingYi.annihilationblade.common.AnnihilationBladeItemSupport;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public record DankongBlinkModePacket(boolean enabled) {
   public static void encode(DankongBlinkModePacket packet, FriendlyByteBuf buffer) {
      buffer.writeBoolean(packet.enabled);
   }

   public static DankongBlinkModePacket decode(FriendlyByteBuf buffer) {
      return new DankongBlinkModePacket(buffer.readBoolean());
   }

   public static void handle(DankongBlinkModePacket packet, Supplier<Context> context) {
      Context ctx = context.get();
      ctx.enqueueWork(() -> {
         ServerPlayer player = ctx.getSender();
         if (player != null && AnnihilationBladeItemSupport.isHoldingAnnihilationBlade(player)) {
            Dankong.setBlinkEnabled(player, packet.enabled);
         }
      });
      ctx.setPacketHandled(true);
   }
}
