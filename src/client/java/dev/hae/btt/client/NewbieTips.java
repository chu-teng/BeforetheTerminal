package dev.hae.btt.client;

import dev.hae.btt.game.SeatAssignment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;










public final class NewbieTips {

    private NewbieTips() {}


    private static final int INTERVAL_TICKS = 20 * 20;






    private static final String KEY_PREFIX = "btt.newbie.";

    private static final int MAX_TIP_INDEX = 256;

    private static boolean enabled = true;
    private static int ticks;
    private static int index;
    private static Text currentTip;
    private static int visibleTicks;

    private static final List<String> playlist = new ArrayList<>();


    public static void register() {
        load();
        net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback.EVENT.register((context, counter) -> {
            var client = MinecraftClient.getInstance();
            if (currentTip == null || visibleTicks <= 0 || !canShow(client)) return;
            if (((dev.hae.btt.client.mixin.game.LobbyOverlayAccessor) client.inGameHud).bttOverlayRemaining() > 0) return;
            context.drawCenteredTextWithShadow(client.textRenderer, currentTip,
                    context.getScaledWindowWidth() / 2, context.getScaledWindowHeight() - 68, 0xFFFFFF);
        });
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("tips")
                                .executes(ctx -> toggle(ctx.getSource()))
                                .then(ClientCommandManager.literal("on").executes(ctx -> set(ctx.getSource(), true)))
                                .then(ClientCommandManager.literal("off").executes(ctx -> set(ctx.getSource(), false)))));
    }


    public static void tick(MinecraftClient client) {
        if (visibleTicks > 0) visibleTicks--;
        if (!canShow(client)) { currentTip = null; visibleTicks = 0; return; }
        if (++ticks < INTERVAL_TICKS) return;
        ticks = 0;
        if (playlist.isEmpty()) buildPlaylist();
        if (playlist.isEmpty()) return;
        if (index >= playlist.size()) {

            Collections.shuffle(playlist);
            index = 0;
        }
        currentTip = Text.translatable(playlist.get(index++));
        visibleTicks = 60;
    }

    private static boolean canShow(MinecraftClient client) {
        if (!enabled || client == null || client.player == null || client.world == null || client.options.hudHidden) return false;
        return dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(client.world).getGameStatus()
                == dev.doctor4t.wathe.cca.GameWorldComponent.GameStatus.INACTIVE;
    }





    private static void buildPlaylist() {
        playlist.clear();
        for (int i = 1; i <= MAX_TIP_INDEX; i++) {
            String key = KEY_PREFIX + i;
            if (hasTranslation(key)) playlist.add(key);
        }
        Collections.shuffle(playlist);
        index = 0;
    }

    private static boolean hasTranslation(String key) {
        String resolved = Text.translatable(key).getString();
        return resolved != null && !resolved.isEmpty() && !resolved.equals(key);
    }

    private static int toggle(net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource src) {
        return set(src, !enabled);
    }

    private static int set(net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource src, boolean value) {
        enabled = value;
        ticks = 0; currentTip = null; visibleTicks = 0;
        save();
        src.sendFeedback(Text.translatable(value ? "btt.newbie.on" : "btt.newbie.off").formatted(Formatting.GOLD));
        return 1;
    }



    private static java.nio.file.Path configPath() {
        return net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir()
                .resolve("btt_newbie_tips.json");
    }


    public static void load() {
        try {
            java.nio.file.Path path = configPath();
            if (!java.nio.file.Files.exists(path)) return;
            com.google.gson.JsonObject json = com.google.gson.JsonParser
                    .parseString(java.nio.file.Files.readString(path)).getAsJsonObject();
            if (json.has("enabled")) enabled = json.get("enabled").getAsBoolean();
        } catch (Exception ignored) {

        }
    }

    private static void save() {
        try {
            com.google.gson.JsonObject json = new com.google.gson.JsonObject();
            json.addProperty("enabled", enabled);
            java.nio.file.Files.writeString(configPath(), json.toString());
        } catch (Exception ignored) {

        }
    }
}
