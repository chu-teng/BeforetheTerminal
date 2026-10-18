package dev.hae.btt.mixin.game;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.entity.BttArrowMarker;
import dev.hae.btt.game.SeatAssignment;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
















@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin {
    private static final int MULTISHOT = 3;
    private static final float SPREAD = 7.0F;
    private static final int PIERCE = 4;

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void btt$crossbowUse(World world, PlayerEntity user, Hand hand,
                                CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (!btt$canUse(world, user)) return;
        ItemStack stack = user.getStackInHand(hand);
        if (CrossbowItem.isCharged(stack)) {
            btt$fire(world, user, stack);
        } else {
            user.setCurrentHand(hand);
        }
        cir.setReturnValue(TypedActionResult.consume(stack));
    }

    @Inject(method = "onStoppedUsing", at = @At("HEAD"), cancellable = true)
    private void btt$crossbowCharge(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if (!(user instanceof PlayerEntity player) || !btt$canUse(world, player)) return;
        int used = stack.getMaxUseTime(user) - remainingUseTicks;
        if (used < stack.getMaxUseTime(user)) {
            ci.cancel();
            return;
        }
        stack.set(DataComponentTypes.CHARGED_PROJECTILES,
                ChargedProjectilesComponent.of(new ItemStack(Items.ARROW)));
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ITEM_CROSSBOW_LOADING_END, SoundCategory.PLAYERS, 1.0F, 1.0F);
        ci.cancel();
    }


    private static void btt$fire(World world, PlayerEntity user, ItemStack stack) {
        stack.remove(DataComponentTypes.CHARGED_PROJECTILES);
        if (world.isClient || !(user instanceof ServerPlayerEntity shooter)) return;
        world.playSound(null, shooter.getX(), shooter.getEyeY(), shooter.getZ(),
                SoundEvents.ITEM_CROSSBOW_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F);
        for (int i = 0; i < MULTISHOT; i++) {

            ArrowEntity arrow = new ArrowEntity(world, shooter, new ItemStack(Items.ARROW), stack);
            float yaw = shooter.getYaw() + (i - (MULTISHOT - 1) / 2.0F) * SPREAD;
            arrow.setVelocity(shooter, shooter.getPitch(), yaw, 0.0F, 3.0F, 0.0F);
            arrow.pickupType = PersistentProjectileEntity.PickupPermission.DISALLOWED;
            ((BttArrowMarker) arrow).btt$markArrow(true, PIERCE);
            world.spawnEntity(arrow);
        }
    }


    private static boolean btt$canUse(World world, PlayerEntity user) {
        if (!SeatAssignment.isBttMode(world)) return false;
        if (!GameWorldComponent.KEY.get(world).isRunning()) return false;
        return GameFunctions.isPlayerAliveAndSurvival(user);
    }
}
