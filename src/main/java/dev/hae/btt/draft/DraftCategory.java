package dev.hae.btt.draft;

import dev.doctor4t.wathe.api.Role;
import dev.hae.btt.role.Roles;







public enum DraftCategory {

    PRINCIPAL(1),

    OUTSIDER(2),

    ACCOMPLICE(3),

    ENFORCER(4),

    SOLO(6),

    MAD(8),

    CIVILIAN(9);

    private final int funRank;

    DraftCategory(int funRank) {
        this.funRank = funRank;
    }


    public int funRank() {
        return funRank;
    }








    public static DraftCategory of(Role role) {
        Roles.Kind kind = Roles.kindOf(role);
        if (kind == null) return null;
        return switch (kind) {
            case PRINCIPAL -> PRINCIPAL;
            case OUTSIDER -> OUTSIDER;
            case ACCOMPLICE -> ACCOMPLICE;
            case ENFORCER -> ENFORCER;

            case LONE -> SOLO;

            case CIVILIAN -> CIVILIAN;
            case MAD -> MAD;

        };
    }
}
