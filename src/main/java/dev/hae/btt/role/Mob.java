package dev.hae.btt.role;


public final class Mob {
    private Mob() {}
    public static final int PINK = 1;
    public static final int AZURE = 2;
    public static final int PINK_COLOR = 0xFFC0CB;
    public static final int AZURE_COLOR = 0x87CEEB;
    public static int teamOf(net.minecraft.entity.player.PlayerEntity player) {
        return player == null ? 0 : dev.hae.btt.component.PlayerState.KEY.get(player).mobTeam;
    }
    public static int colorOf(int team) {
        return team == PINK ? PINK_COLOR : team == AZURE ? AZURE_COLOR : -1;
    }
}
