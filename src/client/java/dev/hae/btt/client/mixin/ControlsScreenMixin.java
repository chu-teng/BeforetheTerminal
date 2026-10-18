package dev.hae.btt.client.mixin;

import dev.hae.btt.client.InstinctMode;
import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.List;








@Mixin(ControlsOptionsScreen.class)
public abstract class ControlsScreenMixin {






    @Inject(method = "getOptions", at = @At("RETURN"), cancellable = true)
    private static void btt$appendInstinctOption(GameOptions options,
                                                 CallbackInfoReturnable<SimpleOption<?>[]> cir) {
        SimpleOption<?>[] vanilla = cir.getReturnValue();
        SimpleOption<?>[] withInstinct = Arrays.copyOf(vanilla, vanilla.length + 1);



        withInstinct[vanilla.length] = new SimpleOption<InstinctMode.Mode>(
                "btt.instinct.option",
                SimpleOption.emptyTooltip(),
                (option, value) -> net.minecraft.text.Text.translatable(
                        value == InstinctMode.Mode.TOGGLE ? "btt.instinct.option.toggle" : "btt.instinct.option.hold"),
                new SimpleOption.PotentialValuesBasedCallbacks<InstinctMode.Mode>(
                        List.of(InstinctMode.Mode.values()),
                        InstinctMode.MODE_CODEC),
                InstinctMode.mode(),
                InstinctMode::setMode);
        cir.setReturnValue(withInstinct);
    }
}
