package dev.hae.btt.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;
import dev.hae.btt.game.GameModes;





public record SelectC2SPacket(UUID target, String guess) implements CustomPayload {
    public static final Identifier ID_RAW = Identifier.of(GameModes.BEFORE_THE_TERMINAL.ID.getNamespace(), "select");
    public static final Id<SelectC2SPacket> ID = new Id<>(ID_RAW);
    public static final PacketCodec<RegistryByteBuf, SelectC2SPacket> CODEC = PacketCodec.of(
            SelectC2SPacket::write, SelectC2SPacket::read);

    private static void write(SelectC2SPacket p, PacketByteBuf buf) {
        buf.writeUuid(p.target);
        buf.writeString(p.guess);
    }

    private static SelectC2SPacket read(PacketByteBuf buf) {
        return new SelectC2SPacket(buf.readUuid(), buf.readString());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
