package dev.hae.btt.role;

import dev.doctor4t.wathe.api.Role;
import net.minecraft.entity.player.PlayerEntity;
import dev.hae.btt.component.PlayerState;













public final class Drunk {
    private Drunk() {}

    public static boolean isDrunk(PlayerEntity player) {
        return player != null && PlayerState.KEY.get(player).isDrunk();
    }


    public static void interruptToggleAbilities(PlayerEntity player) {
        if (!(player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer)
                || !dev.hae.btt.game.SeatAssignment.isBttMode(player.getWorld())) return;
        var game = dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(player.getWorld());
        var state = PlayerState.KEY.get(player);
        if (!game.isRunning() || !state.isDrunk()) return;
        var role = Roles.effectiveRole(game, player);
        if (state.colonelFeigning) {
            state.colonelFeigning = false;
            state.sync();
        }

        if ((role == Roles.ACTOR || role == Roles.SHOUJO) && state.isFakeDead()
                && state.tenaciousTicks <= 0) state.setFakeDead(false);
        if ((role == Roles.MUYUUBYOU || role == Roles.ASTRAL) && state.isProjecting()) {
            dev.hae.btt.mechanic.SpiritWalk.forceReturn(serverPlayer, null);
        }
    }


    public static boolean invertIfDrunk(PlayerEntity player, boolean answer) {
        return isDrunk(player) ? !answer : answer;
    }









    public static int fizzleCost(Role role) {
        if (role == null) return 0;
        if (role == Roles.MAGICIAN || role == Roles.RIGGER || role == Roles.JUDGE || role == Roles.PROPHET
                || role == Roles.PUPPETEER || role == Roles.ARCHITECT || role == Roles.LOCKSMITH
                || role == Roles.SUCCUBUS || role == Roles.BARTENDER || role == Roles.IMP
                || role == Roles.EXECUTIONER || role == Roles.CANNIBAL || role == Roles.BODYGUARD) {
            return 60 * 20;
        }
        if (role == Roles.LAWYER || role == Roles.PROFESSOR) return 120 * 20;
        return 0;
    }





    public static boolean unaffectedByDrunk(Role role) {
        if (role == null) return false;
        return role == Roles.ENGINEER
                || role == Roles.JOURNALIST
                || role == Roles.DOCTOR
                || role == Roles.AGENT
                || role == Roles.DETECTIVE
                || role == Roles.INVESTIGATOR
                || role == Roles.TAILOR;
    }
}
