package QWQ.QingYi.annihilationblade.blood_prison.network;

import QWQ.QingYi.annihilationblade.blood_prison.client.BloodPrisonClientEffects;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public record BloodPrisonDomainPacket(int durationTicks) {
   public static void encode(BloodPrisonDomainPacket packet, FriendlyByteBuf buffer) {
      buffer.writeVarInt(Math.max(0, packet.durationTicks));
   }

   public static BloodPrisonDomainPacket decode(FriendlyByteBuf buffer) {
      return new BloodPrisonDomainPacket(buffer.readVarInt());
   }

   public static void handle(BloodPrisonDomainPacket packet, Supplier<Context> context) {
      Context ctx = context.get();
      ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> BloodPrisonClientEffects.setDomainTicks(packet.durationTicks)));
      ctx.setPacketHandled(true);
   }
}
