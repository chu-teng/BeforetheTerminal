package dev.hae.btt.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import dev.hae.btt.BeforeTheTerminal;

import java.util.UUID;

public record DisguiseC2SPacket(UUID player) implements CustomPayload {
    public static final Identifier DISGUISE_PAYLOAD_ID = Identifier.of(BeforeTheTerminal.MOD_ID, "disguise");
    public static final CustomPayload.Id<DisguiseC2SPacket> ID = new CustomPayload.Id<>(DISGUISE_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, DisguiseC2SPacket> CODEC;

    public DisguiseC2SPacket(UUID player) {
        this.player = player;
    }

    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
        buf.writeUuid(this.player);
    }

    public static DisguiseC2SPacket read(PacketByteBuf buf) {
        return new DisguiseC2SPacket(buf.readUuid());
    }

    public UUID player() {
        return this.player;
    }

    static {
        CODEC = PacketCodec.of(DisguiseC2SPacket::write, DisguiseC2SPacket::read);
    }
}