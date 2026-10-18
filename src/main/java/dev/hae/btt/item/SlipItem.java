package dev.hae.btt.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;








public class SlipItem extends Item {
    public SlipItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient() && dev.hae.btt.encounter.Memory.isSlip(user.getStackInHand(hand))) {
            dev.hae.btt.api.ClientBridge.memoryOpener.run();
            return TypedActionResult.success(user.getStackInHand(hand));
        }
        if (!world.isClient() && world instanceof ServerWorld sw && user instanceof ServerPlayerEntity sp) {
            if (dev.hae.btt.encounter.EncounterRules.useSlip(sp, user.getStackInHand(hand)))
                return TypedActionResult.success(user.getStackInHand(hand));
            if (dev.hae.btt.encounter.Memory.useSlip(sp, user.getStackInHand(hand)))
                return TypedActionResult.success(user.getStackInHand(hand));
            if (dev.hae.btt.game.ServerSettings.useAdminControl(sp, user.getStackInHand(hand)))
                return TypedActionResult.success(user.getStackInHand(hand));
            dev.hae.btt.draft.DraftManager.pick(sw, sp, sp.getInventory().selectedSlot);
            return TypedActionResult.success(user.getStackInHand(hand));
        }
        return TypedActionResult.pass(user.getStackInHand(hand));
    }


    @Override
    public int getMaxUseTime(ItemStack stack, net.minecraft.entity.LivingEntity user) {
        return 0;
    }
}
