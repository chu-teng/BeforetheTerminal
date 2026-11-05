package dev.hae.btt.item;

import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.combat.DeathReasons;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;


public final class AmuletItem extends Item {
    public static final double RANGE = 256;
    public AmuletItem(Settings settings) { super(settings); }
    @Override public int getMaxUseTime(ItemStack stack, LivingEntity user) { return 40; }
    @Override public UseAction getUseAction(ItemStack stack) { return UseAction.BOW; }
    @Override public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        var stack = user.getStackInHand(hand);
        if (ItemUse.cooling(user, this)) return TypedActionResult.pass(stack);
        if (user instanceof ServerPlayerEntity player) {
            var state = dev.hae.btt.component.PlayerState.KEY.get(player);
            state.amuletYaw = player.getHeadYaw(); state.amuletPitch = player.getPitch();
        }
        user.setCurrentHand(hand);
        return TypedActionResult.consume(stack);
    }
    public static boolean isCharging(PlayerEntity player) {
        return player != null && player.isUsingItem() && player.getActiveItem().isOf(dev.hae.btt.registry.BttItems.AMULET);
    }
    public static ServerPlayerEntity aimedTarget(ServerPlayerEntity user) {
        return aimedTarget(user, user.getRotationVec(1));
    }
    private static ServerPlayerEntity aimedTarget(ServerPlayerEntity user, net.minecraft.util.math.Vec3d direction) {
        var start = user.getEyePos(); var end = start.add(direction.multiply(RANGE));
        ServerPlayerEntity nearest = null; double distance = Double.MAX_VALUE;
        for (var target : user.getServerWorld().getPlayers()) {
            if (target == user || !GameFunctions.isPlayerAliveAndSurvival(target)) continue;
            var hit = target.getBoundingBox().expand(0.1).raycast(start, end);
            if (hit.isPresent() && start.squaredDistanceTo(hit.get()) < distance) {
                nearest = target; distance = start.squaredDistanceTo(hit.get());
            }
        }
        return nearest;
    }
    @Override public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (user instanceof ServerPlayerEntity player && !ItemUse.cooling(player, this)
                && GameFunctions.isPlayerAliveAndSurvival(player)) {
            var state = dev.hae.btt.component.PlayerState.KEY.get(player);
            var target = aimedTarget(player, net.minecraft.util.math.Vec3d.fromPolar(state.amuletPitch, state.amuletYaw));
            if (target != null) {
                ItemUse.applyCooldown(player, this, 2400);
                GameFunctions.killPlayer(target, true, player, DeathReasons.MAGIC);
            }
        }
        return stack;
    }
}
