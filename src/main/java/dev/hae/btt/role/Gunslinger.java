package dev.hae.btt.role;

import dev.hae.btt.combat.DeathReasons;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;











public final class Gunslinger {
    private Gunslinger() {}


    public static boolean unblockable(@Nullable PlayerEntity killer, @Nullable Identifier reason) {
        if (killer == null || reason == null) return false;
        if (!GameConstants.DeathReasons.GUN.equals(reason)) return false;
        return Roles.hasCurrentRole(GameWorldComponent.KEY.get(killer.getWorld()), killer, Roles.GUNSLINGER);
    }
}
