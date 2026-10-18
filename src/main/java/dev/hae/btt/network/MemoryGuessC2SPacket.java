package dev.hae.btt.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;


public record MemoryGuessC2SPacket(String role) implements CustomPayload {
    public static final Id<MemoryGuessC2SPacket> ID = new Id<>(Identifier.of("btt", "memory_guess"));
    public static final PacketCodec<RegistryByteBuf, MemoryGuessC2SPacket> CODEC = PacketCodec.of(
            (payload, buf) -> buf.writeString(payload.role, 64),
            buf -> new MemoryGuessC2SPacket(buf.readString(64)));
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
