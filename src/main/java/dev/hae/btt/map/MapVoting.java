package dev.hae.btt.map;

import dev.doctor4t.wathe.api.WatheMapEffects;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.MapVariablesWorldComponent;
import dev.hae.btt.component.MapVoteState;
import dev.hae.btt.game.BeforeTheTerminalGameMode;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;














public final class MapVoting {
    private MapVoting() {}


    private static final int VOTE_TICKS = 10 * 20;
    private static final int ALL_VOTED_TICKS = 5 * 20;
    private static final int ROULETTE_TICKS = 6 * 20;

    private static final int START_DELAY_TICKS = 10 * 20;


    private static final java.util.Map<ServerWorld, Integer> PENDING_START = new java.util.HashMap<>();


    public static void clearPending() { PENDING_START.clear(); }


    public static MapVoteState state(ServerWorld anyWorld) {
        MinecraftServer server = anyWorld.getServer();
        return MapVoteState.KEY.get(server == null ? anyWorld : server.getOverworld());
    }


    private static List<ServerPlayerEntity> allPlayers(MinecraftServer server) {
        List<ServerPlayerEntity> out = new ArrayList<>();
        for (ServerWorld w : server.getWorlds()) out.addAll(w.getPlayers());
        return out;
    }




    public static void startAfterGame(ServerWorld world) {
        MinecraftServer server = world.getServer();
        if (server == null) return;
        MapVoteState st = state(world);
        if (st.active) return;


        if (START_DELAY_TICKS > 0) {
            PENDING_START.put(server.getOverworld(), START_DELAY_TICKS);
            BeforeTheTerminalGameMode.LOGGER.info("[BTT] map vote scheduled in {} s", START_DELAY_TICKS / 20);
            return;
        }
        startNow(world);
    }





    public static void startNow(ServerWorld world) {
        MinecraftServer server = world.getServer();
        if (server == null) return;
        int playerCount = allPlayers(server).size();
        begin(world, BttMaps.playable(), playerCount, false);
    }





    public static void startPreview(ServerWorld world) {
        MinecraftServer server = world.getServer();
        if (server == null) return;
        begin(world, BttMaps.playable(), allPlayers(server).size(), true);
    }








    private static void begin(ServerWorld world, List<BttMapEntry> candidates, int playerCount, boolean preview) {
        MapVoteState st = state(world);
        if (st.active) {
            BeforeTheTerminalGameMode.LOGGER.info("[BTT] map vote ignored: a vote is already active");
            return;
        }

        boolean[] ok = new boolean[candidates.size()];
        int okCount = 0;
        int okIndex = -1;
        for (int i = 0; i < candidates.size(); i++) {
            ok[i] = preview || candidates.get(i).isEligible(playerCount);
            if (ok[i]) {
                okCount++;
                okIndex = i;
            }
        }
        if (okCount == 0) {
            boolean headcountOk = playerCount >= dev.hae.btt.game.SeatAssignment.MIN_PLAYERS
                    && playerCount <= dev.hae.btt.game.SeatAssignment.MAX_PLAYERS;
            BeforeTheTerminalGameMode.LOGGER.warn(
                    "[BTT] map vote not started: no eligible map for {} player(s){}; unavailable: {}",
                    playerCount,
                    headcountOk ? "" : " (BTT 开局需要 " + dev.hae.btt.game.SeatAssignment.MIN_PLAYERS
                            + "–" + dev.hae.btt.game.SeatAssignment.MAX_PLAYERS + " 人)",
                    BttMaps.unavailableReasons(playerCount));
            return;
        }
        if (okCount == 1) {


            String err = apply(world, candidates.get(okIndex), false);
            if (err != null) BeforeTheTerminalGameMode.LOGGER.warn("[BTT] map apply failed: {}", err);
            return;
        }

        st.ids.clear();
        st.names.clear();
        st.descs.clear();
        st.minimums.clear(); st.maximums.clear();
        for (BttMapEntry e : candidates) {
            st.ids.add(e.id());
            st.names.add(e.displayName());
            st.descs.add(e.description());
            st.minimums.add(e.minPlayers()); st.maximums.add(e.maxPlayers());
        }
        st.eligible = ok;
        st.counts = new int[candidates.size()];
        st.votes.clear();
        st.active = true;
        st.roulette = false;
        st.rouletteTicks = 0;
        st.selected = -1;
        st.ticks = VOTE_TICKS;
        st.sync();
        BeforeTheTerminalGameMode.LOGGER.info("[BTT] map vote started: {} candidates ({} eligible, players={}{}), unavailable: {}",
                candidates.size(), okCount, playerCount, preview ? ", preview" : "",
                BttMaps.unavailableReasons(playerCount));
    }


    public static void cancel(ServerWorld world) {
        PENDING_START.remove(world.getServer().getOverworld());
        MapVoteState st = state(world);
        if (st.active) {
            BeforeTheTerminalGameMode.LOGGER.info("[BTT] map vote cancelled (a new round started)");
            st.reset();
        }
    }


    public static boolean tickPending(ServerWorld world) {
        Integer left = PENDING_START.get(world);
        if (left == null) return false;
        if (left <= 1) {
            PENDING_START.remove(world);
            startNow(world);
            return true;
        }
        PENDING_START.put(world, left - 1);
        return true;
    }


    public static void castVote(ServerWorld world, UUID player, int index) {
        MapVoteState st = state(world);
        if (!st.active || st.roulette) return;
        if (index < 0 || index >= st.ids.size()) return;

        if (index < st.eligible.length && !st.eligible[index]) return;
        Integer old = st.votes.get(player);
        if (old != null && old >= 0 && old < st.counts.length) st.counts[old] = Math.max(0, st.counts[old] - 1);
        st.votes.put(player, index);
        st.counts[index]++;
        MinecraftServer server = world.getServer();
        if (server != null && st.votes.size() >= allPlayers(server).size() && st.ticks > ALL_VOTED_TICKS) {
            st.ticks = ALL_VOTED_TICKS;
        }
        st.sync();
    }


    public static void tick(ServerWorld world) {
        MapVoteState st = state(world);
        if (!st.active) return;
        if (st.roulette) {



            if (--st.rouletteTicks <= 0) finish(world);
            else if (st.rouletteTicks % 2 == 0) st.sync();
            return;
        }
        if (st.ticks > 0 && --st.ticks <= 0) endVoting(world);
        if (st.ticks % 20 == 0) st.sync();
    }


    private static void endVoting(ServerWorld world) {
        MapVoteState st = state(world);
        st.selected = weightedPick(st.counts, st.eligible);
        st.roulette = true;
        st.rouletteTicks = ROULETTE_TICKS;
        st.sync();
        BeforeTheTerminalGameMode.LOGGER.info("[BTT] map vote ended: {} of {} (votes={})",
                st.selected >= 0 && st.selected < st.ids.size() ? st.ids.get(st.selected) : "?",
                st.ids.size(), java.util.Arrays.toString(st.counts));
    }


    private static void finish(ServerWorld world) {
        MapVoteState st = state(world);
        if (st.selected < 0 || st.selected >= st.ids.size()) {
            st.reset();
            return;
        }
        BttMapEntry entry = BttMaps.get(st.ids.get(st.selected));
        if (entry == null) {
            st.reset();
            return;
        }
        String err = apply(world, entry);
        if (err != null) BeforeTheTerminalGameMode.LOGGER.warn("[BTT] map apply failed: {}", err);
        st.reset();
    }







    public static String apply(ServerWorld anyWorld, BttMapEntry entry) {
        return apply(anyWorld, entry, true);
    }








    public static String apply(ServerWorld anyWorld, BttMapEntry entry, boolean movePlayers) {
        MinecraftServer server = anyWorld.getServer();
        if (server == null) return "服务端不可用";
        Identifier dimId = Identifier.tryParse(entry.dimension());
        if (dimId == null) {
            BeforeTheTerminalGameMode.LOGGER.warn("[BTT] map '{}' has an invalid dimension id", entry.id());
            return "地图 " + entry.id() + " 的 dimension 非法：" + entry.dimension();
        }
        ServerWorld target = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, dimId));
        if (target == null) {
            BeforeTheTerminalGameMode.LOGGER.warn("[BTT] map '{}' dimension {} not loaded", entry.id(), dimId);
            return "地图 " + entry.id() + " 的维度未加载：" + dimId;
        }

        MapVariablesWorldComponent vars = MapVariablesWorldComponent.KEY.get(target);
        entry.spawnPos().ifPresent(p -> vars.setSpawnPos(new MapVariablesWorldComponent.PosWithOrientation(
                p, entry.spawnYaw().orElse(0f), entry.spawnPitch().orElse(0f))));
        entry.spectatorSpawnPos().ifPresent(p -> vars.setSpectatorSpawnPos(
                new MapVariablesWorldComponent.PosWithOrientation(p, 0f, 0f)));
        entry.readyArea().ifPresent(vars::setReadyArea);
        entry.playArea().ifPresent(vars::setPlayArea);
        entry.resetTemplateArea().ifPresent(vars::setResetTemplateArea);
        entry.playAreaOffset().ifPresent(vars::setPlayAreaOffset);
        entry.resetPasteOffset().ifPresent(vars::setResetPasteOffset);


        if (entry.mapEffect() != null && !entry.mapEffect().isEmpty()) {
            Identifier fxId = Identifier.tryParse(entry.mapEffect());
            var fx = fxId == null ? null : WatheMapEffects.MAP_EFFECTS.get(fxId);
            if (fx != null) {
                GameWorldComponent.KEY.get(target).setMapEffect(fx);
            } else {
                BeforeTheTerminalGameMode.LOGGER.warn("[BTT] map '{}' unknown map effect {}", entry.id(), entry.mapEffect());
            }
        }

        dev.hae.btt.game.RoundResults.apply(target);
        Vec3d spawn = vars.getSpawnPos().pos;
        float yaw = vars.getSpawnPos().yaw;
        float pitch = vars.getSpawnPos().pitch;
        if (movePlayers) {
            for (ServerPlayerEntity p : allPlayers(server)) {

                p.teleport(target, spawn.getX() + 0.5, spawn.getY() + 1, spawn.getZ() + 0.5, yaw, pitch);
                p.getInventory().clear();

                dev.doctor4t.wathe.compat.TrainVoicePlugin.resetPlayer(p.getUuid());
            }
        }
        MapVoteState st = state(anyWorld);
        st.lastSelected = entry.id();
        MapVoteState targetState = MapVoteState.KEY.get(target);
        if (!entry.rooms().isEmpty()) {
            targetState.rooms.clear();
            targetState.rooms.addAll(entry.rooms());
            targetState.roomsMapId = entry.id();
        }
        st.sync();
        targetState.enhancements = entry.enhancements() == null ? MapEnhancements.NONE : entry.enhancements();
        targetState.sync();
        if (!targetState.enhancements.isDefault()) {

            BeforeTheTerminalGameMode.LOGGER.info("[BTT] map enhancements: blacklist={} block(s)+{} tag(s), walk={}, sprint={}, gravity={}",
                    targetState.enhancements.blacklistBlocks().size(), targetState.enhancements.blacklistTags().size(),
                    targetState.enhancements.walkSpeedMultiplier(), targetState.enhancements.sprintSpeedMultiplier(),
                    targetState.enhancements.gravityMultiplier());
        }
        MapEnhancements.restoreLobbyVisual(target);
        BeforeTheTerminalGameMode.LOGGER.info("[BTT] map applied: {} ({}) for {} player(s)",
                entry.id(), dimId, server.getCurrentPlayerCount());
        return null;
    }


    private static int weightedPick(int[] counts, boolean[] eligible) {
        if (counts == null || counts.length == 0) return -1;
        boolean anyEligible = false;
        for (int i = 0; i < counts.length; i++) {
            if (i >= eligible.length || eligible[i]) { anyEligible = true; break; }
        }
        if (!anyEligible) return -1;
        boolean any = false;
        for (int i = 0; i < counts.length; i++) {
            if (i < eligible.length && !eligible[i]) continue;
            if (counts[i] > 0) { any = true; break; }
        }
        int total = 0;
        int[] w = new int[counts.length];
        for (int i = 0; i < counts.length; i++) {
            if (i < eligible.length && !eligible[i]) { w[i] = 0; continue; }
            w[i] = any ? Math.max(0, counts[i]) : 1;
            total += w[i];
        }
        if (total <= 0) return firstEligible(eligible);
        int roll = java.util.concurrent.ThreadLocalRandom.current().nextInt(total);
        int acc = 0;
        for (int i = 0; i < w.length; i++) {
            acc += w[i];
            if (roll < acc) return i;
        }
        return firstEligible(eligible);
    }


    private static int firstEligible(boolean[] eligible) {
        for (int i = 0; i < eligible.length; i++) {
            if (eligible[i]) return i;
        }
        return 0;
    }






    public static String force(ServerWorld world, String mapId) {
        BttMapEntry e = BttMaps.get(mapId);
        if (e == null) return "未找到地图 " + mapId + "（数据包 data/btt/maps 或 data/wathe/maps）";
        String err = apply(world, e);
        if (err == null) state(world).sync();
        return err;
    }
}
