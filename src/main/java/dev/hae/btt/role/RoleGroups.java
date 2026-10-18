package dev.hae.btt.role;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import dev.hae.btt.component.PlayerState;
















public final class RoleGroups {
    private RoleGroups() {}


    private static final int GROUP_SIZE = 4;


    public static void assignAll(ServerWorld world, GameWorldComponent gwc) {
        List<ServerPlayerEntity> all = new ArrayList<>(world.getPlayers());
        for (ServerPlayerEntity player : all) {
            Role role = gwc.getRole(player);
            if (role == Roles.NOBLE) {
                setGroup(player, "kins", pickKins(player, all, gwc));
            } else if (role == Roles.BALLOONIST) {
                setGroup(player, "reps", pickReps(player, all, gwc));
            }
        }
    }


    private static List<ServerPlayerEntity> pickKins(ServerPlayerEntity noble, List<ServerPlayerEntity> all,
                                                     GameWorldComponent gwc) {
        List<ServerPlayerEntity> others = others(noble, all);
        List<ServerPlayerEntity> killers = new ArrayList<>();
        for (ServerPlayerEntity p : others) {
            Roles.Kind kind = Roles.kindOfReal(gwc, p);
            if (kind != null && kind.isKillerSeat()) killers.add(p);
        }
        List<ServerPlayerEntity> picked = new ArrayList<>();
        if (!killers.isEmpty()) picked.add(killers.get(noble.getRandom().nextInt(killers.size())));
        Collections.shuffle(others, new java.util.Random(noble.getRandom().nextLong()));
        for (ServerPlayerEntity p : others) {
            if (picked.size() >= GROUP_SIZE) break;
            if (!picked.contains(p)) picked.add(p);
        }
        return picked;
    }


    private static List<ServerPlayerEntity> pickReps(ServerPlayerEntity owner, List<ServerPlayerEntity> all,
                                                     GameWorldComponent gwc) {
        List<ServerPlayerEntity> others = others(owner, all);
        Collections.shuffle(others, new java.util.Random(owner.getRandom().nextLong()));
        List<ServerPlayerEntity> picked = new ArrayList<>();
        picked.add(firstOf(others, picked, gwc, Roles.Kind.ENFORCER, Roles.Kind.ACTION,
                Roles.Kind.INFORMATION, Roles.Kind.SUPPORT));
        picked.add(firstOf(others, picked, gwc, Roles.Kind.LONE));
        picked.add(firstOf(others, picked, gwc, Roles.Kind.PRINCIPAL, Roles.Kind.ACCOMPLICE));
        picked.add(firstOf(others, picked, gwc, Roles.Kind.OUTSIDER));
        for (ServerPlayerEntity p : others) {
            if (picked.size() >= GROUP_SIZE) break;
            if (!picked.contains(p)) picked.add(p);
        }
        picked.removeIf(java.util.Objects::isNull);
        return picked;
    }

    private static ServerPlayerEntity firstOf(List<ServerPlayerEntity> pool, List<ServerPlayerEntity> picked,
                                              GameWorldComponent gwc, Roles.Kind... kinds) {
        for (ServerPlayerEntity p : pool) {
            if (picked.contains(p)) continue;
            Role r = gwc.getRole(p);
            if (r == null) continue;
            for (Roles.Kind k : kinds) {
                if (Roles.kindOf(r) == k) return p;
            }
        }
        return null;
    }

    private static List<ServerPlayerEntity> others(ServerPlayerEntity self, List<ServerPlayerEntity> all) {
        List<ServerPlayerEntity> out = new ArrayList<>();
        for (ServerPlayerEntity p : all) {
            if (p != self) out.add(p);
        }
        return out;
    }

    private static void setGroup(ServerPlayerEntity owner, String kind, List<ServerPlayerEntity> group) {
        StringBuilder sb = new StringBuilder();
        for (ServerPlayerEntity p : group) {
            if (sb.length() > 0) sb.append(',');
            sb.append(p.getUuid());
        }
        PlayerState pc = PlayerState.KEY.get(owner);
        if ("kins".equals(kind)) pc.kins = sb.toString();
        else pc.balloonReps = sb.toString();
        pc.sync();
    }


    public static boolean inGroup(String csv, java.util.UUID uuid) {
        if (csv == null || csv.isEmpty()) return false;
        String id = uuid.toString();
        for (String s : csv.split(",")) {
            if (s.equals(id)) return true;
        }
        return false;
    }
}
