package dev.hae.btt.mixin.game;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.role.RoleDefs;
import dev.hae.btt.role.Roles;
import dev.hae.btt.trait.TraitRules;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@Mixin(value = GameFunctions.class, priority = 500)
public abstract class RoleProtectionMixin {
    private static final ThreadLocal<Set<UUID>> BTT_DAMAGE_PATH = ThreadLocal.withInitial(HashSet::new);

    @Inject(method = "killPlayer(Lnet/minecraft/entity/player/PlayerEntity;ZLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Identifier;)V", at = @At("HEAD"), cancellable = true)
    private static void bttProtectRole(PlayerEntity victim, boolean body, PlayerEntity killer, Identifier reason, CallbackInfo ci) {
        if (!(victim instanceof ServerPlayerEntity player) || !SeatAssignment.isBttMode(player.getWorld())
                || !TraitRules.harmfulDeath(reason) || TraitRules.voluntaryDeath(player, killer, reason)
                || !player.isAlive() || !GameFunctions.isPlayerAliveAndSurvival(player)) return;
        var game = GameWorldComponent.KEY.get(player.getWorld());
        if (game.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE) return;
        if (dev.doctor4t.wathe.game.GameConstants.DeathReasons.POISON.equals(reason)) {
            var poison = dev.doctor4t.wathe.cca.PlayerPoisonComponent.KEY.get(player);
            var source = poison.poisoner == null ? killer : player.getWorld().getPlayerByUuid(poison.poisoner);
            if (source instanceof ServerPlayerEntity caster) {
                var sourceDef = RoleDefs.get(game.getRole(caster));
                if (sourceDef != null && sourceDef.recruitPoisoned(caster, player)) { ci.cancel(); return; }
            }
        }
        var path = BTT_DAMAGE_PATH.get();
        if (!path.add(player.getUuid())) return;
        try {
            for (var guardian : player.getServerWorld().getPlayers()) {
                if (path.contains(guardian.getUuid()) || !guardian.isAlive()
                        || !GameFunctions.isPlayerAliveAndSurvival(guardian) || dev.hae.btt.role.Drunk.isDrunk(guardian)) continue;
                var definition = RoleDefs.get(game.getRole(guardian));
                if (definition != null && definition.protects(guardian, player)) {
                    dev.hae.btt.combat.DeathResolution.shieldBreak(player, killer);
                    definition.redirectLethal(guardian, player, killer, reason);
                    ci.cancel(); return;
                }
            }
            var definition = RoleDefs.get(game.getRole(player));
            if (definition != null && !definition.allowLethal(player, killer, reason)) ci.cancel();
        } finally {
            path.remove(player.getUuid());
            if (path.isEmpty()) BTT_DAMAGE_PATH.remove();
        }
    }
}
