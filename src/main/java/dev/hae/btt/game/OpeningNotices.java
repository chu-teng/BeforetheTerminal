package dev.hae.btt.game;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.component.GameState;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.role.Roles;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;




public final class OpeningNotices {
    private OpeningNotices() {}

    private static final int RESEND_INTERVAL = 20;

    private static final int WELCOME_TEXT_TICKS = 180;

    public static void enqueue(ServerPlayerEntity player, Text text) {
        var state = PlayerState.KEY.get(player);
        state.openingMessages.add(Text.Serialization.toJsonString(text, player.getRegistryManager()));
        state.openingMessageAt = 0;
    }


    public static void send(ServerPlayerEntity player, Text text) {
        if (player.getWorld().getTime() < GameState.KEY.get(player.getWorld()).roleEffects.getLong("openingUntil")) enqueue(player, text);
        else player.sendMessage(text, true);
    }

    public static void tick(ServerPlayerEntity player) {
        var state = PlayerState.KEY.get(player);
        if (state.openingMessages.isEmpty()) return;
        long now = player.getWorld().getTime();
        long until = GameState.KEY.get(player.getWorld()).roleEffects.getLong("openingUntil");

        if (!GameFunctions.isPlayerAliveAndSurvival(player) || now >= until) {
            if (state.openingMessageAt != 0) player.sendMessage(Text.empty(), true);
            state.openingMessages.clear();
            state.openingMessageAt = 0;
            return;
        }
        if (now < until - WELCOME_TEXT_TICKS || now < state.openingMessageAt) return;
        int index = state.openingMessages.size() == 1
                ? 0 : (int) ((now / RESEND_INTERVAL) % state.openingMessages.size());
        var text = Text.Serialization.fromJson(state.openingMessages.get(index), player.getRegistryManager());
        state.openingMessageAt = now + RESEND_INTERVAL;
        if (text != null) player.sendMessage(text, true);
    }

    public static void direction(ServerPlayerEntity player, boolean nearest) {
        if (PlayerState.KEY.get(player).isDrunk()) return;
        var game = GameWorldComponent.KEY.get(player.getWorld());
        var state = GameState.KEY.get(player.getWorld());
        var killers = player.getServerWorld().getPlayers().stream().filter(p -> {
            var kind = Roles.kindOfReal(game, p);
            return p != player && kind != null && kind.isKillerSeat();
        }).toList();
        if (killers.isEmpty()) return;
        int own = state.numberOf(player.getUuid());
        int head = 0, tail = 0;
        if (nearest) {
            int distance = killers.stream().mapToInt(p -> Math.abs(state.numberOf(p.getUuid()) - own)).min().orElse(0);
            for (var p : killers) if (Math.abs(state.numberOf(p.getUuid()) - own) == distance) {
                if (state.numberOf(p.getUuid()) < own) head++; else tail++;
            }
        } else {
            double middle = (state.letterNumbers.split(",").length + 1) / 2.0;
            double average = killers.stream().mapToInt(p -> state.numberOf(p.getUuid())).average().orElse(middle);
            head = average < middle ? 1 : 0; tail = average > middle ? 1 : 0;
        }
        boolean front = head > tail || head == tail && player.getRandom().nextBoolean();
        enqueue(player, Text.translatable(nearest ? "btt.action.attendant.direction" : "btt.action.conductor.direction",
                Text.translatable(front ? "btt.direction.head" : "btt.direction.tail"))
                .withColor(game.getRole(player).color()));
    }
}
