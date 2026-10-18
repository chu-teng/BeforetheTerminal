package dev.hae.btt.mixin.game;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.hae.btt.map.MapDimensions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;









@Mixin(GameWorldComponent.class)
public abstract class GameWorldComponentDimensionMixin {

    @WrapOperation(method = "serverTick", at = @At(value = "INVOKE",
            target = "Ldev/doctor4t/wathe/game/GameFunctions;limitPlayerToBox(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/util/math/Box;)V"))
    private void bttFreeRemnant(net.minecraft.server.network.ServerPlayerEntity player, net.minecraft.util.math.Box box, Operation<Void> original) {
        if (!dev.hae.btt.role.Voodoo.isRemnant(player)) original.call(player, box);
    }

    @WrapOperation(method = "serverTick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/MinecraftServer;getOverworld()Lnet/minecraft/server/world/ServerWorld;"))
    private ServerWorld btt$gameWorldInsteadOfOverworld(MinecraftServer server, Operation<ServerWorld> original) {
        return MapDimensions.gameWorldOrOverworld(server);
    }
}
