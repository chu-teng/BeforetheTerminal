package dev.hae.btt.role;

import dev.hae.btt.combat.DeathReasons;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.game.GameEvents;
import dev.hae.btt.mechanic.SpiritWalk;














public final class Astral {
    private Astral() {}


    private static final double RANGE = 3.0;


    public static void toggle(ServerPlayerEntity user, GameWorldComponent gwc) {
        PlayerState pc = PlayerState.KEY.get(user);
        if (!pc.isProjecting()) {
            refresh(user, gwc);
            SpiritWalk.toggle(user);
            return;
        }
        ServerPlayerEntity victim = beside(user, pc);


        SpiritWalk.returnToBody(user, null);
        if (victim != null) {

            GameFunctions.killPlayer(victim, true, user, DeathReasons.MAGIC);
            refresh(user, gwc);
        }
    }


    public static void tick(ServerPlayerEntity user, GameWorldComponent gwc) {
        PlayerState pc = PlayerState.KEY.get(user);
        if (!alive(user, pc.astralPassenger) || !alive(user, pc.astralPassenger2) || !alive(user, pc.astralKiller)) {
            refresh(user, gwc);
        }
    }





    private static void refresh(ServerPlayerEntity user, GameWorldComponent gwc) {
        PlayerState pc = PlayerState.KEY.get(user);
        List<ServerPlayerEntity> passengers = new ArrayList<>();
        List<ServerPlayerEntity> killers = new ArrayList<>();
        for (ServerPlayerEntity p : user.getServerWorld().getPlayers()) {
            if (p == user || !GameFunctions.isPlayerAliveAndSurvival(p)) continue;
            var role = gwc.getRole(p);
            if (role == null) continue;
            if (Roles.isKillerCampFor(gwc, p)) killers.add(p);
            else if (Roles.isPassengerCampFor(gwc, p)) passengers.add(p);
        }
        if (alive(user, pc.astralPassenger) && alive(user, pc.astralPassenger2) && alive(user, pc.astralKiller)) {
            return;
        }
        if (!alive(user, pc.astralPassenger) && !passengers.isEmpty()) {
            ServerPlayerEntity pick = passengers.remove(user.getRandom().nextInt(passengers.size()));
            pc.astralPassenger = pick.getUuidAsString();
        }
        if (!alive(user, pc.astralPassenger2) && !passengers.isEmpty()) {
            ServerPlayerEntity pick = passengers.remove(user.getRandom().nextInt(passengers.size()));
            pc.astralPassenger2 = pick.getUuidAsString();
        }
        if (!alive(user, pc.astralKiller) && !killers.isEmpty()) {
            pc.astralKiller = killers.get(user.getRandom().nextInt(killers.size())).getUuidAsString();
        }
        pc.sync();


    }


    private static ServerPlayerEntity beside(ServerPlayerEntity user, PlayerState pc) {
        for (String id : new String[]{pc.astralPassenger, pc.astralPassenger2, pc.astralKiller}) {
            if (id == null || id.isEmpty()) continue;
            try {
                if (user.getServerWorld().getPlayerByUuid(UUID.fromString(id)) instanceof ServerPlayerEntity p
                        && GameFunctions.isPlayerAliveAndSurvival(p)
                        && user.squaredDistanceTo(p) <= RANGE * RANGE) {
                    return p;
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }

    private static boolean alive(ServerPlayerEntity user, String id) {
        if (id == null || id.isEmpty()) return false;
        try {
            return user.getServerWorld().getPlayerByUuid(UUID.fromString(id)) instanceof ServerPlayerEntity p
                    && GameFunctions.isPlayerAliveAndSurvival(p);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static String name(ServerPlayerEntity user, String id) {
        if (id == null || id.isEmpty()) return "—";
        try {
            return user.getServerWorld().getPlayerByUuid(UUID.fromString(id)) instanceof ServerPlayerEntity p
                    ? p.getName().getString() : "—";
        } catch (IllegalArgumentException e) {
            return "—";
        }
    }
}
