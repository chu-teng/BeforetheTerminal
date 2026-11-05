package dev.hae.btt.victory;

import dev.hae.btt.victory.Endings;

import dev.hae.btt.api.event.BttEvents;
import net.minecraft.server.world.ServerWorld;














public final class EndingPipeline {
    private EndingPipeline() {}

    public static Endings.Ending factionWin(net.minecraft.server.network.ServerPlayerEntity actor) {
        var game = dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(actor.getWorld());
        if (dev.hae.btt.game.GameModes.modeOf(game) == dev.hae.btt.game.BeforeTheTerminalGameMode.Mode.MOB) {
            return switch (dev.hae.btt.component.PlayerState.KEY.get(actor).mobTeam) {
                case dev.hae.btt.role.Mob.PINK -> Endings.Ending.MOB_PINK_WIN;
                case dev.hae.btt.role.Mob.AZURE -> Endings.Ending.MOB_AZURE_WIN;
                default -> Endings.Ending.NONE;
            };
        }
        return switch (dev.hae.btt.role.Roles.campOf(game, actor)) {
            case PASSENGER -> Endings.Ending.JOURNEY_END;
            case KILLER -> Endings.Ending.BLOOD_EXPRESS;
            case OUTSIDER -> {
                var role = dev.hae.btt.faction.Camps.campRole(game, actor);
                var def = dev.hae.btt.role.RoleDefs.get(role);
                yield def != null && def.outsiderHost() != null
                        ? def.outsiderHost().ending() : Endings.Ending.NONE;
            }
            case SOLO -> Endings.Ending.NONE;
        };
    }


    public static Endings.Ending preferFactionEnding(Endings.Ending first, Endings.Ending next) {
        return factionPriority(next) > factionPriority(first) ? next : first;
    }

    private static int factionPriority(Endings.Ending ending) {
        return switch (ending) {
            case NONE -> 0;
            case TRIAL_COMPLETE, JOURNEY_END, BLACKDEATH_PASSENGER, HERETIC_PASSENGER -> 1;
            case BLOOD_EXPRESS, NAKU_KORO, BLACKDEATH_KILLER, HERETIC_KILLER -> 2;
            case MESSIAH_WIN, MAJO_WIN, GARDENER_WIN, LEGION_WIN, VOODOO_WIN, GODFATHER_WIN -> 3;
            default -> 4;
        };
    }

    public static void requestFactionWin(net.minecraft.server.network.ServerPlayerEntity actor) {
        var effects = dev.hae.btt.component.GameState.KEY.get(actor.getWorld()).roleEffects;
        var ending = preferFactionEnding(Endings.byName(effects.getString("factionObjective")), factionWin(actor));
        if (ending != Endings.Ending.NONE) effects.putString("factionObjective", ending.name());
    }

    public static void factionLoss(net.minecraft.server.network.ServerPlayerEntity actor) {
        var game = dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(actor.getWorld());
        var result = dev.hae.btt.role.Roles.isPassengerCampFor(game, actor)
                ? Endings.Ending.BLOOD_EXPRESS : Endings.Ending.TRIAL_COMPLETE;
        dev.hae.btt.component.GameState.KEY.get(actor.getWorld()).roleEffects
                .putString("factionLoss", result.name());
    }


    public static Endings.Ending resolve(ServerWorld world, Endings.Ending decided) {
        var out = rewrite(world, decided);
        return allow(world, out) ? out : Endings.Ending.NONE;
    }

    public static Endings.Ending rewrite(ServerWorld world, Endings.Ending decided) {
        if (decided == null || decided == Endings.Ending.NONE) return Endings.Ending.NONE;
        Endings.Ending rewritten = BttEvents.EndingRewrite.EVENT.invoker().rewrite(world, decided);
        return rewritten == null ? decided : rewritten;
    }

    public static boolean allow(ServerWorld world, Endings.Ending ending) {
        return ending == Endings.Ending.NONE || BttEvents.EndingVeto.EVENT.invoker().allow(world, ending);
    }
}
