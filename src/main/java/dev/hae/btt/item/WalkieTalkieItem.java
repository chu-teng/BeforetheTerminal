package dev.hae.btt.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

import java.util.List;
import dev.hae.btt.registry.BttItems;











public class WalkieTalkieItem extends Item {
    public WalkieTalkieItem(Settings settings) {
        super(settings);
    }


    public static boolean isHeld(PlayerEntity player) {
        return player.getMainHandStack().isOf(dev.hae.btt.registry.BttItems.WALKIE_TALKIE)
                || player.getOffHandStack().isOf(dev.hae.btt.registry.BttItems.WALKIE_TALKIE);
    }


    public static boolean isCarried(PlayerEntity player) {
        if (player.getOffHandStack().isOf(dev.hae.btt.registry.BttItems.WALKIE_TALKIE)) return true;
        for (ItemStack stack : player.getInventory().main) {
            if (stack.isOf(dev.hae.btt.registry.BttItems.WALKIE_TALKIE)) return true;
        }
        return false;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.btt.walkie_talkie.tooltip"));
        super.appendTooltip(stack, context, tooltip, type);
    }
}
