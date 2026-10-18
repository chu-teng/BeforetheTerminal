package dev.hae.btt.mixin.game;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public abstract class LobbyInventoryDropMixin {
    @Inject(method = "onSlotClick", at = @At("HEAD"), cancellable = true)
    private void bttKeepLobbyItems(int slot, int button, SlotActionType type, PlayerEntity player, CallbackInfo ci) {
        if (dev.hae.btt.game.ServerSettings.forbidDrop(player)
                && (type == SlotActionType.THROW || type == SlotActionType.PICKUP && slot == -999)) ci.cancel();
    }
}
