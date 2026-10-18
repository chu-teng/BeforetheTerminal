package dev.hae.btt.role;

import dev.hae.btt.component.AbilityState;
import dev.hae.btt.component.PlayerState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;


public final class Craftsman {
    private Craftsman() {}

    public static boolean ready(ServerPlayerEntity player) {
        var state = PlayerState.KEY.get(player);
        return state.craftItem.isEmpty() ? AbilityState.KEY.get(player).cooldown <= 0
                : player.getServerWorld().getTime() >= state.craftReadyAt;
    }

    public static void use(ServerPlayerEntity player, ServerPlayerEntity target) {
        if (player == target || !ready(player)) return;
        var state = PlayerState.KEY.get(player);
        if (!state.craftItem.isEmpty()) {
            ItemStack gift = state.craftItem.copy();
            state.craftItem = ItemStack.EMPTY;
            state.craftReadyAt = 0;
            target.giveItemStack(gift);
        } else {
            if (target.getMainHandStack().isEmpty()) return;
            state.craftItem = target.getMainHandStack().copyWithCount(1);
            state.craftReadyAt = player.getServerWorld().getTime() + 600;
            AbilityState.KEY.get(player).setCooldown(RoleDefs.CD_1MIN);
        }
        state.sync();
    }
}
