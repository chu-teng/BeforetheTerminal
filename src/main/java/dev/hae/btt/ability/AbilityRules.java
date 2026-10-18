package dev.hae.btt.ability;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.component.AbilityState;
import dev.hae.btt.role.Drunk;
import dev.hae.btt.role.RoleDefs;
import dev.hae.btt.role.Roles;
import net.minecraft.server.network.ServerPlayerEntity;


public final class AbilityRules {
    private AbilityRules() {}

    public static boolean canAct(ServerPlayerEntity player) {
        if (player == null || !player.isAlive() || !SeatAssignment.isBttMode(player.getWorld())) return false;
        var game = GameWorldComponent.KEY.get(player.getWorld());
        return game.isRunning() && game.getRole(player) != null && GameFunctions.isPlayerAliveAndSurvival(player);
    }

    public static boolean inTargetRange(ServerPlayerEntity caster, ServerPlayerEntity target) {
        return target != null && target.getWorld() == caster.getWorld()
                && (!RoleDefs.isNearbySkill(Roles.effectiveRole(GameWorldComponent.KEY.get(caster.getWorld()), caster))
                    || caster.distanceTo(target) <= 6.0);
    }


    static void consumeDrunkCooldown(ServerPlayerEntity user, GameWorldComponent game) {
        int cost = Drunk.fizzleCost(Roles.effectiveRole(game, user));
        if (cost <= 0) return;
        AbilityState ability = AbilityState.KEY.get(user);
        if (ability.cooldown <= 0) {
            ability.setCooldown(cost);
            ability.sync();
        }
    }

    static void setCooldown(AbilityState ability, int ticks) {
        ability.setCooldown(ticks);
        ability.sync();
    }

    static int roleColor(ServerPlayerEntity user) {
        var role = GameWorldComponent.KEY.get(user.getWorld()).getRole(user);
        return role == null ? 0xFFFFFF : role.color();
    }
    static void recordUse(ServerPlayerEntity user, ServerPlayerEntity target, dev.doctor4t.wathe.api.Role role) {
        dev.hae.btt.api.event.BttEvents.SkillUsed.EVENT.invoker()
                .onSkillUsed(user, target, role.identifier());
        }

}
