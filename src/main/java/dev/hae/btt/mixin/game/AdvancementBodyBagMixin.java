package dev.hae.btt.mixin.game;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.item.BodyBagItem;
import dev.hae.btt.advancement.BttAdvancements;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(BodyBagItem.class)
public abstract class AdvancementBodyBagMixin {
    @WrapMethod(method = "useOnEntity")
    private ActionResult bttBag(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand, Operation<ActionResult> original) {
        boolean body = entity instanceof PlayerBodyEntity && !entity.isRemoved();
        var result = original.call(stack, user, entity, hand);
        if (body && entity.isRemoved() && result.isAccepted() && user instanceof ServerPlayerEntity player)
            BttAdvancements.fact(player, "body_bag");
        return result;
    }
}
