package dev.hae.btt.mixin.game;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.text.Text;
import dev.hae.btt.combat.DeathReasons;
import dev.hae.btt.role.Guard;
import dev.hae.btt.game.SeatAssignment;
import java.util.ArrayList;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.role.RoleDef;
import dev.hae.btt.role.RoleDefs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import dev.hae.btt.role.BlackDeath;
import dev.hae.btt.trait.Traits;





@Mixin(GameFunctions.class)
public abstract class KillHookMixin {


    @com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod(method = "killPlayer(Lnet/minecraft/entity/player/PlayerEntity;ZLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Identifier;)V")
    private static void bttResolveDeath(PlayerEntity victim, boolean body, PlayerEntity killer, Identifier reason,
                                       com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original) {
        try (var context = dev.hae.btt.combat.DeathResolution.open(victim, body, killer, reason)) {
            original.call(victim, body, killer, reason);
            context.finish();
        }
    }

    @Inject(method = "killPlayer(Lnet/minecraft/entity/player/PlayerEntity;ZLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Identifier;)V",
            at = @At(value = "INVOKE", target = "Ldev/doctor4t/wathe/cca/PlayerMoodComponent;reset()V"))
    private static void bttConfirmDeath(PlayerEntity victim, boolean body, PlayerEntity killer, Identifier reason, CallbackInfo ci) {
        dev.hae.btt.combat.DeathResolution.confirm(victim);
    }








    @Inject(method = "killPlayer(Lnet/minecraft/entity/player/PlayerEntity;ZLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Identifier;)V",
            at = @At("HEAD"), cancellable = true)
    private static void bttVirginShield(PlayerEntity victim, boolean spawnBody, PlayerEntity killer,
                                      Identifier reason, CallbackInfo ci) {
        if (!SeatAssignment.isBttMode(victim.getWorld())) return;

        if (reason != GameConstants.DeathReasons.GUN && reason != dev.hae.btt.combat.DeathReasons.ARROW
                && reason != GameConstants.DeathReasons.KNIFE) return;
        if (!(victim instanceof ServerPlayerEntity virgin)) return;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(victim.getWorld());
        if (!gwc.isRole(victim, dev.hae.btt.role.Roles.VIRGIN)) return;

        if (dev.hae.btt.role.Gunslinger.unblockable(killer, reason)) {
            dev.hae.btt.combat.DeathResolution.shieldSound(virgin, killer);
            dev.hae.btt.record.GameRecordManager.recordTalentUse(virgin, "virgin_shield", null);
            return;
        }

        if (PlayerState.KEY.get(virgin).isDrunk()) return;

        for (PlayerEntity p : victim.getWorld().getPlayers()) {

            if (gwc.isRole(p, dev.hae.btt.role.Roles.RIOT)
                    && GameFunctions.isPlayerAliveAndSurvival(p)
                && !PlayerState.KEY.get(p).isDrunk()) return;
        }
        btt$shieldBreak(virgin, killer);

        if (killer instanceof ServerPlayerEntity shooter && shooter != victim && dev.hae.btt.role.Colonel.executionUser(shooter, reason)) {
            GameFunctions.killPlayer(shooter, true, shooter, dev.hae.btt.combat.DeathReasons.SELF_EXECUTION);
        }
        dev.hae.btt.record.GameRecordManager.recordTalentUse(virgin, "virgin_shield", null);
        ci.cancel();
    }










    @Inject(method = "killPlayer(Lnet/minecraft/entity/player/PlayerEntity;ZLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Identifier;)V",
            at = @At("HEAD"), cancellable = true)
    private static void bttMajoShield(PlayerEntity victim, boolean spawnBody, PlayerEntity killer,
                                      Identifier reason, CallbackInfo ci) {
        if (!SeatAssignment.isBttMode(victim.getWorld())) return;
        if (!(victim instanceof ServerPlayerEntity majo)) return;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(victim.getWorld());
        if (!gwc.isRole(victim, dev.hae.btt.role.Roles.MAJO)) return;

        PlayerState majoState = PlayerState.KEY.get(majo);
        if (majoState.isDrunk()) return;
        if (!majoState.majoShield) return;
        dev.hae.btt.combat.DeathResolution.blocked(majo, killer, true);
        majoState.majoShield = false;
        majoState.sync();
        btt$shieldBreak(majo, killer);

        btt$majoRecruit(majo, killer, gwc);
        dev.hae.btt.record.GameRecordManager.recordTalentUse(majo, "majo_shield", null);
        ci.cancel();
    }






    private static void btt$majoRecruit(ServerPlayerEntity majo, PlayerEntity killer, GameWorldComponent gwc) {
        if (!(killer instanceof ServerPlayerEntity recruit) || recruit == majo) return;
        if (!GameFunctions.isPlayerAliveAndSurvival(recruit)) return;
        if (PlayerState.KEY.get(recruit).isMajoRecruit()) return;
        for (ServerPlayerEntity p : majo.getServerWorld().getPlayers()) {
            if (p != recruit && PlayerState.KEY.get(p).isMajoRecruit()) return;
        }
        dev.hae.btt.advancement.BttAdvancements.recruit(majo, recruit);
        PlayerState.KEY.get(recruit).joinMajo();

        dev.hae.btt.record.GameRecordManager.recordCampChange(recruit, "MAJO", majo);

    }





    private static void btt$shieldBreak(ServerPlayerEntity victim, PlayerEntity killer) {
        dev.hae.btt.combat.DeathResolution.shieldBreak(victim, killer);
    }

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(
            method = "killPlayer(Lnet/minecraft/entity/player/PlayerEntity;ZLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Identifier;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;playSoundToPlayer(Lnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V", ordinal = 0))
    private static void bttPsychoShieldSound(PlayerEntity listener, net.minecraft.sound.SoundEvent sound,
            net.minecraft.sound.SoundCategory category, float volume, float pitch,
            com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original,
            PlayerEntity victim, boolean body, PlayerEntity killer, Identifier reason) {
        if (SeatAssignment.isBttMode(victim.getWorld())) dev.hae.btt.combat.DeathResolution.blocked(victim, killer, true);
        if (SeatAssignment.isBttMode(victim.getWorld())) dev.hae.btt.combat.DeathResolution.shieldBreak(victim, killer);
        else original.call(listener, sound, category, volume, pitch);
    }






    @Inject(method = "killPlayer(Lnet/minecraft/entity/player/PlayerEntity;ZLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Identifier;)V",
            at = @At("HEAD"), cancellable = true)
    private static void bttLawyerShield(PlayerEntity victim, boolean spawnBody, PlayerEntity killer,
                                        Identifier reason, CallbackInfo ci) {
        if (!SeatAssignment.isBttMode(victim.getWorld())) return;
        if (reason != GameConstants.DeathReasons.GUN) return;
        if (!(victim instanceof ServerPlayerEntity shielded)) return;
        if (!PlayerState.KEY.get(shielded).lawyerShield) return;
        dev.hae.btt.combat.DeathResolution.blocked(shielded, killer, true);
        PlayerState.KEY.get(shielded).consumeLawyerShield();
        btt$shieldBreak(shielded, killer);
        ci.cancel();
    }

}
