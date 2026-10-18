package dev.hae.btt.item;

import dev.doctor4t.wathe.block.TrimmedBedBlock;
import dev.doctor4t.wathe.block_entity.BeveragePlateBlockEntity;
import dev.doctor4t.wathe.block_entity.TrimmedBedBlockEntity;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;










public class PoisonNeedleItem extends Item {

    private static final int BASE_POISON_TICKS = GameConstants.getInTicks(0, 40);

    private static final double RANGE = 3.0;

    public PoisonNeedleItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.btt.poison_needle.tooltip"));
        super.appendTooltip(stack, context, tooltip, type);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (!(entity instanceof PlayerEntity target) || target == user) return ActionResult.PASS;
        World world = user.getWorld();
        if (world.isClient()) return ActionResult.PASS;
        if (!(user instanceof ServerPlayerEntity alchemist) || !(target instanceof ServerPlayerEntity victim)) {
            return ActionResult.PASS;
        }
        if (!ItemUse.inBttRound(world)) return ActionResult.PASS;

        if (!GameFunctions.isPlayerAliveAndSurvival(victim)) return ActionResult.PASS;
        if (alchemist.distanceTo(victim) > RANGE) return ActionResult.PASS;
        if (ItemUse.cooling(alchemist, this)) return ActionResult.PASS;

        PlayerPoisonComponent poison = PlayerPoisonComponent.KEY.get(victim);
        int ticks = poison.poisonTicks > 0
                ? Math.max(1, poison.poisonTicks - BASE_POISON_TICKS)
                : BASE_POISON_TICKS;
        poison.setPoisonTicks(ticks, alchemist.getUuid());
        applyCooldown(alchemist);

        return ActionResult.SUCCESS;
    }






    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (world.isClient()) return ActionResult.PASS;
        if (context.getHand() != Hand.MAIN_HAND) return ActionResult.PASS;
        if (!(context.getPlayer() instanceof ServerPlayerEntity alchemist)) return ActionResult.PASS;
        if (!ItemUse.inBttRound(world)) return ActionResult.PASS;


        if (world.getBlockEntity(context.getBlockPos()) instanceof BeveragePlateBlockEntity plate) {

            if (plate.getPoisoner() != null) return ActionResult.SUCCESS;
            if (ItemUse.cooling(alchemist, this)) return ActionResult.PASS;
            plate.setPoisoner(alchemist.getUuidAsString());
            applyCooldown(alchemist);
            return ActionResult.SUCCESS;
        }


        TrimmedBedBlockEntity bed = bedAt(world, context.getBlockPos());
        if (bed != null) {
            if (bed.hasScorpion()) return ActionResult.SUCCESS;
            if (ItemUse.cooling(alchemist, this)) return ActionResult.PASS;
            bed.setHasScorpion(true, alchemist.getUuid());
            applyCooldown(alchemist);
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }





    private static TrimmedBedBlockEntity bedAt(World world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof TrimmedBedBlockEntity bed) return bed;
        if (!(world.getBlockState(pos).getBlock() instanceof TrimmedBedBlock)) return null;
        BlockPos other = pos.offset(TrimmedBedBlock.getDirection(world, pos));
        return world.getBlockEntity(other) instanceof TrimmedBedBlockEntity bed ? bed : null;
    }


    private void applyCooldown(ServerPlayerEntity alchemist) {
        ItemUse.applyCooldown(alchemist, this, GameConstants.getInTicks(0, 30));
    }
}
