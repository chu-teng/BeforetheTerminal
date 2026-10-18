package dev.hae.btt.network;

import dev.hae.btt.game.SeatAssignment;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.CustomPayload;















public final class RolePackets {
    private RolePackets() {}

    public static final CustomPayload.Id<DisguiseC2SPacket> DISGUISE = DisguiseC2SPacket.ID;
    public static final CustomPayload.Id<AbilityC2SPacket> ABILITY = AbilityC2SPacket.ID;
    public static final CustomPayload.Id<CorpseEatC2SPacket> CORPSE_EAT = CorpseEatC2SPacket.ID;

    public static void register() {
        PayloadTypeRegistry.playC2S().register(DISGUISE, DisguiseC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(ABILITY, AbilityC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(CORPSE_EAT, CorpseEatC2SPacket.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(DISGUISE, (payload, context) -> dev.hae.btt.ability.TargetedAbilities.witch(context.player(), payload.player()));
        ServerPlayNetworking.registerGlobalReceiver(ABILITY, (payload, context) -> dev.hae.btt.ability.UntargetedAbilities.stowaway(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(CORPSE_EAT, (payload, context) -> dev.hae.btt.ability.CorpseAbilities.scavenge(context.player(), payload.playerBody()));
    }

}
