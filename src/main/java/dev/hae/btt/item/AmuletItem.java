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
    @Override public int getMaxUseTime(ItemStack stack, LivingEntity user) { return 20; }
    @Override public UseAction getUseAction(ItemStack stack) { return UseAction.BOW; }
    @Override public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        var stack = user.getStackInHand(hand);
        if (ItemUse.cooling(user, this)) return TypedActionResult.pass(stack);
        user.setCurrentHand(hand);
        return TypedActionResult.consume(stack);
    }
    public static ServerPlayerEntity aimedTarget(ServerPlayerEntity user) {
        var start = user.getEyePos(); var end = start.add(user.getRotationVec(1).multiply(RANGE));
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
            var target = aimedTarget(player);
            if (target != null) {
                ItemUse.applyCooldown(player, this, 2400);
                GameFunctions.killPlayer(target, true, player, DeathReasons.MAGIC);
            }
        }
        return stack;
    }
}
