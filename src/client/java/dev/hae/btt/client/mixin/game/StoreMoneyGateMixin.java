package dev.hae.btt.client.mixin.game;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.StoreRenderer;
import dev.hae.btt.game.SeatAssignment;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(StoreRenderer.class)
public abstract class StoreMoneyGateMixin {
    @WrapOperation(method = "renderHud", at = @At(value = "INVOKE", target = "Ldev/doctor4t/wathe/cca/GameWorldComponent;canUseKillerFeatures(Lnet/minecraft/entity/player/PlayerEntity;)Z"))
    private static boolean bttShopMoney(GameWorldComponent game, PlayerEntity player, Operation<Boolean> original) {
        return SeatAssignment.isBttMode(player.getWorld()) ? dev.hae.btt.ability.shop.ShopGate.canAccess(game,player) : original.call(game,player);
    }
}
