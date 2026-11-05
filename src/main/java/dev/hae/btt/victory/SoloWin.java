package dev.hae.btt.victory;

import dev.hae.btt.victory.Endings;
import dev.hae.btt.encounter.Backgrounds;

import dev.hae.btt.component.GameState;
import net.minecraft.server.world.ServerWorld;










public final class SoloWin {
    private SoloWin() {}


    public static void request(ServerWorld world, Endings.Ending ending) {
        request(world, ending, null);
    }

    public static void request(ServerWorld world, Endings.Ending ending, net.minecraft.server.network.ServerPlayerEntity actor) {
        if (world == null || ending == null || ending == Endings.Ending.NONE || Endings.isRelationEnding(ending)) return;
        if (actor != null && !Endings.isRelationEnding(ending) && dev.hae.btt.role.Godfather.objective(actor)) return;
        if (actor != null && !Endings.isRelationEnding(ending) && dev.hae.btt.role.Roles.kindOf(dev.hae.btt.role.Roles.realRole(
                dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(world), actor)) == dev.hae.btt.role.Roles.Kind.LONE) {
            var state = dev.hae.btt.component.PlayerState.KEY.get(actor);
            if (!state.soloObjectiveComplete) {
                state.soloObjectiveComplete = true;
                dev.hae.btt.role.Monk.objectiveCompleted(actor);
                state.sync();
            }
            var game = dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(world);
            if (dev.hae.btt.role.Roles.campOf(game, actor) != dev.hae.btt.role.Roles.Faction.SOLO) return;
            if (state.deified || Backgrounds.isActive(world, Backgrounds.LIFE)) return;
        }
        GameState btt = GameState.KEY.get(world);
        var request = new VictoryRequest(ending, actor == null ? null : actor.getUuid());
        if (!btt.pendingVictories.contains(request)) btt.pendingVictories.add(request);
        if (btt.pendingEnding.isEmpty()) btt.pendingEnding = ending.name();
        btt.sync();
    }


    public static boolean eligible(ServerWorld world, VictoryRequest request) {
        if (Endings.isRelationEnding(request.ending())) return false;
        if (request.ending() == Endings.Ending.GODFATHER_WIN) {
            return request.actor() != null && world.getPlayerByUuid(request.actor()) instanceof net.minecraft.server.network.ServerPlayerEntity member
                    && dev.hae.btt.faction.Camps.sharesRoleCamp(dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(world), member, dev.hae.btt.role.Roles.GODFATHER);
        }
        if (request.actor() == null || !(world.getPlayerByUuid(request.actor()) instanceof net.minecraft.server.network.ServerPlayerEntity actor)) return false;
        var game = dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(world);
        return dev.hae.btt.role.Roles.kindOfReal(game, actor) == dev.hae.btt.role.Roles.Kind.LONE
                && dev.hae.btt.role.Roles.campOf(game, actor) == dev.hae.btt.role.Roles.Faction.SOLO
                && !dev.hae.btt.component.PlayerState.KEY.get(actor).deified
                && !Backgrounds.isActive(world, Backgrounds.LIFE);
    }

    public static boolean deferredWinner(net.minecraft.server.network.ServerPlayerEntity actor) {
        var game = dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(actor.getWorld());
        var state = dev.hae.btt.component.PlayerState.KEY.get(actor);
        return dev.hae.btt.role.Roles.kindOfReal(game, actor) == dev.hae.btt.role.Roles.Kind.LONE
                && dev.hae.btt.role.Roles.campOf(game, actor) == dev.hae.btt.role.Roles.Faction.SOLO
                && state.soloObjectiveComplete && (state.deified || Backgrounds.isActive(actor.getWorld(), Backgrounds.LIFE));
    }
}
