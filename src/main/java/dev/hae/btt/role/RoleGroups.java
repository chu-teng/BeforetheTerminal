package dev.hae.btt.role;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.component.PlayerState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


public final class RoleGroups {
    private RoleGroups() {}

    public static void assignAll(ServerWorld world, GameWorldComponent game, List<ServerPlayerEntity> players) {
        var participants = players.stream().filter(GameFunctions::isPlayerAliveAndSurvival).toList();
        for (var noble : participants) {
            if (!game.isRole(noble, Roles.NOBLE)) continue;
            var pool = new ArrayList<>(participants);
            pool.remove(noble);
            Collections.shuffle(pool, new java.util.Random(noble.getRandom().nextLong()));
            var branches = new ArrayList<ServerPlayerEntity>();
            if (participants.size() >= 12) {
                for (var kind : List.of(Roles.Kind.CIVILIAN, Roles.Kind.ACCOMPLICE, Roles.Kind.LONE, Roles.Kind.OUTSIDER)) {
                    pool.stream().filter(p -> Roles.kindOfReal(game, p) == kind).findFirst().ifPresent(p -> {
                        branches.add(p);
                        pool.remove(p);
                    });
                }
            }
            var main = new ArrayList<ServerPlayerEntity>();
            main.addAll(pool.stream().filter(p -> Roles.isPassengerCampFor(game, p)).limit(2).toList());
            main.addAll(pool.stream().filter(p -> !Roles.isPassengerCampFor(game, p)).limit(1).toList());
            pool.removeAll(main);
            Collections.shuffle(pool, new java.util.Random(noble.getRandom().nextLong()));
            var state = PlayerState.KEY.get(noble);
            state.kins = csv(main);
            state.branchKins = csv(branches);
            var fakePool = new ArrayList<>(participants);
            fakePool.remove(noble);
            fakePool.removeAll(branches);
            Collections.shuffle(fakePool, new java.util.Random(noble.getRandom().nextLong()));
            var fakeBranches = branches.isEmpty() ? List.<ServerPlayerEntity>of() : fakePool.stream().limit(4).toList();
            fakePool = new ArrayList<>(participants);
            fakePool.remove(noble);
            fakePool.removeAll(main);
            fakePool.removeAll(fakeBranches);
            Collections.shuffle(fakePool, new java.util.Random(noble.getRandom().nextLong()));
            state.drunkKins = csv(fakePool.stream().limit(3).toList());
            state.drunkBranchKins = csv(fakeBranches);
            state.sync();
        }
    }

    private static String csv(List<ServerPlayerEntity> group) {
        return String.join(",", group.stream().map(ServerPlayerEntity::getUuidAsString).toList());
    }

    public static boolean inGroup(String csv, UUID uuid) {
        return csv != null && !csv.isEmpty() && List.of(csv.split(",")).contains(uuid.toString());
    }
}
