package dev.hae.btt.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record AdminMenuS2CPacket(String category, String selected, String mode) implements CustomPayload {
    public static final Id<AdminMenuS2CPacket> ID = new Id<>(Identifier.of("btt", "admin_menu"));
    public static final PacketCodec<RegistryByteBuf, AdminMenuS2CPacket> CODEC = PacketCodec.of(
            (packet, buf) -> { buf.writeString(packet.category, 32); buf.writeString(packet.selected, 64); buf.writeString(packet.mode, 16); },
            buf -> new AdminMenuS2CPacket(buf.readString(32), buf.readString(64), buf.readString(16)));
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
