package dev.hae.btt.client.mixin.game;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.hae.btt.trait.Traits;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;








@Mixin(HeldItemFeatureRenderer.class)
public abstract class SleightHeldItemMixin {

    @WrapOperation(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getMainHandStack()Lnet/minecraft/item/ItemStack;"))
    private ItemStack bttHideMain(LivingEntity entity, Operation<ItemStack> original) {
        return bttHide(entity, entity.getMainHandStack(), original.call(entity));
    }
    @WrapOperation(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getOffHandStack()Lnet/minecraft/item/ItemStack;"))
    private ItemStack bttHideOff(LivingEntity entity, Operation<ItemStack> original) {
        return bttHide(entity, entity.getOffHandStack(), original.call(entity));
    }
    private static ItemStack bttHide(LivingEntity entity, ItemStack actual, ItemStack stack) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null
                || !dev.hae.btt.game.SeatAssignment.isBttMode(client.world)
                || !dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(client.world).isRunning()) return stack;
        if (entity == client.player) return stack;
        if (actual.isOf(dev.hae.btt.registry.BttItems.MICRO_WALKIE_TALKIE)) return ItemStack.EMPTY;
        if (entity instanceof PlayerEntity p && dev.hae.btt.component.PlayerState.KEY.get(p).observedHideHeld) return ItemStack.EMPTY;
        return stack;
    }
}
