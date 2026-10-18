package dev.hae.btt.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;
import dev.hae.btt.game.GameModes;





public record CorpseActionC2SPacket(UUID body, int action) implements CustomPayload {
    public static final Identifier ID_RAW = Identifier.of(GameModes.BEFORE_THE_TERMINAL.ID.getNamespace(), "corpse_action");
    public static final Id<CorpseActionC2SPacket> ID = new Id<>(ID_RAW);
    public static final PacketCodec<RegistryByteBuf, CorpseActionC2SPacket> CODEC = PacketCodec.of(
            CorpseActionC2SPacket::write, CorpseActionC2SPacket::read);

    private static void write(CorpseActionC2SPacket p, PacketByteBuf buf) {
        buf.writeUuid(p.body);
        buf.writeVarInt(p.action);
    }

    private static CorpseActionC2SPacket read(PacketByteBuf buf) {
        return new CorpseActionC2SPacket(buf.readUuid(), buf.readVarInt());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
