package dev.hae.btt.role;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Set;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.game.GameEvents;
import dev.hae.btt.network.ActionReceiver;


public final class Folklorist {
    private Folklorist() {}


    private static final int REVEAL_TICKS = 200;


    public static void onAnyoneAbility(ServerPlayerEntity user, GameWorldComponent gwc) {
        Role role = Roles.effectiveRole(gwc, user);
        if (role == null || !(RoleDefs.isAnyTarget(role) || role == Roles.WITCH)) return;
        for (ServerPlayerEntity p : user.getServerWorld().getPlayers()) {
            if (p == user) continue;
            if (!GameFunctions.isPlayerAliveAndSurvival(p)) continue;
            if (!gwc.isRole(p, Roles.FOLKLORIST)) continue;
            PlayerState pc = PlayerState.KEY.get(p);


            if (pc.isDrunk()) continue;
            pc.folkTarget = user.getUuid().toString();
            pc.folkTicks = REVEAL_TICKS;
            pc.sync();
        }
    }

    public static void targeted(ServerPlayerEntity user, java.util.List<ServerPlayerEntity> targets) {
        var game = GameWorldComponent.KEY.get(user.getWorld());
        var role = Roles.effectiveRole(game, user);
        if (!(RoleDefs.isAnyTarget(role) || role == Roles.WITCH)) return;
        String names = targets.stream().distinct().map(p -> p.getName().getString()).collect(java.util.stream.Collectors.joining("、"));
        if (names.isEmpty()) return;
        for (var watcher : user.getServerWorld().getPlayers()) {
            if (game.isRole(watcher, Roles.FOLKLORIST) && GameFunctions.isPlayerAliveAndSurvival(watcher)
                    && !PlayerState.KEY.get(watcher).isDrunk()) watcher.sendMessage(
                    net.minecraft.text.Text.translatable("btt.action.folklorist.target", names).withColor(Roles.FOLKLORIST.color()), true);
        }
    }


    public static void tick(ServerPlayerEntity player, PlayerState pc) {
        if (pc.folkTicks <= 0) return;
        if (--pc.folkTicks > 0) return;
        pc.folkTarget = "";
        pc.sync();
    }
}
