package dev.hae.btt.item;

import net.minecraft.item.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.server.network.ServerPlayerEntity;

public final class BearTrapItem extends Item {
    public BearTrapItem(Settings settings) { super(settings); }
    @Override public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        var stack = user.getStackInHand(hand);
        if (ItemUse.cooling(user, this)) return TypedActionResult.fail(stack);
        if (user instanceof ServerPlayerEntity player) {
            dev.hae.btt.role.Toymaker.place(player);
            ItemUse.applyCooldown(user, this, 600);
        }
        return TypedActionResult.success(stack, world.isClient());
    }
}
