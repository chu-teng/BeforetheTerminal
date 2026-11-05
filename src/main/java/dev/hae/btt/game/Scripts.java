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
        new Script("locked_room_murders", List.of(
                Roles.VIGILANTE, Roles.RAILWAY_POLICE, Roles.WITCH, Roles.VETERAN, Roles.ENGINEER, Roles.ARCHITECT, Roles.POSTMAN, Roles.COLLECTOR, Roles.DOCTOR, Roles.NOBLE, Roles.PHARMACIST, Roles.BARTENDER, Roles.JOURNALIST, Roles.SCRIBE, Roles.MAID, Roles.DETECTIVE, Roles.ASSASSIN, Roles.DUCE, Roles.HARPY, Roles.HARLOT, Roles.STOWAWAY, Roles.ACTOR, Roles.TOYMAKER, Roles.ALCHEMIST, Roles.TERRORIST, Roles.SAINT, Roles.HERMIT, Roles.DELINQUENT, Roles.GAMBLER, Roles.ARSONIST, Roles.THIEF, Roles.ASTRAL, Roles.MESSIAH, Roles.GARDENER)),
        new Script("sects_and_sinners", List.of(
                Roles.HUNTER, Roles.JUDGE, Roles.CHESS_PLAYER, Roles.TLAMACAZQUI, Roles.UNDERCOVER, Roles.PUPPETEER, Roles.CRAFTSMAN, Roles.SMUGGLER, Roles.MORTICIAN, Roles.FOLKLORIST, Roles.MEDIUM, Roles.MONK, Roles.VIRGIN, Roles.STAR, Roles.PROFESSOR, Roles.BODYGUARD, Roles.MAGICIAN, Roles.RIGGER, Roles.IMP, Roles.LEECH, Roles.MASTERMIND, Roles.CLEANER, Roles.COLONEL, Roles.LAWYER, Roles.WARDEN, Roles.DAMSEL, Roles.POLITICIAN, Roles.MARIONETTE, Roles.HERETIC, Roles.JESTER, Roles.EXECUTIONER, Roles.KIDNAPPER, Roles.MAJO, Roles.GODFATHER)),
        new Script("the_dreamers_diary", List.of(
                Roles.CABALLERO, Roles.XIA, Roles.NIGHT_WATCHMAN, Roles.AVIATEUR, Roles.MERCENARY, Roles.SNAKE_CHARMER, Roles.CANNIBAL, Roles.PHILOSOPHER, Roles.INVESTIGATOR, Roles.PROPHET, Roles.MUYUUBYOU, Roles.HIKIKOMORI, Roles.CONDUCTOR, Roles.ATTENDANT, Roles.DEVUSHKA, Roles.POPPY_GROWER, Roles.SCULPTOR, Roles.BARBER, Roles.REVOLUCIONARIO, Roles.WEREWOLF, Roles.LIBRARIAN, Roles.COMEDIAN, Roles.KHESHIG, Roles.PSYCHOPATH, Roles.GURU, Roles.TRAITOR, Roles.LUNATIC, Roles.AMNESIAC, Roles.BLACKDEATH, Roles.NOVELIST, Roles.ANGEL, Roles.MESSENGER, Roles.VOODOO, Roles.LEGION)));
    public static Script byId(String id) { return ALL.stream().filter(s -> s.id().equals(id)).findFirst().orElse(null); }
    public static Script draw(Random random) { return ALL.get(random.nextInt(ALL.size())); }
    public static Script draw(net.minecraft.util.math.random.Random random) { return ALL.get(random.nextInt(ALL.size())); }

    public static int number(Role role) {
        int scriptIndex = 0;
        for (Script script : ALL) {
            int position = script.roles().indexOf(role);
            if (position >= 0) {
                return position * 3 + scriptIndex;
            }
            scriptIndex++;
        }
        return Integer.MAX_VALUE;
    }
}
