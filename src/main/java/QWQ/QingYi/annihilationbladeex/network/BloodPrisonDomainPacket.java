package QWQ.QingYi.annihilationbladeex.network;

import QWQ.QingYi.annihilationbladeex.AnnihilationBladeEX;
import QWQ.QingYi.annihilationbladeex.client.BloodPrisonClientEffects;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BloodPrisonDomainPacket(int ticks) implements CustomPacketPayload {
    public static final Type<BloodPrisonDomainPacket> TYPE =
            new Type<>(AnnihilationBladeEX.prefix("blood_prison_domain"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BloodPrisonDomainPacket> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, BloodPrisonDomainPacket::ticks, BloodPrisonDomainPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(BloodPrisonDomainPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> BloodPrisonClientEffects.setDomainTicks(packet.ticks()));
    }
}
