package dev.hae.btt.client.input;

import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedHandledScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;


public final class ScreenMovement {

    private ScreenMovement() {}

    private static boolean managing = false;

    public static void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        Screen screen = client.currentScreen;
        if (!(screen instanceof LimitedHandledScreen<?> || screen instanceof dev.hae.btt.client.ui.GuideBookScreen
                || screen instanceof dev.hae.btt.client.ui.RolePickScreen || screen instanceof dev.hae.btt.client.ui.LetterScreen
                || screen instanceof dev.hae.btt.client.ui.ConsoleScreen || screen instanceof dev.hae.btt.client.ui.AdminSettingsScreen
                || screen instanceof dev.hae.btt.client.ui.MapVoteScreen) || client.player == null || !client.isWindowFocused()) {
            if (managing && screen != null) KeyBinding.unpressAll();
            managing = false;
            return;
        }
        managing = true;

        GameOptions options = client.options;
        long handle = client.getWindow().getHandle();
        sync(handle, options.forwardKey);
        sync(handle, options.backKey);
        sync(handle, options.leftKey);
        sync(handle, options.rightKey);
        sync(handle, options.sprintKey);
        sync(handle, options.sneakKey);
        sync(handle, options.jumpKey);
    }


    public static boolean isManaging() {
        return managing;
    }

    private static void sync(long handle, KeyBinding key) {
        InputUtil.Key bound = InputUtil.fromTranslationKey(key.getBoundKeyTranslationKey());
        if (bound.getCategory() != InputUtil.Type.KEYSYM) return;
        boolean pressed = InputUtil.isKeyPressed(handle, bound.getCode());
        if (key.isPressed() != pressed) KeyBinding.setKeyPressed(bound, pressed);
    }
}
