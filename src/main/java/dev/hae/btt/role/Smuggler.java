package dev.hae.btt.role;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.component.AbilityState;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.mechanic.Attachment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public final class Smuggler {
    private Smuggler() {}

    public static void use(ServerPlayerEntity user) {
        var ability = AbilityState.KEY.get(user);
        if (ability.cooldown > 0) return;
        if (Drunk.isDrunk(user)) { ability.setCooldown(RoleDefs.CD_30S); return; }
        var eye = user.getEyePos();
        var end = eye.add(user.getRotationVec(1).multiply(6));
        var aimed = user.getServerWorld().getPlayers().stream()
                .filter(p -> p != user && GameFunctions.isPlayerAliveAndSurvival(p))
                .filter(p -> p.getBoundingBox().expand(0.1).raycast(eye, end).isPresent())
                .min(java.util.Comparator.comparingDouble(user::squaredDistanceTo));
        if (aimed.isPresent()) {
            var target = Puppeteer.applyRedirect(user, aimed.get());
            if (target == user) return;
            var state = PlayerState.KEY.get(target);
            if (!GameFunctions.isPlayerAliveAndSurvival(target) || state.isAttached() || state.isProjecting()
                    || user.squaredDistanceTo(target) > 36 || !user.canSee(target)) return;
            ability.setCooldown(RoleDefs.CD_30S);
            if (Goon.interceptTarget(user, target)) return;
            state.smugglerHost = user.getUuidAsString();
            state.smugglerReleaseAt = user.getServerWorld().getTime() + 600;
            Kidnapper.attachVictim(user, target, false);
            state.attachKind = "SMUGGLER";
            state.digestible = false; state.digestTicks = 0; state.digestDrain = 0;
            state.sync();
            Attachment.tick(target, user.getServerWorld());
            return;
        }
        var ready = user.getServerWorld().getPlayers().stream()
                .filter(p -> PlayerState.KEY.get(p).isSwallowed()
                        && PlayerState.KEY.get(p).smugglerHost.equals(user.getUuidAsString()))
                .filter(p -> PlayerState.KEY.get(p).smugglerReleaseAt <= user.getServerWorld().getTime())
                .min(java.util.Comparator.comparingLong(p -> PlayerState.KEY.get(p).smugglerReleaseAt));
        if (ready.isPresent()) { ability.setCooldown(RoleDefs.CD_30S); release(ready.get(), user); }
        else user.sendMessage(Text.translatable("btt.action.smuggler.wait").withColor(Roles.SMUGGLER.color()), true);
    }

    public static boolean hiddenAlive(ServerPlayerEntity player) {
        return player.isAlive() && PlayerState.KEY.get(player).isSwallowed()
                && !PlayerState.KEY.get(player).smugglerHost.isEmpty();
    }

    public static void tickWorld(ServerWorld world) {
        var game = GameWorldComponent.KEY.get(world);
        for (var player : world.getPlayers()) {
            var state = PlayerState.KEY.get(player);
            if (state.smugglerHost.isEmpty()) continue;
            if (!state.isSwallowed()) { state.smugglerHost = ""; state.smugglerReleaseAt = 0; continue; }
            if (!"SMUGGLER".equals(state.attachKind) || state.digestible || state.digestTicks != 0 || state.digestDrain != 0) {
                state.attachKind = "SMUGGLER";
                state.digestible = false; state.digestTicks = 0; state.digestDrain = 0;
                state.sync();
            }
            ServerPlayerEntity host;
            try { host = world.getServer().getPlayerManager().getPlayer(java.util.UUID.fromString(state.smugglerHost)); }
            catch (IllegalArgumentException ignored) { host = null; }
            if (!game.isRunning() || host == null || host.getWorld() != world
                    || !GameFunctions.isPlayerAliveAndSurvival(host) || !Roles.hasCurrentRole(game, host, Roles.SMUGGLER)) {
                release(player, host != null && host.getWorld() == world ? host : null);
            } else Attachment.tick(player, world);
        }
    }

    public static void releaseOwned(ServerPlayerEntity host) {
        for (var player : host.getServerWorld().getPlayers())
            if (PlayerState.KEY.get(player).smugglerHost.equals(host.getUuidAsString())) release(player, host);
    }

    public static void release(ServerPlayerEntity player, ServerPlayerEntity host) {
        var state = PlayerState.KEY.get(player);
        var mood = dev.doctor4t.wathe.cca.PlayerMoodComponent.KEY.get(player);
        float remaining = mood.getMood();
        state.smugglerHost = "";
        state.smugglerReleaseAt = 0;
        if (state.isSwallowed()) {
            Kidnapper.releaseWithMood(player, state, host, remaining);
        }
        state.sync();
    }
}
