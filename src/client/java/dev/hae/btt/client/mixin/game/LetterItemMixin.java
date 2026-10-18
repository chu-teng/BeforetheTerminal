package dev.hae.btt.client.mixin.game;

import dev.doctor4t.wathe.index.WatheItems;
import dev.hae.btt.game.SeatAssignment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;








@Mixin(Item.class)
public abstract class LetterItemMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void btt$openLetterScreen(World world, PlayerEntity user, Hand hand,
                                      CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (!world.isClient) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.player != user) return;
        ItemStack stack = user.getStackInHand(hand);
        if (stack.isEmpty() || !stack.isOf(WatheItems.LETTER)) return;
        if (!SeatAssignment.isBttMode(world)) return;
        if (!dev.hae.btt.client.ui.LetterScreen.open(client)) return;
        cir.setReturnValue(TypedActionResult.success(stack));
    }
}
