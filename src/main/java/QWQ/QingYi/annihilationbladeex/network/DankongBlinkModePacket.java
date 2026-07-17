package QWQ.QingYi.annihilationbladeex.network;

import QWQ.QingYi.annihilationbladeex.AnnihilationBladeEX;
import QWQ.QingYi.annihilationbladeex.annihilation_blade.specialeffect.Dankong;
import QWQ.QingYi.annihilationbladeex.common.AnnihilationBladeItemSupport;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DankongBlinkModePacket(boolean enabled) implements CustomPacketPayload {
   public static final Type<DankongBlinkModePacket> TYPE = new Type<>(AnnihilationBladeEX.prefix("dankong_blink_mode"));
   public static final StreamCodec<RegistryFriendlyByteBuf, DankongBlinkModePacket> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.BOOL,
      DankongBlinkModePacket::enabled,
      DankongBlinkModePacket::new
   );

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(DankongBlinkModePacket packet, IPayloadContext context) {
      context.enqueueWork(() -> {
         if (context.player() instanceof ServerPlayer player && AnnihilationBladeItemSupport.isHoldingAnnihilationBlade(player)) {
            Dankong.setBlinkEnabled(player, packet.enabled());
         }
      });
   }
}
