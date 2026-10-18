package dev.hae.btt.role;

import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheSounds;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.victory.Endings;
import dev.hae.btt.victory.SoloWin;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;

public final class Angel {
    private Angel() {}
    public static void initialize(ServerPlayerEntity player) {
        var state = PlayerState.KEY.get(player); state.angelShields = 3; state.angelTarget = ""; state.sync();
    }
    public static boolean ready(ServerPlayerEntity player) { return PlayerState.KEY.get(player).angelTarget.isEmpty(); }
    public static void guard(ServerPlayerEntity player, ServerPlayerEntity target) {
        if (player == target || !ready(player)) return;
        dev.hae.btt.advancement.BttAdvancements.protection(player, target);
        var state = PlayerState.KEY.get(player); state.angelTarget = target.getUuidAsString(); state.sync();
    }
    public static boolean protects(ServerPlayerEntity player, ServerPlayerEntity victim) {
        return victim.getUuidAsString().equals(PlayerState.KEY.get(player).angelTarget);
    }
    public static void redirect(ServerPlayerEntity guardian, ServerPlayerEntity victim, PlayerEntity killer, Identifier reason) {
        var state = PlayerState.KEY.get(guardian);
        boolean previous = state.angelSacrifice;
        state.angelSacrifice = true;
        try { GameFunctions.killPlayer(guardian, true, killer, reason); }
        finally { state.angelSacrifice = previous; }
    }
    public static boolean allowLethal(ServerPlayerEntity player, PlayerEntity killer, Identifier reason) {
        var state = PlayerState.KEY.get(player);
        if (state.isDrunk() || state.angelShields <= 0 || Gunslinger.unblockable(killer, reason)) return true;
        dev.hae.btt.combat.DeathResolution.blocked(player, killer, true);
        state.angelShields--;
        state.sync();
        dev.hae.btt.combat.DeathResolution.shieldBreak(player, killer);
        return false;
    }
    public static void died(ServerPlayerEntity player, ServerPlayerEntity killer, Identifier reason) {
        if (PlayerState.KEY.get(player).angelSacrifice) SoloWin.request(player.getServerWorld(), Endings.Ending.ANGEL_WIN, player);
    }
}
