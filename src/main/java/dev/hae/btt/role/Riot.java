package dev.hae.btt.role;

import dev.hae.btt.combat.DeathReasons;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.trait.Traits;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;













public final class Riot {
    private Riot() {}


    public static boolean isExecution(Identifier reason) {
        return reason == GameConstants.DeathReasons.GUN
                || reason == GameConstants.DeathReasons.KNIFE
                || reason == dev.hae.btt.combat.DeathReasons.ARROW;
    }


    public static boolean alive(ServerWorld world, GameWorldComponent gwc) {
        for (ServerPlayerEntity p : world.getPlayers()) {
            if (!gwc.isRole(p, Roles.RIOT)) continue;
            if (!GameFunctions.isPlayerAliveAndSurvival(p)) continue;
            if (PlayerState.KEY.get(p).isDrunk()) continue;
            return true;
        }
        return false;
    }





    public static void reloadDerringerIfShot(net.minecraft.entity.player.PlayerEntity killer, Identifier reason) {
        if (reason != GameConstants.DeathReasons.GUN) return;
        if (!(killer instanceof ServerPlayerEntity shooter)) return;
        var inventory = shooter.getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            var stack = inventory.getStack(i);
            if (stack.isOf(WatheItems.DERRINGER)) {
                stack.remove(dev.doctor4t.wathe.index.WatheDataComponentTypes.USED);
                shooter.getItemCooldownManager().set(WatheItems.DERRINGER, GameConstants.getInTicks(0, 15));
                shooter.currentScreenHandler.syncState();
                return;
            }
        }
    }


    public static boolean vetoExecution(ServerPlayerEntity victim, PlayerEntity killer, Identifier reason) {
        if (!isExecution(reason)) return false;
        if (killer == null || killer == victim) return false;
        if (!(victim.getWorld() instanceof ServerWorld world)) return false;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(world);
        if (gwc == null || !gwc.isRunning()) return false;
        if (!Colonel.executionUser(killer, reason)) return false;
        if (!alive(world, gwc)) return false;
        PlayerState pc = PlayerState.KEY.get(victim);
        if (pc.tenaciousTicks > 0) return false;
        pc.tenaciousTicks = Traits.TENACIOUS_DOWN_TICKS;
        pc.tenaciousReason = reason.toString();
        pc.fakeDead = true;
        victim.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS,
                Traits.TENACIOUS_DOWN_TICKS, 6, false, false));
        victim.giveItemStack(new ItemStack(WatheItems.DERRINGER));
        pc.sync();
        return true;
    }
}