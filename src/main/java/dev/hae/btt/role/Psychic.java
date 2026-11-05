package dev.hae.btt.role;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.component.GameState;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.combat.DeathReasons;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public final class Psychic {
    private Psychic() {}
    private static String key(ServerPlayerEntity owner) { return "psychicTarget:" + owner.getUuidAsString(); }
    public static ServerPlayerEntity target(ServerPlayerEntity owner) {
        var id = GameState.KEY.get(owner.getWorld()).roleEffects.getString(key(owner));
        if (id.isEmpty()) return null;
        try {
            var p = owner.getServerWorld().getPlayerByUuid(java.util.UUID.fromString(id));
            return p instanceof ServerPlayerEntity s && GameFunctions.isPlayerAliveAndSurvival(s) ? s : null;
        } catch (IllegalArgumentException ignored) { return null; }
    }
    public static void observe(ServerPlayerEntity actor) {
        var world = actor.getServerWorld(); var game = GameWorldComponent.KEY.get(world);
        if (!SeatAssignment.isBttMode(world) || !game.isRunning() || !GameFunctions.isPlayerAliveAndSurvival(actor)) return;
        for (var owner : world.getPlayers()) {
            if (owner == actor || !Roles.hasCurrentRole(game, owner, Roles.PSYCHIC)
                    || !GameFunctions.isPlayerAliveAndSurvival(owner) || Drunk.isDrunk(owner) || target(owner) != null) continue;
            GameState.KEY.get(world).roleEffects.putString(key(owner), actor.getUuidAsString());
            dev.hae.btt.record.GameRecordManager.recordTalentUse(owner, "psychic_target", actor);
        }
        refresh(world);
    }
    public static void refresh(ServerWorld world) {
        var protectedIds = new java.util.HashSet<java.util.UUID>();
        var game = GameWorldComponent.KEY.get(world);
        if (SeatAssignment.isBttMode(world) && game.isRunning()) for (var owner : world.getPlayers()) {
            if (!Roles.hasCurrentRole(game, owner, Roles.PSYCHIC) || !GameFunctions.isPlayerAliveAndSurvival(owner) || Drunk.isDrunk(owner)) continue;
            var target = target(owner);
            if (target != null && !owner.canSee(target)) protectedIds.add(target.getUuid());
        }
        for (var p : world.getPlayers()) {
            var state = PlayerState.KEY.get(p); boolean next = protectedIds.contains(p.getUuid());
            if (state.psychicSheltered != next) { state.psychicSheltered = next; state.sync(); }
        }
    }
    public static boolean sheltered(PlayerEntity player) { return PlayerState.KEY.get(player).psychicSheltered; }
    public static void tick(ServerPlayerEntity owner, ServerWorld world, GameWorldComponent game) {
        if (Drunk.isDrunk(owner) || !GameFunctions.isPlayerAliveAndSurvival(owner)) return;
        var target = target(owner);
        if (target != null && PlayerMoodComponent.KEY.get(target).getMood() < GameConstants.MID_MOOD_THRESHOLD) {
            GameFunctions.killPlayer(target, true, owner, DeathReasons.MAGIC);
        }
    }
}
