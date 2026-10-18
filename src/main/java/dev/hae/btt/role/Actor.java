package dev.hae.btt.role;

import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.game.GameEvents;
import dev.hae.btt.network.DisguiseC2SPacket;
import dev.hae.btt.BeforeTheTerminal;














public final class Actor {
    private Actor() {}






    public static void toggle(ServerPlayerEntity user, PlayerState pc, dev.doctor4t.wathe.api.Role colorRole) {
        if (!GameFunctions.isPlayerAliveAndSurvival(user)) return;
        boolean lying = !pc.isFakeDead();
        pc.setFakeDead(lying);

    }


    public static void tick(ServerPlayerEntity player, PlayerState pc) {
        if (pc.isFakeDead() && !GameFunctions.isPlayerAliveAndSurvival(player)) pc.setFakeDead(false);
    }
}
