package dev.hae.btt.role;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import net.minecraft.util.Identifier;
import dev.hae.btt.BeforeTheTerminal;
import java.awt.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.game.GameEvents;
import dev.hae.btt.game.SeatAssignment;


















public final class Roles {
    private Roles() {}









    public enum Faction {

        PASSENGER(true, false, Role.MoodType.REAL, 200, false),

        KILLER(false, true, Role.MoodType.FAKE, -1, true),

        SOLO(false, false, Role.MoodType.FAKE, 200, false),

        OUTSIDER(false, false, Role.MoodType.FAKE, -1, true);

        public final boolean innocent;
        public final boolean canUseKiller;
        public final Role.MoodType mood;
        public final int maxSprintTime;
        public final boolean canSeeTime;

        Faction(boolean innocent, boolean canUseKiller, Role.MoodType mood, int maxSprintTime, boolean canSeeTime) {
            this.innocent = innocent;
            this.canUseKiller = canUseKiller;
            this.mood = mood;
            this.maxSprintTime = maxSprintTime;
            this.canSeeTime = canSeeTime;
        }
    }












    private static final Map<Role, Faction> FACTIONS = new java.util.LinkedHashMap<>();
    private static final List<Identifier> NEW_ROLE_IDS = new ArrayList<>();







    public enum Kind {

        ENFORCER,

        ACTION,

        INFORMATION,

        SUPPORT,

        PRINCIPAL,

        ACCOMPLICE,

        LONE,

        OUTSIDER,

        MAD;


        public boolean isKillerSeat() {
            return this == PRINCIPAL || this == ACCOMPLICE;
        }


        public boolean isNeutralKind() {
            return this == LONE || this == OUTSIDER || this == MAD;
        }


        public boolean isInfoOrSupport() {
            return this == INFORMATION || this == SUPPORT;
        }
    }

    private static final Map<Role, Kind> KINDS = new HashMap<>();


    public static Kind kindOf(Role role) {
        return role == null ? null : KINDS.get(role);
    }


    public static Map<Kind, List<Role>> kindPools() {
        Map<Kind, List<Role>> pools = new java.util.EnumMap<>(Kind.class);
        for (var e : KINDS.entrySet()) {
            if (EXCLUDED.contains(e.getKey())) continue;
            pools.computeIfAbsent(e.getValue(), k -> new ArrayList<>()).add(e.getKey());
        }
        return pools;
    }


    private static Role register(String path, int color, Faction f, Kind kind) {
        Role r = WatheRoles.registerRole(new Role(Identifier.of(BeforeTheTerminal.MOD_ID, path), color,
                f.innocent, f.canUseKiller, f.mood, f.maxSprintTime, f.canSeeTime));
        FACTIONS.put(r, f);
        KINDS.put(r, kind);
        NEW_ROLE_IDS.add(r.identifier());
        return r;
    }


    private static Role register(String path, int color, Faction f, Kind kind,
                                 boolean innocent, boolean canUseKiller,
                                 Role.MoodType mood, int maxSprintTime, boolean canSeeTime) {




        if (maxSprintTime == Integer.MAX_VALUE) maxSprintTime = -1;
        Role r = WatheRoles.registerRole(new Role(Identifier.of(BeforeTheTerminal.MOD_ID, path), color,
                innocent, canUseKiller, mood, maxSprintTime, canSeeTime));
        FACTIONS.put(r, f);
        KINDS.put(r, kind);
        NEW_ROLE_IDS.add(r.identifier());
        return r;
    }



    public static final Role VIGILANTE = register("vigilante", 0x0000FF, Faction.PASSENGER, Kind.ENFORCER);
    public static final Role RAILWAY_POLICE = register("railway_police", 0x0000FF, Faction.PASSENGER, Kind.ENFORCER,
            true, false, Role.MoodType.REAL, -1, false);
    public static final Role HUNTER = register("hunter", 0xCCCCFF, Faction.PASSENGER, Kind.ENFORCER);
    public static final Role JUDGE = register("judge", 0xCCCCFF, Faction.PASSENGER, Kind.ENFORCER);
    public static final Role CABALLERO = register("caballero", 0x8000FF, Faction.PASSENGER, Kind.ENFORCER);
    public static final Role RANGER = register("ranger", 0x8000FF, Faction.PASSENGER, Kind.ENFORCER);
    public static final Role WITCH = register("witch", new Color(128,0,128).getRGB(), Faction.PASSENGER, Kind.ENFORCER,
            true,false, Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(), false);
    public static final Role VETERAN = register("veteran", 0x800080, Faction.PASSENGER, Kind.ENFORCER);

    public static final Role GUNSLINGER = register("gunslinger", 0xAFEEEE, Faction.PASSENGER, Kind.ENFORCER);
    public static final Role NIGHT_WATCHMAN = register("night_watchman", 0xAFEEEE, Faction.PASSENGER, Kind.ENFORCER);
    public static final Role ELDER = register("elder", 0x008080, Faction.PASSENGER, Kind.ENFORCER);
    public static final Role SAINT = register("saint", 0x008080, Faction.PASSENGER, Kind.ENFORCER);
    public static final Role DETECTIVE = register("detective", 0xFFFF00, Faction.PASSENGER, Kind.INFORMATION);
    public static final Role PROPHET = register("prophet", 0xFFFF00, Faction.PASSENGER, Kind.INFORMATION);
    public static final Role DOCTOR = register("doctor", new Color(255,102,0).getRGB(), Faction.PASSENGER, Kind.INFORMATION,
            true,false, Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(), false);
    public static final Role INVESTIGATOR = register("investigator", 0xFFEFD5, Faction.PASSENGER, Kind.INFORMATION);
    public static final Role JOURNALIST = register("journalist", 0xFFE5B4, Faction.PASSENGER, Kind.INFORMATION);
    public static final Role ENGINEER = register("engineer", 0xFFE5B4, Faction.PASSENGER, Kind.INFORMATION);
    public static final Role FOLKLORIST = register("folklorist", 0xFFD700, Faction.PASSENGER, Kind.INFORMATION);
    public static final Role MUYUUBYOU = register("muyuubyou", 0xDB7093, Faction.PASSENGER, Kind.INFORMATION);
    public static final Role NOBLE = register("noble", 0xFFA500, Faction.PASSENGER, Kind.INFORMATION);
    public static final Role BALLOONIST = register("balloonist", 0xFFA500, Faction.PASSENGER, Kind.INFORMATION);
    public static final Role VIRGIN = register("virgin", 0x98FB98, Faction.PASSENGER, Kind.SUPPORT,
            true,false, Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(), false);
    public static final Role STAR = register("star", 0x98FB98, Faction.PASSENGER, Kind.SUPPORT);
    public static final Role PROFESSOR = register("professor", 0x66CDAA, Faction.PASSENGER, Kind.SUPPORT);
    public static final Role BODYGUARD = register("bodyguard", 0x66CDAA, Faction.PASSENGER, Kind.SUPPORT);
    public static final Role SNAKE_CHARMER = register("snake_charmer", 0x006400, Faction.PASSENGER, Kind.SUPPORT);


    public static final Role MONK = register("monk", 0x006400, Faction.PASSENGER, Kind.SUPPORT);
    public static final Role CANNIBAL = register("cannibal", 0xBC8F8F, Faction.PASSENGER, Kind.ACTION);
    public static final Role PHILOSOPHER = register("philosopher", 0xBC8F8F, Faction.PASSENGER, Kind.ACTION);
    public static final Role UNDERCOVER = register("undercover", 0x6A5ACD, Faction.PASSENGER, Kind.ACTION,
            true,false, Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(), false);
    public static final Role EX_UNDERCOVER = register("ex_undercover", 0x6A5ACD, Faction.PASSENGER, Kind.ACTION);
    public static final Role CONDUCTOR = register("conductor", new Color(102,255,0).getRGB(), Faction.PASSENGER, Kind.SUPPORT,
            true,false, Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(), false);
    public static final Role ATTENDANT = register("attendant", 0x66FF00, Faction.PASSENGER, Kind.SUPPORT,
            true, false, Role.MoodType.REAL, 200, true);
    public static final Role ARCHITECT = register("architect", 0xFFC0CB, Faction.PASSENGER, Kind.ACTION);
    public static final Role LOCKSMITH = register("locksmith", 0xFFC0CB, Faction.PASSENGER, Kind.ACTION);
    public static final Role PHARMACIST = register("pharmacist", 0x6B8E23, Faction.PASSENGER, Kind.SUPPORT,
            true,false, Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(), false);
    public static final Role BARTENDER = register("bartender", PHARMACIST.color(), Faction.PASSENGER, Kind.SUPPORT);
    public static final Role SHOUJO = register("shoujo", 0x40E0D0, Faction.PASSENGER, Kind.SUPPORT);
    public static final Role POPPY_GROWER = register("poppy_grower", 0x40E0D0, Faction.PASSENGER, Kind.SUPPORT);
    public static final Role MAID = register("maid", 0xDB7093, Faction.PASSENGER, Kind.INFORMATION);
    public static final Role POSTMAN = register("postman", 0xD8BFD8, Faction.PASSENGER, Kind.ACTION);
    public static final Role ASSASSIN = register("assassin", 0xFF0000, Faction.KILLER, Kind.PRINCIPAL,
            false,true, Role.MoodType.FAKE, -1, true);

    public static final Role AGENT = register("agent", 0xFF0000, Faction.KILLER, Kind.PRINCIPAL);
    public static final Role ACTOR = register("actor", 0x8B0000, Faction.KILLER, Kind.PRINCIPAL,
            false,true, Role.MoodType.FAKE, -1, true);
    public static final Role STOWAWAY = register("stowaway", 0x8B0000, Faction.KILLER, Kind.PRINCIPAL,
            false,true, Role.MoodType.FAKE, -1, true);
    public static final Role IMP = register("imp", 0xDC143C, Faction.KILLER, Kind.PRINCIPAL);
    public static final Role LEECH = register("leech", 0xDC143C, Faction.KILLER, Kind.PRINCIPAL);
    public static final Role RIOT = register("riot", 0xFF8C00, Faction.KILLER, Kind.PRINCIPAL);
    public static final Role VORTEX = register("vortex", 0x8B4513, Faction.KILLER, Kind.PRINCIPAL);
    public static final Role SCULPTOR = register("sculptor", 0x8B4513, Faction.KILLER, Kind.PRINCIPAL);
    public static final Role LEGION = register("legion", 0x663399, Faction.OUTSIDER, Kind.OUTSIDER);
    public static final Role SWORDSMAN = register("swordsman", 0xC41E3A, Faction.KILLER, Kind.ACCOMPLICE);
    public static final Role HIKIKOMORI = register("hikikomori", 0xC41E3A, Faction.KILLER, Kind.ACCOMPLICE);
    public static final Role ALCHEMIST = register("alchemist", 0x808000, Faction.KILLER, Kind.ACCOMPLICE);
    public static final Role TERRORIST = register("terrorist", 0xD2B48C, Faction.KILLER, Kind.ACCOMPLICE);
    public static final Role CLEANER = register("cleaner", 0x808000, Faction.KILLER, Kind.ACCOMPLICE);
    public static final Role COLONEL = register("colonel", 0x556B2F, Faction.KILLER, Kind.ACCOMPLICE);
    public static final Role COMEDIAN = register("comedian", 0xFF1493, Faction.KILLER, Kind.ACCOMPLICE);
    public static final Role PSYCHOPATH = register("psychopath", 0xFF1493, Faction.KILLER, Kind.ACCOMPLICE);

    public static final Role MAGICIAN = register("magician", new Color(139,0,139).getRGB(), Faction.KILLER, Kind.PRINCIPAL,
            false,true, Role.MoodType.FAKE, -1, true);
    public static final Role RIGGER = register("rigger", 0x8B008B, Faction.KILLER, Kind.PRINCIPAL);
    public static final Role SUCCUBUS = register("succubus", 0xCD5C5C, Faction.KILLER, Kind.ACCOMPLICE);
    public static final Role LAWYER = register("lawyer", 0x556B2F, Faction.KILLER, Kind.ACCOMPLICE);
    public static final Role NOVELIST = register("novelist", 0x00FFFF, Faction.SOLO, Kind.LONE);
    public static final Role EXECUTIONER = register("executioner", 0xFF69B4, Faction.SOLO, Kind.LONE,
            false,false, Role.MoodType.FAKE, WatheRoles.CIVILIAN.getMaxSprintTime(), true);
    public static final Role JESTER = register("jester", 0xFF69B4, Faction.SOLO, Kind.LONE,

            false,false, Role.MoodType.FAKE, WatheRoles.CIVILIAN.getMaxSprintTime(), true);
    public static final Role PUPPETEER = register("puppeteer", 0xDDA0DD, Faction.PASSENGER, Kind.ACTION);
    public static final Role ARSONIST = register("arsonist", 0xFF4500, Faction.SOLO, Kind.LONE);
    public static final Role THIEF = register("thief", new Color(255,69,0).getRGB(), Faction.SOLO, Kind.LONE,
            false,false, Role.MoodType.FAKE, WatheRoles.CIVILIAN.getMaxSprintTime(), true);
    public static final Role MAJO = register("majo", 0xFF00FF, Faction.OUTSIDER, Kind.OUTSIDER);
    public static final Role MESSIAH = register("messiah", 0xFF00FF, Faction.OUTSIDER, Kind.OUTSIDER);
    public static final Role KIDNAPPER = register("kidnapper", 0xD2B48C, Faction.OUTSIDER, Kind.OUTSIDER);
    public static final Role ASTRAL = register("astral", 0xD2B48C, Faction.OUTSIDER, Kind.OUTSIDER);
    public static final Role GARDENER = register("gardener", 0x7FFFD4, Faction.OUTSIDER, Kind.OUTSIDER);


    public static final Role COLD_WAVE = register("cold_wave", 0x7FFFD4, Faction.OUTSIDER, Kind.OUTSIDER);
    public static final Role BLACKDEATH = register("black_death", 0x800000, Faction.KILLER, Kind.MAD,
            false, false, Role.MoodType.FAKE, 200, false);
    public static final Role HERETIC = register("heretic", 0x800000, Faction.PASSENGER, Kind.MAD);
    public static final Role AMNESIAC = register("amnesiac", 0xADD8E6, Faction.PASSENGER, Kind.MAD);
    public static final Role HERMIT = register("hermit", 0x9ACD32, Faction.PASSENGER, Kind.MAD);
    public static final Role MARIONETTE = register("marionette", 0xDB7093, Faction.KILLER, Kind.MAD,
            true, false, Role.MoodType.REAL, 200, false);
    public static final Role LUNATIC = register("lunatic", 0xDB7093, Faction.PASSENGER, Kind.MAD);
    public static final Role TRAITOR = register("traitor", 0x2F4F4F, Faction.KILLER, Kind.MAD,
            false, true, Role.MoodType.FAKE, -1, true);
    public static final Role EX_TRAITOR = register("ex_traitor", 0x2F4F4F, Faction.KILLER, Kind.MAD,
            false, true, Role.MoodType.FAKE, -1, true);




    public static final Role GODFATHER = register("godfather", 0xCD5C5C, Faction.KILLER, Kind.ACCOMPLICE);



    public static final Role COLLECTOR = register("collector", 0xDDA0DD, Faction.PASSENGER, Kind.ACTION);

    public static final Role MORTICIAN = register("mortician", 0xFF6600, Faction.PASSENGER, Kind.INFORMATION);




    public static final Role TAILOR = register("tailor", 0xFFEFD5, Faction.PASSENGER, Kind.INFORMATION);





    public static final Role MEDIUM = register("medium", 0xFFD700, Faction.PASSENGER, Kind.INFORMATION);

    public static final Role GAMBLER = register("gambler", 0xADD8E6, Faction.PASSENGER, Kind.MAD);
    public static final Role GOON = register("goon", 0x9ACD32, Faction.PASSENGER, Kind.MAD);

    public static final Role LEVIATHAN = register("leviathan", 0xFF8C00, Faction.KILLER, Kind.PRINCIPAL);
    public static final Role CRAFTSMAN = register("craftsman", 0xD8BFD8, Faction.PASSENGER, Kind.ACTION);

    public static final Role WARDEN = register("warden", 0xD2B48C, Faction.KILLER, Kind.ACCOMPLICE);

    public static final Role VOODOO = register("voodoo", 0x663399, Faction.OUTSIDER, Kind.OUTSIDER);
    public static final Role ANGEL = register("angel", NOVELIST.color(), Faction.SOLO, Kind.LONE);




    private static final java.util.Set<Role> IMPLEMENTED = java.util.Set.of(
            AGENT, GODFATHER, SCULPTOR, VIGILANTE, DOCTOR, INVESTIGATOR, VIRGIN, JESTER, CONDUCTOR,
            ACTOR, UNDERCOVER, WITCH, RAILWAY_POLICE, VETERAN, HUNTER,

            GUNSLINGER, NIGHT_WATCHMAN, ELDER, SAINT,
            COLONEL, PSYCHOPATH, CLEANER, STAR, DETECTIVE,
            PHARMACIST, RIGGER, AMNESIAC, THIEF, MAID, SWORDSMAN, STOWAWAY,
            PROPHET, ASSASSIN, MAGICIAN, NOVELIST, SNAKE_CHARMER, MAJO, MESSIAH,
            BARTENDER, SUCCUBUS, POPPY_GROWER, RIOT, VORTEX, LEVIATHAN,
            JUDGE, JOURNALIST, ENGINEER, CABALLERO, RANGER, TRAITOR, EX_TRAITOR,
            IMP, TERRORIST, WARDEN, COMEDIAN, GARDENER, EX_UNDERCOVER, BLACKDEATH,
            SHOUJO, HERETIC, ARCHITECT, ALCHEMIST, MUYUUBYOU, ARSONIST, PROFESSOR, KIDNAPPER,
            CANNIBAL, BODYGUARD, LEECH, LOCKSMITH, NOBLE, BALLOONIST, MONK,

            COLLECTOR, MORTICIAN, CRAFTSMAN,

            TAILOR,

            MEDIUM,
            MARIONETTE, LUNATIC, HERMIT, FOLKLORIST, POSTMAN, GAMBLER, GOON,

            EXECUTIONER, ANGEL,

            LAWYER,

            PHILOSOPHER,

            HIKIKOMORI,

            LEGION, VOODOO,

            PUPPETEER, ASTRAL, COLD_WAVE,


            ATTENDANT);

    public static final java.util.Set<Role> SOLO_NEUTRALS = java.util.Set.of(NOVELIST, ANGEL, EXECUTIONER, JESTER, THIEF, ARSONIST);


    private static final java.util.Set<Role> EXCLUDED = java.util.Set.of();






    private static final Map<Role, Role> SAME_COLOR_PARTNER = new HashMap<>();
    static {

        Role[][] pairs = {


                {VIGILANTE, RAILWAY_POLICE}, {HUNTER, JUDGE}, {GUNSLINGER, NIGHT_WATCHMAN},
                {ELDER, SAINT}, {CABALLERO, RANGER}, {WITCH, VETERAN},



                {DETECTIVE, PROPHET}, {DOCTOR, MORTICIAN}, {JOURNALIST, ENGINEER}, {MAID, MUYUUBYOU},
                {NOBLE, BALLOONIST}, {TAILOR, INVESTIGATOR}, {MEDIUM, FOLKLORIST},

                {PROFESSOR, BODYGUARD}, {VIRGIN, STAR}, {SNAKE_CHARMER, MONK}, {CANNIBAL, PHILOSOPHER},
                {UNDERCOVER, EX_UNDERCOVER},


                {CONDUCTOR, ATTENDANT}, {ARCHITECT, LOCKSMITH}, {PHARMACIST, BARTENDER}, {SHOUJO, POPPY_GROWER},
                {POSTMAN, CRAFTSMAN}, {PUPPETEER, COLLECTOR},

                {ASSASSIN, AGENT}, {ACTOR, STOWAWAY}, {IMP, LEECH}, {RIOT, LEVIATHAN}, {SCULPTOR, VORTEX},


                {MAGICIAN, RIGGER},

                {SWORDSMAN, HIKIKOMORI}, {ALCHEMIST, CLEANER}, {TERRORIST, WARDEN}, {COLONEL, LAWYER}, {COMEDIAN, PSYCHOPATH},
                {SUCCUBUS, GODFATHER},

                {NOVELIST, ANGEL}, {JESTER, EXECUTIONER}, {THIEF, ARSONIST},

                {MAJO, MESSIAH}, {KIDNAPPER, ASTRAL}, {GARDENER, COLD_WAVE}, {VOODOO, LEGION},

                {BLACKDEATH, HERETIC}, {AMNESIAC, GAMBLER}, {GOON, HERMIT}, {MARIONETTE, LUNATIC}, {TRAITOR, EX_TRAITOR},
        };
        for (Role[] pair : pairs) {
            SAME_COLOR_PARTNER.put(pair[0], pair[1]);
            SAME_COLOR_PARTNER.put(pair[1], pair[0]);
        }
    }


    public static Role sameColorPartner(Role role) {
        return SAME_COLOR_PARTNER.get(role);
    }

    public static boolean isImplemented(Role role) {
        return IMPLEMENTED.contains(role);
    }


    public static Faction factionOf(Role role) {
        return role == null ? null : FACTIONS.get(role);
    }





    public static boolean isKillerCamp(Role role) {
        return factionOf(role) == Faction.KILLER;
    }





    public static boolean isKillerCampNonSeat(Role role) {
        Kind kind = kindOf(role);
        return kind != null && kind == Kind.MAD && factionOf(role) == Faction.KILLER;
    }








    public static boolean canUseKillerShop(Role role) {
        Kind kind = kindOf(role);
        return role == TRAITOR || kind != null && kind.isKillerSeat();
    }




    public static final String CAMP_PASSENGER = "PASSENGER";
    public static final String CAMP_KILLER = "KILLER";
    public static final String CAMP_SOLO = "LONE";
    public static final String CAMP_OUTSIDER = "OUTSIDER";


    public static final java.util.List<String> LETTER_CAMPS =
            List.of(CAMP_PASSENGER, CAMP_KILLER, CAMP_SOLO, CAMP_OUTSIDER);


    public static boolean isCampValue(String value) {
        return value != null && LETTER_CAMPS.contains(value);
    }


    public static String campLangKey(String camp) {
        if (CAMP_PASSENGER.equals(camp)) return "btt.camp.passenger";
        if (CAMP_KILLER.equals(camp)) return "btt.camp.killer";
        if (CAMP_SOLO.equals(camp)) return "btt.camp.lone";
        if (CAMP_OUTSIDER.equals(camp)) return "btt.camp.outsider";
        return null;
    }





    public static int campColor(String camp) {
        if (CAMP_KILLER.equals(camp)) return 0xDD0000;
        if (CAMP_PASSENGER.equals(camp)) return 0x4EDD35;
        if (CAMP_SOLO.equals(camp) || CAMP_OUTSIDER.equals(camp)) return 0x00FFFF;
        return -1;
    }


    public static String campNameOf(Role role) {
        return campName(factionOf(role));
    }


    public static String campOverride(net.minecraft.entity.player.PlayerEntity player) {
        return PlayerState.KEY.get(player).campOverride;
    }


    public static String campName(Faction faction) {
        if (faction == null) return "";
        return switch (faction) {
            case PASSENGER -> CAMP_PASSENGER;
            case KILLER -> CAMP_KILLER;
            case SOLO -> CAMP_SOLO;
            case OUTSIDER -> CAMP_OUTSIDER;
        };
    }









    public static Faction campOf(dev.doctor4t.wathe.cca.GameWorldComponent gwc,
                                 net.minecraft.entity.player.PlayerEntity p) {
        if (p == null) return null;
        Faction camp = unmappedCampOf(gwc, p);
        if (dev.hae.btt.game.GameModes.modeOf(gwc) == dev.hae.btt.game.BeforeTheTerminalGameMode.Mode.DUEL) {
            if (camp == Faction.SOLO) return Faction.PASSENGER;
            if (camp == Faction.OUTSIDER) return Faction.KILLER;
        }
        return camp;
    }

    private static Faction unmappedCampOf(dev.doctor4t.wathe.cca.GameWorldComponent gwc, net.minecraft.entity.player.PlayerEntity p) {
        Faction byOverride = switch (campOverride(p)) {
            case CAMP_PASSENGER -> Faction.PASSENGER;
            case CAMP_KILLER -> Faction.KILLER;
            case CAMP_SOLO -> Faction.SOLO;
            case CAMP_OUTSIDER -> Faction.OUTSIDER;
            default -> null;
        };
        if (byOverride != null) return byOverride;
        if (PlayerState.KEY.get(p).isMessiahFollower()) return Faction.OUTSIDER;
        Role role = gwc == null ? null : gwc.getRole(p);
        return role == null ? null : factionOf(role);
    }


    public static boolean isPassengerCampFor(dev.doctor4t.wathe.cca.GameWorldComponent gwc,
                                             net.minecraft.entity.player.PlayerEntity player) {
        return campOf(gwc, player) == Faction.PASSENGER;
    }


    public static boolean isKillerCampFor(dev.doctor4t.wathe.cca.GameWorldComponent gwc,
                                          net.minecraft.entity.player.PlayerEntity player) {
        return campOf(gwc, player) == Faction.KILLER;
    }







    private static Faction campFactionFor(dev.doctor4t.wathe.cca.GameWorldComponent gwc,
                                          net.minecraft.entity.player.PlayerEntity player) {
        if (gwc != null && (hasCurrentOrPriorRole(gwc, player, LUNATIC) || hasCurrentOrPriorRole(gwc, player, MARIONETTE))) return null;

        if (campOverride(player).isEmpty() && !PlayerState.KEY.get(player).isMessiahFollower()) return null;
        return campOf(gwc, player);
    }


    public static int maxSprintTimeFor(dev.doctor4t.wathe.cca.GameWorldComponent gwc,
                                       net.minecraft.entity.player.PlayerEntity player) {
        Faction camp = campFactionFor(gwc, player);
        if (camp != null) return camp.maxSprintTime;
        Role role = gwc == null ? null : gwc.getRole(player);
        return role == null ? -1 : role.getMaxSprintTime();
    }


    public static int maxSprintTimeFor(dev.doctor4t.wathe.cca.GameWorldComponent gwc,
                                       net.minecraft.entity.player.PlayerEntity player, int original) {
        Faction camp = campFactionFor(gwc, player);
        return camp == null ? original : camp.maxSprintTime;
    }


    public static boolean canSeeTimeFor(dev.doctor4t.wathe.cca.GameWorldComponent gwc,
                                        net.minecraft.entity.player.PlayerEntity player) {
        Faction camp = campFactionFor(gwc, player);
        if (camp != null) return camp.canSeeTime;
        Role role = gwc == null ? null : gwc.getRole(player);
        return role != null && role.canSeeTime();
    }





    public static boolean isKillerSeat(Role role) {
        Kind kind = kindOf(role);
        return kind != null && kind.isKillerSeat();
    }




    public static Role byId(String id) {
        if (id == null || id.isEmpty()) return null;
        for (Role r : WatheRoles.ROLES) {
            if (r.identifier().toString().equals(id)) return r;
        }
        return null;
    }


    public static Role effectiveRole(dev.doctor4t.wathe.cca.GameWorldComponent gwc, net.minecraft.entity.player.PlayerEntity p) {
        return gwc.getRole(p);
    }









    public static Role realRole(dev.doctor4t.wathe.cca.GameWorldComponent gwc, net.minecraft.entity.player.PlayerEntity p) {
        PlayerState pc = PlayerState.KEY.get(p);
        if (pc.hasPriorRole()) {
            Role prior = byId(pc.priorRole);
            if (prior != null) return prior;
        }
        return gwc.getRole(p);
    }


    public static boolean isKillerSeatFor(dev.doctor4t.wathe.cca.GameWorldComponent gwc,
                                          net.minecraft.entity.player.PlayerEntity p) {
        return isKillerSeat(realRole(gwc, p));
    }


    public static Faction factionOfReal(dev.doctor4t.wathe.cca.GameWorldComponent gwc,
                                        net.minecraft.entity.player.PlayerEntity p) {
        return factionOf(realRole(gwc, p));
    }


    public static Kind kindOfReal(dev.doctor4t.wathe.cca.GameWorldComponent gwc,
                                  net.minecraft.entity.player.PlayerEntity p) {
        return kindOf(realRole(gwc, p));
    }






    public static dev.doctor4t.wathe.api.Role.MoodType effectiveMoodType(
            dev.doctor4t.wathe.cca.GameWorldComponent gwc, net.minecraft.entity.player.PlayerEntity p) {
        if (ColdWave.affectsSanity(p)) return Role.MoodType.REAL;
        PlayerState pc = PlayerState.KEY.get(p);
        if (pc != null) {
            String o = pc.moodOverride;
            if ("REAL".equals(o)) return dev.doctor4t.wathe.api.Role.MoodType.REAL;
            if ("FAKE".equals(o)) return dev.doctor4t.wathe.api.Role.MoodType.FAKE;
            if ("NONE".equals(o)) return dev.doctor4t.wathe.api.Role.MoodType.NONE;
        }

        Faction camp = campFactionFor(gwc, p);
        if (camp != null) return camp.mood;
        Role role = gwc == null ? null : gwc.getRole(p);
        return role == null ? dev.doctor4t.wathe.api.Role.MoodType.NONE : role.getMoodType();
    }


    public static boolean hasCurrentRole(dev.doctor4t.wathe.cca.GameWorldComponent gwc, net.minecraft.entity.player.PlayerEntity p, Role role) {
        return gwc != null && p != null && role != null && effectiveRole(gwc, p) == role;
    }


    public static boolean hasCurrentOrPriorRole(dev.doctor4t.wathe.cca.GameWorldComponent gwc, net.minecraft.entity.player.PlayerEntity p, Role role) {
        if (gwc.isRole(p, role)) return true;
        PlayerState pc = PlayerState.KEY.get(p);
        return pc.hasPriorRole() && byId(pc.priorRole) == role;
    }


    public static boolean isPlayingAs(dev.doctor4t.wathe.cca.GameWorldComponent gwc, net.minecraft.entity.player.PlayerEntity p, Role role) {
        return hasCurrentOrPriorRole(gwc, p, role);
    }


    public static boolean isPassengerCamp(Role role) {
        return factionOf(role) == Faction.PASSENGER;
    }









    public static boolean isInfoOrSupportPassenger(Role role) {
        Kind kind = kindOf(role);
        return kind != null && kind.isInfoOrSupport();
    }


    public static java.util.List<Role> allRoles() {
        return List.copyOf(FACTIONS.keySet());
    }

    public static java.util.List<Role> displayRoles() {
        var result = new java.util.ArrayList<>(allRoles());
        result.remove(VOODOO); result.remove(LEGION);
        int position = result.indexOf(MESSIAH) + 1;
        result.add(position, VOODOO); result.add(position + 1, LEGION);
        result.remove(ALCHEMIST); result.remove(CLEANER); result.remove(TERRORIST); result.remove(WARDEN);
        int accomplice = result.indexOf(HIKIKOMORI) + 1;
        result.addAll(accomplice, java.util.List.of(ALCHEMIST, CLEANER, TERRORIST, WARDEN));
        return java.util.List.copyOf(result);
    }


    public static Role byPath(String path) {
        for (Role r : FACTIONS.keySet()) {
            if (r.identifier().getPath().equalsIgnoreCase(path)) return r;
        }
        return null;
    }


    public static Role resolve(String guess) {
        if (guess == null || guess.isEmpty()) return null;
        Role byFullId = byId(guess);
        if (byFullId != null) return byFullId;
        int colon = guess.indexOf(':');
        return byPath(colon >= 0 ? guess.substring(colon + 1) : guess);
    }


    public static List<Identifier> newRoleIds() {
        return List.copyOf(NEW_ROLE_IDS);
    }

    public static void register() {

    }
}
