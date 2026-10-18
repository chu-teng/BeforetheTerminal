package dev.hae.btt.mixin.game;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.block.TrainDoorBlock;
import dev.doctor4t.wathe.index.WatheItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import dev.hae.btt.BeforeTheTerminal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import dev.hae.btt.registry.BttItems;

@Mixin(TrainDoorBlock.class)
public abstract class MasterKeyTrainDoorMixin {

    @WrapOperation(method = "onUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"))
    private boolean conductor(ItemStack instance, Item item, Operation<Boolean> original) {
        return  original.call(instance,item) || instance.isOf(BttItems.MASTER_KEY);
    }

}
