package dev.hae.btt.mixin.game;

import dev.doctor4t.wathe.cca.GameRoundEndComponent;
import dev.doctor4t.wathe.client.gui.RoleAnnouncementTexts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;






@Mixin(GameRoundEndComponent.RoundEndData.class)
public abstract class RoundEndDataGuardMixin {

    @Redirect(method = "<init>(Lnet/minecraft/nbt/NbtCompound;)V",
            at = @At(value = "INVOKE", target = "Ljava/util/ArrayList;get(I)Ljava/lang/Object;"))
    private static Object bttClampRoleIndex(ArrayList list, int index) {
        if (index < 0 || index >= list.size()) return RoleAnnouncementTexts.BLANK;
        return list.get(index);
    }
}
