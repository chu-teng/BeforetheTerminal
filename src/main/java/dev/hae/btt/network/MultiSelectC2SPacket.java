package dev.hae.btt.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import dev.hae.btt.game.GameModes;
import dev.hae.btt.game.SeatAssignment;
import io.netty.handler.codec.DecoderException;





public record MultiSelectC2SPacket(List<UUID> picks) implements CustomPayload {
    public static final Identifier ID_RAW = Identifier.of(GameModes.BEFORE_THE_TERMINAL.ID.getNamespace(), "judge");
    public static final Id<MultiSelectC2SPacket> ID = new Id<>(ID_RAW);
    public static final PacketCodec<RegistryByteBuf, MultiSelectC2SPacket> CODEC = PacketCodec.of(
            MultiSelectC2SPacket::write, MultiSelectC2SPacket::read);

    private static void write(MultiSelectC2SPacket p, PacketByteBuf buf) {
        buf.writeVarInt(p.picks.size());
        for (UUID uuid : p.picks) buf.writeUuid(uuid);
    }

    private static MultiSelectC2SPacket read(PacketByteBuf buf) {
        int n = buf.readVarInt();
        if (n < 0 || n > SeatAssignment.MAX_PLAYERS || n > buf.readableBytes() / 16) {
            throw new DecoderException("Invalid BTT selection size: " + n);
        }
        List<UUID> picks = new ArrayList<>(n);
        for (int i = 0; i < n; i++) picks.add(buf.readUuid());
        return new MultiSelectC2SPacket(picks);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
