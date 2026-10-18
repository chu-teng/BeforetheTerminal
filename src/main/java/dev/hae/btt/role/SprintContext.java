package dev.hae.btt.role;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.hae.btt.game.SeatAssignment;
import net.minecraft.entity.player.PlayerEntity;


public final class SprintContext {
    private SprintContext() {}
    private static final ThreadLocal<PlayerEntity> PLAYER = new ThreadLocal<>();
    public static void run(PlayerEntity player, Runnable action) {
        PlayerEntity previous = PLAYER.get();
        PLAYER.set(player);
        try { action.run(); }
        finally { if (previous == null) PLAYER.remove(); else PLAYER.set(previous); }
    }
    public static int limit(dev.doctor4t.wathe.api.Role role, int original) {
        PlayerEntity player = PLAYER.get();
        if (player == null || !SeatAssignment.isBttMode(player.getWorld())) return original;
        GameWorldComponent game = GameWorldComponent.KEY.get(player.getWorld());
        return game.isRunning() && game.getRole(player) == role ? Roles.maxSprintTimeFor(game, player, original) : original;
    }
}
