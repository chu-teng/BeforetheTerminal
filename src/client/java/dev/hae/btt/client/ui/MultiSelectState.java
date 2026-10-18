package dev.hae.btt.client.ui;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import dev.hae.btt.role.Roles;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;






public final class MultiSelectState {
    private MultiSelectState() {}

    private static final List<UUID> PICKS = new ArrayList<>();

    public static List<UUID> picks() {
        return PICKS;
    }

    public static boolean isPicked(UUID uuid) {
        return PICKS.contains(uuid);
    }

    public static void reset() {
        PICKS.clear();
    }






    public static int needed(ClientPlayerEntity self) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return 0;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(client.world);
        if (Roles.isPlayingAs(gwc, self, Roles.PROPHET)) return 3;
        if (Roles.isPlayingAs(gwc, self, Roles.TAILOR)) return 2;
        if (Roles.isPlayingAs(gwc, self, Roles.PUPPETEER)) return 2;
        if (Roles.isPlayingAs(gwc, self, Roles.RIGGER)) return 2;
        if (Roles.isPlayingAs(gwc, self, Roles.MAGICIAN) || Roles.isPlayingAs(gwc, self, Roles.ACTOR)) return 2;
        int n = 0;
        for (AbstractClientPlayerEntity p : client.world.getPlayers()) {

            if (Roles.isKillerSeatFor(gwc, p)) n++;
        }
        return n;
    }
}
