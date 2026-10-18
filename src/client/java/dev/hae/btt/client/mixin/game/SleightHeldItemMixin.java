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
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;getMainHandStack()Lnet/minecraft/item/ItemStack;"))
    private ItemStack bttSleightHideHeldItem(LivingEntity entity, Operation<ItemStack> original) {
        ItemStack stack = original.call(entity);
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return stack;
        if (entity == client.player) return stack;
        if (entity instanceof PlayerEntity p && Traits.has(p, Traits.SLEIGHT)) return ItemStack.EMPTY;
        return stack;
    }
}
