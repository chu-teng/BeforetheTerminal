package dev.hae.btt.map;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
















public final class MapDimensions {
    private MapDimensions() {}


    public static ServerWorld gameWorldOrOverworld(MinecraftServer server) {
        if (server == null) return null;
        ServerWorld pending = null;
        for (ServerWorld world : server.getWorlds()) {
            GameWorldComponent.GameStatus status = GameWorldComponent.KEY.get(world).getGameStatus();
            if (status == GameWorldComponent.GameStatus.ACTIVE) return world;
            if (pending == null && (status == GameWorldComponent.GameStatus.STARTING
                    || status == GameWorldComponent.GameStatus.STOPPING)) {
                pending = world;
            }
        }
        return pending != null ? pending : server.getOverworld();
    }
}
