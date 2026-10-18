package dev.hae.btt.item;

import dev.doctor4t.wathe.game.GameConstants;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;
import dev.hae.btt.mixin.game.PipeAttackMixin;
import dev.hae.btt.registry.BttItems;














public class PipeItem extends Item {

    public static final int WINDOW_TICKS = GameConstants.getInTicks(0, 5);

    public static final int COOLDOWN_TICKS = GameConstants.getInTicks(2, 0);
    public PipeItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.btt.pipe.tooltip"));
        super.appendTooltip(stack, context, tooltip, type);
    }


    public static void endWindow(ServerPlayerEntity ghost) {
        ItemUse.applyCooldown(ghost, BttItems.PIPE, COOLDOWN_TICKS);
    }


    public static void tick(ServerPlayerEntity player, dev.hae.btt.component.PlayerState pc) {
        boolean holdingPipe = player.getMainHandStack().isOf(dev.hae.btt.registry.BttItems.PIPE);
        if (holdingPipe != pc.pipeHeld) {
            pc.pipeHeld = holdingPipe;
            if (holdingPipe && pc.pipeTicks <= 0
                    && !player.getItemCooldownManager().isCoolingDown(dev.hae.btt.registry.BttItems.PIPE)) {
                pc.pipeTicks = dev.hae.btt.item.PipeItem.WINDOW_TICKS;
                pc.sync();

            }
        }
        if (pc.pipeTicks > 0) {

            if (--pc.pipeTicks == 0) {
                dev.hae.btt.item.PipeItem.endWindow((net.minecraft.server.network.ServerPlayerEntity) player);
                pc.sync();

            } else if (pc.pipeTicks % 4 == 0) {
                pc.sync();
            }
        }
    }

}
