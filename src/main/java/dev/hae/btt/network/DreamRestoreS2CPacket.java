package dev.hae.btt.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;


public record DreamRestoreS2CPacket() implements CustomPayload {
    public static final Id<DreamRestoreS2CPacket> ID = new Id<>(Identifier.of("btt", "dream_restore"));
    public static final PacketCodec<RegistryByteBuf, DreamRestoreS2CPacket> CODEC = PacketCodec.of((p, b) -> {}, b -> new DreamRestoreS2CPacket());
    public Id<? extends CustomPayload> getId() { return ID; }
}
