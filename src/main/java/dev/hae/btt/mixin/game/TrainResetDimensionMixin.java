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

    @org.spongepowered.asm.mixin.injection.Inject(method = "tryResetTrain", at = @At("HEAD"))
    private static void bttLoadResetChunks(ServerWorld world,
            org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> ci) {
        if (!dev.hae.btt.game.SeatAssignment.isBttMode(world)) return;
        var vars = dev.doctor4t.wathe.cca.MapVariablesWorldComponent.KEY.get(world);
        var template = vars.getResetTemplateArea();
        var target = template.offset(net.minecraft.util.math.Vec3d.of(vars.getResetPasteOffset()));
        for (var box : java.util.List.of(template, target.expand(1))) {
            int minX = net.minecraft.util.math.MathHelper.floor(box.minX) >> 4;
            int maxX = net.minecraft.util.math.MathHelper.floor(box.maxX) >> 4;
            int minZ = net.minecraft.util.math.MathHelper.floor(box.minZ) >> 4;
            int maxZ = net.minecraft.util.math.MathHelper.floor(box.maxZ) >> 4;
            for (int x = minX; x <= maxX; x++)
                for (int z = minZ; z <= maxZ; z++) world.getChunk(x, z);
        }
    }

    @WrapOperation(method = "tryResetTrain", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/MinecraftServer;getOverworld()Lnet/minecraft/server/world/ServerWorld;"))
    private static ServerWorld btt$trainResetDimension(MinecraftServer server, Operation<ServerWorld> original) {
        return MapDimensions.gameWorldOrOverworld(server);
    }
}
