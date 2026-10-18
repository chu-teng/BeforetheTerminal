package dev.hae.btt.item;

import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import dev.hae.btt.mechanic.DelayedAbility;
import dev.hae.btt.component.PlayerState;

import java.util.List;
import dev.hae.btt.network.ActionReceiver;












public class BalloonItem extends Item {

    private static final double RANGE = 6.0;

    public BalloonItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.btt.balloon.tooltip"));
        super.appendTooltip(stack, context, tooltip, type);
    }


    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (!(entity instanceof ServerPlayerEntity target) || target == user) return ActionResult.PASS;
        if (!tryUse(user, target, stack.getItem())) return ActionResult.PASS;
        user.swingHand(hand);
        return ActionResult.SUCCESS;
    }


    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!(user instanceof ServerPlayerEntity caster)) return TypedActionResult.pass(stack);
        ServerPlayerEntity target = aimed(caster);
        if (target == null || !tryUse(caster, target, stack.getItem())) return TypedActionResult.pass(stack);
        caster.swingHand(hand);
        return TypedActionResult.success(stack);
    }


    private static ServerPlayerEntity aimed(ServerPlayerEntity caster) {
        Vec3d eye = caster.getEyePos();
        Vec3d dir = caster.getRotationVec(1.0F);
        ServerPlayerEntity best = null;
        double bestAlong = Double.MAX_VALUE;
        for (ServerPlayerEntity p : caster.getServerWorld().getPlayers()) {
            if (p == caster || !GameFunctions.isPlayerAliveAndSurvival(p)) continue;
            Vec3d to = p.getPos().add(0, p.getHeight() / 2.0, 0).subtract(eye);
            double along = to.dotProduct(dir);
            if (along <= 0 || along > RANGE) continue;
            if (to.subtract(dir.multiply(along)).length() > 1.0) continue;
            if (!caster.canSee(p)) continue;
            if (along < bestAlong) {
                bestAlong = along;
                best = p;
            }
        }
        return best;
    }


    private static boolean tryUse(PlayerEntity user, ServerPlayerEntity target, Item balloon) {
        if (!(user instanceof ServerPlayerEntity caster)) return false;
        World world = caster.getWorld();
        if (world.isClient()) return false;
        if (!ItemUse.inBttRound(world)) return false;

        if (!GameFunctions.isPlayerAliveAndSurvival(target)) return false;
        if (caster.distanceTo(target) > RANGE) return false;
        if (ItemUse.cooling(caster, balloon)) return false;
        if (!DelayedAbility.schedule(caster, DelayedAbility.COMEDIAN, target)) return false;
        ItemUse.applyCooldown(caster, balloon, GameConstants.getInTicks(0, 30));
        return true;
    }
}
