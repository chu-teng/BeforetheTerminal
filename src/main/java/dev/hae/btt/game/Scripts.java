package dev.hae.btt.game;

import dev.doctor4t.wathe.api.Role;
import dev.hae.btt.role.Roles;
import java.util.List;
import java.util.Random;


public final class Scripts {
    private Scripts() {}
    public record Script(String id, List<Role> roles) {
        public boolean allows(Role role) { return roles.contains(role); }
        public boolean allowsKiller(Role role) { return allows(role); }
        public boolean allowsEnforcer(Role role) { return allows(role); }
        public List<Role> killers() { return roles.stream().filter(r -> Roles.kindOf(r) == Roles.Kind.PRINCIPAL).toList(); }
        public List<Role> enforcers() { return roles.stream().filter(r -> Roles.kindOf(r) == Roles.Kind.ENFORCER).toList(); }
    }
    public static final List<Script> ALL = List.of(
        new Script("undercurrent", List.of(
                Roles.VIGILANTE, Roles.RAILWAY_POLICE, Roles.WITCH, Roles.VETERAN, Roles.ARCHITECT, Roles.LOCKSMITH, Roles.POSTMAN, Roles.COLLECTOR,
                Roles.DOCTOR, Roles.NOBLE, Roles.PHARMACIST, Roles.BARTENDER, Roles.JOURNALIST, Roles.ENGINEER, Roles.EMPATH, Roles.DETECTIVE,
                Roles.ASSASSIN, Roles.AGENT, Roles.ACTOR, Roles.STOWAWAY,
                Roles.ALCHEMIST, Roles.CLEANER, Roles.TERRORIST, Roles.WARDEN, Roles.HARPY,
                Roles.TRAITOR, Roles.GAMBLER, Roles.DAMSEL, Roles.SAINT,
                Roles.ARSONIST, Roles.THIEF, Roles.ASTRAL,
                Roles.MESSIAH, Roles.GARDENER)),
        new Script("moonrise", List.of(
                Roles.HUNTER, Roles.JUDGE, Roles.GUNSLINGER, Roles.NIGHT_WATCHMAN, Roles.UNDERCOVER, Roles.PUPPETEER, Roles.CRAFTSMAN, Roles.SMUGGLER,
                Roles.MORTICIAN, Roles.FOLKLORIST, Roles.MEDIUM, Roles.MONK, Roles.VIRGIN, Roles.STAR, Roles.PROFESSOR, Roles.BODYGUARD,
                Roles.MAGICIAN, Roles.RIGGER, Roles.IMP, Roles.LEECH,
                Roles.COLONEL, Roles.LAWYER, Roles.SUCCUBUS, Roles.GODFATHER, Roles.MASTERMIND,
                Roles.MARIONETTE, Roles.HERETIC, Roles.GOON, Roles.HERMIT,
                Roles.JESTER, Roles.EXECUTIONER, Roles.KIDNAPPER,
                Roles.MAJO, Roles.VOODOO)),
        new Script("spring_fall", List.of(
                Roles.CABALLERO, Roles.RANGER, Roles.PRIEST, Roles.AVIATEUR, Roles.EX_UNDERCOVER, Roles.SNAKE_CHARMER, Roles.CANNIBAL, Roles.PHILOSOPHER,
                Roles.INVESTIGATOR, Roles.PROPHET, Roles.MUYUUBYOU, Roles.MAID, Roles.CONDUCTOR, Roles.ATTENDANT, Roles.SHOUJO, Roles.POPPY_GROWER,
                Roles.SCULPTOR, Roles.VORTEX, Roles.RIOT, Roles.LEVIATHAN,
                Roles.COMEDIAN, Roles.PSYCHOPATH, Roles.SWORDSMAN, Roles.HIKIKOMORI, Roles.LIBRARIAN,
                Roles.BLACKDEATH, Roles.LUNATIC, Roles.EX_TRAITOR, Roles.AMNESIAC,
                Roles.NOVELIST, Roles.ANGEL, Roles.COLD_WAVE,
                Roles.WEREWOLF, Roles.LEGION)));
    public static Script byId(String id) { return ALL.stream().filter(s -> s.id().equals(id)).findFirst().orElse(null); }
    public static Script draw(Random random) { return ALL.get(random.nextInt(ALL.size())); }
    public static Script draw(net.minecraft.util.math.random.Random random) { return ALL.get(random.nextInt(ALL.size())); }

    public static int number(Role role) {
        int scriptIndex = 0;
        for (Script script : ALL) {
            int position = script.roles().indexOf(role);
            if (position >= 0) return position * 3 + scriptIndex;
            scriptIndex++;
        }
        return Integer.MAX_VALUE;
    }
}
