package dev.hae.btt.item;

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
import dev.hae.btt.mechanic.Bomb;
import dev.hae.btt.component.PlayerState;

import java.util.List;
import dev.hae.btt.network.ActionReceiver;










public class BombItem extends Item {
    public BombItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.btt.bomb.tooltip"));
        super.appendTooltip(stack, context, tooltip, type);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (!(entity instanceof PlayerEntity target) || target == user) return ActionResult.PASS;
        World world = user.getWorld();
        if (world.isClient()) return ActionResult.PASS;
        if (!(user instanceof ServerPlayerEntity owner) || !(target instanceof ServerPlayerEntity victim)) {
            return ActionResult.PASS;
        }
        if (!ItemUse.inBttRound(world)) return ActionResult.PASS;

        if (!GameFunctions.isPlayerAliveAndSurvival(victim)) return ActionResult.PASS;


        PlayerState holder = PlayerState.KEY.get(owner);
        if (holder.bombPlaced && holder.bombBeeping) {
            return Bomb.transfer(owner, victim) ? ActionResult.CONSUME : ActionResult.PASS;
        }


        if (ItemUse.cooling(owner, this)) return ActionResult.PASS;
        if (!Bomb.place(owner, victim)) return ActionResult.PASS;
        ItemUse.applyCooldown(owner, this, Bomb.PLACE_CD_TICKS);
        return ActionResult.CONSUME;
    }
}
