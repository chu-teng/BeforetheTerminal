package dev.hae.btt.record.replay;

import dev.hae.btt.record.GameRecordManager;
import dev.hae.btt.record.MatchRecord;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;










public final class ReplayPlayer {

    private ReplayPlayer() {}


    public static final int DEFAULT_INTERVAL = 5;

    public static final int AUTO_INTERVAL = 3;

    private static final class Session {
        final List<Text> lines;
        final int interval;
        int index;
        int counter;

        Session(List<Text> lines, int interval) {
            this.lines = lines;
            this.interval = interval;
        }
    }

    private static final Map<UUID, Session> SESSIONS = new HashMap<>();

    public static void clear() {
        SESSIONS.clear();
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(ReplayPlayer::tick);
    }


    public static boolean start(ServerPlayerEntity player) {
        MatchRecord match = GameRecordManager.getLastFinishedMatch();
        if (match == null) {
            player.sendMessage(Text.literal("[BTT] 还没有已结束的对局记录。").formatted(Formatting.YELLOW), false);
            return false;
        }
        List<Text> lines = ReplayGenerator.lines(match, player.getServerWorld());
        SESSIONS.put(player.getUuid(), new Session(lines, DEFAULT_INTERVAL));


        return true;
    }


    public static void startAll(MinecraftServer server, List<Text> lines) {
        if (lines.isEmpty()) return;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            SESSIONS.put(player.getUuid(), new Session(lines, AUTO_INTERVAL));
        }
    }


    public static boolean stop(ServerPlayerEntity player) {
        return SESSIONS.remove(player.getUuid()) != null;
    }

    public static boolean isPlaying(ServerPlayerEntity player) {
        return SESSIONS.containsKey(player.getUuid());
    }

    private static void tick(MinecraftServer server) {
        if (SESSIONS.isEmpty()) return;
        SESSIONS.entrySet().removeIf(entry -> {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());
            if (player == null) return true;
            Session session = entry.getValue();
            if (session.counter++ < session.interval) return false;
            session.counter = 0;
            if (session.index >= session.lines.size()) {

                return true;
            }
            player.sendMessage(session.lines.get(session.index++), false);
            return false;
        });
    }
}
