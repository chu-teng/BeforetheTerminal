package dev.hae.btt.mixin.game;

import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.hae.btt.game.SeatAssignment;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;










@Mixin(PlayerMoodComponent.class)
public abstract class NeedsTaskTakeoverMixin {

    @Shadow @Final private PlayerEntity player;

    @Inject(method = "generateTask", at = @At("HEAD"), cancellable = true)
    private void bttNoVanillaTask(CallbackInfoReturnable<Object> cir) {
        if (player == null || !SeatAssignment.isBttMode(player.getWorld())) return;
        cir.setReturnValue(null);
    }

    @Inject(method = "eatFood", at = @At("HEAD"))
    private void bttNeedEat(CallbackInfo ci) {
        if (player instanceof net.minecraft.server.network.ServerPlayerEntity sp) {
            dev.hae.btt.game.Needs.onConsume(sp, "EAT");
        }
    }

    @Inject(method = "drinkCocktail", at = @At("HEAD"))
    private void bttNeedDrink(CallbackInfo ci) {
        if (player instanceof net.minecraft.server.network.ServerPlayerEntity sp) {
            dev.hae.btt.game.Needs.onConsume(sp, "DRINK");
        }
    }
}
