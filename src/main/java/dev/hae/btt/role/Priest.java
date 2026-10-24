package dev.hae.btt.role;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.hae.btt.component.GameState;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.trait.TraitRules;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class Priest {
    private Priest() {}

    public static void misexecution(ServerPlayerEntity player) {
        if (Drunk.isDrunk(player)) return;
        GameState.KEY.get(player.getWorld()).roleEffects.putLong("priestProtection", player.getWorld().getTime() + 600);
    }

    public static boolean protects(PlayerEntity victim, PlayerEntity killer, Identifier reason) {
        if (!SeatAssignment.isBttMode(victim.getWorld())
                || !GameWorldComponent.KEY.get(victim.getWorld()).isRunning()
                || TraitRules.voluntaryDeath(victim, killer, reason)
                || !TraitRules.harmfulDeath(reason)) return false;
        return GameState.KEY.get(victim.getWorld()).roleEffects.getLong("priestProtection") > victim.getWorld().getTime();
    }
}
