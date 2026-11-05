package dev.hae.btt.game;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;


public final class RoundCleanup {
    private RoundCleanup() {}


    public static void clearServerSessions() {
        dev.hae.btt.perception.ObservationRefresh.clear();
        dev.hae.btt.record.GameRecordManager.clear();
        dev.hae.btt.record.replay.ReplayPlayer.clear();
        dev.hae.btt.map.MapEnhancements.clearExplicitEffects();
        dev.hae.btt.map.MapVoting.clearPending();
        dev.hae.btt.draft.DraftManager.reset();
        dev.hae.btt.role.Engineer.clearAll();
        dev.hae.btt.role.Flowers.forgetTransientState();
        dev.hae.btt.role.Librarian.clear();
        Trophies.clearPendingAwards();
    }


    public static void prepareWorld(ServerWorld world, java.util.List<ServerPlayerEntity> players) {
        dev.hae.btt.component.GameState.KEY.get(world).lucidReturns = new net.minecraft.nbt.NbtCompound();
        dev.hae.btt.role.LucidDream.clear(world);
        dev.hae.btt.mechanic.Mines.clear(world);
        dev.hae.btt.role.Toymaker.clear(world);
        dev.hae.btt.role.Librarian.clear(world);
        dev.hae.btt.role.ColdWave.clear(world);
        dev.hae.btt.role.Engineer.clear(world);
        Trophies.clearAll(world);
        dev.hae.btt.registry.Sounds.clearEpilogue(world);
        for (var player : players) Needs.clear(player);
    }

    public static void clearWorldRules(ServerWorld world) {
        dev.hae.btt.component.GameState.KEY.get(world).lucidReturns = new net.minecraft.nbt.NbtCompound();
        dev.hae.btt.role.LucidDream.clear(world);
        dev.hae.btt.mechanic.Mines.clear(world);
        dev.hae.btt.role.Toymaker.clear(world);
        dev.hae.btt.component.DraftState.KEY.get(world).knownRoles = "";
        dev.hae.btt.component.DraftState.KEY.get(world).sync();
        dev.hae.btt.role.Warden.clear(world);
        dev.hae.btt.role.Voodoo.clear(world);
        dev.hae.btt.role.Flowers.clearWorld(world);
        dev.hae.btt.role.Librarian.clear(world);
        dev.hae.btt.role.ColdWave.clear(world);
        dev.hae.btt.role.Engineer.clear(world);
        dev.hae.btt.registry.Sounds.clearEpilogue(world);
        dev.hae.btt.role.Kidnapper.releaseAll(world);

        for (dev.hae.btt.entity.FlyingSwordEntity sword
                : world.getEntitiesByType(dev.hae.btt.registry.BttEntities.FLYING_SWORD, e -> true)) {
            sword.discard();
        }

    }


    public static void resetPlayerState(ServerPlayerEntity p) {
        dev.hae.btt.component.PlayerState.KEY.get(p).reset();
        Needs.clear(p);

        dev.hae.btt.map.MapEnhancements.clearGravity(p);

    }
}
