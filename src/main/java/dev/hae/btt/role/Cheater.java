package dev.hae.btt.role;

import dev.doctor4t.wathe.game.GameConstants;
import dev.hae.btt.component.AbilityState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;













public final class Cheater {
    private Cheater() {}


    private static final double RANGE = 20.0;


    public static void summon(ServerPlayerEntity user) {
        AbilityState ability = AbilityState.KEY.get(user);
        if (ability.cooldown > 0) return;
        ItemEntity nearest = null;
        double best = RANGE * RANGE;
        for (ItemEntity item : user.getServerWorld().getEntitiesByClass(ItemEntity.class,
                user.getBoundingBox().expand(RANGE), e -> !e.getStack().isEmpty())) {
            double distance = user.squaredDistanceTo(item);
            if (distance < best) {
                best = distance;
                nearest = item;
            }
        }
        if (nearest == null) return;
        ItemStack stack = nearest.getStack().copy();
        nearest.discard();
        user.getInventory().offerOrDrop(stack);
        ability.setCooldown(GameConstants.getInTicks(0, 30));
        ability.sync();

    }
}
