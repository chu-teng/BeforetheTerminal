package dev.hae.btt.client.mixin.game;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.trait.Traits;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(value = KeyBinding.class, priority = 2000)
public abstract class EnergeticJumpMixin {

    @Shadow
    public abstract boolean equals(KeyBinding other);

    private static boolean btt$energetic() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.world == null) return false;
        if (!dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(client.world).isRunning()) return true;
        if (!SeatAssignment.isBttMode(client.world)) return false;
        return Traits.has(client.player, Traits.ENERGETIC);
    }

    private boolean btt$isJumpKey() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.options != null && this.equals(client.options.jumpKey);
    }

    @ModifyReturnValue(method = "matchesKey", at = @At("RETURN"))
    private boolean bttJumpMatchesKey(boolean original, int keyCode, int scanCode) {
        if (btt$isJumpKey() && btt$energetic()) {
            var key = ((KeyBindingAccessor) (Object) this).btt$boundKey();
            return key.getCategory() == net.minecraft.client.util.InputUtil.Type.KEYSYM
                    ? keyCode == key.getCode()
                    : key.getCategory() == net.minecraft.client.util.InputUtil.Type.SCANCODE && scanCode == key.getCode();
        }
        return original;
    }

    @ModifyReturnValue(method = "wasPressed", at = @At("RETURN"))
    private boolean bttEnergeticWasPressed(boolean original) {
        if (!original && btt$isJumpKey() && btt$energetic()) return ((KeyBindingAccessor) (Object) this).btt$isPressedRaw();
        return original;
    }

    @ModifyReturnValue(method = "isPressed", at = @At("RETURN"))
    private boolean bttEnergeticIsPressed(boolean original) {
        if (!original && btt$isJumpKey() && btt$energetic()) return ((KeyBindingAccessor) (Object) this).btt$isPressedRaw();
        return original;
    }
}
