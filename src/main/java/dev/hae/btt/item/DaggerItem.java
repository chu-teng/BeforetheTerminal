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
import net.minecraft.world.World;
import dev.hae.btt.combat.DeathReasons;

import java.util.List;






public class DaggerItem extends Item {

    private static final double RANGE = 3.0;

    public DaggerItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.btt.dagger.tooltip"));
        super.appendTooltip(stack, context, tooltip, type);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (!(entity instanceof PlayerEntity target) || target == user) return ActionResult.PASS;
        World world = user.getWorld();
        if (world.isClient()) return ActionResult.PASS;
        if (!(user instanceof ServerPlayerEntity attacker) || !(target instanceof ServerPlayerEntity victim)) {
            return ActionResult.PASS;
        }
        if (!ItemUse.inBttRound(world)) return ActionResult.PASS;

        if (!GameFunctions.isPlayerAliveAndSurvival(victim)) return ActionResult.PASS;
        if (attacker.distanceTo(victim) > RANGE) return ActionResult.PASS;
        if (ItemUse.cooling(attacker, this)) return ActionResult.PASS;


        GameFunctions.killPlayer(victim, true, attacker, DeathReasons.DAGGER);
        attacker.swingHand(hand);
        ItemUse.applyCooldown(attacker, this, GameConstants.getInTicks(1, 0));
        return ActionResult.SUCCESS;
    }
}
