package dev.hae.btt.client.ui;

import net.minecraft.text.Text;


public final class SkillText {
    private SkillText() {}
    public static Text prompt(Text ready, int cooldown, Text state) {
        Text line = cooldown > 0 ? Text.translatable("tip.btt.cooldown", (cooldown + 19) / 20) : ready;
        return state == null ? line : Text.empty().append(line).append(Text.translatable("btt.hud.state_separator")).append(state);
    }

    public static Text ongoing(Text state, int cooldown) {
        return cooldown <= 0 ? state : state.copy().append(
                Text.translatable("btt.hud.cooldown_suffix", (cooldown + 19) / 20));
    }
}
