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
        if (world == null || ending == null || ending == Endings.Ending.NONE) return;
        if (actor != null && dev.hae.btt.role.Roles.kindOf(dev.hae.btt.role.Roles.realRole(
                dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(world), actor)) == dev.hae.btt.role.Roles.Kind.LONE) {
            var state = dev.hae.btt.component.PlayerState.KEY.get(actor);
            if (state.joinedCampRole.equals(dev.hae.btt.role.Roles.LEGION.identifier().toString())
                    && dev.hae.btt.role.Roles.campOf(dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(world), actor)
                    == dev.hae.btt.role.Roles.Faction.OUTSIDER) return;
            if (dev.hae.btt.game.GameModes.modeOf(dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(world))
                    == dev.hae.btt.game.BeforeTheTerminalGameMode.Mode.DUEL) {
                dev.hae.btt.game.Duel.complete(actor);
                return;
            }
            if (Backgrounds.isActive(world, Backgrounds.LIFE)) {
                dev.hae.btt.component.PlayerState.KEY.get(actor).soloObjectiveComplete = true;
                return;
            }
        }
        GameState btt = GameState.KEY.get(world);
        var request = new VictoryRequest(ending, actor == null ? null : actor.getUuid());
        if (!btt.pendingVictories.contains(request)) btt.pendingVictories.add(request);
        if (btt.pendingEnding.isEmpty()) btt.pendingEnding = ending.name();
        btt.sync();
    }
}
