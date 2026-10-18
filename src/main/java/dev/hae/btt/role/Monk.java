package dev.hae.btt.role;

import dev.hae.btt.component.AbilityState;
import dev.hae.btt.component.PlayerState;
import net.minecraft.server.network.ServerPlayerEntity;















public final class Monk {
    private Monk() {}


    private static final double RANGE = 6.0;


    public static void deify(ServerPlayerEntity monk, ServerPlayerEntity target) {
        AbilityState ability = AbilityState.KEY.get(monk);
        if (ability.cooldown > 0) return;
        if (monk == target) return;
        if (monk.distanceTo(target) > RANGE) return;
        PlayerState targetState = PlayerState.KEY.get(target);
        if (dev.hae.btt.game.GameModes.modeOf(dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(monk.getWorld())) == dev.hae.btt.game.BeforeTheTerminalGameMode.Mode.DUEL) {
            if (Roles.kindOfReal(dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(monk.getWorld()), target) == Roles.Kind.LONE) {
                dev.hae.btt.advancement.BttAdvancements.protection(monk, target);
                targetState.monkShields++;
                targetState.sync();
            }
            ability.setCooldown(RoleDefs.CD_1MIN);
            ability.sync();
            return;
        }
        if (targetState.deified) return;
        targetState.deified = true;
        targetState.deifiedBy = monk.getUuidAsString();
        ability.setCooldown(RoleDefs.CD_1MIN);
        ability.sync();
    }
}
