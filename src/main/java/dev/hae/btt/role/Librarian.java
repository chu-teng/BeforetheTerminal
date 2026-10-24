package dev.hae.btt.role;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import java.util.*;


public final class Librarian {
    private Librarian() {}
    private record Sample(long expires, double extra) {}
    private static final Map<UUID, Sample> SAMPLES = new HashMap<>();
    private static final Set<UUID> ISOLATED = java.util.concurrent.ConcurrentHashMap.newKeySet();

    public static boolean mayBeIsolated(UUID player) { return ISOLATED.contains(player); }

    public static void refreshListeners(ServerWorld world) {
        var effects = dev.hae.btt.component.GameState.KEY.get(world).roleEffects;
        for (var player : world.getPlayers()) {
            if (effects.getLong("isolated:" + player.getUuidAsString()) > world.getTime()) ISOLATED.add(player.getUuid());
            else ISOLATED.remove(player.getUuid());
        }
    }

    public static void clear(ServerWorld world) {
        for (var player : world.getPlayers()) {
            SAMPLES.remove(player.getUuid());
            ISOLATED.remove(player.getUuid());
        }
    }

    public static double extraDrain(double rms) {

        return rms <= 0.0001 ? 0 : Math.min(7, (rms - 0.0001) / 0.0099 * 7);
    }

    public static void heard(ServerPlayerEntity speaker, ServerPlayerEntity listener, double rms) {
        if (!dev.hae.btt.trait.TraitRules.active(listener) || !(GameFunctions.isPlayerAliveAndSurvival(listener) || dev.hae.btt.component.PlayerState.KEY.get(listener).isSwallowed())
                || dev.hae.btt.component.GameState.KEY.get(listener.getWorld()).roleEffects
                    .getLong("isolated:" + listener.getUuidAsString()) <= listener.getWorld().getTime()
                || speaker == listener || speaker.getWorld() != listener.getWorld()) return;
        double extra = extraDrain(rms);
        if (extra <= 0) return;
        long now = speaker.getWorld().getTime();
        var previous = SAMPLES.get(speaker.getUuid());
        if (previous != null && previous.expires > now) extra = Math.max(extra, previous.extra);
        SAMPLES.put(speaker.getUuid(), new Sample(now + 10, extra));
    }


    public static void tickSpeaker(ServerPlayerEntity speaker) {
        var sample = SAMPLES.get(speaker.getUuid());
        if (sample == null) return;
        if (sample.expires <= speaker.getWorld().getTime()) { SAMPLES.remove(speaker.getUuid()); return; }
        if (!(GameFunctions.isPlayerAliveAndSurvival(speaker) || dev.hae.btt.component.PlayerState.KEY.get(speaker).isSwallowed())) return;
        var mood = PlayerMoodComponent.KEY.get(speaker);
        mood.setMood(mood.getMood() - (float)sample.extra * dev.hae.btt.game.Needs.drainingCount(speaker) * GameConstants.MOOD_DRAIN);
    }

    public static void clear() { SAMPLES.clear(); ISOLATED.clear(); }
}
