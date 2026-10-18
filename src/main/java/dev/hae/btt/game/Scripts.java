package dev.hae.btt.game;

import dev.doctor4t.wathe.api.Role;
import dev.hae.btt.role.Roles;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;









public final class Scripts {
    private Scripts() {}


    public record Script(String id, List<Role> killers, List<Role> enforcers) {
        public boolean allowsKiller(Role role) {
            return role != null && killers.contains(role);
        }

        public boolean allowsEnforcer(Role role) {
            return role != null && enforcers.contains(role);
        }
    }


    public static final List<Script> ALL = List.of(

            new Script("undercurrent",
                    pairs(Roles.IMP, Roles.ACTOR), pairs(Roles.VIGILANTE, Roles.WITCH)),


            new Script("moonrise",
                    pairs(Roles.ASSASSIN, Roles.MAGICIAN), pairs(Roles.HUNTER, Roles.GUNSLINGER)),

            new Script("spring_fall",
                    pairs(Roles.SCULPTOR, Roles.VORTEX), pairs(Roles.CABALLERO, Roles.ELDER)));


    private static List<Role> pairs(Role... representatives) {
        List<Role> out = new ArrayList<>();
        for (Role r : representatives) {
            out.add(r);
            Role partner = Roles.sameColorPartner(r);
            if (partner != null && partner != r) out.add(partner);
        }
        return List.copyOf(out);
    }


    public static Script byId(String id) {
        if (id == null || id.isEmpty()) return null;
        for (Script s : ALL) {
            if (s.id().equals(id)) return s;
        }
        return null;
    }


    public static Script draw(Random random) {
        return ALL.get(random.nextInt(ALL.size()));
    }


    public static Script draw(net.minecraft.util.math.random.Random random) {
        return ALL.get(random.nextInt(ALL.size()));
    }
}
