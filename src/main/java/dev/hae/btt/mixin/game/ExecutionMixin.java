package dev.hae.btt.mixin.game;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.network.GunShootPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import dev.hae.btt.game.SeatAssignment;
import dev.doctor4t.wathe.util.Scheduler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.role.Roles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;











@Mixin(GunShootPayload.Receiver.class)
public abstract class ExecutionMixin {

    @Inject(method = "receive", at = @At("HEAD"), cancellable = true)
    private static void bttInflatedShot(GunShootPayload payload, ServerPlayNetworking.Context context, CallbackInfo ci) {
        var player = context.player();
        var stack = player.getMainHandStack();
        if (!stack.isIn(dev.doctor4t.wathe.index.tag.WatheItemTags.GUNS)
                || player.getItemCooldownManager().isCoolingDown(stack.getItem())
                || stack.isOf(dev.doctor4t.wathe.index.WatheItems.DERRINGER)
                && Boolean.TRUE.equals(stack.get(dev.doctor4t.wathe.index.WatheDataComponentTypes.USED))) return;
        if (dev.hae.btt.role.Comedian.executionAttempt(player, dev.doctor4t.wathe.game.GameConstants.DeathReasons.GUN))
            ci.cancel();
    }

    @WrapOperation(method = "receive", at = @At(value = "INVOKE",
            target = "Ldev/doctor4t/wathe/cca/GameWorldComponent;isInnocent(Lnet/minecraft/entity/player/PlayerEntity;)Z"))
    private static boolean bttShooterBased(GameWorldComponent instance, PlayerEntity arg, Operation<Boolean> op,
                                           GunShootPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayerEntity shooter = context.player();
        if (!SeatAssignment.isBttMode(shooter.getWorld())) return op.call(instance, arg);
        Entity victim = shooter.getWorld().getEntityById(payload.target());
        if (arg == victim) return dev.hae.btt.role.Colonel.executionUser(shooter, dev.doctor4t.wathe.game.GameConstants.DeathReasons.GUN);
        if (arg == shooter && dev.hae.btt.role.Colonel.pickupPrivilege(shooter)) return true;
        return op.call(instance, arg);
    }

    @WrapOperation(method = "receive", at = @At(value = "INVOKE",
            target = "Ldev/doctor4t/wathe/util/Scheduler;schedule(Ljava/lang/Runnable;I)Ldev/doctor4t/wathe/util/Scheduler$ScheduledTask;"))
    private static Scheduler.ScheduledTask bttExecutionDrop(Runnable task, int delay, Operation<Scheduler.ScheduledTask> op,
                                                            GunShootPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayerEntity shooter = context.player();
        if (!SeatAssignment.isBttMode(shooter.getWorld())) return op.call(task, delay);
        GameWorldComponent gwc = GameWorldComponent.KEY.get(shooter.getWorld());
        if (!dev.hae.btt.role.Colonel.executionUser(shooter, dev.doctor4t.wathe.game.GameConstants.DeathReasons.GUN)) return op.call(task, delay);
        boolean feigningShot = dev.hae.btt.role.Colonel.pickupPrivilege(shooter);
        boolean noDrop = gwc.isRole(shooter, Roles.VIGILANTE) || gwc.isRole(shooter, Roles.RAILWAY_POLICE);
        boolean noMood = gwc.isRole(shooter, Roles.VIGILANTE);
        float pre = PlayerMoodComponent.KEY.get(shooter).getMood();
        Runnable wrapped = noDrop
                ? (noMood ? () -> { } : () -> { PlayerMoodComponent.KEY.get(shooter).setMood(Math.max(0f, pre - 0.4f)); dev.hae.btt.advancement.BttAdvancements.executionSanity(shooter, pre); })
                : () -> {
                    if (!feigningShot && !Roles.isPassengerCampFor(gwc, shooter)) return;
                    task.run();
                    PlayerMoodComponent.KEY.get(shooter).setMood(Math.max(0f, pre - 0.4f));
                    dev.hae.btt.advancement.BttAdvancements.executionSanity(shooter, pre);
                };
        return op.call(wrapped, delay);
    }

    @WrapOperation(method = "receive", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/player/ItemCooldownManager;set(Lnet/minecraft/item/Item;I)V"))
    private static void bttExecutionCd(ItemCooldownManager instance, Item item, int ticks, Operation<Void> op,
                                       GunShootPayload payload, ServerPlayNetworking.Context context) {
        int adjusted = ticks;
        if (item == dev.doctor4t.wathe.index.WatheItems.DERRINGER
                && SeatAssignment.isBttMode(context.player().getWorld())) {
            op.call(instance, item, 300);
            return;
        }
        PlayerEntity shooter = context.player();
        if (SeatAssignment.isBttMode(shooter.getWorld())
                && shooter.getWorld().getEntityById(payload.target()) instanceof PlayerEntity target
                && target.distanceTo(shooter) < 65.0) {
            adjusted = 1200;
        }
        op.call(instance, item, adjusted);
    }







    @Inject(method = "receive", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/player/ItemCooldownManager;set(Lnet/minecraft/item/Item;I)V",
            shift = At.Shift.AFTER))
    private static void bttDebateConsumeGun(GunShootPayload payload, ServerPlayNetworking.Context context,
                                            CallbackInfo ci) {
        ServerPlayerEntity shooter = context.player();
        if (dev.hae.btt.role.Colonel.hypocrisy(shooter) && !Roles.isPassengerCampFor(GameWorldComponent.KEY.get(shooter.getWorld()), shooter)
                && shooter.getMainHandStack().isOf(dev.doctor4t.wathe.index.WatheItems.REVOLVER) && !shooter.isCreative()) {
            for (int slot = 0; slot < shooter.getInventory().size(); slot++) {
                if (shooter.getInventory().getStack(slot).isOf(dev.doctor4t.wathe.index.WatheItems.REVOLVER)) {
                    shooter.dropItem(shooter.getInventory().removeStack(slot, 1), false, false);
                    break;
                }
            }
        }
        if (!dev.hae.btt.game.GameModes.isDebate(shooter.getWorld())) return;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(shooter.getWorld());
        if (!Roles.isPassengerCampFor(gwc, shooter)) return;
        if (Roles.kindOf(gwc.getRole(shooter)) != Roles.Kind.ENFORCER) return;
        if (shooter.isCreative()) return;
        dev.hae.btt.role.RoleDefs.removeOne(shooter, dev.doctor4t.wathe.index.WatheItems.REVOLVER);
    }
    @Inject(method = "receive", at = @At(value = "INVOKE", ordinal = 1,
            target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V", shift = At.Shift.AFTER))
    private static void bttCreativeDerringerCd(GunShootPayload payload, ServerPlayNetworking.Context context, CallbackInfo ci) {
        ServerPlayerEntity player = context.player();
        if (player.isCreative() && SeatAssignment.isBttMode(player.getWorld())
                && player.getMainHandStack().isOf(dev.doctor4t.wathe.index.WatheItems.DERRINGER)) {
            player.getItemCooldownManager().set(dev.doctor4t.wathe.index.WatheItems.DERRINGER, 300);
        }
    }
    @WrapOperation(method = "receive", at = @At(value = "INVOKE",
            target = "Ldev/doctor4t/wathe/game/GameFunctions;killPlayer(Lnet/minecraft/entity/player/PlayerEntity;ZLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Identifier;)V"))
    private static void bttDamselGun(PlayerEntity victim, boolean spawnBody, PlayerEntity killer,
                                     net.minecraft.util.Identifier reason, Operation<Void> original) {
        if (!SeatAssignment.isBttMode(victim.getWorld())) {
            original.call(victim, spawnBody, killer, reason);
            return;
        }
        dev.hae.btt.role.Damsel.fire(victim, killer,
                () -> original.call(victim, spawnBody, killer, reason));
    }

    @Inject(method = "receive", at = @At("TAIL"))
    private static void bttConsumeDamselGun(GunShootPayload payload, ServerPlayNetworking.Context context, CallbackInfo ci) {
        var player = context.player();
        if (!SeatAssignment.isBttMode(player.getWorld())) return;
        var stack = player.getMainHandStack();
        if (dev.hae.btt.role.Damsel.specialGun(stack)
                && (Boolean.TRUE.equals(stack.get(dev.doctor4t.wathe.index.WatheDataComponentTypes.USED))
                || player.getItemCooldownManager().isCoolingDown(stack.getItem()))) {
            stack.decrement(1);
            player.currentScreenHandler.syncState();
        }
    }

}
