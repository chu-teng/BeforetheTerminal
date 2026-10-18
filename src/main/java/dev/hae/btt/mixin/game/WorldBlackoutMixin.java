package dev.hae.btt.mixin.game;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.WorldBlackoutComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import dev.hae.btt.game.SeatAssignment;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.objectweb.asm.Opcodes;
















@Mixin(WorldBlackoutComponent.class)
public abstract class WorldBlackoutMixin {


    @Unique private static final int BTT_BLACKOUT_TICKS = 600;

    @Shadow @Final private World world;
    @Shadow private int ticks;

    @Unique private int bttLastCountdownSec = -1;
    @Unique private boolean bttEffectsActive = false;


    @ModifyExpressionValue(method = "triggerBlackout",
            at = @At(value = "FIELD",
                    target = "Ldev/doctor4t/wathe/game/GameConstants;BLACKOUT_MIN_DURATION:I",
                    opcode = Opcodes.GETSTATIC))
    private int bttBlackoutMin(int original) {
        return BTT_BLACKOUT_TICKS;
    }


    @ModifyExpressionValue(method = "triggerBlackout",
            at = @At(value = "FIELD",
                    target = "Ldev/doctor4t/wathe/game/GameConstants;BLACKOUT_MAX_DURATION:I",
                    opcode = Opcodes.GETSTATIC))
    private int bttBlackoutMax(int original) {
        return BTT_BLACKOUT_TICKS + 1;
    }

    @Inject(method = "serverTick", at = @At("TAIL"))
    private void bttBlackoutTick(CallbackInfo ci) {
        if (!(this.world instanceof ServerWorld sw)) return;
        if (!SeatAssignment.isBttMode(sw)) return;
        if (this.ticks > 0) {
            this.bttEffectsActive = true;
            applyEffects(sw);
        } else if (this.bttEffectsActive) {
            this.bttEffectsActive = false;
            this.bttLastCountdownSec = -1;
            clearEffects(sw);
        }
    }

    @Inject(method = "reset", at = @At("TAIL"))
    private void bttBlackoutReset(CallbackInfo ci) {

        this.ticks = 0;
        this.bttLastCountdownSec = -1;
        this.bttEffectsActive = false;
        if (this.world instanceof ServerWorld sw && SeatAssignment.isBttMode(sw)) clearEffects(sw);
    }


    @Unique private void applyEffects(ServerWorld sw) {
        GameWorldComponent gwc = GameWorldComponent.KEY.get(sw);
        int seconds = (this.ticks + 19) / 20;
        Text countdown = Text.translatable("game.blackout.countdown", seconds).formatted(Formatting.RED);
        boolean announce = seconds != this.bttLastCountdownSec;
        if (announce) this.bttLastCountdownSec = seconds;
        for (ServerPlayerEntity p : sw.getPlayers()) {
            if (!GameFunctions.isPlayerAliveAndSurvival(p)) continue;
            if (gwc.canUseKillerFeatures(p)) {
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, this.ticks, 0, false, false, true));
            } else {
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, this.ticks, 0, false, false, true));
            }
            if (announce) p.sendMessage(countdown, true);
        }
    }

    @Unique private void clearEffects(ServerWorld sw) {
        for (ServerPlayerEntity p : sw.getPlayers()) {
            p.removeStatusEffect(StatusEffects.NIGHT_VISION);
            p.removeStatusEffect(StatusEffects.BLINDNESS);
        }
    }
}
