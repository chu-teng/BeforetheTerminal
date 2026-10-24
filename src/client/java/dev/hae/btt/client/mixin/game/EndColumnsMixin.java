package dev.hae.btt.client.mixin.game;

import dev.doctor4t.wathe.cca.GameRoundEndComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.client.gui.RoundTextRenderer;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.game.BeforeTheTerminalGameMode;
import dev.hae.btt.victory.Endings;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Formatting;
import dev.hae.btt.component.GameState;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.trait.relationship.Relationships;
import dev.hae.btt.role.Roles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import dev.hae.btt.mixin.game.RoundEndRoleMixin;
import dev.hae.btt.role.RoleAnnouncements;


















@Mixin(RoundTextRenderer.class)
public abstract class EndColumnsMixin {

    @Shadow private static int endTime;

    private static final int CARD_WIDTH = 36;
    private static final int CARD_HEIGHT = 28;
    private static final int MAX_ROWS = 4;
    private static final int MIN_PER_ROW = 6;

    @Inject(method = "renderHud", at = @At("HEAD"), cancellable = true)
    private static void bttEndScreen(TextRenderer renderer, ClientPlayerEntity player, DrawContext context, CallbackInfo ci) {
        if (!SeatAssignment.isBttMode(player.getWorld()) && !GameState.KEY.get(player.getWorld()).roundResultBtt) return;
        GameWorldComponent game = GameWorldComponent.KEY.get(player.getWorld());
        if (!(endTime > 0 && endTime < 200 - (GameConstants.FADE_TIME * 2) && !game.isRunning())) return;

        GameRoundEndComponent roundEnd = GameRoundEndComponent.KEY.get(player.getWorld());
        GameFunctions.WinStatus status = roundEnd.getWinStatus();
        String lastEnding = GameState.KEY.get(player.getWorld()).lastEnding;
        boolean soloWin = dev.hae.btt.victory.Endings.keyOf(lastEnding) != null;
        if (status == GameFunctions.WinStatus.NONE && !soloWin) return;

        List<GameRoundEndComponent.RoundEndData> entries = roundEnd.getPlayers();
        List<GameRoundEndComponent.RoundEndData> winners = new ArrayList<>();
        List<GameRoundEndComponent.RoundEndData> losers = new ArrayList<>();


        GameState btt = GameState.KEY.get(player.getWorld());
        List<String> winnerIds = Arrays.stream(btt.winners.split(","))
                .filter(s -> !s.isEmpty()).toList();
        for (GameRoundEndComponent.RoundEndData entry : entries) {
            (winnerIds.contains(entry.player().getId().toString()) ? winners : losers).add(entry);
        }


        losers.sort(java.util.Comparator.comparingInt(e -> btt.numberOf(e.player().getId())));
        winners.sort(java.util.Comparator.comparingInt(e -> btt.numberOf(e.player().getId())));

        context.getMatrices().push();
        context.getMatrices().translate(context.getScaledWindowWidth() / 2f, context.getScaledWindowHeight() / 2f - 40, 0);


        String key = Endings.keyOf(lastEnding);
        if (key != null) {

            int color = Endings.colorFor(lastEnding);
            Text endText = Text.translatable("btt.ending." + key).withColor(color);
            context.getMatrices().push();
            context.getMatrices().scale(2.6f, 2.6f, 1f);
            context.drawTextWithShadow(renderer, endText, -renderer.getWidth(endText) / 2, -12, 0xFFFFFF);
            context.getMatrices().pop();
        }



        if (key != null) {
            String quoteKey = switch (key) {
                case "heretic_killer" -> "btt.special.heretic.killer_win";
                case "heretic_passenger" -> "btt.special.heretic.passenger_win";

                case "blackdeath_killer" -> "btt.special.blackdeath.blackdeath_win";
                case "blackdeath_passenger" -> "btt.special.blackdeath.passenger_win";
                default -> "btt.ending.quote." + key;
            };
            Text declaration = Text.translatable(quoteKey);
            context.getMatrices().push();
            context.getMatrices().scale(1.2f, 1.2f, 1f);
            context.drawTextWithShadow(renderer, declaration, -renderer.getWidth(declaration) / 2, -4, 0xFFFFFF);
            context.getMatrices().pop();
        }


        int winnerPerRow = perRow(winners.size());
        for (int i = 0; i < winners.size(); i++) {
            int row = i / winnerPerRow;
            int col = i % winnerPerRow;
            int itemsInRow = (row == rowsFor(winners.size(), winnerPerRow) - 1)
                    ? (winners.size() - 1) % winnerPerRow + 1 : winnerPerRow;
            int x = -(itemsInRow * CARD_WIDTH) / 2 + col * CARD_WIDTH + CARD_WIDTH / 2 - 8;
            int y = 16 + row * CARD_HEIGHT;
            renderCard(context, renderer, winners.get(i), x, y);
        }


        int losersStartY = 16 + Math.max(1, rowsFor(winners.size(), winnerPerRow)) * CARD_HEIGHT + 8;
        if (!losers.isEmpty()) {
            Text losersTitle = Text.translatable("btt.ending.result.losers");
            context.drawTextWithShadow(renderer, losersTitle, -renderer.getWidth(losersTitle) / 2, losersStartY, 0xFF5555);
            int loserPerRow = perRow(losers.size());
            int gap = 14;
            for (int i = 0; i < losers.size(); i++) {
                int row = i / loserPerRow;
                int col = i % loserPerRow;
                int itemsInRow = (row == rowsFor(losers.size(), loserPerRow) - 1)
                        ? (losers.size() - 1) % loserPerRow + 1 : loserPerRow;
                int x = -(itemsInRow * CARD_WIDTH) / 2 + col * CARD_WIDTH + CARD_WIDTH / 2 - 8;
                int y = losersStartY + gap + row * CARD_HEIGHT;
                renderCard(context, renderer, losers.get(i), x, y);
            }
        }



        context.getMatrices().pop();
        ci.cancel();
    }

    private static int perRow(int size) {
        return Math.max(MIN_PER_ROW, (size + MAX_ROWS - 1) / MAX_ROWS);
    }

    private static int rowsFor(int size, int perRow) {
        return size == 0 ? 0 : Math.min(MAX_ROWS, (size + perRow - 1) / perRow);
    }


    private static void renderCard(DrawContext context, TextRenderer renderer, GameRoundEndComponent.RoundEndData entry, int x, int y) {
        PlayerListEntry ple = WatheClient.PLAYER_ENTRIES_CACHE.get(entry.player().getId());
        Identifier texture = ple == null ? null : ple.getSkinTextures().texture();
        boolean dead = entry.wasDead();
        if (texture != null) {
            context.getMatrices().push();
            context.getMatrices().scale(2f, 2f, 1f);
            context.getMatrices().translate(x / 2f, y / 2f, 0);

            context.drawTexture(texture, 0, 0, 8f, 8f, 8, 8, 64, 64);
            context.getMatrices().push();
            context.getMatrices().translate(-0.5f, -0.5f, 0);
            context.getMatrices().scale(1.125f, 1.125f, 1f);
            context.drawTexture(texture, 0, 0, 40f, 8f, 8, 8, 64, 64);
            context.getMatrices().pop();
            context.getMatrices().pop();
        }
        if (dead) {
            context.getMatrices().push();
            context.getMatrices().scale(2f, 1f, 1f);
            context.getMatrices().translate(x / 2f + 6, y + 8f, 0);
            context.drawText(renderer, "x", -renderer.getWidth("x") / 2, 0, 0xE10000, false);
            context.drawText(renderer, "x", -renderer.getWidth("x") / 2, 1, 0x550000, false);
            context.getMatrices().pop();
        }

        Text roleName = entry.role().roleText;
        context.drawTextWithShadow(renderer, roleName, x + 8 - renderer.getWidth(roleName) / 2, y + 18, entry.role().colour);
    }

}
