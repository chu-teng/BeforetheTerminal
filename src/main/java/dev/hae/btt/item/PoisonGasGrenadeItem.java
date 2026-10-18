package dev.hae.btt.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import dev.hae.btt.entity.PoisonGasBombEntity;

import java.util.List;
import dev.hae.btt.registry.BttEntities;







public class PoisonGasGrenadeItem extends Item {
    public PoisonGasGrenadeItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.btt.poison_gas_grenade.tooltip"));
        super.appendTooltip(stack, context, tooltip, type);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient()) {
            PoisonGasBombEntity bomb = new PoisonGasBombEntity(BttEntities.POISON_GAS_BOMB, world);
            bomb.setOwner(user);
            bomb.setPos(user.getX(), user.getEyeY() - 0.1, user.getZ());
            bomb.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 0.5F, 1.0F);
            world.spawnEntity(bomb);
        }
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        stack.decrementUnlessCreative(1, user);
        return TypedActionResult.success(stack, world.isClient());
    }
}
