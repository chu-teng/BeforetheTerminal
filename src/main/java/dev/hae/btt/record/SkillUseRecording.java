package dev.hae.btt.record;

import net.minecraft.server.network.ServerPlayerEntity;
import java.util.ArrayDeque;
import java.util.Deque;


public final class SkillUseRecording implements AutoCloseable {
    private static final ThreadLocal<Deque<SkillUseRecording>> SCOPES = ThreadLocal.withInitial(ArrayDeque::new);
    private final ServerPlayerEntity user;
    private boolean spent;
    private Runnable pending;

    private SkillUseRecording(ServerPlayerEntity user) {
        this.user = user;
        SCOPES.get().push(this);
    }

    public static SkillUseRecording begin(ServerPlayerEntity user) { return new SkillUseRecording(user); }

    public static void capture(ServerPlayerEntity user, Runnable record) {
        var scopes = SCOPES.get();
        for (var scope : scopes) {
            if (scope.user == user) { scope.pending = record; return; }
        }
        if (scopes.isEmpty()) SCOPES.remove();
    }

    public static void cooldownSpent(net.minecraft.entity.player.PlayerEntity player) {
        if (dev.hae.btt.role.RoleTransitions.isConverting(player)) return;
        var scopes = SCOPES.get();
        for (var scope : scopes) if (scope.user == player) scope.spent = true;
        if (scopes.isEmpty()) SCOPES.remove();
    }

    @Override public void close() {
        var scopes = SCOPES.get();
        if (scopes.pop() != this) throw new IllegalStateException("Unbalanced skill recording scope");
        if (scopes.isEmpty()) SCOPES.remove();
        if (pending != null && spent) pending.run();
    }
}
