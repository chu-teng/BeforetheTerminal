package dev.hae.btt.game;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.component.DebateState;
import dev.hae.btt.component.LetterState;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.combat.DeathReasons;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;










public final class Debate {
    private Debate() {}




    public static void report(ServerPlayerEntity reporter, PlayerBodyEntity body) {
        ServerWorld world = reporter.getServerWorld();
        if (!GameModes.isDebate(world)) return;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(world);
        if (!gwc.isRunning()) return;
        if (!GameFunctions.isPlayerAliveAndSurvival(reporter)) return;
        DebateState st = DebateState.KEY.get(world);
        if (st.phase != DebateState.PHASE_IDLE) return;
        st.phase = DebateState.PHASE_MEETING;
        st.ticksLeft = DebateState.MEETING_TICKS;
        st.reportedBody = body == null ? "" : body.getUuidAsString();
        st.reporter = reporter.getUuidAsString();
        st.votes = "";

        st.tieDrunk = "";

        int alive = aliveCount(world);
        int deaths = st.lastMeetingAlive < 0 ? 0 : Math.max(0, st.lastMeetingAlive - alive);
        st.lastMeetingAlive = alive;
        markDeadInLetters(world);
        if (body != null) body.setGlowing(true);
        reporter.setGlowing(true);
        st.sync();
        broadcast(world, Text.translatable("btt.debate.meeting.start", deaths));
    }



    public static void tick(ServerWorld world) {
        if (!GameModes.isDebate(world)) return;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(world);
        DebateState st = DebateState.KEY.get(world);

        if (!st.tieDrunk.isEmpty()) {
            for (String uuid : st.tieDrunk.split(",")) {
                ServerPlayerEntity p = playerByUuid(world, uuid);
                if (p != null && GameFunctions.isPlayerAliveAndSurvival(p)) {
                    PlayerState.KEY.get(p).applyDrunk(2);
                }
            }
        }
        if (st.phase == DebateState.PHASE_IDLE) return;
        if (!gwc.isRunning()) {
            endMeeting(world, st);
            return;
        }
        if (--st.ticksLeft > 0) {
            if (st.ticksLeft % 20 == 0) st.sync();
            return;
        }
        if (st.phase == DebateState.PHASE_MEETING) {
            st.phase = DebateState.PHASE_VOTING;
            st.ticksLeft = DebateState.VOTING_TICKS;
            st.votes = "";
            st.sync();
            broadcast(world, Text.translatable("btt.debate.vote.start"));
            return;
        }
        settle(world, st);
    }


    public static void vote(ServerPlayerEntity voter, UUID target) {
        ServerWorld world = voter.getServerWorld();
        if (!GameModes.isDebate(world)) return;
        if (!GameFunctions.isPlayerAliveAndSurvival(voter)) return;
        if (target == null || target.equals(voter.getUuid())) return;
        ServerPlayerEntity victim = playerByUuid(world, target.toString());
        if (victim == null || !GameFunctions.isPlayerAliveAndSurvival(victim)) return;
        DebateState st = DebateState.KEY.get(world);
        if (st.phase != DebateState.PHASE_VOTING) return;
        Map<String, String> votes = parseVotes(st.votes);
        votes.put(voter.getUuidAsString(), target.toString());
        st.votes = joinVotes(votes);
        st.sync();
    }






    public static boolean handleLetterVote(ServerPlayerEntity voter,
                                           dev.hae.btt.network.LetterC2SPacket payload) {
        if (voter == null || payload == null) return false;
        ServerWorld world = voter.getServerWorld();
        if (!GameModes.isDebate(world)) return false;
        DebateState st = DebateState.KEY.get(world);
        if (st.phase != DebateState.PHASE_VOTING) return false;
        if (!GameFunctions.isPlayerAliveAndSurvival(voter)) return true;

        if (payload.value() != null && !payload.value().isEmpty()) return true;
        if (payload.status() == LetterState.STATUS_ALIVE) {
            clearVote(voter);
        } else if (payload.status() >= 0) {
            vote(voter, payload.target());
        }
        return true;
    }


    public static void clearVote(ServerPlayerEntity voter) {
        ServerWorld world = voter.getServerWorld();
        if (!GameModes.isDebate(world)) return;
        DebateState st = DebateState.KEY.get(world);
        if (st.phase != DebateState.PHASE_VOTING) return;
        Map<String, String> votes = parseVotes(st.votes);
        if (votes.remove(voter.getUuidAsString()) == null) return;
        st.votes = joinVotes(votes);
        st.sync();
    }


    public static boolean vetoDamage(net.minecraft.world.World world, Identifier reason) {
        if (!GameModes.isDebate(world)) return false;
        DebateState st = DebateState.KEY.get(world);
        if (st.phase == DebateState.PHASE_IDLE) return false;
        if (reason == null) return true;
        return !reason.equals(dev.doctor4t.wathe.game.GameConstants.DeathReasons.FELL_OUT_OF_TRAIN)
                && !reason.equals(DeathReasons.ESCAPE);
    }


    public static void reset(ServerWorld world) {
        DebateState st = DebateState.KEY.get(world);
        st.reset();
        st.sync();
    }


    public static void onRoundStart(ServerWorld world, List<ServerPlayerEntity> players) {
        DebateState st = DebateState.KEY.get(world);
        st.reset();
        st.lastMeetingAlive = players.size();
        st.sync();
    }



    private static void settle(ServerWorld world, DebateState st) {
        Map<String, String> votes = parseVotes(st.votes);
        Map<String, Integer> tally = new HashMap<>();
        for (String target : votes.values()) {
            if (target == null || target.isEmpty()) continue;
            tally.merge(target, 1, Integer::sum);
        }
        int alive = aliveCount(world);
        int threshold = MathHelper.ceil(alive / 2.0);
        int top = 0;
        for (int n : tally.values()) top = Math.max(top, n);
        List<String> tied = new ArrayList<>();
        for (Map.Entry<String, Integer> e : tally.entrySet()) {
            if (e.getValue() == top) tied.add(e.getKey());
        }

        endMeeting(world, st);
        if (top >= 1 && top >= threshold) {
            if (tied.size() == 1) {
                ServerPlayerEntity victim = playerByUuid(world, tied.get(0));
                if (victim != null && GameFunctions.isPlayerAliveAndSurvival(victim)) {

                    GameFunctions.killPlayer(victim, true, null, DeathReasons.EXECUTION);
                    if (!GameFunctions.isPlayerAliveAndSurvival(victim)) {
                        dev.hae.btt.advancement.BttAdvancements.debate(world, victim, votes, alive);
                        broadcast(world, Text.translatable("btt.debate.result.executed",
                                victim.getName().getString(), top));
                    }
                }
            } else {
                broadcast(world, Text.translatable("btt.debate.result.tie", tied.size(), top));
                Set<String> drunk = new HashSet<>();
                for (String uuid : st.tieDrunk.split(",")) if (!uuid.isEmpty()) drunk.add(uuid);
                drunk.addAll(tied);
                st.tieDrunk = String.join(",", drunk);

                for (String uuid : tied) {
                    ServerPlayerEntity p = playerByUuid(world, uuid);
                    if (p != null) PlayerState.KEY.get(p).applyDrunk(DebateState.VOTING_TICKS + 40);
                }
            }
        } else {
            broadcast(world, Text.translatable("btt.debate.result.none", top, threshold));
        }
        st.sync();
    }


    private static void endMeeting(ServerWorld world, DebateState st) {
        if (!st.reportedBody.isEmpty()) {
            try {
                if (world.getEntity(UUID.fromString(st.reportedBody)) instanceof PlayerBodyEntity b) {
                    b.setGlowing(false);
                }
            } catch (IllegalArgumentException ignored) {

            }
        }
        if (!st.reporter.isEmpty()) {
            ServerPlayerEntity reporter = playerByUuid(world, st.reporter);
            if (reporter != null) reporter.setGlowing(false);
        }
        st.phase = DebateState.PHASE_IDLE;
        st.ticksLeft = 0;
        st.reportedBody = "";
        st.reporter = "";
        st.votes = "";
        st.sync();
    }


    public static void finishRound(ServerWorld world) {
        DebateState st = DebateState.KEY.get(world);
        endMeeting(world, st);
        st.reset();
        st.sync();
    }



    private static int aliveCount(ServerWorld world) {
        int n = 0;
        for (ServerPlayerEntity p : world.getPlayers()) {
            if (GameFunctions.isPlayerAliveAndSurvival(p)) n++;
        }
        return n;
    }

    private static ServerPlayerEntity playerByUuid(ServerWorld world, String uuid) {
        if (uuid == null || uuid.isEmpty()) return null;
        try {
            return world.getPlayerByUuid(UUID.fromString(uuid)) instanceof ServerPlayerEntity p ? p : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }


    private static void markDeadInLetters(ServerWorld world) {
        List<UUID> dead = new ArrayList<>();
        for (ServerPlayerEntity p : world.getPlayers()) {
            if (!GameFunctions.isPlayerAliveAndSurvival(p)) dead.add(p.getUuid());
        }
        if (dead.isEmpty()) return;
        for (ServerPlayerEntity p : world.getPlayers()) {
            LetterState letter = LetterState.KEY.get(p);
            for (UUID uuid : dead) letter.setStatus(uuid.toString(), LetterState.STATUS_DEAD);
            letter.sync();
        }
    }

    private static void broadcast(ServerWorld world, Text text) {
        for (ServerPlayerEntity p : world.getPlayers()) p.sendMessage(text, true);
    }

    private static Map<String, String> parseVotes(String csv) {
        Map<String, String> out = new LinkedHashMap<>();
        if (csv == null || csv.isEmpty()) return out;
        for (String part : csv.split(",")) {
            int colon = part.indexOf(':');
            if (colon <= 0) continue;
            out.put(part.substring(0, colon), part.substring(colon + 1));
        }
        return out;
    }

    private static String joinVotes(Map<String, String> votes) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : votes.entrySet()) {
            if (sb.length() > 0) sb.append(',');
            sb.append(e.getKey()).append(':').append(e.getValue());
        }
        return sb.toString();
    }
}
