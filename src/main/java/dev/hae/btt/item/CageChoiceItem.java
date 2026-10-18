package dev.hae.btt.item;

import dev.hae.btt.role.Warden;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public final class CageChoiceItem extends Item {
    public CageChoiceItem(Settings settings) { super(settings); }
    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, java.util.List<net.minecraft.text.Text> tooltip, net.minecraft.item.tooltip.TooltipType type) {
        tooltip.add(net.minecraft.text.Text.translatable("item.btt.cage_choice.tooltip", Warden.DECISION_TICKS / 20));
        super.appendTooltip(stack, context, tooltip, type);
    }
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        var stack = user.getStackInHand(hand);
        return user instanceof ServerPlayerEntity player && Warden.choose(player)
                ? TypedActionResult.success(stack) : TypedActionResult.pass(stack);
    }
}
