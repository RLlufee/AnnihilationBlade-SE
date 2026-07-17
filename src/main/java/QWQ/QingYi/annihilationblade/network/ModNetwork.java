package QWQ.QingYi.annihilationblade.network;

import QWQ.QingYi.annihilationblade.blood_prison.network.BloodPrisonDomainPacket;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
   @SuppressWarnings("unused")
   private static final String PROTOCOL_VERSION = "1";
   private static int messageId;
   public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
      ResourceLocation.fromNamespaceAndPath("annihilationblade", "main"), () -> "1", "1"::equals, "1"::equals
   );

   private ModNetwork() {
   }

   public static void register() {
      CHANNEL.registerMessage(
         messageId++,
         BloodPrisonDomainPacket.class,
         BloodPrisonDomainPacket::encode,
         BloodPrisonDomainPacket::decode,
         BloodPrisonDomainPacket::handle,
         Optional.of(NetworkDirection.PLAY_TO_CLIENT)
      );
      CHANNEL.registerMessage(
         messageId++,
         DankongBlinkModePacket.class,
         DankongBlinkModePacket::encode,
         DankongBlinkModePacket::decode,
         DankongBlinkModePacket::handle,
         Optional.of(NetworkDirection.PLAY_TO_SERVER)
      );
   }

   public static void sendBloodPrisonDomain(ServerPlayer player, int durationTicks) {
      CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new BloodPrisonDomainPacket(durationTicks));
   }
}
