package dev.hae.btt.mixin.game;

import org.spongepowered.asm.mixin.injection.ModifyVariable;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import dev.hae.btt.combat.DeathReasons;
import dev.hae.btt.trait.Traits;








@Mixin(GameFunctions.class)
public abstract class ClosureCorpseMixin {

    @ModifyVariable(method = "killPlayer(Lnet/minecraft/entity/player/PlayerEntity;ZLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Identifier;)V",
            at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static boolean bttClosureNoBody(boolean original, PlayerEntity victim, boolean spawnBody,
                                            PlayerEntity killer, Identifier reason) {
        if (!original) return false;
        if (!(killer instanceof PlayerEntity k) || k == victim) return true;
        if (!Traits.has(k, Traits.CLOSURE)) return true;
        if (!dev.hae.btt.game.SeatAssignment.isBttMode(victim.getWorld())) return true;
        return !(reason == GameConstants.DeathReasons.KNIFE
                || reason == GameConstants.DeathReasons.BAT
                || reason == DeathReasons.AXE
                || reason == DeathReasons.DAGGER
                || reason == DeathReasons.PIPE);
    }
}
