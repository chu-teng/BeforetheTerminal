package dev.hae.btt.item;

import dev.doctor4t.wathe.game.GameConstants;
import dev.hae.btt.registry.BttItems;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;

public class FireAxeItem extends Item {
    public static final int WINDOW_TICKS = GameConstants.getInTicks(0, 5);
    public static final int COOLDOWN_TICKS = GameConstants.getInTicks(2, 0);
    public FireAxeItem(Settings settings) {
        super(settings);
    }

    public static void endWindow(ServerPlayerEntity player) {
        ItemUse.applyCooldown(player, BttItems.FIRE_AXE, COOLDOWN_TICKS);
    }

    public static void tick(ServerPlayerEntity player, dev.hae.btt.component.PlayerState pc) {
        if (!dev.doctor4t.wathe.game.GameFunctions.isPlayerAliveAndSurvival(player) || !ItemUse.inBttRound(player.getWorld())) {
            if (pc.axeTicks != 0 || pc.axeHeld) { pc.axeTicks = 0; pc.axeHeld = false; pc.sync(); }
            return;
        }
        boolean holdingAxe = player.getMainHandStack().isOf(dev.hae.btt.registry.BttItems.FIRE_AXE);
        if (holdingAxe != pc.axeHeld) {
            pc.axeHeld = holdingAxe;
            if (holdingAxe && pc.axeTicks <= 0
                    && !player.getItemCooldownManager().isCoolingDown(dev.hae.btt.registry.BttItems.FIRE_AXE)) {
                pc.axeTicks = dev.hae.btt.item.FireAxeItem.WINDOW_TICKS;
                pc.sync();
            }
        }
        if (pc.axeTicks > 0) {
            if (--pc.axeTicks == 0) {
                dev.hae.btt.item.FireAxeItem.endWindow(player);
                pc.sync();
            } else if (pc.axeTicks % 4 == 0) {
                pc.sync();
            }
        }
    }

}
