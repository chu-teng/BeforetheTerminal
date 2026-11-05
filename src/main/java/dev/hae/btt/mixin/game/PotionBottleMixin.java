package dev.hae.btt.mixin.game;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.item.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(PotionItem.class)
public abstract class PotionBottleMixin {
    @ModifyExpressionValue(method = "finishUsing", at = @At(value = "NEW", target = "net/minecraft/item/ItemStack"))
    private ItemStack bttRecycle(ItemStack result, ItemStack consumed, World world, LivingEntity user) {
        return user instanceof PlayerEntity && result.isOf(Items.GLASS_BOTTLE)
                && dev.hae.btt.game.SeatAssignment.isBttMode(world)
                && dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(world).isRunning() ? ItemStack.EMPTY : result;
    }
}
