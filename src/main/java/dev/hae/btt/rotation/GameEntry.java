package dev.hae.btt.rotation;








public record GameEntry(
        RoleCategory bucket,
        String roleId,
        int roster,
        int killerSeats,
        int vigilanteSeats,
        int neutralSeats
) {

    public double shareOf(RoleCategory c) {
        int total = Math.max(1, roster);
        int seats = switch (c) {
            case KILLER -> killerSeats;
            case VIGILANTE -> vigilanteSeats;
            case NEUTRAL -> neutralSeats;
            case CIVILIAN -> Math.max(0, roster - killerSeats - vigilanteSeats - neutralSeats);
        };
        return seats / (double) total;
    }
}
