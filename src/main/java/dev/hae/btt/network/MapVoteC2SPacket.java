package dev.hae.btt.network;

import dev.hae.btt.BeforeTheTerminal;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import dev.hae.btt.map.MapVoting;





public record MapVoteC2SPacket(int index) implements CustomPayload {
    public static final Id<MapVoteC2SPacket> ID =
            new Id<>(Identifier.of(BeforeTheTerminal.MOD_ID, "map_vote"));
    public static final PacketCodec<RegistryByteBuf, MapVoteC2SPacket> CODEC =
            PacketCodec.of(
                    (payload, buf) -> buf.writeVarInt(payload.index()),
                    buf -> new MapVoteC2SPacket(buf.readVarInt()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            if (!(player.getWorld() instanceof net.minecraft.server.world.ServerWorld sw)) return;
            MapVoting.castVote(sw, player.getUuid(), payload.index());
        });
    }
}
