package dev.hae.btt.combat;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.hae.btt.api.event.BttEvents;
import dev.hae.btt.game.SeatAssignment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import java.util.ArrayDeque;
import java.util.Deque;


public final class DeathResolution implements AutoCloseable {
    private static final ThreadLocal<Deque<DeathResolution>> CALLS = ThreadLocal.withInitial(ArrayDeque::new);
    private final PlayerEntity victim;
    private final PlayerEntity killer;
    private final boolean body;
    private final Identifier reason;
    private boolean confirmed;
    private boolean defenseHeard;

    private DeathResolution(PlayerEntity victim, boolean body, PlayerEntity killer, Identifier reason) {
        this.victim = victim;
        this.body = body;
        this.killer = killer;
        this.reason = reason;
    }

    public static DeathResolution open(PlayerEntity victim, boolean body, PlayerEntity killer, Identifier reason) {
        var scope = new DeathResolution(victim, body, killer, reason);
        CALLS.get().push(scope);
        return scope;
    }

    public static void confirm(PlayerEntity victim) {
        var scopes = CALLS.get();
        if (!scopes.isEmpty() && scopes.peek().victim == victim) scopes.peek().confirmed = true;
        if (scopes.isEmpty()) CALLS.remove();
    }


    public static void shieldBreak(PlayerEntity victim, PlayerEntity killer) {
        blocked(victim, killer, false);
        shieldSound(victim, killer);
    }

    public static void shieldSound(PlayerEntity victim, PlayerEntity killer) {
        if (!(victim instanceof ServerPlayerEntity target)) return;
        var calls = CALLS.get();
        if (!calls.isEmpty() && calls.peek().victim == victim) {
            if (calls.peek().defenseHeard) return;
            calls.peek().defenseHeard = true;
        }
        if (calls.isEmpty()) CALLS.remove();
        for (var listener : target.getServerWorld().getPlayers()) {
            if (listener == victim || listener == killer || listener.squaredDistanceTo(victim) <= 256)
                listener.playSoundToPlayer(net.minecraft.sound.SoundEvents.ITEM_SHIELD_BREAK,
                        net.minecraft.sound.SoundCategory.PLAYERS, 1f, 1f);
        }
    }

    public static void blocked(PlayerEntity victim, PlayerEntity killer, boolean consumed) {
        var scopes = CALLS.get();
        if (!scopes.isEmpty() && scopes.peek().victim == victim)
            dev.hae.btt.advancement.BttAdvancements.blocked(victim, killer, scopes.peek().reason, consumed);
        if (scopes.isEmpty()) CALLS.remove();
    }

    public void finish() {
        if (!confirmed || !(victim instanceof ServerPlayerEntity dead)
                || !SeatAssignment.isBttMode(dead.getWorld())
                || !GameWorldComponent.KEY.get(dead.getWorld()).isRunning()) return;
        confirmed = false;
        var source = killer instanceof ServerPlayerEntity player ? player : null;
        notifyDeath(dead, source, reason, body);
        DeathRules.resolve(dead, killer, reason);
        BttEvents.DeathReaction.EVENT.invoker().onDeath(dead, source, reason);
    }


    public static void notifyDeath(ServerPlayerEntity dead, ServerPlayerEntity source, Identifier reason, boolean body) {
        BttEvents.Death.EVENT.invoker().onDeath(dead, source, reason);
        DeathRules.onConfirmedDeath(dead, reason, body);
    }

    @Override public void close() {
        var scopes = CALLS.get();
        if (scopes.pop() != this) throw new IllegalStateException("Unbalanced death resolution scope");
        if (scopes.isEmpty()) CALLS.remove();
    }
}
