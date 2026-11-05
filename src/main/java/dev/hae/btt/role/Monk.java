package dev.hae.btt.role;

import dev.hae.btt.component.AbilityState;
import dev.hae.btt.component.PlayerState;
import net.minecraft.server.network.ServerPlayerEntity;


public final class Monk {
    private Monk() {}
    private static final double RANGE = 6.0;

    public static void deify(ServerPlayerEntity monk, ServerPlayerEntity target) {
        AbilityState ability = AbilityState.KEY.get(monk);
        if (ability.cooldown > 0 || monk == target || monk.distanceTo(target) > RANGE) return;
        PlayerState state = PlayerState.KEY.get(target);
        if (state.deified) return;
        if (Werewolf.kind(dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(monk.getWorld()), monk, target) != Roles.Kind.LONE) {
            ability.setCooldown(RoleDefs.CD_1MIN); ability.sync(); return;
        }
        state.deified = true;
        state.deifiedBy = monk.getUuidAsString();
        state.sync();
        ability.setCooldown(RoleDefs.CD_1MIN);
        ability.sync();
    }

    public static void objectiveCompleted(ServerPlayerEntity actor) {
        var state = PlayerState.KEY.get(actor);
        if (!state.deified || !dev.hae.btt.game.GameModes.hasRule(
                actor.getWorld(), dev.hae.btt.game.Rule.DUEL)) return;
        state.monkShields++;
        try {
            if (actor.getServerWorld().getPlayerByUuid(java.util.UUID.fromString(state.deifiedBy)) instanceof ServerPlayerEntity monk)
                dev.hae.btt.advancement.BttAdvancements.protection(monk, actor);
        } catch (IllegalArgumentException ignored) { }
        state.sync();
    }
}
