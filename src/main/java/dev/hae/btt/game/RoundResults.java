package dev.hae.btt.game;

import dev.doctor4t.wathe.cca.GameRoundEndComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.hae.btt.component.GameState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;


public final class RoundResults {
    private RoundResults() {}

    public static void register() {
        dev.hae.btt.api.event.BttEvents.GameEnded.EVENT.register((world, ending) -> capture(world));
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            for (var world : server.getWorlds()) apply(world);
        });
    }

    public static void capture(ServerWorld world) {
        var state = GameState.KEY.get(world);
        if (GameWorldComponent.KEY.get(world).getRoles().isEmpty()
                || dev.hae.btt.victory.Endings.keyOf(state.lastEnding) == null) return;
        var round = new NbtCompound();
        GameRoundEndComponent.KEY.get(world).writeToNbt(round, world.getRegistryManager());
        if (round.getList("players", 10).isEmpty()) return;
        var snapshot = new NbtCompound();
        snapshot.put("round", round);
        snapshot.putString("ending", state.lastEnding);
        snapshot.putString("winners", state.winners);
        snapshot.putString("numbers", state.letterNumbers);
        GameState.KEY.get(world.getServer().getOverworld()).serverLastRound = snapshot;
        state.roundResultBtt = true;
        state.sync();
        for (var destination : world.getServer().getWorlds()) if (destination != world) apply(destination);
    }

    public static void apply(ServerWorld world) {
        if (GameWorldComponent.KEY.get(world).isRunning()) return;
        var snapshot = GameState.KEY.get(world.getServer().getOverworld()).serverLastRound;
        if (snapshot.isEmpty()) return;
        var state = GameState.KEY.get(world);
        state.lastEnding = snapshot.getString("ending");
        state.winners = snapshot.getString("winners");
        state.letterNumbers = snapshot.getString("numbers");
        state.roundResultBtt = true;
        GameRoundEndComponent.KEY.get(world).readFromNbt(snapshot.getCompound("round"), world.getRegistryManager());
        GameRoundEndComponent.KEY.get(world).sync();
        state.sync();
    }
}
