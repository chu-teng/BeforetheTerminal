package dev.hae.btt.mixin.game;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.entity.BttArrowMarker;
import dev.hae.btt.game.SeatAssignment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.BowItem;
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













@Mixin(BowItem.class)
public abstract class BowItemMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void btt$bowUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (!btt$canUse(world, user)) return;
        ItemStack stack = user.getStackInHand(hand);
        if (user.getItemCooldownManager().isCoolingDown(stack.getItem())) {
            cir.setReturnValue(TypedActionResult.fail(stack));
            return;
        }
        user.setCurrentHand(hand);
        cir.setReturnValue(TypedActionResult.consume(stack));
    }

    @Inject(method = "onStoppedUsing", at = @At("HEAD"), cancellable = true)
    private void btt$bowShoot(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if (!(user instanceof PlayerEntity player)) return;
        if (!btt$canUse(world, player)) return;

        int used = ((BowItem) (Object) this).getMaxUseTime(stack, user) - remainingUseTicks;
        float power = BowItem.getPullProgress(used);
        if (power < 0.1F) {
            ci.cancel();
            return;
        }

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F / (world.getRandom().nextFloat() * 0.4F + 1.2F) + power * 0.5F);

        if (!world.isClient && player instanceof ServerPlayerEntity shooter) {
            if (dev.hae.btt.role.Comedian.executionAttempt(shooter, dev.hae.btt.combat.DeathReasons.ARROW)) { ci.cancel(); return; }

            ArrowEntity arrow = new ArrowEntity(world, shooter, new ItemStack(Items.ARROW), stack);
            arrow.setVelocity(shooter, shooter.getPitch(), shooter.getYaw(), 0.0F, power * 3.0F, 1.0F);
            arrow.pickupType = PersistentProjectileEntity.PickupPermission.DISALLOWED;
            ((BttArrowMarker) arrow).btt$markArrow(false, 0);
            world.spawnEntity(arrow);
            if (dev.hae.btt.role.Colonel.hypocrisy(shooter)
                    && !dev.hae.btt.role.Roles.isPassengerCampFor(GameWorldComponent.KEY.get(world), shooter)) {
                for (int slot = 0; slot < shooter.getInventory().size(); slot++) {
                    if (shooter.getInventory().getStack(slot) == stack) {
                        shooter.dropItem(shooter.getInventory().removeStack(slot, 1), false, false);
                        break;
                    }
                }
            }

            if (dev.hae.btt.game.GameModes.isDebate(world)
                    && dev.hae.btt.role.Roles.kindOf(GameWorldComponent.KEY.get(world).getRole(shooter))
                    == dev.hae.btt.role.Roles.Kind.ENFORCER
                    && !shooter.isCreative()) {
                dev.hae.btt.role.RoleDefs.removeOne(shooter, Items.BOW);
            }
        }
        player.incrementStat(net.minecraft.stat.Stats.USED.getOrCreateStat(stack.getItem()));
        ci.cancel();
    }


    private static boolean btt$canUse(World world, PlayerEntity user) {
        if (!SeatAssignment.isBttMode(world)) return false;
        if (!GameWorldComponent.KEY.get(world).isRunning()) return false;
        return GameFunctions.isPlayerAliveAndSurvival(user);
    }
}
