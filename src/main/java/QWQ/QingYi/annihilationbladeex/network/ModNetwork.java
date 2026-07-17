package QWQ.QingYi.annihilationbladeex.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class ModNetwork {
    private ModNetwork() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToClient(BloodPrisonDomainPacket.TYPE, BloodPrisonDomainPacket.STREAM_CODEC, BloodPrisonDomainPacket::handle)
                .playToServer(DankongBlinkModePacket.TYPE, DankongBlinkModePacket.STREAM_CODEC, DankongBlinkModePacket::handle);
    }

    public static void sendBloodPrisonDomain(ServerPlayer player, int ticks) {
        PacketDistributor.sendToPlayer(player, new BloodPrisonDomainPacket(ticks));
    }
}
