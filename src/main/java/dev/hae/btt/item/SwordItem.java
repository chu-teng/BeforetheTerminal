package dev.hae.btt.item;

import dev.doctor4t.wathe.game.GameConstants;
import dev.hae.btt.entity.FlyingSwordEntity;
import dev.hae.btt.registry.BttEntities;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

import java.util.List;











public class SwordItem extends Item {

    public SwordItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.btt.sword.tooltip"));
        super.appendTooltip(stack, context, tooltip, type);
    }


    private boolean canCharge(World world, PlayerEntity user) {


        return ItemUse.inBttRound(world) && !ItemUse.cooling(user, this);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!canCharge(world, user)) return TypedActionResult.pass(stack);
        user.setCurrentHand(hand);
        return TypedActionResult.consume(stack);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity thrower)) return;
        if (!canCharge(world, thrower)) return;

        int used = this.getMaxUseTime(stack, user) - remainingUseTicks;
        float power = getPowerForTime(used);
        if (power < 0.25F) return;

        world.playSound(null, thrower.getX(), thrower.getY(), thrower.getZ(),
                SoundEvents.ITEM_TRIDENT_THROW, SoundCategory.PLAYERS, 1.0F, 1.0F);

        if (!world.isClient) {
            FlyingSwordEntity sword = new FlyingSwordEntity(BttEntities.FLYING_SWORD, world);
            sword.setOwner(thrower);
            sword.setSwordStack(stack.copy());
            sword.setPosition(thrower.getX(), thrower.getEyeY() - 0.1, thrower.getZ());
            float velocity = 0.4F + (power * 2.0F);
            sword.setVelocity(thrower, thrower.getPitch(), thrower.getYaw(), 0.0F, velocity, 1.0F);
            world.spawnEntity(sword);

            ItemUse.applyCooldown(thrower, this, GameConstants.getInTicks(2, 0));
        }
        thrower.incrementStat(net.minecraft.stat.Stats.USED.getOrCreateStat(this));
    }


    public static float getPowerForTime(int time) {
        float f = (float) time / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        return f > 1.0F ? 1.0F : f;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }
}
