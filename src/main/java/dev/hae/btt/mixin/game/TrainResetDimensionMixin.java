package dev.hae.btt.mixin.game;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.map.MapDimensions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;






@Mixin(GameFunctions.class)
public abstract class TrainResetDimensionMixin {

    @WrapOperation(method = "tryResetTrain", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/MinecraftServer;getOverworld()Lnet/minecraft/server/world/ServerWorld;"))
    private static ServerWorld btt$trainResetDimension(MinecraftServer server, Operation<ServerWorld> original) {
        return MapDimensions.gameWorldOrOverworld(server);
    }
}
