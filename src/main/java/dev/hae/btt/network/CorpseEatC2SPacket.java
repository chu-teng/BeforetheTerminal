package dev.hae.btt.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import dev.hae.btt.BeforeTheTerminal;

import java.util.UUID;

public record CorpseEatC2SPacket(UUID playerBody) implements CustomPayload {
    public static final Identifier CORPSE_EAT_PAYLOAD_ID = Identifier.of(BeforeTheTerminal.MOD_ID, "corpse_eat");
    public static final Id<CorpseEatC2SPacket> ID = new Id<>(CORPSE_EAT_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, CorpseEatC2SPacket> CODEC;

    public CorpseEatC2SPacket(UUID playerBody) {
        this.playerBody = playerBody;
    }

    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
        buf.writeUuid(this.playerBody);
    }

    public static CorpseEatC2SPacket read(PacketByteBuf buf) {
        return new CorpseEatC2SPacket(buf.readUuid());
    }

    public UUID playerBody() {
        return this.playerBody;
    }

    static {
        CODEC = PacketCodec.of(CorpseEatC2SPacket::write, CorpseEatC2SPacket::read);
    }
}