package dev.hae.btt.role;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.component.GameState;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.record.GameRecordManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public final class Werewolf {
    private Werewolf() {}

    public static void recruit(ServerPlayerEntity leader, List<Role> assigned) {
        var world = leader.getServerWorld();
        var game = GameWorldComponent.KEY.get(world);
        var opening = GameState.KEY.get(world).openingRoles;
        var candidates = new ArrayList<>(world.getPlayers().stream()
                .filter(p -> p != leader && opening.containsKey(p.getUuid()))
                .filter(GameFunctions::isPlayerAliveAndSurvival)
                .filter(p -> game.getRole(p) != Roles.WEREWOLF && game.getRole(p) != Roles.BLACKDEATH).toList());
        Collections.shuffle(candidates, new java.util.Random(leader.getRandom().nextLong()));
        for (var player : candidates.stream().limit(assigned.size() / 3).toList()) {
            var state = PlayerState.KEY.get(player);
            state.werewolfOwner = leader.getUuidAsString();
            state.sync();
            GameRecordManager.recordCampChange(player, Roles.campName(Roles.campOf(game, player)), leader);
            player.sendMessage(Text.translatable("btt.action.werewolf.join").withColor(Roles.WEREWOLF.color()), true);
        }
    }

    public static boolean member(PlayerEntity player) {
        var state = PlayerState.KEY.get(player);
        if (player.getWorld().isClient()) return state.werewolfAlly;
        if (state.werewolfOwner.isEmpty()) return false;
        try {
            var leader = player.getWorld().getPlayerByUuid(java.util.UUID.fromString(state.werewolfOwner));
            return leader != null && leader.isAlive() && GameFunctions.isPlayerAliveAndSurvival(leader)
                    && GameWorldComponent.KEY.get(player.getWorld()).isRole(leader, Roles.WEREWOLF);
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    public static boolean samePack(PlayerEntity left, PlayerEntity right) {
        return member(left) && member(right)
                && PlayerState.KEY.get(left).werewolfOwner.equals(PlayerState.KEY.get(right).werewolfOwner);
    }

    public static void died(ServerPlayerEntity leader, ServerPlayerEntity killer, Identifier reason) {
        for (var player : leader.getServerWorld().getPlayers()) {
            var state = PlayerState.KEY.get(player);
            if (!state.werewolfOwner.equals(leader.getUuidAsString())) continue;
            state.werewolfOwner = "";
            state.sync();
            var game = GameWorldComponent.KEY.get(player.getWorld());
            GameRecordManager.recordCampChange(player, Roles.campName(Roles.campOf(game, player)), leader);
        }
    }
}
