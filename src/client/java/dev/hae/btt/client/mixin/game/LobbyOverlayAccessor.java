package dev.hae.btt.client.mixin.game;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(InGameHud.class)
public interface LobbyOverlayAccessor {
    @Accessor("overlayRemaining") int bttOverlayRemaining();
}
