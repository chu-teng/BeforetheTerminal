package dev.hae.btt.trait;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.combat.DeathReasons;
import dev.hae.btt.role.Roles;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;


public final class TraitRules {
    private TraitRules() {}

    public static int halfCooldown(int ticks) {
        return ticks <= 0 ? ticks : ticks / 2 + ticks % 2;
    }

    public static boolean active(PlayerEntity player) {
        return player != null && SeatAssignment.isBttMode(player.getWorld())
                && GameWorldComponent.KEY.get(player.getWorld()).isRunning();
    }

    private static boolean roundConfigured(PlayerEntity player) {
        return player != null && SeatAssignment.isBttMode(player.getWorld())
                && GameWorldComponent.KEY.get(player.getWorld()).getGameStatus() != GameWorldComponent.GameStatus.INACTIVE;
    }

    public static int itemCooldown(PlayerEntity player, int ticks) {
        return roundConfigured(player) && Traits.has(player, Traits.FAMILIAR) ? halfCooldown(ticks) : ticks;
    }

    public static int skillCooldown(PlayerEntity player, int ticks) {
        return roundConfigured(player) && Traits.has(player, Traits.EXCELLENT) ? halfCooldown(ticks) : ticks;
    }

    public static boolean execution(PlayerEntity victim, PlayerEntity killer, Identifier reason) {
        return DeathReasons.EXECUTION.equals(reason) || killer != null && killer != victim
                && dev.hae.btt.role.Colonel.executionUser(killer, reason)
                && dev.hae.btt.encounter.EncounterRules.execution(reason);
    }

    public static boolean harmfulDeath(Identifier reason) {
        if (reason == null || DeathReasons.isSuicide(reason)) return false;
        return java.util.Set.of(GameConstants.DeathReasons.KNIFE, GameConstants.DeathReasons.GUN,
                GameConstants.DeathReasons.BAT, GameConstants.DeathReasons.GRENADE, GameConstants.DeathReasons.POISON,
                DeathReasons.MAGIC, DeathReasons.FAILURE, DeathReasons.AXE, DeathReasons.BOMB, DeathReasons.BLOOM,
                DeathReasons.FLYING_SWORD, DeathReasons.PIPE, DeathReasons.ARROW, DeathReasons.DAGGER,
                DeathReasons.REVENGE, DeathReasons.DIGESTED, DeathReasons.EXECUTION).contains(reason);
    }

    public static boolean allowDeath(ServerPlayerEntity victim, PlayerEntity killer, Identifier reason) {
        if (!active(victim)) return true;
        if (voluntaryDeath(victim, killer, reason)) return true;
        var state = dev.hae.btt.component.PlayerState.KEY.get(victim);
        if (Traits.has(victim, Traits.AFTERLIFE) && !state.afterlifeShieldUsed
                && killer instanceof ServerPlayerEntity attacker && harmfulDeath(reason)) {
        dev.hae.btt.combat.DeathResolution.blocked(victim, killer, true);
            state.afterlifeShieldUsed = true;
            dev.hae.btt.faction.Camps.joinCamp(victim, attacker);
            dev.hae.btt.combat.DeathResolution.shieldBreak(victim, killer);
            state.sync();
            return false;
        }
        boolean blocked = Traits.has(victim, Traits.CATCHER) && !execution(victim, killer, reason)
                || Traits.has(victim, Traits.LUCKY) && harmfulDeath(reason) && victim.getRandom().nextBoolean();
        if (blocked) dev.hae.btt.combat.DeathResolution.shieldBreak(victim, killer);
        return !blocked;
    }

    public static boolean voluntaryDeath(PlayerEntity victim, PlayerEntity killer, Identifier reason) {
        return killer == victim || DeathReasons.isSuicide(reason)
                || GameConstants.DeathReasons.FELL_OUT_OF_TRAIN.equals(reason);
    }

    public static boolean silentAction(Identifier sound) {
        String path = sound.getPath();
        return path.endsWith(".step") || path.contains("swim") || path.contains("splash")
                || path.endsWith(".eat") || path.endsWith(".drink") || path.endsWith(".burp");
    }

    public static boolean intercept(ServerPlayerEntity caster, ServerPlayerEntity target) {
        if (!active(target) || target == caster || !Traits.has(target, Traits.DISGUISED)
                || !GameFunctions.isPlayerAliveAndSurvival(target) || !GameFunctions.isPlayerAliveAndSurvival(caster)
                || dev.hae.btt.role.Drunk.isDrunk(caster)) return false;
        dev.hae.btt.component.PlayerState.KEY.get(caster).applyDrunk(20);
        return true;
    }
}
