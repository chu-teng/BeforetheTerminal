package dev.hae.btt.game;

import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.network.TaskCompletePayload;
import dev.hae.btt.component.NeedsState;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;























public final class Needs {
    private Needs() {}







    private static final List<String> POOL = List.of("EAT", "DRINK", "SLEEP", "OUTSIDE",
            "SOLITUDE", "CROWD", "BATH", "MUSIC");

    private static final int COOLDOWN_TICKS = GameConstants.getInTicks(0, 45);

    private static final int SLEEP_LIKE_TICKS = GameConstants.SLEEP_TASK_DURATION;

    private static final int OUTSIDE_LIKE_TICKS = GameConstants.OUTSIDE_TASK_DURATION;

    private static final int MUSIC_HITS = 7;
    private static final double RANGE = 20.0;


    private static boolean advance(java.util.List<Integer> ticks, int i) {
        int next = ticks.get(i) - 1;
        ticks.set(i, next);
        return next <= 0;
    }





    private static void complete(ServerPlayerEntity player, PlayerMoodComponent mood, String kind) {
        mood.setMood(mood.getMood() + GameConstants.MOOD_GAIN);

        ServerPlayNetworking.send(player, new TaskCompletePayload());
    }







    private static void fireCompleted(ServerPlayerEntity player, String kind) {
        dev.hae.btt.api.event.BttEvents.TaskCompleted.EVENT.invoker()
                .onTaskCompleted(player, kind.toLowerCase(java.util.Locale.ROOT));
    }


    public static void tick(ServerPlayerEntity player) {
        if (!GameFunctions.isPlayerAliveAndSurvival(player)) return;
        NeedsState st = NeedsState.KEY.get(player);
        if (!dev.hae.btt.game.SeatAssignment.isBttMode(player.getWorld())) return;
        PlayerMoodComponent mood = PlayerMoodComponent.KEY.get(player);
        java.util.List<String> kinds = split(st.kinds);
        java.util.List<Integer> ticks = ints(st.ticksCsv, kinds.size());
        java.util.List<Integer> hits = ints(st.hitsCsv, kinds.size());


        boolean changed = false;
        boolean dirty = false;
        java.util.List<String> completed = new java.util.ArrayList<>();
        for (int i = kinds.size() - 1; i >= 0; i--) {
            String kind = kinds.get(i);
            if (!isRecovery(kind)) mood.setMood(mood.getMood() - GameConstants.MOOD_DRAIN);
            boolean done = false;
            switch (kind) {
                case "SOLITUDE" -> {
                    if (countNearby(player) == 0) {
                        dirty = true;
                        done = advance(ticks, i);
                    }
                }
                case "CROWD" -> {
                    if (countNearby(player) >= 2) {
                        dirty = true;
                        done = advance(ticks, i);
                    }
                }
                case "BATH" -> {
                    if (touchingWater(player)) {
                        dirty = true;
                        done = advance(ticks, i);
                    }
                }
                case "MUSIC" -> done = hits.get(i) >= MUSIC_HITS;
                case "RECOVER_ROOM" -> {
                    var pc = dev.hae.btt.component.PlayerState.KEY.get(player);
                    done = player.squaredDistanceTo(pc.roomX, pc.roomY, pc.roomZ) <= 9;
                }
                case "RECOVER_LOOKOUT" -> done = player.getVehicle() instanceof dev.doctor4t.wathe.block.entity.SeatEntity seat
                        && player.getWorld().getBlockState(seat.getBlockPos()).isOf(dev.hae.btt.registry.BttBlocks.LOOKOUT_CHAIR);
                case "RECOVER_CONSOLE" -> {}
                case "EAT", "DRINK" -> {}

                case "SLEEP" -> {
                    if (player.isSleeping()) {
                        dirty = true;
                        done = advance(ticks, i);
                    }
                }
                case "OUTSIDE" -> {
                    if (dev.doctor4t.wathe.Wathe.isSkyVisibleAdjacent(player)) {
                        dirty = true;
                        done = advance(ticks, i);
                    }
                }
                default -> done = true;
            }
            if (done) {
                if (!isRecovery(kind)) complete(player, mood, kind);
                kinds.remove(i);
                ticks.remove(i);
                hits.remove(i);
                completed.add(kind);
                changed = true;
            }
        }



        int watheCount = mood.tasks.size();
        int cap = Math.max(0, MAX_TOTAL - watheCount);
        if (kinds.size() < cap) {
            if (st.nextIn > 0) st.nextIn--;
            else {
                java.util.List<String> free = new java.util.ArrayList<>(POOL);
                free.removeAll(kinds);
                if (!free.isEmpty()) {
                    String kind = free.get(player.getRandom().nextInt(free.size()));
                    kinds.add(kind);
                    ticks.add(switch (kind) {
                case "SOLITUDE", "CROWD", "SLEEP" -> SLEEP_LIKE_TICKS;
                case "BATH", "OUTSIDE" -> OUTSIDE_LIKE_TICKS;
                default -> 0;
                    });
                    hits.add(0);
                    st.nextIn = generationInterval(player, COOLDOWN_TICKS + (watheCount + kinds.size()) * INTERVAL_PER_ACTIVE);
                    changed = true;
                }
            }
        }
        if (changed || dirty) {
            st.kinds = String.join(",", kinds);
            st.ticksCsv = joinInts(ticks);
            st.hitsCsv = joinInts(hits);
        }


        if (changed) st.sync();

        for (String kind : completed) {
            if (isRecovery(kind)) ActionRewards.grantRecoveredItem(player);
            else fireCompleted(player, kind);
        }
    }

    public static boolean isRecovery(String kind) {
        return kind != null && kind.startsWith("RECOVER_");
    }

    public static void addRecovery(ServerPlayerEntity player, String kind) {
        NeedsState state = NeedsState.KEY.get(player);
        var kinds = split(state.kinds);
        var ticks = ints(state.ticksCsv, kinds.size());
        var hits = ints(state.hitsCsv, kinds.size());
        if (kinds.stream().anyMatch(Needs::isRecovery)) return;
        kinds.add(kind);
        ticks.add(0);
        hits.add(0);
        state.kinds = String.join(",", kinds);
        state.ticksCsv = joinInts(ticks);
        state.hitsCsv = joinInts(hits);
        state.sync();
    }

    public static void recoverAtConsole(ServerPlayerEntity player) {
        NeedsState state = NeedsState.KEY.get(player);
        var kinds = split(state.kinds);
        int index = kinds.indexOf("RECOVER_CONSOLE");
        if (index < 0) return;
        var ticks = ints(state.ticksCsv, kinds.size());
        var hits = ints(state.hitsCsv, kinds.size());
        kinds.remove(index); ticks.remove(index); hits.remove(index);
        state.kinds = String.join(",", kinds);
        state.ticksCsv = joinInts(ticks);
        state.hitsCsv = joinInts(hits);
        state.sync();
        ActionRewards.grantRecoveredItem(player);
    }


    private static final int MAX_TOTAL = 4;

    private static final int INTERVAL_PER_ACTIVE = GameConstants.getInTicks(0, 15);

    private static java.util.List<String> split(String csv) {
        java.util.List<String> out = new java.util.ArrayList<>();
        if (csv == null || csv.isEmpty()) return out;
        for (String s : csv.split(",")) if (!s.isEmpty()) out.add(s);
        return out;
    }

    private static java.util.List<Integer> ints(String csv, int size) {
        java.util.List<Integer> out = new java.util.ArrayList<>();
        if (csv != null && !csv.isEmpty()) for (String s : csv.split(",")) {
            try {
                out.add(Integer.parseInt(s));
            } catch (NumberFormatException ignored) {
                out.add(0);
            }
        }
        while (out.size() < size) out.add(0);
        return out;
    }

    private static String joinInts(java.util.List<Integer> list) {
        StringBuilder sb = new StringBuilder();
        for (int v : list) {
            if (sb.length() > 0) sb.append(',');
            sb.append(v);
        }
        return sb.toString();
    }


    public static void onBlockUse(ServerPlayerEntity player, String kind) {
        NeedsState st = NeedsState.KEY.get(player);
        if ("MUSIC".equals(kind)) {
            java.util.List<String> kinds = split(st.kinds);
            java.util.List<Integer> hits = ints(st.hitsCsv, kinds.size());
            int idx = kinds.indexOf("MUSIC");
            if (idx < 0) return;
            hits.set(idx, hits.get(idx) + 1);
            st.hitsCsv = joinInts(hits);
            st.sync();
        }
    }


    public static void onConsume(ServerPlayerEntity player, String kind) {
        NeedsState st = NeedsState.KEY.get(player);
        java.util.List<String> kinds = split(st.kinds);
        int idx = kinds.indexOf(kind);
        if (idx < 0) return;
        java.util.List<Integer> ticks = ints(st.ticksCsv, kinds.size());
        java.util.List<Integer> hits = ints(st.hitsCsv, kinds.size());
        kinds.remove(idx);
        ticks.remove(idx);
        hits.remove(idx);
        complete(player, PlayerMoodComponent.KEY.get(player), kind);
        st.kinds = String.join(",", kinds);
        st.ticksCsv = joinInts(ticks);
        st.hitsCsv = joinInts(hits);
        st.sync();
        fireCompleted(player, kind);
    }


    private static boolean touchingWater(ServerPlayerEntity player) {
        if (player.isTouchingWater()) return true;
        return player.getWorld().getBlockState(player.getBlockPos())
                .isOf(net.minecraft.block.Blocks.WATER_CAULDRON);
    }


    public static boolean has(ServerPlayerEntity player, String kind) {
        return split(NeedsState.KEY.get(player).kinds).contains(kind);
    }


    public static int count(ServerPlayerEntity player) {
        return split(NeedsState.KEY.get(player).kinds).size();
    }


    public static int drainingCount(ServerPlayerEntity player) {
        return (int) active(player).stream().filter(kind -> !isRecovery(kind)).count();
    }


    private static int generationInterval(ServerPlayerEntity player, int interval) {
        return dev.hae.btt.trait.Traits.has(player, dev.hae.btt.trait.Traits.ANXIOUS)
                ? Math.max(2, interval * 2 / 3) : interval;
    }


    public static java.util.List<String> active(ServerPlayerEntity player) {
        return split(NeedsState.KEY.get(player).kinds);
    }


    private static int countNearby(ServerPlayerEntity player) {
        int n = 0;
        for (ServerPlayerEntity other : player.getServerWorld().getPlayers()) {
            if (other == player) continue;
            if (!GameFunctions.isPlayerAliveAndSurvival(other)) continue;
            if (player.squaredDistanceTo(other) <= RANGE * RANGE) n++;
        }
        return n;
    }


    public static void clear(ServerPlayerEntity player) {
        NeedsState st = NeedsState.KEY.get(player);
        st.kinds = "";
        st.ticksCsv = "";
        st.hitsCsv = "";
        st.nextIn = generationInterval(player, COOLDOWN_TICKS);
        st.sync();
    }
}
