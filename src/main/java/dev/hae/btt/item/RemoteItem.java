package dev.hae.btt.item;

import dev.doctor4t.wathe.game.GameConstants;
import dev.hae.btt.block.BttTrapBlock;
import dev.hae.btt.block_entity.BttTrapBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;


public class RemoteItem extends Item {

    private static final String LINK_KEY = "btt_linked";

    private static final double RANGE = 20.0;





    private static final int COOLDOWN_TICKS = dev.doctor4t.wathe.game.GameConstants.getInTicks(1, 0);

    public RemoteItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.btt.remote.tooltip"));
        super.appendTooltip(stack, context, tooltip, type);
    }


    @Override
    public net.minecraft.util.TypedActionResult<ItemStack> use(World world, net.minecraft.entity.player.PlayerEntity user, net.minecraft.util.Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!(user instanceof ServerPlayerEntity player)) return net.minecraft.util.TypedActionResult.pass(stack);
        return btt$interact(world, player, stack) ? net.minecraft.util.TypedActionResult.success(stack)
                : net.minecraft.util.TypedActionResult.pass(stack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        ServerPlayerEntity player = context.getPlayer() instanceof ServerPlayerEntity sp ? sp : null;
        World world = context.getWorld();
        if (player == null) return ActionResult.PASS;
        return btt$interact(world, player, context.getStack()) ? ActionResult.SUCCESS : ActionResult.PASS;
    }


    private boolean btt$interact(World world, ServerPlayerEntity player, ItemStack stack) {
        if (!ItemUse.inBttRound(world)) return false;
        if (player.getItemCooldownManager().isCoolingDown(this)) {
            return false;
        }

        NbtCompound data = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();

        BttTrapBlockEntity linked = readLink(world, data);
        if (linked == null || player.getPos().distanceTo(linked.getPos().toCenterPos()) > RANGE) {
            BttTrapBlockEntity nearest = findNearest(world, player);
            if (nearest == null) {
                data.remove(LINK_KEY);
                stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(data));
                return true;
            }
            data.putLong(LINK_KEY, nearest.getPos().asLong());
            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(data));
            return true;
        }

        linked.arm(player);
        data.remove(LINK_KEY);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(data));
        player.getItemCooldownManager().set(this, COOLDOWN_TICKS);
        return true;
    }

    private static BttTrapBlockEntity readLink(World world, NbtCompound data) {
        if (!data.contains(LINK_KEY)) return null;
        BlockPos pos = BlockPos.fromLong(data.getLong(LINK_KEY));
        return world.getBlockEntity(pos) instanceof BttTrapBlockEntity trap ? trap : null;
    }


    private static BttTrapBlockEntity findNearest(World world, ServerPlayerEntity player) {
        BlockPos origin = player.getBlockPos();
        int radius = (int) Math.ceil(RANGE);
        BttTrapBlockEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.iterate(origin.add(-radius, -radius, -radius), origin.add(radius, radius, radius))) {
            if (!(world.getBlockState(pos).getBlock() instanceof BttTrapBlock)) continue;
            if (!(world.getBlockEntity(pos) instanceof BttTrapBlockEntity trap)) continue;
            double distance = player.getPos().distanceTo(pos.toCenterPos());
            if (distance > RANGE || distance >= bestDistance) continue;
            best = trap;
            bestDistance = distance;
        }
        return best;
    }
}
