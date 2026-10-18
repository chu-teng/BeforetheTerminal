package dev.hae.btt.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record AdminSettingC2SPacket(String category, String value) implements CustomPayload {
    public static final Id<AdminSettingC2SPacket> ID = new Id<>(Identifier.of("btt", "admin_setting"));
    public static final PacketCodec<RegistryByteBuf, AdminSettingC2SPacket> CODEC = PacketCodec.of(
            (packet, buf) -> { buf.writeString(packet.category, 32); buf.writeString(packet.value, 64); },
            buf -> new AdminSettingC2SPacket(buf.readString(32), buf.readString(64)));
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
