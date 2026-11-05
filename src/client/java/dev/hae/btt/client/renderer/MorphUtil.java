package dev.hae.btt.client.renderer;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.PlayerEntityModel;

public class MorphUtil {
    public static PlayerEntityModel<AbstractClientPlayerEntity> SLIM;
    public static PlayerEntityModel<AbstractClientPlayerEntity> CLASSIC;


    public static net.minecraft.client.util.SkinTextures skin(AbstractClientPlayerEntity player) {
        var disguise = dev.hae.btt.component.DisguiseState.KEY.get(player);
        if (disguise.getMorphTicks() > 0 && disguise.disguise != null
                && player.getWorld().getPlayerByUuid(disguise.disguise) instanceof AbstractClientPlayerEntity source) {
            return source.getSkinTextures();
        }
        var mood = dev.doctor4t.wathe.client.WatheClient.moodComponent;
        if (mood != null && mood.isLowerThanDepressed()
                && dev.hae.btt.component.ConfigState.KEY.get(player.getWorld()).insaneSeesMorphs) {
            var id = dev.hae.btt.client.BttClient.SHUFFLED_PLAYER_ENTRIES_CACHE.get(player.getUuid());
            var entry = dev.doctor4t.wathe.client.WatheClient.PLAYER_ENTRIES_CACHE.get(id);
            if (entry != null) return entry.getSkinTextures();
        }
        return player.getSkinTextures();
    }
}
