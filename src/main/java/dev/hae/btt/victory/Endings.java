package dev.hae.btt.victory;

import dev.hae.btt.game.BeforeTheTerminalGameMode;

import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.role.Roles;










public final class Endings {
    private Endings() {}

    public enum Ending {
        NONE, TRIAL_COMPLETE, JOURNEY_END, BLOOD_EXPRESS,

        NAKU_KORO,





        BLACKDEATH_KILLER, BLACKDEATH_PASSENGER,

        THIEF_WIN,

        NOVELIST_WIN,

        ARSONIST_WIN,

        EXECUTIONER_WIN,

        RESERVED_11,

        MAJO_WIN,

        MESSIAH_WIN,

        KIDNAPPER_WIN,

        GARDENER_WIN,

        ASTRAL_WIN,

        COLD_WAVE_WIN,

        LOVERS_WIN,

        ARCHENEMY_WIN,

        HERETIC_KILLER,

        HERETIC_PASSENGER,
        ANGEL_WIN, LEGION_WIN, VOODOO_WIN, WEREWOLF_WIN
    }








    public static Ending decide(int alivePrincipals, int aliveAccomplices, int alivePassengers,
                                int aliveOutsiderNeutrals, boolean stationReached) {
        return decide(alivePrincipals, aliveAccomplices, alivePassengers, aliveOutsiderNeutrals, stationReached, false);
    }






    public static Ending decide(int alivePrincipals, int aliveAccomplices, int alivePassengers,
                                int aliveOutsiderNeutrals, boolean stationReached, boolean killerSeatsExist) {


        if (alivePassengers <= 0 && aliveOutsiderNeutrals <= 0) {
            if (alivePrincipals > 0) return Ending.BLOOD_EXPRESS;
            if (aliveAccomplices > 0) return Ending.NAKU_KORO;

            return killerSeatsExist ? Ending.BLOOD_EXPRESS : Ending.TRIAL_COMPLETE;
        }
        if (alivePrincipals <= 0 && aliveAccomplices <= 0 && aliveOutsiderNeutrals <= 0) {
            return Ending.TRIAL_COMPLETE;
        }

        if (alivePrincipals <= 0) {
            return stationReached ? Ending.JOURNEY_END : Ending.NONE;
        }
        if (stationReached) {
            return Ending.JOURNEY_END;
        }
        return Ending.NONE;
    }


    public static GameFunctions.WinStatus flip(GameFunctions.WinStatus ws) {
        return switch (ws) {
            case PASSENGERS, TIME -> GameFunctions.WinStatus.KILLERS;
            case KILLERS -> GameFunctions.WinStatus.PASSENGERS;
            default -> ws;
        };
    }


    public static Ending byName(String name) {
        if (name == null || name.isEmpty()) return Ending.NONE;
        try {
            Ending ending = Ending.valueOf(name);
            return ending == Ending.RESERVED_11 ? Ending.NONE : ending;
        } catch (IllegalArgumentException e) {
            return Ending.NONE;
        }
    }


    public static boolean isRelationEnding(Ending ending) {
        return ending == Ending.LOVERS_WIN || ending == Ending.ARCHENEMY_WIN;
    }


    public static GameFunctions.WinStatus winStatusOf(Ending ending) {
        return switch (ending) {
            case BLOOD_EXPRESS, NAKU_KORO, BLACKDEATH_KILLER -> GameFunctions.WinStatus.KILLERS;
            case BLACKDEATH_PASSENGER -> GameFunctions.WinStatus.PASSENGERS;
            case JOURNEY_END -> GameFunctions.WinStatus.TIME;
            case TRIAL_COMPLETE -> GameFunctions.WinStatus.PASSENGERS;
            case HERETIC_KILLER -> GameFunctions.WinStatus.KILLERS;
            case HERETIC_PASSENGER -> GameFunctions.WinStatus.PASSENGERS;
            default -> GameFunctions.WinStatus.NONE;
        };
    }







    public static String keyOf(String lastEnding) {
        return switch (lastEnding == null ? "NONE" : lastEnding) {
            case "TRIAL_COMPLETE" -> "trial";
            case "JOURNEY_END" -> "journey";
            case "BLOOD_EXPRESS" -> "blood";
            case "THIEF_WIN" -> "thief";
            case "NOVELIST_WIN" -> "novelist";
            case "NAKU_KORO" -> "naku";
            case "BLACKDEATH_KILLER" -> "blackdeath_killer";
            case "BLACKDEATH_PASSENGER" -> "blackdeath_passenger";
            case "HERETIC_KILLER" -> "heretic_killer";
            case "HERETIC_PASSENGER" -> "heretic_passenger";
            case "MAJO_WIN" -> "majo";
            case "MESSIAH_WIN" -> "messiah";
            case "KIDNAPPER_WIN" -> "kidnapper";
            case "GARDENER_WIN" -> "gardener";
            case "ARSONIST_WIN" -> "arsonist";
            case "ANGEL_WIN" -> "angel";
            case "LEGION_WIN" -> "legion";
            case "VOODOO_WIN" -> "voodoo";
            case "WEREWOLF_WIN" -> "werewolf";
            case "EXECUTIONER_WIN" -> "executioner";
            case "ASTRAL_WIN" -> "astral";
            case "COLD_WAVE_WIN" -> "cold_wave";
            case "LOVERS_WIN" -> "lovers";
            case "ARCHENEMY_WIN" -> "archenemy";
            default -> null;
        };
    }





    public static int colorFor(String lastEnding) {
        String key = keyOf(lastEnding);
        if (key == null) return 0xFFFFFFFF;
        return switch (key) {
            case "trial", "journey" -> 0xFF000000 | BeforeTheTerminalGameMode.FACTION_PASSENGER_COLOR;


            case "blood", "naku", "blackdeath_killer", "heretic_killer" -> 0xFFFF5555;
            case "blackdeath_passenger", "heretic_passenger" ->
                    0xFF000000 | BeforeTheTerminalGameMode.FACTION_PASSENGER_COLOR;
            case "thief" -> 0xFF000000 | Roles.THIEF.color();
            case "novelist" -> 0xFF000000 | Roles.NOVELIST.color();
            case "majo" -> 0xFF000000 | Roles.MAJO.color();
            case "messiah" -> 0xFF000000 | Roles.MESSIAH.color();
            case "kidnapper" -> 0xFF000000 | Roles.KIDNAPPER.color();
            case "gardener" -> 0xFF000000 | Roles.GARDENER.color();
            case "arsonist" -> 0xFF000000 | Roles.ARSONIST.color();
            case "angel" -> 0xFF000000 | Roles.ANGEL.color();
            case "legion" -> 0xFF000000 | Roles.LEGION.color();
            case "voodoo" -> 0xFF000000 | Roles.VOODOO.color();
            case "executioner" -> 0xFF000000 | Roles.EXECUTIONER.color();
            case "werewolf" -> 0xFF000000 | Roles.WEREWOLF.color();
            case "astral" -> 0xFF000000 | Roles.ASTRAL.color();
            case "cold_wave" -> 0xFF000000 | Roles.COLD_WAVE.color();
            case "lovers" -> 0xFF000000 | dev.hae.btt.trait.relationship.Relationships.COLOR_LOVERS;
            case "archenemy" -> 0xFF000000 | dev.hae.btt.trait.relationship.Relationships.COLOR_ARCHENEMY;
            default -> 0xFFFFFFFF;
        };
    }
}
