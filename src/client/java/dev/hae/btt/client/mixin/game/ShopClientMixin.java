package dev.hae.btt.client.mixin.game;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedHandledScreen;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.role.Roles;
import dev.hae.btt.ability.shop.ShopGate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import dev.hae.btt.mixin.game.ShopBuyMixin;







@Mixin(LimitedInventoryScreen.class)
public abstract class ShopClientMixin extends LimitedHandledScreen<PlayerScreenHandler> {

    @Shadow @Final public ClientPlayerEntity player;

    public ShopClientMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }


    @WrapOperation(method = "init", at = @At(value = "INVOKE",
            target = "Ldev/doctor4t/wathe/cca/GameWorldComponent;canUseKillerFeatures(Lnet/minecraft/entity/player/PlayerEntity;)Z"))
    private static boolean bttKillerOnlyShop(GameWorldComponent instance, PlayerEntity p, Operation<Boolean> original) {
        if (!SeatAssignment.isBttMode(p.getWorld())) return original.call(instance, p);

        return Roles.canUseKillerShop(instance.getRole(p));
    }


    @ModifyExpressionValue(method = "init",
            at = @At(value = "FIELD", target = "Ldev/doctor4t/wathe/game/GameConstants;SHOP_ENTRIES:Ljava/util/List;"))
    private List<ShopEntry> bttShopEntries(List<ShopEntry> original) {
        if (player == null || !SeatAssignment.isBttMode(player.getWorld())) return original;

        return ShopGate.build(player.getWorld(), player);
    }
}
