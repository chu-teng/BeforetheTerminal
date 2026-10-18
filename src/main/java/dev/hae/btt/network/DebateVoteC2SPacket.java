package dev.hae.btt.network;

import dev.hae.btt.BeforeTheTerminal;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;







public record DebateVoteC2SPacket(UUID target) implements CustomPayload {
    public static final Id<DebateVoteC2SPacket> ID =
            new Id<>(Identifier.of(BeforeTheTerminal.MOD_ID, "debate_vote"));
    public static final PacketCodec<RegistryByteBuf, DebateVoteC2SPacket> CODEC = PacketCodec.of(
            DebateVoteC2SPacket::write, DebateVoteC2SPacket::read);

    private static void write(DebateVoteC2SPacket p, PacketByteBuf buf) {
        buf.writeUuid(p.target);
    }

    private static DebateVoteC2SPacket read(PacketByteBuf buf) {
        return new DebateVoteC2SPacket(buf.readUuid());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
