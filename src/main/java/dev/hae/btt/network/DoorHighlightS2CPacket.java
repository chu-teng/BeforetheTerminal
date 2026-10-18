package dev.hae.btt.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import dev.hae.btt.BeforeTheTerminal;





public record DoorHighlightS2CPacket(BlockPos pos, boolean blasted) implements CustomPayload {
    public static final Id<DoorHighlightS2CPacket> ID =
            new Id<>(Identifier.of(BeforeTheTerminal.MOD_ID, "door_highlight"));
    public static final PacketCodec<RegistryByteBuf, DoorHighlightS2CPacket> CODEC = PacketCodec.of(
            DoorHighlightS2CPacket::write, DoorHighlightS2CPacket::read);

    private static void write(DoorHighlightS2CPacket payload, PacketByteBuf buf) {
        buf.writeBlockPos(payload.pos);
        buf.writeBoolean(payload.blasted);
    }

    private static DoorHighlightS2CPacket read(PacketByteBuf buf) {
        return new DoorHighlightS2CPacket(buf.readBlockPos(), buf.readBoolean());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
