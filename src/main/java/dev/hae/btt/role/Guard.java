package dev.hae.btt.role;

import dev.hae.btt.combat.DeathReasons;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import dev.hae.btt.component.AbilityState;

import java.util.UUID;
import dev.hae.btt.component.PlayerState;














public final class Guard {
    private Guard() {}


    private static final double RANGE = 6.0;

    public static final int GUARD_TICKS = GameConstants.getInTicks(0, 30);

    public static final int GUARD_CD = GameConstants.getInTicks(2, 0);




    public static void guard(ServerPlayerEntity bodyguard, ServerPlayerEntity target) {
        AbilityState ability = AbilityState.KEY.get(bodyguard);
        if (ability.cooldown > 0) return;
        if (bodyguard.distanceTo(target) > RANGE) return;
        ability.setCooldown(GUARD_CD);
        ability.sync();
        PlayerState g = PlayerState.KEY.get(bodyguard);
        g.guardTarget = target.getUuid().toString();
        g.sync();
        PlayerState t = PlayerState.KEY.get(target);
        t.guardedBy = bodyguard.getUuid().toString();
        dev.hae.btt.advancement.BttAdvancements.protection(bodyguard, target);
        t.guardedTicks = GUARD_TICKS;
        t.sync();

    }




    public static void parasitize(ServerPlayerEntity leech, ServerPlayerEntity target) {
        PlayerState lc = PlayerState.KEY.get(leech);
        if (!lc.parasiteHost.isEmpty()) return;
        if (leech.distanceTo(target) > RANGE) return;
        lc.parasiteHost = target.getUuid().toString();
        lc.sync();
    }







    public static boolean vetoDeath(ServerPlayerEntity victim) {
        if (!(victim.getWorld() instanceof ServerWorld world)) return false;
        PlayerState pc = PlayerState.KEY.get(victim);
        if (pc.guardedTicks > 0 && !pc.guardedBy.isEmpty()) {
            ServerPlayerEntity guardian = resolve(world, pc.guardedBy);
            if (guardian != null && GameFunctions.isPlayerAliveAndSurvival(guardian)) {

                dev.hae.btt.record.GameRecordManager.recordTalentUse(guardian, "bodyguard_guard", victim);
                return true;
            }
            clearGuard(pc);
        }
        if (!pc.parasiteHost.isEmpty()) {
            ServerPlayerEntity host = resolve(world, pc.parasiteHost);

            if (host != null && GameFunctions.isPlayerAliveAndSurvival(host) && !pc.isDrunk()) {

                dev.hae.btt.record.GameRecordManager.recordTalentUse(victim, "leech_host", host);
                return true;
            }
            pc.parasiteHost = "";
            pc.sync();
        }
        return false;
    }



    public static void tick(ServerPlayerEntity player, PlayerState pc) {
        if (pc.guardedTicks <= 0) return;
        if (--pc.guardedTicks > 0) return;
        clearGuard(pc);
    }




    public static void onDeath(ServerPlayerEntity victim, ServerWorld world, GameWorldComponent gwc) {
        if (gwc.isRole(victim, Roles.BODYGUARD)) {
            PlayerState bc = PlayerState.KEY.get(victim);
            ServerPlayerEntity guarded = resolve(world, bc.guardTarget);
            bc.guardTarget = "";
            bc.sync();
            if (guarded != null && GameFunctions.isPlayerAliveAndSurvival(guarded)) {
                PlayerState gc = PlayerState.KEY.get(guarded);
                clearGuard(gc);

                GameFunctions.killPlayer(guarded, true, victim, DeathReasons.FAILURE);
            }
        }

        for (ServerPlayerEntity p : world.getPlayers()) {
            PlayerState c = PlayerState.KEY.get(p);
            if (victim.getUuid().toString().equals(c.parasiteHost)) {
                c.parasiteHost = "";
                c.sync();
                if (GameFunctions.isPlayerAliveAndSurvival(p)) {
                    GameFunctions.killPlayer(p, true, victim, DeathReasons.FAILURE);
                }
            }
        }
    }

    private static void clearGuard(PlayerState pc) {
        pc.guardedBy = "";
        pc.guardedTicks = 0;
        pc.sync();
    }

    private static ServerPlayerEntity resolve(ServerWorld world, String uuid) {
        if (uuid == null || uuid.isEmpty()) return null;
        try {
            return world.getPlayerByUuid(UUID.fromString(uuid)) instanceof ServerPlayerEntity s ? s : null;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
