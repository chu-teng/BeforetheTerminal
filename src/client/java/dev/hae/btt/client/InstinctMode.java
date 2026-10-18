package dev.hae.btt.client;

import dev.hae.btt.config.BttConfig;








public final class InstinctMode {

    private InstinctMode() {}


    public enum Mode { HOLD, TOGGLE }

    public static final com.mojang.serialization.Codec<Mode> MODE_CODEC =
            com.mojang.serialization.Codec.STRING.xmap(Mode::valueOf, Mode::name);

    public static Mode mode() {
        return isToggleMode() ? Mode.TOGGLE : Mode.HOLD;
    }

    public static void setMode(Mode mode) {
        setToggleMode(mode == Mode.TOGGLE);
    }


    private static boolean toggledOn = false;

    public static boolean isToggleMode() {
        return BttConfig.HANDLER.instance().instinctToggle;
    }

    public static void toggleMode() {
        setToggleMode(!isToggleMode());
    }


    public static void setToggleMode(boolean toggle) {
        BttConfig.HANDLER.instance().instinctToggle = toggle;
        BttConfig.HANDLER.save();
        toggledOn = false;
    }

    public static boolean isToggledOn() {
        return toggledOn;
    }

    public static void setToggledOn(boolean on) {
        toggledOn = on;
    }


    public static boolean active(boolean holdPressed) {
        return isToggleMode() ? toggledOn : holdPressed;
    }
}
