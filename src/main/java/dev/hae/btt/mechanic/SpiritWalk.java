package dev.hae.btt.mechanic;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import dev.hae.btt.component.AbilityState;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.mixin.game.SpiritDamageMixin;
import dev.hae.btt.role.Roles;








public final class SpiritWalk {
    private SpiritWalk() {}


    public static void toggle(ServerPlayerEntity user) {
        PlayerState pc = PlayerState.KEY.get(user);
        if (pc.isProjecting()) {


            returnToBody(user, null);
            return;
        }
        AbilityState ability = AbilityState.KEY.get(user);
        if (ability.cooldown > 0) return;
        if (!GameFunctions.isPlayerAliveAndSurvival(user)) return;
        pc.startProjecting(user.getX(), user.getY(), user.getZ());
    }





    public static void returnToBody(ServerPlayerEntity user, Text message) {
        if (!PlayerState.KEY.get(user).isProjecting()) return;
        returnToBodyInternal(user, message);
    }


    public static void forceReturn(ServerPlayerEntity user, Text message) {
        if (!PlayerState.KEY.get(user).isProjecting()) return;
        returnToBodyInternal(user, message);
    }

    private static void returnToBodyInternal(ServerPlayerEntity user, Text message) {
        PlayerState pc = PlayerState.KEY.get(user);
        pc.stopProjecting();
        if (GameWorldComponent.KEY.get(user.getWorld()).isRole(user, Roles.ASTRAL)) {
            AbilityState ability = AbilityState.KEY.get(user);
            ability.setCooldown(30 * 20);
            ability.sync();
        }
        if (message != null) user.sendMessage(message.copy().withColor(Roles.MUYUUBYOU.color()), true);
    }


    public static void tick(ServerPlayerEntity user, GameWorldComponent gwc) {
        PlayerState pc = PlayerState.KEY.get(user);
        if (!pc.isProjecting()) return;
        if (!GameFunctions.isPlayerAliveAndSurvival(user)) {
            pc.stopProjecting();
            return;
        }
        if (!gwc.isRole(user, Roles.MUYUUBYOU)) {
            returnToBody(user, Text.translatable("btt.action.muyuubyou.interrupted"));
        }
    }


    public static void abortAll(java.util.List<ServerPlayerEntity> players) {
        for (ServerPlayerEntity p : players) {
            PlayerState pc = PlayerState.KEY.get(p);
            if (pc.isProjecting()) pc.stopProjecting();
        }
    }
}
