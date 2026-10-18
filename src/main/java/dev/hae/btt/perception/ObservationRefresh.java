package dev.hae.btt.perception;

import dev.hae.btt.component.NeedsState;
import dev.hae.btt.encounter.Memory;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.role.Roles;
import net.minecraft.server.world.ServerWorld;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


public final class ObservationRefresh {
    private ObservationRefresh() {}
    private static final java.util.Map<ServerWorld, Set<UUID>> MAIDS = new java.util.WeakHashMap<>();
    private static final java.util.Map<ServerWorld, java.util.Map<UUID, net.minecraft.nbt.NbtCompound>> ROLES = new java.util.WeakHashMap<>();
    private static final java.util.Map<ServerWorld, java.util.Map<String, net.minecraft.nbt.NbtCompound>> PLAYERS = new java.util.WeakHashMap<>();
    private static final java.util.Map<ServerWorld, java.util.Map<UUID, net.minecraft.nbt.NbtCompound>> OWNERS = new java.util.WeakHashMap<>();

    public static void tick(ServerWorld world) {
        if (!SeatAssignment.isBttMode(world)) { MAIDS.remove(world); ROLES.remove(world); PLAYERS.remove(world); OWNERS.remove(world); return; }
        refreshPlayers(world);
        Set<UUID> now = new HashSet<>();
        for (var viewer : world.getPlayers())
            if (canSeeNeedCondition(viewer)) now.add(viewer.getUuid());
        Set<UUID> previous = MAIDS.put(world, now);
        if (now.equals(previous)) return;
        for (var target : world.getPlayers()) NeedsState.KEY.get(target).sync();
    }

    public static void clear() { MAIDS.clear(); ROLES.clear(); PLAYERS.clear(); OWNERS.clear(); }

    private static void refreshPlayers(ServerWorld world) {
        var game = dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(world);
        var savedGame = new net.minecraft.nbt.NbtCompound();
        game.writeToNbt(savedGame, world.getRegistryManager());
        var oldRoles = ROLES.getOrDefault(world, java.util.Map.of());
        var oldPlayers = PLAYERS.getOrDefault(world, java.util.Map.of());
        var oldOwners = OWNERS.getOrDefault(world, java.util.Map.of());
        var nextRoles = new java.util.HashMap<UUID, net.minecraft.nbt.NbtCompound>();
        var nextPlayers = new java.util.HashMap<String, net.minecraft.nbt.NbtCompound>();
        var nextOwners = new java.util.HashMap<UUID, net.minecraft.nbt.NbtCompound>();
        var savedPlayers = new java.util.HashMap<UUID, net.minecraft.nbt.NbtCompound>();
        var tracking = new java.util.HashMap<UUID, Set<UUID>>();
        for (var target : world.getPlayers()) {
            var saved = new net.minecraft.nbt.NbtCompound();
            dev.hae.btt.component.PlayerState.KEY.get(target).writeToNbt(saved, world.getRegistryManager());
            savedPlayers.put(target.getUuid(), saved);
            Set<UUID> recipients = new HashSet<>();
            for (var recipient : ((org.ladysnake.cca.api.v3.component.ComponentProvider) target).getRecipientsForComponentSync())
                recipients.add(recipient.getUuid());
            tracking.put(target.getUuid(), recipients);
        }
        for (var viewer : world.getPlayers()) {
            var ownerView = dev.hae.btt.component.PlayerStateSync.forOwner(savedPlayers.get(viewer.getUuid()), viewer);
            var ownerSnapshot = new net.minecraft.nbt.NbtCompound();
            for (String key : new String[]{"observedGuideRole", "priorRole", "campOverride", "observedPassiveColor"})
                if (ownerView.contains(key)) ownerSnapshot.put(key, ownerView.get(key).copy());
            ownerSnapshot.putBoolean("fullObserver", NativeRoleView.fullObserver(viewer));
            nextOwners.put(viewer.getUuid(), ownerSnapshot);
            if (!ownerSnapshot.equals(oldOwners.get(viewer.getUuid()))) {
                dev.hae.btt.component.PlayerState.KEY.syncWith(viewer,
                        (org.ladysnake.cca.api.v3.component.ComponentProvider) viewer);
                var previousOwner = oldOwners.get(viewer.getUuid());
                if (previousOwner == null || previousOwner.getBoolean("fullObserver") != NativeRoleView.fullObserver(viewer))
                    org.agmas.harpymodloader.component.WorldModifierComponent.KEY.syncWith(viewer,
                            (org.ladysnake.cca.api.v3.component.ComponentProvider) world);
            }
            var roleView = NativeRoleView.forPlayer(savedGame, game, viewer);
            var roleSnapshot = new net.minecraft.nbt.NbtCompound();
            for (String key : new String[]{"GameMode", "GameStatus", "PsychosActive"})
                if (roleView.contains(key)) roleSnapshot.put(key, roleView.get(key).copy());
            for (var role : dev.doctor4t.wathe.api.WatheRoles.ROLES) {
                String key = role.identifier().toString();
                if (roleView.contains(key)) roleSnapshot.put(key, roleView.get(key).copy());
            }
            nextRoles.put(viewer.getUuid(), roleSnapshot);
            if (!roleSnapshot.equals(oldRoles.get(viewer.getUuid())))
                dev.doctor4t.wathe.cca.GameWorldComponent.KEY.syncWith(viewer,
                        (org.ladysnake.cca.api.v3.component.ComponentProvider) world);
            for (var target : world.getPlayers()) {
                if (target == viewer || !tracking.get(target.getUuid()).contains(viewer.getUuid())) continue;
                String pair = viewer.getUuidAsString() + ":" + target.getUuidAsString();
                var view = dev.hae.btt.component.PlayerStateSync.forPlayer(savedPlayers.get(target.getUuid()), target, viewer);
                nextPlayers.put(pair, view);
                if (!view.equals(oldPlayers.get(pair)))
                    dev.hae.btt.component.PlayerState.KEY.syncWith(viewer,
                            (org.ladysnake.cca.api.v3.component.ComponentProvider) target);
            }
        }
        ROLES.put(world, nextRoles);
        PLAYERS.put(world, nextPlayers);
        OWNERS.put(world, nextOwners);
    }

    public static boolean canSeeNeedCondition(net.minecraft.entity.player.PlayerEntity viewer) {
        return dev.doctor4t.wathe.game.GameFunctions.isPlayerAliveAndSurvival(viewer)
                && dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(viewer.getWorld()).isRole(viewer, Roles.MAID)
                && !Memory.isBlind(viewer);
    }
}
