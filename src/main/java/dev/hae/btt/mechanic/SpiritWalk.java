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
    public static void beginTimed(ServerPlayerEntity player) {
        var state = PlayerState.KEY.get(player);
        state.sleepwalkTicks = 200;
        state.dreamX = player.getX(); state.dreamY = player.getY(); state.dreamZ = player.getZ();
        state.startProjecting(player.getX(), player.getY(), player.getZ());
    }


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
        pc.sleepwalkTicks = 0;
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
        if (gwc.isRole(user, Roles.MUYUUBYOU) && pc.sleepwalkTicks > 0 && --pc.sleepwalkTicks == 0) {
            double x = pc.dreamX, y = pc.dreamY, z = pc.dreamZ;
            pc.stopProjecting();
            user.teleport(user.getServerWorld(), x, y, z, user.getYaw(), user.getPitch());
            user.fallDistance = 0;
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
