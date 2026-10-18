package dev.hae.btt.mixin.game;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.role.Roles;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;









@Mixin(PlayerPoisonComponent.class)
public abstract class PoisonToShieldMixin {

    @Shadow @Final private PlayerEntity player;
    @Shadow public abstract void reset();

    @Inject(method = "setPoisonTicks", at = @At("HEAD"), cancellable = true)
    private void bttPoisonToShield(int ticks, UUID poisoner, CallbackInfo ci) {
        if (poisoner == null) return;
        if (!(player.getWorld().getPlayerByUuid(poisoner) instanceof PlayerEntity owner)) return;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(player.getWorld());
        if (!Roles.hasCurrentOrPriorRole(gwc, owner, Roles.PROFESSOR)) return;
        if (owner instanceof net.minecraft.server.network.ServerPlayerEntity caster) dev.hae.btt.advancement.BttAdvancements.protection(caster, player);
        PlayerState.KEY.get(player).professorShield = true;
        PlayerState.KEY.get(player).sync();
        this.reset();
        ci.cancel();
    }
}
