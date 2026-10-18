package dev.hae.btt.item;

import dev.doctor4t.wathe.game.GameConstants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.world.World;
















public final class ItemUse {
    private ItemUse() {}





    public static boolean inBttRound(World world) {
        return true;
    }


    public static int cooldownTicks(Item item, int fallbackTicks) {
        return GameConstants.ITEM_COOLDOWNS.getOrDefault(item, fallbackTicks);
    }

    public static boolean cooling(PlayerEntity player, Item item) {
        return player.getItemCooldownManager().isCoolingDown(item);
    }


    public static void applyCooldown(PlayerEntity player, Item item, int fallbackTicks) {
        if (player.isCreative()) return;
        player.getItemCooldownManager().set(item, cooldownTicks(item, fallbackTicks));
    }
}
