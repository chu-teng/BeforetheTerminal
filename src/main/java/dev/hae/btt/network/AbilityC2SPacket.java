package dev.hae.btt.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import dev.hae.btt.BeforeTheTerminal;

import java.util.UUID;

public record AbilityC2SPacket() implements CustomPayload {
    public static final Identifier ABILITY_PAYLOAD_ID = Identifier.of(BeforeTheTerminal.MOD_ID, "ability");
    public static final Id<AbilityC2SPacket> ID = new Id<>(ABILITY_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, AbilityC2SPacket> CODEC;

    public AbilityC2SPacket() {
    }

    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {

    }

    public static AbilityC2SPacket read(PacketByteBuf buf) {
        return new AbilityC2SPacket();
    }

    static {
        CODEC = PacketCodec.of(AbilityC2SPacket::write, AbilityC2SPacket::read);
    }
}