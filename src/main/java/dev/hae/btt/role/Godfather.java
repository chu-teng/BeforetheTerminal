package dev.hae.btt.role;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.component.GameState;
import dev.hae.btt.component.PlayerState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import java.util.List;


public final class Godfather {
    private Godfather() {}
    public static boolean transformed(ServerPlayerEntity player) {
        var game = GameWorldComponent.KEY.get(player.getWorld());
        opening(player, player.getServerWorld().getPlayers().stream().map(p -> Roles.realRole(game, p)).toList());
        return game.getRole(player) != Roles.GODFATHER;
    }
    public static void opening(ServerPlayerEntity player, List<Role> assigned) {
        var game = GameWorldComponent.KEY.get(player.getWorld());
        boolean duel = dev.hae.btt.game.GameModes.hasRule(player.getWorld(), dev.hae.btt.game.Rule.DUEL);
        var candidates = Roles.allRoles().stream().filter(r -> Roles.kindOf(r) == Roles.Kind.LONE && Roles.isImplemented(r))
                .filter(r -> !assigned.contains(r)).toList();
        if (candidates.isEmpty()) return;
        var borrowed = candidates.get(player.getRandom().nextInt(candidates.size()));
        RoleTransitions.replace(player, borrowed, 0, duel ? Roles.CAMP_KILLER : Roles.CAMP_OUTSIDER, false);
        var state = PlayerState.KEY.get(player); state.joinedCampRole = Roles.GODFATHER.identifier().toString(); state.sync();
        if (!dev.hae.btt.encounter.Memory.isBlind(player))
            dev.hae.btt.game.OpeningNotices.enqueue(player, net.minecraft.text.Text.translatable("btt.action.godfather.borrowed",
                    dev.hae.btt.game.SeatAssignment.displayName(borrowed).getString()).withColor(Roles.GODFATHER.color()));
        tick(player.getServerWorld());
    }
    public static void tick(ServerWorld world) {
        var game = GameWorldComponent.KEY.get(world);
        var owner = world.getPlayers().stream().filter(p -> Roles.realRole(game, p) == Roles.GODFATHER)
                .filter(GameFunctions::isPlayerAliveAndSurvival).filter(p -> !Drunk.isDrunk(p)).findFirst().orElse(null);
        if (owner == null) return;
        boolean duel = dev.hae.btt.game.GameModes.hasRule(world, dev.hae.btt.game.Rule.DUEL);
        for (var player : world.getPlayers()) {
            if (Roles.kindOfReal(game, player) != Roles.Kind.LONE) continue;
            var state = PlayerState.KEY.get(player);
            if (state.joinedCampRole.equals(Roles.GODFATHER.identifier().toString())) continue;
            state.joinedCampRole = Roles.GODFATHER.identifier().toString();
            state.setCampOverride(duel ? Roles.CAMP_KILLER : Roles.CAMP_OUTSIDER); state.sync();
            dev.hae.btt.record.GameRecordManager.recordCampChange(player, state.campOverride, owner);
            if (!duel) dev.hae.btt.game.OpeningNotices.send(player, net.minecraft.text.Text.translatable("btt.action.godfather.join").withColor(Roles.GODFATHER.color()));
            if (state.soloObjectiveComplete) objective(player);
        }
    }
    public static boolean objective(ServerPlayerEntity actor) {
        var game = GameWorldComponent.KEY.get(actor.getWorld());
        var state = PlayerState.KEY.get(actor);

        boolean duel = dev.hae.btt.game.GameModes.hasRule(actor.getWorld(), dev.hae.btt.game.Rule.DUEL);
        boolean godfather = Roles.realRole(game, actor) == Roles.GODFATHER;
        boolean member = state.joinedCampRole.equals(Roles.GODFATHER.identifier().toString())
                && Roles.campOf(game, actor) == (duel ? Roles.Faction.KILLER : Roles.Faction.OUTSIDER);
        if (!member && !godfather) return false;
        if (!state.soloObjectiveComplete) { state.soloObjectiveComplete = true; Monk.objectiveCompleted(actor); state.sync(); }
        if (duel) {

            if (member) dev.hae.btt.victory.EndingPipeline.requestFactionWin(actor);
        } else if (member) {
            var shared = GameState.KEY.get(actor.getWorld());
            var request = new dev.hae.btt.victory.VictoryRequest(dev.hae.btt.victory.Endings.Ending.GODFATHER_WIN, actor.getUuid());
            if (!shared.pendingVictories.contains(request)) shared.pendingVictories.add(request);
            shared.sync();
        }
        return true;
    }
}
