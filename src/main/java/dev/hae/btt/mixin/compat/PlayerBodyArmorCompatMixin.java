package dev.hae.btt.mixin.compat;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;






















@Mixin(PlayerBodyEntity.class)
public abstract class PlayerBodyArmorCompatMixin {

    @ModifyReturnValue(method = "getArmorItems", at = @At("RETURN"))
    private Iterable<ItemStack> btt$nonNullArmorItems(Iterable<ItemStack> original) {
        return original != null ? original : java.util.List.of();
    }
}
