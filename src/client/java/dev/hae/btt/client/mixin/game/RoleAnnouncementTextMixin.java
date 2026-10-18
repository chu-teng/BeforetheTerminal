package dev.hae.btt.client.mixin.game;

import dev.doctor4t.wathe.client.gui.RoleAnnouncementTexts;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;













@Mixin(RoleAnnouncementTexts.RoleAnnouncementText.class)
public abstract class RoleAnnouncementTextMixin {
    @Mutable @Shadow @Final public Text roleText;
    @Mutable @Shadow @Final public Text welcomeText;

    @Mutable
    @Shadow
    @Final
    public Function<Integer, Text> goalText;

    @Mutable
    @Shadow
    @Final
    public Function<Integer, Text> premiseText;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void btt$singleGoalKey(String name, int colour, CallbackInfo ci) {
        if (name == null || !name.startsWith("btt.")) return;
        if ("btt.memory".equals(name)) {
            roleText = Text.translatable("btt.memory.role");
            welcomeText = Text.translatable("btt.memory.welcome");
        }
        this.goalText = count -> "btt.memory".equals(name) ? Text.translatable("btt.memory.goal")
                : Text.translatable("announcement.goal." + name.toLowerCase()).withColor(colour);


        this.premiseText = count -> dev.hae.btt.client.util.AnnouncePremise.line();
    }
}
