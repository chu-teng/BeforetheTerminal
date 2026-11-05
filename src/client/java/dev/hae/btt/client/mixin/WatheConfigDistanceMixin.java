package dev.hae.btt.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.WatheConfig;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(WatheConfig.class)
public abstract class WatheConfigDistanceMixin {
    @WrapOperation(method = "writeChanges", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/option/SimpleOption;setValue(Ljava/lang/Object;)V"))
    private void btt$preserveDistance(SimpleOption<?> option, Object value, Operation<Void> original) {

    }
}
