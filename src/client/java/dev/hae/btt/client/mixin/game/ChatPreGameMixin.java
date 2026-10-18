package dev.hae.btt.client.mixin.game;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.hae.btt.game.SeatAssignment;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;













@Mixin(WatheClient.class)
public abstract class ChatPreGameMixin {

    @ModifyReturnValue(method = "isPlayerAliveAndInSurvival", at = @At("RETURN"))
    private static boolean bttChatPreGame(boolean original) {
        if (!original) return false;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) return original;

        return GameWorldComponent.KEY.get(client.world).isRunning();
    }
}
