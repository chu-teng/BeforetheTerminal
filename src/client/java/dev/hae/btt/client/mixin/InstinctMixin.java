package dev.hae.btt.client.mixin;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.hae.btt.role.Roles;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;













@Mixin(WatheClient.class)
public abstract class InstinctMixin {





    @Inject(method = "isInstinctEnabled", at = @At("HEAD"), cancellable = true)
    private static void btt$instinctToggleMode(CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient bttClient = MinecraftClient.getInstance();
        if (bttClient.player != null && dev.hae.btt.game.SeatAssignment.isBttMode(bttClient.player.getWorld())) {
            if (dev.hae.btt.role.Voodoo.isRemnant(bttClient.player)) { cir.setReturnValue(false); return; }
            GameWorldComponent gwc = GameWorldComponent.KEY.get(bttClient.player.getWorld());
            boolean eligible = gwc.getRole(bttClient.player) != null
                    && !Roles.isPlayingAs(gwc, bttClient.player, Roles.WEREWOLF)
                    && (!Roles.isPlayingAs(gwc, bttClient.player, Roles.TRAITOR) || dev.hae.btt.component.PlayerState.KEY.get(bttClient.player).werewolfAlly)
                    && (Roles.isKillerCampFor(gwc, bttClient.player)
                        || Roles.isPlayingAs(gwc, bttClient.player, Roles.LUNATIC)
                        || Roles.isPlayingAs(gwc, bttClient.player, Roles.ARSONIST)
                        || Roles.isPlayingAs(gwc, bttClient.player, Roles.THIEF)
                        || dev.hae.btt.trait.Traits.has(bttClient.player, dev.hae.btt.trait.Traits.INSIGHT)
                        || gwc.getRole(bttClient.player) == Roles.VOODOO
                        || gwc.getRole(bttClient.player) == Roles.MESSIAH
                        || gwc.getRole(bttClient.player) == Roles.MAJO
                        || dev.hae.btt.component.PlayerState.KEY.get(bttClient.player).isMessiahFollower()
                        || dev.hae.btt.component.PlayerState.KEY.get(bttClient.player).isMajoRecruit()
                        || dev.hae.btt.component.PlayerState.KEY.get(bttClient.player).werewolfAlly);
            boolean allowed = dev.doctor4t.wathe.game.GameFunctions.isPlayerSpectatingOrCreative(bttClient.player)
                    || (eligible && WatheClient.isPlayerAliveAndInSurvival())
;
            if (!allowed) dev.hae.btt.client.InstinctMode.setToggledOn(false);
            boolean rawPressed = WatheClient.instinctKeybind != null
                    && ((dev.hae.btt.client.mixin.game.KeyBindingAccessor) (Object) WatheClient.instinctKeybind).btt$isPressedRaw();
            cir.setReturnValue(allowed && dev.hae.btt.client.InstinctMode.active(rawPressed));
            return;
        }
        if (!dev.hae.btt.client.InstinctMode.isToggleMode()) return;





        MinecraftClient client = MinecraftClient.getInstance();
        boolean allowed = (WatheClient.isKiller() && WatheClient.isPlayerAliveAndInSurvival())
                || dev.doctor4t.wathe.game.GameFunctions.isPlayerSpectatingOrCreative(client.player);
        if (!allowed) {
            dev.hae.btt.client.InstinctMode.setToggledOn(false);
            cir.setReturnValue(false);
            return;
        }
        cir.setReturnValue(dev.hae.btt.client.InstinctMode.isToggledOn());
    }



    @Inject(method = "getInstinctHighlight", at = @At("HEAD"), cancellable = true)
    private static void btt$instinctColors(Entity target, CallbackInfoReturnable<Integer> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || !dev.hae.btt.game.SeatAssignment.isBttMode(client.player.getWorld())) return;
        if (dev.hae.btt.role.Voodoo.isRemnant(client.player)) { cir.setReturnValue(-1); return; }
        GameWorldComponent gwc = GameWorldComponent.KEY.get(client.player.getWorld());
        if (!(target instanceof PlayerEntity player) || player.isSpectator()) return;

        int observed = dev.hae.btt.component.PlayerState.KEY.get(player).observedPassiveColor;
        if (observed != -1) {
            cir.setReturnValue(observed);
            return;
        }
        if (!WatheClient.isInstinctEnabled()) return;



        if (gwc.isRole(client.player, Roles.EXECUTIONER)) {
            cir.setReturnValue(Roles.EXECUTIONER.color());
            cir.cancel();
        }
    }
}
