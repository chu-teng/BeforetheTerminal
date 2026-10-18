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


    private static final Set<Role> ANYONE_USERS = Set.of(
            Roles.DETECTIVE, Roles.HUNTER, Roles.MESSIAH, Roles.NOVELIST,
            Roles.JOURNALIST, Roles.MAGICIAN, Roles.RIGGER,

            Roles.WITCH);


    public static void onAnyoneAbility(ServerPlayerEntity user, GameWorldComponent gwc) {
        Role role = Roles.effectiveRole(gwc, user);
        if (role == null || !ANYONE_USERS.contains(role)) return;
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


    public static void tick(ServerPlayerEntity player, PlayerState pc) {
        if (pc.folkTicks <= 0) return;
        if (--pc.folkTicks > 0) return;
        pc.folkTarget = "";
        pc.sync();
    }
}
