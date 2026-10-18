package dev.hae.btt.client.ui;

import dev.doctor4t.wathe.cca.GameTimeComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.network.ConsoleC2SPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;





public class ConsoleScreen extends Screen {

    public ConsoleScreen() {
        super(Text.translatable("btt.console.title"));
    }

    @Override
    public void removed() {
        if (ClientPlayNetworking.canSend(ConsoleC2SPacket.ID)) ClientPlayNetworking.send(new ConsoleC2SPacket(false));
        super.removed();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x88000000);
        super.render(context, mouseX, mouseY, delta);
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int half = Math.min(160, width / 2 - 16);
        context.fill(centerX - half, centerY - 65, centerX + half, centerY + 65, 0xF0362819);
        context.fill(centerX - half, centerY - 65, centerX + half, centerY - 63, 0xFFD6C394);
        context.fill(centerX - half + 16, centerY + 24, centerX + half - 16, centerY + 25, 0xFF745329);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, centerY - 40, 0xE2CD9B);

        int time = this.client.world == null ? 0 : GameTimeComponent.KEY.get(this.client.world).getTime();
        int totalSeconds = Math.max(0, time / 20);
        String clock = String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("btt.console.time", clock), centerX, centerY - 12, 0xE2CD9B);

        int alive = 0;
        for (var player : this.client.world == null ? java.util.List.<net.minecraft.entity.player.PlayerEntity>of() : this.client.world.getPlayers()) {
            if (GameFunctions.isPlayerAliveAndSurvival(player)) alive++;
        }
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("btt.console.alive", alive), centerX, centerY + 6, 0xE2B65B);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("btt.console.broadcast").formatted(Formatting.GRAY), centerX, centerY + 32, 0xAAAAAA);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("btt.console.hint").formatted(Formatting.DARK_GRAY), centerX, centerY + 46, 0x888888);
    }

    @Override public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {}

    @Override
    public boolean shouldPause() {
        return false;
    }
}
