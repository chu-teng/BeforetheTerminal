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

    public static boolean isRadio(ItemStack stack) { return stack.isOf(BttItems.WALKIE_TALKIE) || stack.isOf(BttItems.MICRO_WALKIE_TALKIE); }
    public static boolean microHeld(PlayerEntity player) { return player.getMainHandStack().isOf(BttItems.MICRO_WALKIE_TALKIE) || player.getOffHandStack().isOf(BttItems.MICRO_WALKIE_TALKIE); }


    public static boolean isHeld(PlayerEntity player) {
        return isRadio(player.getMainHandStack())
                || isRadio(player.getOffHandStack());
    }


    public static boolean isCarried(PlayerEntity player) {
        if (isRadio(player.getOffHandStack())) return true;
        for (ItemStack stack : player.getInventory().main) {
            if (isRadio(stack)) return true;
        }
        return false;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable(this == BttItems.MICRO_WALKIE_TALKIE ? "item.btt.micro_walkie_talkie.tooltip" : "item.btt.walkie_talkie.tooltip"));
        super.appendTooltip(stack, context, tooltip, type);
    }
}
