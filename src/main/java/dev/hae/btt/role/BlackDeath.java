package dev.hae.btt.role;

import dev.hae.btt.combat.DeathReasons;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.UUID;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.game.GameEvents;
import dev.hae.btt.mixin.game.KillHookMixin;





















public final class BlackDeath {
    private BlackDeath() {}


    public static void onRoundStart(ServerWorld world, java.util.List<ServerPlayerEntity> players) {
        GameWorldComponent gwc = GameWorldComponent.KEY.get(world);
        for (ServerPlayerEntity p : players) {
            PlayerState c = PlayerState.KEY.get(p);
            boolean isBlackdeath = gwc.isRole(p, Roles.BLACKDEATH);
            c.blackdeathPatient = isBlackdeath;
            c.blackdeathHost = "";
        }
    }


    public static boolean isBodylessPathogen(PlayerEntity player) {
        if (player == null) return false;
        PlayerState c = PlayerState.KEY.get(player);
        if (c.blackdeathHost.isEmpty()) return false;
        return GameWorldComponent.KEY.get(player.getWorld()).isRole(player, Roles.BLACKDEATH);
    }


    public static boolean isPresent(ServerWorld world, ServerPlayerEntity blackdeath) {
        PlayerState c = PlayerState.KEY.get(blackdeath);
        if (c.blackdeathHost.isEmpty()) {
            return GameFunctions.isPlayerAliveAndSurvival(blackdeath);
        }
        ServerPlayerEntity host = hostOf(world, c.blackdeathHost);
        return host != null && GameFunctions.isPlayerAliveAndSurvival(host);
    }


    public static ServerPlayerEntity ownerOf(ServerWorld world, ServerPlayerEntity patient) {
        for (ServerPlayerEntity p : world.getPlayers()) {
            if (p == patient) {

                if (GameWorldComponent.KEY.get(world).isRole(p, Roles.BLACKDEATH)
                        && PlayerState.KEY.get(p).blackdeathPatient) return p;
                continue;
            }
            String host = PlayerState.KEY.get(p).blackdeathHost;
            if (!host.isEmpty() && host.equals(patient.getUuidAsString())) return p;
        }
        return null;
    }







    public static boolean onPatientDeath(ServerPlayerEntity patient, PlayerEntity killer) {
        ServerWorld world = patient.getServerWorld();
        var owners = world.getPlayers().stream().filter(p ->
                p == patient && GameWorldComponent.KEY.get(world).isRole(p, Roles.BLACKDEATH) && PlayerState.KEY.get(p).blackdeathPatient
                        || patient.getUuidAsString().equals(PlayerState.KEY.get(p).blackdeathHost)).toList();
        boolean continued = false;
        for (var owner : owners) continued |= transferPatient(patient, killer, owner);
        return continued;
    }

    private static boolean transferPatient(ServerPlayerEntity patient, PlayerEntity killer, ServerPlayerEntity blackdeath) {
        PlayerState bdState = PlayerState.KEY.get(blackdeath);
        if (killer instanceof ServerPlayerEntity newHost && newHost != patient
                && GameFunctions.isPlayerAliveAndSurvival(newHost)) {

            PlayerState.KEY.get(patient).blackdeathPatient = false;
            PlayerState.KEY.get(newHost).blackdeathPatient = true;
            bdState.blackdeathHost = newHost.getUuidAsString();
            bdState.sync();
            PlayerState.KEY.get(newHost).sync();

            dev.hae.btt.mechanic.Attachment.attach(blackdeath, newHost, dev.hae.btt.mechanic.Attachment.KIND_BLACKDEATH);

            dev.hae.btt.record.GameRecordManager.recordTalentUse(blackdeath, "blackdeath_possess", newHost);

            return true;
        }

        PlayerState.KEY.get(patient).blackdeathPatient = false;
        PlayerState.KEY.get(patient).sync();
        terminate(blackdeath);
        return false;
    }


    public static void terminate(ServerPlayerEntity blackdeath) {


        PlayerState c = PlayerState.KEY.get(blackdeath);
        if (c.blackdeathHost.isEmpty() && !c.blackdeathPatient) return;
        if (terminating) return;
        terminating = true;
        c.blackdeathHost = "";
        c.blackdeathPatient = false;
        dev.hae.btt.mechanic.Attachment.detach(blackdeath);
        c.sync();

        if (blackdeath.getWorld() instanceof ServerWorld w) {
            GameWorldComponent gwc = GameWorldComponent.KEY.get(w);
            for (ServerPlayerEntity p : w.getPlayers()) {
                if (p == blackdeath) continue;
                if (!GameFunctions.isPlayerAliveAndSurvival(p)) continue;
                if (PlayerState.KEY.get(p).blackdeathPatient) continue;
                if (!Roles.isKillerCampFor(gwc, p)) continue;
                GameFunctions.killPlayer(p, true, p, DeathReasons.BLACKDEATH_CURTAIN);
            }
        }
        terminating = false;
    }


    private static boolean terminating = false;


    public static void tick(ServerWorld world) {
        GameWorldComponent gwc = GameWorldComponent.KEY.get(world);
        for (ServerPlayerEntity p : world.getPlayers()) {
            if (!gwc.isRole(p, Roles.BLACKDEATH)) continue;
            PlayerState c = PlayerState.KEY.get(p);
            if (c.blackdeathHost.isEmpty()) continue;
            ServerPlayerEntity host = hostOf(world, c.blackdeathHost);
            if (host == null) continue;
            if (!GameFunctions.isPlayerAliveAndSurvival(host)) continue;
            dev.hae.btt.mechanic.Attachment.tick(p, world);


        }
    }

    private static ServerPlayerEntity hostOf(ServerWorld world, String uuid) {
        return hostOf0(world, uuid);
    }





    public static void transferTo(ServerPlayerEntity newHost, ServerPlayerEntity oldHost) {
        if (!(newHost.getWorld() instanceof ServerWorld w)) return;
        PlayerState.KEY.get(oldHost).blackdeathPatient = false;
        PlayerState.KEY.get(oldHost).sync();
        PlayerState.KEY.get(newHost).blackdeathPatient = true;
        PlayerState.KEY.get(newHost).sync();
        for (ServerPlayerEntity p : w.getPlayers()) {
            if (!GameWorldComponent.KEY.get(w).isRole(p, Roles.BLACKDEATH)) continue;
            PlayerState bd = PlayerState.KEY.get(p);
            if (!bd.blackdeathHost.equals(oldHost.getUuidAsString())) continue;
            bd.blackdeathHost = newHost.getUuidAsString();
            bd.sync();
            dev.hae.btt.mechanic.Attachment.attach(p, newHost, dev.hae.btt.mechanic.Attachment.KIND_BLACKDEATH);
        }
    }

    private static ServerPlayerEntity hostOf0(ServerWorld world, String uuid) {
        try {
            return world.getPlayerByUuid(UUID.fromString(uuid)) instanceof ServerPlayerEntity p ? p : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static void notify(ServerPlayerEntity player, String key, String arg) {
        if (player == null) return;
        net.minecraft.text.MutableText text = arg == null
                ? Text.translatable(key) : Text.translatable(key, arg);
        player.sendMessage(text.withColor(Roles.BLACKDEATH.color()), true);
    }

    private static String nameOf(ServerPlayerEntity p) {
        return p == null ? "？" : p.getGameProfile().getName();
    }
}
