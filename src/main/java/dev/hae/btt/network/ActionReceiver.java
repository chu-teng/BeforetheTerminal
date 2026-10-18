package dev.hae.btt.network;

import dev.hae.btt.ability.CorpseAbilities;
import dev.hae.btt.ability.SelectionAbilities;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.role.Architect;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;


public final class ActionReceiver {
    private ActionReceiver() {}
    public static void register() {
        PayloadTypeRegistry.playC2S().register(MemoryGuessC2SPacket.ID, MemoryGuessC2SPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(MemoryGuessC2SPacket.ID,
                (payload, context) -> dev.hae.btt.encounter.Memory.guess(context.player(), payload.role()));
        PayloadTypeRegistry.playC2S().register(SelectC2SPacket.ID, SelectC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(CorpseActionC2SPacket.ID, CorpseActionC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(MultiSelectC2SPacket.ID, MultiSelectC2SPacket.CODEC);

        PayloadTypeRegistry.playC2S().register(ConsoleC2SPacket.ID, ConsoleC2SPacket.CODEC);

        PayloadTypeRegistry.playC2S().register(LetterC2SPacket.ID, LetterC2SPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(LetterC2SPacket.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            player.server.execute(() -> {

                if (dev.hae.btt.game.Debate.handleLetterVote(player, payload)) return;
                dev.hae.btt.game.LetterNotes.apply(player, payload);
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(ConsoleC2SPacket.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            player.server.execute(() -> {

                if (!payload.open()) PlayerState.KEY.get(player).closeConsole();
            });
        });

        PayloadTypeRegistry.playC2S().register(DebateVoteC2SPacket.ID, DebateVoteC2SPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(DebateVoteC2SPacket.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            player.server.execute(() -> dev.hae.btt.game.Debate.vote(player, payload.target()));
        });
        Architect.register();
        ServerPlayNetworking.registerGlobalReceiver(CorpseActionC2SPacket.ID,
                (payload, context) -> CorpseAbilities.use(context.player(), payload.body()));

        ServerPlayNetworking.registerGlobalReceiver(MultiSelectC2SPacket.ID, (payload, context) -> SelectionAbilities.multi(context.player(), payload.picks()));
        ServerPlayNetworking.registerGlobalReceiver(SelectC2SPacket.ID,
                (payload, context) -> SelectionAbilities.select(context.player(), payload.target(), payload.guess()));
    }
}
