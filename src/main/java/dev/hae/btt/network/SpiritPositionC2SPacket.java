package dev.hae.btt.network;

import dev.hae.btt.component.PlayerState;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.role.Roles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;


public record SpiritPositionC2SPacket(double x, double y, double z) implements CustomPayload {
    public static final Id<SpiritPositionC2SPacket> ID = new Id<>(Identifier.of("btt", "spirit_position"));
    public static final PacketCodec<RegistryByteBuf, SpiritPositionC2SPacket> CODEC = PacketCodec.of(
            (p, b) -> { b.writeDouble(p.x); b.writeDouble(p.y); b.writeDouble(p.z); },
            b -> new SpiritPositionC2SPacket(b.readDouble(), b.readDouble(), b.readDouble()));
    public Id<? extends CustomPayload> getId() { return ID; }
    public void receive(ServerPlayerEntity player) {
        if (!SeatAssignment.isBttMode(player.getWorld()) || !GameFunctions.isPlayerAliveAndSurvival(player)
                || !GameWorldComponent.KEY.get(player.getWorld()).isRole(player, Roles.MUYUUBYOU)) return;
        var state = PlayerState.KEY.get(player);
        if (!state.isProjecting() || state.sleepwalkTicks <= 0 || !Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) return;
        if (new Vec3d(x, y, z).squaredDistanceTo(state.bodyX, state.bodyY, state.bodyZ) > 900.01) return;
        if (y < player.getWorld().getBottomY() || y >= player.getWorld().getTopY()) return;
        state.dreamX = x; state.dreamY = y; state.dreamZ = z;
    }
}
