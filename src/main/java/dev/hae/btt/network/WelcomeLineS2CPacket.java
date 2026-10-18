package dev.hae.btt.network;

import dev.hae.btt.BeforeTheTerminal;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;












public record WelcomeLineS2CPacket(int kind, String arg1, String arg2) implements CustomPayload {

    public static final Id<WelcomeLineS2CPacket> ID =
            new Id<>(Identifier.of(BeforeTheTerminal.MOD_ID, "welcome_line"));

    public static final PacketCodec<RegistryByteBuf, WelcomeLineS2CPacket> CODEC = PacketCodec.of(
            WelcomeLineS2CPacket::write, WelcomeLineS2CPacket::read);

    private static void write(WelcomeLineS2CPacket payload, PacketByteBuf buf) {
        buf.writeVarInt(payload.kind);
        buf.writeString(payload.arg1 == null ? "" : payload.arg1);
        buf.writeString(payload.arg2 == null ? "" : payload.arg2);
    }

    private static WelcomeLineS2CPacket read(PacketByteBuf buf) {
        return new WelcomeLineS2CPacket(buf.readVarInt(), buf.readString(), buf.readString());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
