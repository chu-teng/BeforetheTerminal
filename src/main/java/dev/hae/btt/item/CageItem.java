package dev.hae.btt.item;

import dev.hae.btt.role.Warden;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

public final class CageItem extends Item {
    public CageItem(Settings settings) { super(settings); }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        return user instanceof ServerPlayerEntity caster && entity instanceof ServerPlayerEntity target
                && Warden.select(caster, target, stack) ? ActionResult.SUCCESS : ActionResult.PASS;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        var stack = user.getStackInHand(hand);
        if (user instanceof ServerPlayerEntity caster
                && ProjectileUtil.getCollision(caster, e -> e instanceof ServerPlayerEntity && e != caster, 6.0)
                instanceof EntityHitResult hit && hit.getEntity() instanceof ServerPlayerEntity target
                && Warden.select(caster, target, stack)) return TypedActionResult.success(stack);
        return TypedActionResult.pass(stack);
    }
}
