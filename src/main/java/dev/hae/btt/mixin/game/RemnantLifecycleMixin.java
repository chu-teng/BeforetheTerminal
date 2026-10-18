package dev.hae.btt.mixin.game;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.api.event.AllowPlayerDeath;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.role.Voodoo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(GameFunctions.class)
public abstract class RemnantLifecycleMixin {
    @org.spongepowered.asm.mixin.injection.Inject(method = "killPlayer(Lnet/minecraft/entity/player/PlayerEntity;ZLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Identifier;)V",
            at = @At("RETURN"))
    private static void bttKeepSpent(PlayerEntity victim, boolean body, PlayerEntity killer, Identifier reason,
                                   org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (victim instanceof net.minecraft.server.network.ServerPlayerEntity player) Voodoo.ensureSpent(player);
    }
    @WrapOperation(method = "killPlayer(Lnet/minecraft/entity/player/PlayerEntity;ZLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Identifier;)V",
            at = @At(value = "INVOKE", target = "Ldev/doctor4t/wathe/api/event/AllowPlayerDeath;allowDeath(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Identifier;)Z"))
    private static boolean bttExpire(AllowPlayerDeath event, PlayerEntity victim, PlayerEntity killer, Identifier reason, Operation<Boolean> original) {
        return Voodoo.dissolving(victim) || original.call(event, victim, killer, reason);
    }
}
