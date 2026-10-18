package dev.hae.btt.network;

import dev.hae.btt.BeforeTheTerminal;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;


public record ConsoleC2SPacket(boolean open) implements CustomPayload {

    public static final Id<ConsoleC2SPacket> ID =
            new Id<>(Identifier.of(BeforeTheTerminal.MOD_ID, "console"));
    public static final PacketCodec<RegistryByteBuf, ConsoleC2SPacket> CODEC = PacketCodec.of(
            ConsoleC2SPacket::write, ConsoleC2SPacket::read);

    private static void write(ConsoleC2SPacket payload, PacketByteBuf buf) {
        buf.writeBoolean(payload.open);
    }

    private static ConsoleC2SPacket read(PacketByteBuf buf) {
        return new ConsoleC2SPacket(buf.readBoolean());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
