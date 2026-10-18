package dev.hae.btt.role;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.component.AbilityState;
import dev.hae.btt.component.PlayerState;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.List;
import java.util.UUID;


public final class Puppeteer {
    private Puppeteer() {}

    public static void manipulate(ServerPlayerEntity user, GameWorldComponent game, List<UUID> picks) {
        if (AbilityState.KEY.get(user).cooldown > 0 || picks.size() != 2) return;
        if (!(user.getServerWorld().getPlayerByUuid(picks.get(0)) instanceof ServerPlayerEntity puppet)
                || !(user.getServerWorld().getPlayerByUuid(picks.get(1)) instanceof ServerPlayerEntity recipient)) return;
        if (!GameFunctions.isPlayerAliveAndSurvival(puppet) || !GameFunctions.isPlayerAliveAndSurvival(recipient)) return;
        var state = PlayerState.KEY.get(puppet);
        state.redirectTarget = recipient.getUuidAsString();
        state.redirectOwner = user.getUuidAsString();
        state.sync();
        AbilityState.KEY.get(user).setCooldown(RoleDefs.CD_1MIN);
    }

    private static ServerPlayerEntity consume(ServerPlayerEntity user) {
        var state = PlayerState.KEY.get(user);
        if (state.redirectTarget.isEmpty()) return null;
        String raw = state.redirectTarget;
        state.redirectTarget = "";
        state.redirectOwner = "";
        state.sync();
        try {
            if (user.getServerWorld().getPlayerByUuid(UUID.fromString(raw)) instanceof ServerPlayerEntity target
                    && target.isAlive() && GameFunctions.isPlayerAliveAndSurvival(target)) return target;
        } catch (IllegalArgumentException ignored) {}
        return null;
    }

    public static ServerPlayerEntity applyRedirect(ServerPlayerEntity user, ServerPlayerEntity original) {
        var target = consume(user);
        return target == null ? original : target;
    }

    public static List<UUID> redirectPicks(ServerPlayerEntity user, List<UUID> original) {
        if (original.isEmpty()) return original;
        var target = consume(user);
        return target == null ? original : java.util.Collections.nCopies(original.size(), target.getUuid());
    }

    public static UUID redirectUuid(ServerPlayerEntity user, UUID original) {
        if (original == null) return null;
        if (!(user.getServerWorld().getPlayerByUuid(original) instanceof ServerPlayerEntity target)) return original;
        return applyRedirect(user, target).getUuid();
    }
}
