package dev.hae.btt.role;

import dev.hae.btt.combat.DeathReasons;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.MapVariablesWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.game.SeatAssignment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class Voodoo {
    private Voodoo() {}
    private static final Set<UUID> DISSOLVING = new HashSet<>();
    public static boolean dissolving(PlayerEntity player) { return DISSOLVING.contains(player.getUuid()); }
    public static boolean isRemnant(PlayerEntity player) { return PlayerState.KEY.get(player).voodooPhase == 1; }
    public static void observeDeath(ServerPlayerEntity victim, ServerPlayerEntity killer, Identifier reason) {
        var state = PlayerState.KEY.get(victim);
        if (state.voodooPhase >= 2) { state.voodooPhase = 3; state.voodooUntil = 0; state.sync(); return; }
        if (isRemnant(victim) || GameConstants.DeathReasons.FELL_OUT_OF_TRAIN.equals(reason)
                || DeathReasons.ESCAPE.equals(reason) || DeathReasons.DISSIPATION.equals(reason)) return;
        var game = GameWorldComponent.KEY.get(victim.getWorld());
        if (!Roles.isKillerCampFor(game, victim) && Roles.kindOf(Roles.realRole(game, victim)) != Roles.Kind.ENFORCER) return;
        var owners = victim.getServerWorld().getPlayers().stream().filter(p -> p != victim
                && game.getRole(p) == Roles.VOODOO && GameFunctions.isPlayerAliveAndSurvival(p) && !Drunk.isDrunk(p)).toList();
        if (owners.isEmpty()) return;
        var owner = owners.get(victim.getRandom().nextInt(owners.size()));
        state.voodooPhase = 1; state.voodooOwner = owner.getUuidAsString();
        dev.hae.btt.faction.Camps.joinCamp(victim, owner);
        victim.clearStatusEffects();
        victim.setSilent(true);
        victim.setInvisible(true);
        victim.setCameraEntity(victim);
        CampItems.tick(victim);
        state.sync();
        victim.sendMessage(Text.translatable("btt.voodoo.remnant").withColor(Roles.VOODOO.color()), true);
    }
    public static void revive(ServerPlayerEntity caster, ServerPlayerEntity target) {
        var state = PlayerState.KEY.get(target);
        if (!isRemnant(target) || !state.voodooOwner.equals(caster.getUuidAsString()) || Drunk.isDrunk(caster)) return;
        dev.hae.btt.advancement.BttAdvancements.revivedByVoodoo(caster);
        state.voodooPhase = 2;
        state.voodooUntil = caster.getServerWorld().getTime() + 600;
        dev.hae.btt.voice.VoiceChatPlugin.detachRemnant(target);
        target.setCameraEntity(target);
        target.setSilent(false); target.setInvisible(false);
        target.changeGameMode(GameMode.ADVENTURE);
        target.setHealth(target.getMaxHealth());
        state.sync();
    }
    public static void tick(ServerPlayerEntity player) {
        if (!SeatAssignment.isBttMode(player.getWorld())) return;
        var game = GameWorldComponent.KEY.get(player.getWorld());
        if (game.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE) return;
        var state = PlayerState.KEY.get(player);
        if (state.voodooPhase != 1 && state.voodooPhase != 2) return;
        var owner = ownerOf(player);
        if (owner == null || !GameFunctions.isPlayerAliveAndSurvival(owner)
                || game.getRole(owner) != Roles.VOODOO) { finish(player); return; }
        if (isRemnant(player)) {
            dev.hae.btt.voice.VoiceChatPlugin.detachRemnant(player);
            if (player.getY() < MapVariablesWorldComponent.KEY.get(player.getWorld()).getPlayArea().minY) { finish(player); return; }
            player.clearStatusEffects();
            player.setSilent(true); player.setInvisible(true); player.noClip = true;
            if (!player.isSpectator()) player.changeGameMode(GameMode.SPECTATOR);
            player.setCameraEntity(player);
        } else if (player.getWorld().getTime() >= state.voodooUntil) finish(player);
    }
    public static void ownerDied(ServerPlayerEntity owner, ServerPlayerEntity killer, Identifier reason) {
        for (var p : owner.getServerWorld().getPlayers()) {
            var state = PlayerState.KEY.get(p);
            if ((state.voodooPhase == 1 || state.voodooPhase == 2) && state.voodooOwner.equals(owner.getUuidAsString())) finish(p);
        }
    }
    private static ServerPlayerEntity ownerOf(ServerPlayerEntity player) {
        try { return player.getServerWorld().getPlayerByUuid(UUID.fromString(PlayerState.KEY.get(player).voodooOwner))
                instanceof ServerPlayerEntity owner ? owner : null; }
        catch (IllegalArgumentException ignored) { return null; }
    }
    private static void finish(ServerPlayerEntity player) {
        var state = PlayerState.KEY.get(player);
        boolean revived = state.voodooPhase == 2;
        state.voodooPhase = 3; state.voodooUntil = 0;
        player.setSilent(false); player.setInvisible(false);
        state.sync();
        if (!revived) { dev.doctor4t.wathe.compat.TrainVoicePlugin.addPlayer(player.getUuid()); return; }
        DISSOLVING.add(player.getUuid());
        try { GameFunctions.killPlayer(player, false, null, DeathReasons.DISSIPATION); }
        finally { DISSOLVING.remove(player.getUuid()); }
    }
    public static void ensureSpent(ServerPlayerEntity player) {
        var state = PlayerState.KEY.get(player);
        if (state.voodooPhase != 3 || state.voodooOwner.isEmpty() || dissolving(player)
                || !GameFunctions.isPlayerAliveAndSurvival(player)) return;
        DISSOLVING.add(player.getUuid());
        try { GameFunctions.killPlayer(player, false, null, DeathReasons.DISSIPATION); }
        finally { DISSOLVING.remove(player.getUuid()); }
    }
    public static boolean hearsRemnant(ServerPlayerEntity listener, ServerPlayerEntity speaker) {
        var state = PlayerState.KEY.get(speaker);
        return listener.getUuidAsString().equals(state.voodooOwner)
                || PlayerState.KEY.get(listener).voodooOwner.equals(state.voodooOwner)
                    && (isRemnant(listener) || PlayerState.KEY.get(listener).voodooPhase == 2);
    }
    public static boolean hearsLiving(ServerPlayerEntity listener, ServerPlayerEntity speaker) {
        var owner = PlayerState.KEY.get(listener).voodooOwner;
        return speaker.getUuidAsString().equals(owner)
                || PlayerState.KEY.get(speaker).voodooPhase == 2 && PlayerState.KEY.get(speaker).voodooOwner.equals(owner);
    }
    public static void clear(ServerWorld world) {
        for (var p : world.getPlayers()) {
            var state = PlayerState.KEY.get(p);
            if (state.voodooPhase != 0) { p.setInvisible(false); p.setSilent(false); }
            state.voodooPhase = 0; state.voodooOwner = ""; state.voodooUntil = 0; state.sync();
        }
    }
}
