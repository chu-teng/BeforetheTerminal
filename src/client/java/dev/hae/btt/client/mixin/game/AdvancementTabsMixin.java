package dev.hae.btt.client.mixin.game;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlacedAdvancement;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AdvancementsScreen.class)
public abstract class AdvancementTabsMixin {
    @Inject(method = {"onRootAdded", "onDependentAdded"}, at = @At("HEAD"), cancellable = true)
    private void bttOnlyTabs(PlacedAdvancement advancement, CallbackInfo ci) {
        if (!"btt".equals(advancement.getRoot().getAdvancementEntry().id().getNamespace())) ci.cancel();
    }
    @Inject(method = "selectTab", at = @At("HEAD"), cancellable = true)
    private void bttSelectedTab(AdvancementEntry entry, CallbackInfo ci) {
        if (entry != null && !"btt".equals(entry.id().getNamespace())) ci.cancel();
    }
}
