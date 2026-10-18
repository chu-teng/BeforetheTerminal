package dev.hae.btt.client.mixin.game;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.hae.btt.game.SeatAssignment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;















@Mixin(InGameHud.class)
public abstract class StaminaBarMixin {


    @Unique private static final int BAR_WIDTH = 166;
    @Unique private static final int BAR_HEIGHT = 3;

    @Inject(method = "render", at = @At("TAIL"))
    private void bttStaminaBar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;
        if (!WatheClient.isPlayerAliveAndInSurvival()) return;
        if (!SeatAssignment.isBttMode(client.player.getWorld())) return;
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(client.player.getWorld());
        if (gameWorld.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE) return;
        Role role = gameWorld.getRole(client.player);
        if (role == null) return;

        int maxSprintTime = dev.hae.btt.role.Roles.maxSprintTimeFor(gameWorld, client.player);
        if (maxSprintTime == -1) return;


        NbtCompound nbt = client.player.writeNbt(new NbtCompound());
        float sprintTime = nbt.getFloat("sprintingTicks");
        float percent = maxSprintTime <= 0 ? 0f : Math.max(0f, Math.min(1f, sprintTime / maxSprintTime));

        int x = client.getWindow().getScaledWidth() / 2 - BAR_WIDTH / 2;
        int y = client.getWindow().getScaledHeight() - 31;

        context.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0x80000000);


        int center = x + BAR_WIDTH / 2;
        int half = (int) (BAR_WIDTH / 2 * percent);
        if (half <= 0) return;
        context.fill(center - half, y, center + half, y + BAR_HEIGHT, bttBarColor(percent));
    }






    @Unique
    private static int bttBarColor(float percent) {
        final int FULL = 0x00AA00;
        final int EMPTY = 0xFFAA00;
        int r = (int) (0x00 + (0xFF - 0x00) * (1f - percent));
        int g = (int) (0xAA + (0xAA - 0xAA) * (1f - percent));
        int b = (int) (0x00 + (0x00 - 0x00) * (1f - percent));

        return 0xFF000000 | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }
}
