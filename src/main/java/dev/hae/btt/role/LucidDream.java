package dev.hae.btt.role;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.compat.TrainVoicePlugin;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.component.GameState;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.mechanic.Attachment;
import dev.hae.btt.mixin.game.CooldownEntryAccessor;
import dev.hae.btt.mixin.game.CooldownSnapshotAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.*;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import org.ladysnake.cca.api.v3.component.ComponentProvider;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import java.util.ArrayList;
import java.util.UUID;


public final class LucidDream {
    private LucidDream() {}
    private static final String KIND = "LUCID_DREAM";

    public static boolean active(ServerWorld world) { return !GameState.KEY.get(world).lucidSnapshot.isEmpty(); }
    public static boolean captive(PlayerEntity player) { return KIND.equals(PlayerState.KEY.get(player).attachKind); }
    public static boolean dreamDead(ServerPlayerEntity player) {
        var world = player.getServerWorld();
        return active(world) && GameState.KEY.get(world).lucidSnapshot.getCompound("deaths").getBoolean(player.getUuidAsString());
    }

    public static void wokeUp(ServerPlayerEntity player) {
        var game = GameWorldComponent.KEY.get(player.getWorld());
        if (!SeatAssignment.isBttMode(player.getWorld()) || !game.isRunning()
                || !GameFunctions.isPlayerAliveAndSurvival(player) || Drunk.isDrunk(player)) return;
        var definition = RoleDefs.get(game.getRole(player));
        if (definition != null) definition.dispatchWake(player);
    }

    public static void begin(ServerPlayerEntity player) {
        var world = player.getServerWorld();
        var state = PlayerState.KEY.get(player);
        if (active(world) || Drunk.isDrunk(player)
                || !GameWorldComponent.KEY.get(world).isRunning()
                || !GameFunctions.isPlayerAliveAndSurvival(player)) return;
        var snapshot = new NbtCompound();
        snapshot.putString("owner", player.getUuidAsString());
        snapshot.putLong("deadline", world.getTime() + 2400);
        snapshot.putLong("time", world.getTime()); snapshot.putLong("dayTime", world.getTimeOfDay());
        var players = new NbtCompound();
        for (var p : world.getPlayers()) {
            var saved = new NbtCompound();
            saved.put("player", p.writeNbt(new NbtCompound()));
            saved.putInt("mode", p.interactionManager.getGameMode().getId());
            saved.put("cooldowns", cooldowns(p));
            players.put(p.getUuidAsString(), saved);
        }
        snapshot.put("players", players);
        snapshot.put("world", ((ComponentProvider)world).getComponentContainer().toTag(new NbtCompound(), world.getRegistryManager()));
        var entities = new NbtList();
        for (Entity entity : world.iterateEntities()) {
            if (entity instanceof PlayerEntity) continue;
            var saved = new NbtCompound();
            if (entity.saveNbt(saved)) { saved.putInt("bttCheckpointAge", entity.age); entities.add(saved); }
        }
        snapshot.put("entities", entities);
        snapshot.put("deaths", new NbtCompound());
        GameState.KEY.get(world).lucidSnapshot = snapshot;
        state.lucidActive = true; state.sync();
    }


    public static boolean beforeDeathReactions(ServerPlayerEntity dead, ServerPlayerEntity killer, Identifier reason) {
        var world = dead.getServerWorld();
        if (!active(world) || dev.hae.btt.combat.DeathReasons.isSuicide(reason)) return false;
        var snapshot = GameState.KEY.get(world).lucidSnapshot;
        if (!snapshot.getString("owner").equals(dead.getUuidAsString())) return false;
        var hostState = snapshot.getCompound("players").getCompound(dead.getUuidAsString()).copy();
        restore(world, snapshot);
        if (world.getPlayerByUuid(dead.getUuid()) == null) {
            hostState.putBoolean("lucidHost", true);
            restorePlayer(dead, hostState);
        }
        dev.hae.btt.combat.DeathResolution.forceFinalDeath(dead, killer, reason, true);
        return true;
    }

    public static void afterDeath(ServerPlayerEntity dead) {
        var world = dead.getServerWorld();
        if (!active(world)) return;
        var host = owner(world);
        var saved = GameState.KEY.get(world).lucidSnapshot.getCompound("players").getCompound(dead.getUuidAsString());
        if (host == null || dead == host || saved.isEmpty()) return;
        var deaths = GameState.KEY.get(world).lucidSnapshot.getCompound("deaths");
        deaths.putBoolean(dead.getUuidAsString(), true);
        GameState.KEY.get(world).lucidSnapshot.put("deaths", deaths);
        if (!dead.isSpectator()) dead.changeGameMode(GameMode.SPECTATOR);
        if (PlayerState.KEY.get(dead).isProjecting()) {
            PlayerState.KEY.get(dead).sleepwalkTicks = 0;
            PlayerState.KEY.get(dead).stopProjecting();
        }
        Attachment.attach(dead, host, KIND);
        dev.hae.btt.voice.VoiceChatPlugin.joinDream(host, dead);
    }

    public static void tick(ServerWorld world) {
        var gameState = GameState.KEY.get(world);
        for (var p : world.getPlayers()) {
            var state = PlayerState.KEY.get(p);
            if (gameState.lucidReturns.contains(p.getUuidAsString())) {
                var saved = gameState.lucidReturns.getCompound(p.getUuidAsString()).copy();
                gameState.lucidReturns.remove(p.getUuidAsString());
                restorePlayer(p, saved);
            }
        }
        if (!active(world)) return;
        var host = owner(world);
        if (host == null) {
            String missingOwner = gameState.lucidSnapshot.getString("owner");
            restore(world, gameState.lucidSnapshot.copy());
            if (gameState.lucidReturns.contains(missingOwner)) gameState.lucidReturns.getCompound(missingOwner).putInt("mode", GameMode.SPECTATOR.getId());
            return;
        }
        if (!GameWorldComponent.KEY.get(world).isRunning()) { clear(world); return; }
        if (!GameFunctions.isPlayerAliveAndSurvival(host) || !host.isAlive()) {
            clear(world);
            return;
        }
        if (world.getTime() >= gameState.lucidSnapshot.getLong("deadline")) {
            clear(world);
            return;
        }
        for (var p : world.getPlayers()) {
            if (dreamDead(p)) {
                if (!p.isSpectator()) p.changeGameMode(GameMode.SPECTATOR);
                if (!captive(p)) afterDeath(p);
            }
        }
    }

    public static void clear(ServerWorld world) {
        var deaths = GameState.KEY.get(world).lucidSnapshot.getCompound("deaths").copy();
        GameState.KEY.get(world).lucidSnapshot = new NbtCompound();
        for (var p : world.getPlayers()) {
            if (deaths.getBoolean(p.getUuidAsString()) && !p.isSpectator()) p.changeGameMode(GameMode.SPECTATOR);
            if (captive(p)) { Attachment.detach(p); TrainVoicePlugin.addPlayer(p.getUuid()); }
            var state = PlayerState.KEY.get(p);
            if (state.lucidActive) {
                state.lucidActive = false; state.sync();
            }
        }
    }

    private static ServerPlayerEntity owner(ServerWorld world) {
        try { return world.getPlayerByUuid(UUID.fromString(GameState.KEY.get(world).lucidSnapshot.getString("owner"))) instanceof ServerPlayerEntity p ? p : null; }
        catch (IllegalArgumentException e) { return null; }
    }

    private static void restore(ServerWorld world, NbtCompound source) {
        var snapshot = source.copy();
        rebaseClocks(snapshot, world.getTime() - snapshot.getLong("time"));
        var owner = snapshot.getString("owner");
        dev.hae.btt.registry.Sounds.clearEpilogue(world);
        clear(world);
        var container = ((ComponentProvider)world).getComponentContainer();
        container.fromTag(snapshot.getCompound("world"), world.getRegistryManager());
        var entities = new ArrayList<Entity>();
        world.iterateEntities().forEach(e -> { if (!(e instanceof PlayerEntity)) entities.add(e); });
        entities.forEach(Entity::discard);
        for (var tag : snapshot.getList("entities", NbtElement.COMPOUND_TYPE)) {
            Entity entity = EntityType.loadEntityWithPassengers((NbtCompound)tag, world, e -> e);
            if (entity != null) { entity.age = ((NbtCompound)tag).getInt("bttCheckpointAge"); world.spawnEntityAndPassengers(entity); }
        }
        var players = snapshot.getCompound("players");
        for (String id : players.getKeys()) {
            var saved = players.getCompound(id).copy();
            if (id.equals(owner)) saved.putBoolean("lucidHost", true);
            var p = world.getServer().getPlayerManager().getPlayer(UUID.fromString(id));
            if (p == null) p = world.getPlayerByUuid(UUID.fromString(id)) instanceof ServerPlayerEntity s ? s : null;
            if (p != null) restorePlayer(p, saved);
            else {
                if (id.equals(owner)) saved.putInt("mode", GameMode.SPECTATOR.getId());
                GameState.KEY.get(world).lucidReturns.put(id, saved);
            }
        }
        GameWorldComponent.KEY.get(world).setFade(dev.doctor4t.wathe.game.GameConstants.FADE_TIME);
        for (var key : container.keys()) if (key.get(world) instanceof AutoSyncedComponent) key.sync(world);
        var music = GameState.KEY.get(world).roleEffects;
        String track = music.getString("musicCurrent");
        long remaining = music.getLong("musicUntil") - world.getTime();
        if (!track.isEmpty() && remaining > 0)
            dev.hae.btt.registry.Sounds.requestEpilogue(world, net.minecraft.sound.SoundEvent.of(Identifier.of(track)), (int)Math.min(2400, remaining));
    }

    private static void restorePlayer(ServerPlayerEntity player, NbtCompound saved) {
        if (Attachment.isAttached(player)) Attachment.detach(player);
        TrainVoicePlugin.resetPlayer(player.getUuid());
        player.readNbt(saved.getCompound("player").copy());
        player.changeGameMode(GameMode.byId(saved.getInt("mode")));
        player.sendAbilitiesUpdate();
        player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket(
                net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket.GAME_MODE_CHANGED, saved.getInt("mode")));
        player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket(player.getInventory().selectedSlot));
        player.getServer().getPlayerManager().sendToAll(new net.minecraft.network.packet.s2c.play.PlayerListS2CPacket(
                net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Action.UPDATE_GAME_MODE, player));
        player.setCameraEntity(player);
        var state = PlayerState.KEY.get(player);
        if (saved.getBoolean("lucidHost")) state.lucidActive = false;
        player.teleport(player.getServerWorld(), player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
        var manager = player.getItemCooldownManager();
        var access = (CooldownSnapshotAccessor)manager;
        for (var item : new ArrayList<>(access.btt$entries().keySet())) manager.remove(item);
        for (var tag : saved.getList("cooldowns", NbtElement.COMPOUND_TYPE)) {
            var cd = (NbtCompound)tag;
            var item = Registries.ITEM.get(Identifier.of(cd.getString("item")));
            manager.set(item, cd.getInt("end"));
            var entry = (CooldownEntryAccessor)access.btt$entries().get(item);
            entry.btt$start(access.btt$tick() + cd.getInt("start"));
            entry.btt$end(access.btt$tick() + cd.getInt("end"));
        }
        var container = ((ComponentProvider)player).getComponentContainer();
        for (var key : container.keys()) if (key.get(player) instanceof AutoSyncedComponent) key.sync(player);
        player.currentScreenHandler.sendContentUpdates();
        player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket(
                player.getHealth(), player.getHungerManager().getFoodLevel(), player.getHungerManager().getSaturationLevel()));
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, new dev.hae.btt.network.DreamRestoreS2CPacket());
        if (saved.getInt("mode") == GameMode.SPECTATOR.getId()) TrainVoicePlugin.addPlayer(player.getUuid());
    }

    private static NbtList cooldowns(ServerPlayerEntity player) {
        var saved = new NbtList(); var manager = (CooldownSnapshotAccessor)player.getItemCooldownManager();
        manager.btt$entries().forEach((item, value) -> {
            var entry = (CooldownEntryAccessor)value; var cd = new NbtCompound();
            cd.putString("item", Registries.ITEM.getId(item).toString());
            cd.putInt("start", entry.btt$start() - manager.btt$tick());
            cd.putInt("end", entry.btt$end() - manager.btt$tick()); saved.add(cd);
        });
        return saved;
    }


    private static final java.util.Set<String> CLOCK_KEYS = java.util.Set.of(
            "openingUntil", "openingMessageAt", "smugglerReleaseAt", "voodooUntil", "cageAt", "cageDeadline", "craftReadyAt", "memoryNextGuess",
            "goonNextChange", "openingNoticeAt", "lastExecutionAt",
            "dreamDeadline", "musicUntil", "priestProtection");
    private static void rebaseClocks(NbtElement value, long delta) {
        if (value instanceof NbtCompound compound) {
            for (String key : compound.getKeys()) {
                var child = compound.get(key);
                if (child instanceof NbtLong && (CLOCK_KEYS.contains(key) || key.startsWith("isolated:"))) {
                    long time = compound.getLong(key);
                    if (time > 0) compound.putLong(key, time + delta);
                } else if (child != null) rebaseClocks(child, delta);
            }
        } else if (value instanceof NbtList list) for (var child : list) rebaseClocks(child, delta);
    }
}
