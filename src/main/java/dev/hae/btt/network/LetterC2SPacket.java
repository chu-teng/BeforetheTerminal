package dev.hae.btt.network;

import dev.hae.btt.BeforeTheTerminal;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;











public record LetterC2SPacket(UUID target, String value, int status) implements CustomPayload {

    public static final String CLEAR = "-";

    public static final Id<LetterC2SPacket> ID =
            new Id<>(Identifier.of(BeforeTheTerminal.MOD_ID, "letter"));
    public static final PacketCodec<RegistryByteBuf, LetterC2SPacket> CODEC = PacketCodec.of(
            LetterC2SPacket::write, LetterC2SPacket::read);

    private static void write(LetterC2SPacket p, PacketByteBuf buf) {
        buf.writeUuid(p.target);
        buf.writeString(p.value);
        buf.writeVarInt(p.status);
    }

    private static LetterC2SPacket read(PacketByteBuf buf) {
        return new LetterC2SPacket(buf.readUuid(), buf.readString(), buf.readVarInt());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
