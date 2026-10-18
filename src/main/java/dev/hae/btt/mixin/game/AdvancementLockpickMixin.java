package dev.hae.btt.mixin.game;
import dev.doctor4t.wathe.item.LockpickItem;
import dev.hae.btt.advancement.BttAdvancements;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(LockpickItem.class)
public abstract class AdvancementLockpickMixin {
    @Inject(method = "useOnBlock", at = @At("RETURN"))
    private void bttBolt(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (cir.getReturnValue().isAccepted() && context.getPlayer() instanceof ServerPlayerEntity player)
            BttAdvancements.fact(player, "bolt");
    }
}
