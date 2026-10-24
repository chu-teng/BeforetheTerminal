package dev.hae.btt.game;

import dev.hae.btt.encounter.Backgrounds;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.world.World;
import org.agmas.harpymodloader.Harpymodloader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import dev.hae.btt.role.Roles;




public final class SeatAssignment {
    private SeatAssignment() {}


    public static boolean isBttMode(World world) {
        return GameWorldComponent.KEY.get(world).getGameMode() instanceof BeforeTheTerminalGameMode;
    }


    public static final int MIN_PLAYERS = 6;
    public static final int MAX_PLAYERS = 22;


    public static Map<UUID, Role> assignSeats(List<UUID> players) {
        return assignSeats(players, null);
    }








    public record Roster(List<Role> seats, Map<UUID, Role> forced) {}





    public static Roster buildRoster(List<UUID> players) {
        return buildRoster(players, null);
    }






    private static java.util.Set<String> OPPOSED = java.util.Set.of();
    private static boolean duplicates = false;
    public static void setDuplicates(boolean value) { duplicates = value; }


    public static void setOpposed(java.util.Set<String> paths) {
        OPPOSED = paths == null ? java.util.Set.of() : paths;
    }

    public static Roster buildRoster(List<UUID> players, dev.hae.btt.game.Scripts.Script script) {
        return buildRoster(players, script, false);
    }

    public static Roster buildRoster(List<UUID> players, dev.hae.btt.game.Scripts.Script script, boolean leads) {
        boolean allowDuplicates = duplicates;
        duplicates = false;
        int n = players.size();
        if (n < MIN_PLAYERS || n > MAX_PLAYERS) return null;



        Map<UUID, Role> forced = new HashMap<>(Harpymodloader.FORCED_MODDED_ROLE_FLIP);
        forced.keySet().retainAll(players);
        Harpymodloader.FORCED_MODDED_ROLE.clear();
        Harpymodloader.FORCED_MODDED_ROLE_FLIP.clear();




        BaseSeats baseSeats = baseSeats(n);
        int principal = baseSeats.principal();
        int accomplice = baseSeats.accomplice();
        int lone = baseSeats.lone();
        int outsider = baseSeats.outsider();
        int mad = baseSeats.mad();
        int[] forcedSeat = new int[Roles.Kind.values().length];
        for (Role r : forced.values()) {
            Roles.Kind kind = Roles.kindOf(r);
            if (kind == null) continue;
            switch (kind) {
                case PRINCIPAL -> principal = Math.max(0, principal - 1);
                case ACCOMPLICE -> accomplice = Math.max(0, accomplice - 1);
                case LONE -> lone = Math.max(0, lone - 1);
                case OUTSIDER -> outsider = Math.max(0, outsider - 1);
                case MAD -> mad = Math.max(0, mad - 1);
                default -> forcedSeat[kind.ordinal()]++;
            }
        }
        int remaining = n - forced.size();
        int passenger = baseSeats.passengerCount(n);
        if (passenger < 0) return null;

        int[] quota = passengerQuotas(passenger, n);
        for (int k = 0; k < SEAT_ORDER.size(); k++) {
            quota[k] = Math.max(0, quota[k] - forcedSeat[SEAT_ORDER.get(k).ordinal()]);
        }


        Map<Roles.Kind, List<Role>> kindPools = Roles.kindPools();
        if (script != null) {
            for (Roles.Kind kind : Roles.Kind.values())
                kindPools.put(kind, filterAllowed(kindPools.get(kind), script::allows));
        }

        if (!OPPOSED.isEmpty()) {
            java.util.Set<String> opposed = OPPOSED;
            OPPOSED = java.util.Set.of();
            kindPools.replaceAll((k, pool) ->
                    filterAllowed(pool, r -> !opposed.contains(r.identifier().getPath())));
        }
        kindPools.replaceAll((kind, pool) -> filterAllowed(pool, role -> !forced.containsValue(role)));
        List<Role> seats = new ArrayList<>();
        takeKind(seats, kindPools, Roles.Kind.PRINCIPAL, principal);
        takeKind(seats, kindPools, Roles.Kind.ACCOMPLICE, accomplice);
        takeKind(seats, kindPools, Roles.Kind.LONE, lone);
        takeKind(seats, kindPools, Roles.Kind.OUTSIDER, outsider);
        takeKind(seats, kindPools, Roles.Kind.MAD, mad);
        for (int k = 0; k < SEAT_ORDER.size(); k++) {
            takeFrom(seats, kindPools.getOrDefault(SEAT_ORDER.get(k), List.of()), quota[k]);
        }

        for (Roles.Kind s : SEAT_ORDER) {
            if (seats.size() >= remaining) break;
            takeFrom(seats, kindPools.getOrDefault(s, List.of()), remaining - seats.size());
        }
        Collections.shuffle(seats);
        while (seats.size() > remaining) seats.removeLast();
        if (seats.size() != remaining) return null;

        java.util.List<Role> required = leads ? java.util.List.of(Roles.VIGILANTE, Roles.DETECTIVE, Roles.ASSASSIN) : java.util.List.of();
        for (Role role : required) {
            if (forced.containsValue(role)) continue;
            if (seats.contains(role)) continue;
            int replace = -1;
            for (int i = 0; i < seats.size(); i++)
                if (!required.contains(seats.get(i)) && Roles.kindOf(seats.get(i)) == Roles.kindOf(role)) { replace = i; break; }
            if (replace < 0 && SEAT_ORDER.contains(Roles.kindOf(role)))
                for (int i = 0; i < seats.size(); i++)
                    if (!required.contains(seats.get(i)) && SEAT_ORDER.contains(Roles.kindOf(seats.get(i)))) { replace = i; break; }
            if (replace < 0) return null;
            seats.set(replace, role);
        }

        if (allowDuplicates) {
            var random = java.util.concurrent.ThreadLocalRandom.current();
            for (int i = 0; i < seats.size(); i++) {
                if (!random.nextBoolean()) continue;
                Role original = seats.get(i);
                List<Role> choices = new ArrayList<>();
                for (Role candidate : kindPools.getOrDefault(Roles.kindOf(original), List.of())) {
                    long count = seats.stream().filter(r -> r == candidate).count()
                            + forced.values().stream().filter(r -> r == candidate).count();
                    if (count != 1 || candidate == original) continue;
                    choices.add(candidate);
                }
                if (!choices.isEmpty()) seats.set(i, choices.get(random.nextInt(choices.size())));
            }
        }
        return new Roster(seats, forced);
    }





    public static Map<UUID, Role> assignSeats(List<UUID> players, net.minecraft.server.world.ServerWorld world) {

        Roster roster = buildRoster(players, world == null ? null
                : dev.hae.btt.game.Scripts.byId(dev.hae.btt.component.GameState.KEY.get(world).script),
                world != null && Backgrounds.isActive(world, Backgrounds.LEADS));
        if (roster == null) return null;
        List<Role> seats = new ArrayList<>(roster.seats());
        Map<UUID, Role> forced = roster.forced();
        int n = players.size();
        int remaining = seats.size();

        Collections.shuffle(seats);

        if (dev.hae.btt.role.RoleWeights.hasCustomWeights()) {
            dev.hae.btt.game.BeforeTheTerminalGameMode.LOGGER.info("[BTT] role weights in effect: {}",
                    dev.hae.btt.role.RoleWeights.describeCustomWeights());
        }
        Map<UUID, Role> result = new HashMap<>();
        result.putAll(forced);




        java.util.List<UUID> pool = new ArrayList<>();
        for (UUID uuid : players) {
            if (!forced.containsKey(uuid)) pool.add(uuid);
        }
        java.util.Map<dev.hae.btt.rotation.RoleCategory, java.util.List<Role>> byCategory =
                new java.util.LinkedHashMap<>();
        for (Role r : seats) {
            byCategory.computeIfAbsent(rotationBucket(r), k -> new ArrayList<>()).add(r);
        }
        dev.hae.btt.rotation.RotationStrength strength = rotationStrength();
        dev.hae.btt.component.RotationState rotation =
                world == null ? null : dev.hae.btt.component.RotationState.KEY.get(world);
        java.util.Random rng = new java.util.Random();
        for (java.util.Map.Entry<dev.hae.btt.rotation.RoleCategory, java.util.List<Role>> entry
                : byCategory.entrySet()) {
            java.util.List<Role> roles = new ArrayList<>(entry.getValue());
            Collections.shuffle(roles, rng);
            java.util.List<UUID> picked = dev.hae.btt.rotation.RoleRotation.drawWeighted(
                    pool,
                    uuid -> dev.hae.btt.rotation.RoleRotation.weightOf(
                            dev.hae.btt.rotation.RoleRotation.imbalance(
                                    rotation == null ? java.util.List.of() : rotation.window(uuid), entry.getKey()),
                            strength),
                    roles.size(), rng::nextDouble);
            for (int k = 0; k < picked.size() && k < roles.size(); k++) {
                result.put(picked.get(k), roles.get(k));
            }
            pool.removeAll(picked);
        }

        java.util.List<Role> leftovers = new ArrayList<>();
        for (Role r : seats) {
            if (!result.containsValue(r)) leftovers.add(r);
        }
        int li = 0;
        for (UUID uuid : pool) {
            if (li >= leftovers.size()) break;
            result.put(uuid, leftovers.get(li++));
        }

        if (rotation != null) {
            recordRotation(rotation, result, players.size(), seats);
        }
        return result;
    }





    public static Map<UUID, Role> assignFromDraft(Roster roster, Map<UUID, Role> drafted) {
        if (roster == null || drafted == null) return null;
        Map<UUID, Role> result = new HashMap<>(roster.forced());
        for (Map.Entry<UUID, Role> e : drafted.entrySet()) {
            if (e.getValue() == null) continue;
            result.put(e.getKey(), e.getValue());
        }
        return result;
    }


    private static dev.hae.btt.rotation.RotationStrength rotationStrength() {
        String raw = dev.hae.btt.config.BttConfig.HANDLER.instance().rotationStrength;
        try {
            return dev.hae.btt.rotation.RotationStrength.valueOf(
                    raw == null ? "OFF" : raw.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return dev.hae.btt.rotation.RotationStrength.OFF;
        }
    }


    public static dev.hae.btt.rotation.RoleCategory rotationBucket(Role role) {
        if (Roles.isKillerCamp(role)) return dev.hae.btt.rotation.RoleCategory.KILLER;
        Roles.Kind kind = Roles.kindOf(role);
        if (kind == null) return dev.hae.btt.rotation.RoleCategory.CIVILIAN;
        if (kind == Roles.Kind.ENFORCER) return dev.hae.btt.rotation.RoleCategory.VIGILANTE;
        if (kind.isNeutralKind()) return dev.hae.btt.rotation.RoleCategory.NEUTRAL;
        return dev.hae.btt.rotation.RoleCategory.CIVILIAN;
    }


    private static void recordRotation(dev.hae.btt.component.RotationState rotation, Map<UUID, Role> result,
                                       int total, List<Role> seats) {
        int n = Math.max(1, total);
        int k = 0, v = 0, neu = 0;
        for (Role r : seats) {
            switch (rotationBucket(r)) {
                case KILLER -> k++;
                case VIGILANTE -> v++;
                case NEUTRAL -> neu++;
                default -> {
                }
            }
        }
        Map<UUID, dev.hae.btt.rotation.GameEntry> entries = new HashMap<>();
        for (Map.Entry<UUID, Role> e : result.entrySet()) {
            Role r = e.getValue();
            entries.put(e.getKey(), new dev.hae.btt.rotation.GameEntry(
                    rotationBucket(r), r == null ? "" : r.identifier().toString(),
                    n, k, v, neu));
        }
        rotation.record(entries);
    }


    private static final List<Roles.Kind> SEAT_ORDER = List.of(
            Roles.Kind.ENFORCER, Roles.Kind.CIVILIAN);


    static int[] quotasForTest(int n) {
        return new int[]{1, Math.max(0, n / 6 - 1), n / 8, n >= 12 ? 1 : 0, 1 + n / 8};
    }


    private static void takeKind(List<Role> out, Map<Roles.Kind, List<Role>> pools,
                                 Roles.Kind kind, int count) {
        takeFrom(out, pools.getOrDefault(kind, List.of()), count);
    }


    private static List<Role> filterAllowed(List<Role> pool, java.util.function.Predicate<Role> allowed) {
        if (pool == null || pool.isEmpty()) return List.of();
        List<Role> out = new ArrayList<>();
        for (Role r : pool) {
            if (allowed.test(r)) out.add(r);
        }
        return out;
    }






    private static void takeFrom(List<Role> out, List<Role> pool, int count) {
        if (count <= 0) return;
        List<Role> tier1 = new ArrayList<>();
        List<Role> tier2 = new ArrayList<>();
        for (Role r : pool) {
            if (out.contains(r)) continue;
            (Roles.isImplemented(r) ? tier1 : tier2).add(r);
        }
        int added = 0;
        for (Role r : dev.hae.btt.role.RoleWeights.pick(tier1, count)) {
            if (added >= count) return;
            out.add(r);
            added++;
        }
        for (Role r : dev.hae.btt.role.RoleWeights.pick(tier2, count - added)) {
            if (added >= count) return;
            out.add(r);
            added++;
        }
    }

    private record BaseSeats(int principal, int accomplice, int lone, int outsider, int mad) {
        int passengerCount(int players) {
            return players - (principal + accomplice + lone + outsider + mad);
        }
    }


    private static BaseSeats baseSeats(int players) {
        return new BaseSeats(1, Math.max(0, players / 6 - 1), players / 8,
                players >= 12 ? 1 : 0, 1 + players / 8);
    }


    private static int[] passengerQuotas(int passenger, int players) {
        int[] quota = new int[SEAT_ORDER.size()];
        quota[0] = players / 3;
        quota[1] = Math.max(0, passenger - quota[0]);
        return quota;
    }





    public static java.util.LinkedHashMap<String, Integer> categoryCounts(int players) {
        java.util.LinkedHashMap<String, Integer> out = new java.util.LinkedHashMap<>();
        if (players < MIN_PLAYERS || players > MAX_PLAYERS) return out;
        BaseSeats seats = baseSeats(players);
        int passenger = seats.passengerCount(players);
        if (passenger < 0) return out;
        int[] quota = passengerQuotas(passenger, players);
        for (int k = 0; k < SEAT_ORDER.size(); k++) {
            out.put(passengerSeatKey(SEAT_ORDER.get(k)), quota[k]);
        }
        out.put("btt.guide.killers.主犯凶手.title", seats.principal());
        out.put("btt.guide.killers.从犯凶手.title", seats.accomplice());
        out.put("btt.guide.neutrals.独行中立.title", seats.lone());
        out.put("btt.guide.neutrals.外人中立.title", seats.outsider());
        out.put("btt.guide.neutrals.狂人中立.title", seats.mad());
        return out;
    }








    private static String passengerSeatKey(Roles.Kind seat) {
        return switch (seat) {
            case CIVILIAN -> "btt.guide.passengers.平民乘客.title";
            case ENFORCER -> "btt.guide.passengers.执法乘客.title";


            default -> throw new IllegalArgumentException("Not a passenger seat: " + seat);
        };
    }

    public static String categoryKeyOf(Role role) {
        Roles.Kind kind = Roles.kindOf(role);
        if (kind == null) return null;
        return switch (kind) {
            case CIVILIAN -> "btt.guide.passengers.平民乘客.title";
            case ENFORCER -> "btt.guide.passengers.执法乘客.title";



            case PRINCIPAL -> "btt.guide.killers.主犯凶手.title";
            case ACCOMPLICE -> "btt.guide.killers.从犯凶手.title";
            case LONE -> "btt.guide.neutrals.独行中立.title";
            case OUTSIDER -> "btt.guide.neutrals.外人中立.title";
            case MAD -> "btt.guide.neutrals.狂人中立.title";
        };
    }

    public static MutableText displayName(Role role) {
        return Harpymodloader.getRoleName(role);
    }


    public static String nameOf(ServerPlayerEntity player) {
        return player == null ? "?" : player.getGameProfile().getName();
    }


    public static boolean isRole(PlayerEntity player, Role role) {
        return GameWorldComponent.KEY.get(player.getWorld()).isRole(player, role);
    }
}
