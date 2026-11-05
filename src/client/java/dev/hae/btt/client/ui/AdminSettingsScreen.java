package dev.hae.btt.client.ui;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.hae.btt.encounter.Backgrounds;
import dev.hae.btt.game.BeforeTheTerminalGameMode;
import dev.hae.btt.game.Rule;
import dev.hae.btt.game.Scripts;
import dev.hae.btt.game.ServerSettings;
import dev.hae.btt.network.AdminMenuS2CPacket;
import dev.hae.btt.network.AdminSettingC2SPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;







public final class AdminSettingsScreen extends Screen {
    private static final String[] CATEGORIES = {"mode", "encounter", "script"};
    private static final int PAGE_SIZE = 12;
    private static final int ROW = 23;
    private final AdminMenuS2CPacket settings;
    private final BeforeTheTerminalGameMode.Mode mode;
    private final Set<Rule> rules;
    private final List<String> choices = new ArrayList<>();
    private int page;
    private int panelWidth;
    private int columnWidth;
    private int left;
    private int top;

    public AdminSettingsScreen(AdminMenuS2CPacket settings) {
        super(Text.translatable("btt.admin.title"));
        this.settings = settings;
        this.mode = BeforeTheTerminalGameMode.Mode.valueOf(settings.mode());
        this.rules = Rule.normalize(Rule.parse(settings.rules()));
        choices.addAll(buildChoices(settings.category()));
    }

    private List<String> buildChoices(String category) {
        List<String> out = new ArrayList<>();
        switch (category) {
            case "mode" -> {
                for (var candidate : BeforeTheTerminalGameMode.Mode.displayOrder()) out.add(candidate.name());
            }
            case "encounter" -> {
                if (mode == BeforeTheTerminalGameMode.Mode.MOB) return out;
                out.addAll(List.of("random", "none"));
                Backgrounds.IMPLEMENTED.stream().filter(id -> ServerSettings.modeAllows(mode, id))
                        .sorted().forEach(out::add);
            }
            case "script" -> {
                if (mode == BeforeTheTerminalGameMode.Mode.CUSTOM) {
                    for (Rule rule : Rule.displayOrder()) out.add((rule.base() ? "base:" : "rule:") + rule.name());
                } else if (ruleSet().contains(Rule.SCRIPT)) {
                    out.add("random");
                    for (var script : Scripts.ALL) out.add(script.id());
                }
            }
            default -> { }
        }
        return out;
    }


    private Set<Rule> ruleSet() {
        return mode == BeforeTheTerminalGameMode.Mode.CUSTOM ? rules : mode.rules();
    }

    @Override protected void init() {
        panelWidth = Math.min(400, width - 24);
        left = (width - panelWidth) / 2;
        top = Math.max(8, (height - 212) / 2);
        columnWidth = (panelWidth - 24) / 3;
        for (int column = 0; column < CATEGORIES.length; column++) {
            String category = CATEGORIES[column];
            addDrawableChild(new PanelButton(left + 12 + column * columnWidth, top + 33, columnWidth - 4, 23,
                    Text.translatable("btt.admin." + ("script".equals(category) ? "other" : category)),
                    category.equals(settings.category()),
                    button -> ClientPlayNetworking.send(new AdminSettingC2SPacket(category, ""))));
        }
        int begin = page * PAGE_SIZE;
        for (int i = begin; i < Math.min(choices.size(), begin + PAGE_SIZE); i++) {
            String value = choices.get(i);
            int index = i - begin;
            var button = new PanelButton(left + 12 + index % 3 * columnWidth, top + 83 + index / 3 * ROW,
                    columnWidth - 4, ROW - 3, label(settings.category(), value),
                    isSelected(settings.category(), value),
                    ignored -> ClientPlayNetworking.send(new AdminSettingC2SPacket(settings.category(), value)));
            button.active = categoryEnabled(settings.category())
                    && (!"encounter".equals(settings.category()) || ServerSettings.modeAllows(mode, value));
            if (!button.active) button.setTooltip(Tooltip.of(Text.translatable("btt.admin.incompatible")));
            addDrawableChild(button);
        }
        if (choices.size() > PAGE_SIZE) {
            addDrawableChild(new PanelButton(left + 12, top + 182, 38, 18, Text.literal("‹"), false,
                    button -> { page--; clearAndInit(); })).active = page > 0;
            addDrawableChild(new PanelButton(left + panelWidth - 50, top + 182, 38, 18, Text.literal("›"), false,
                    button -> { page++; clearAndInit(); })).active = (page + 1) * PAGE_SIZE < choices.size();
        }
    }

    private boolean categoryEnabled(String category) {
        if ("mode".equals(category)) return true;
        if ("encounter".equals(category)) return mode != BeforeTheTerminalGameMode.Mode.MOB;
        return mode == BeforeTheTerminalGameMode.Mode.CUSTOM || ruleSet().contains(Rule.SCRIPT);
    }

    private boolean isSelected(String category, String value) {
        return switch (category) {
            case "mode" -> value.equals(settings.mode());
            case "encounter" -> value.equals(settings.encounter());
            case "script" -> {
                if (mode == BeforeTheTerminalGameMode.Mode.CUSTOM) {
                    if (value.startsWith("base:")) {
                        Rule base = Rule.baseOf(rules);
                        yield base != null && value.substring(5).equals(base.name());
                    }
                    Rule rule = Rule.byKey(value.startsWith("rule:") ? value.substring(5) : value);
                    yield rule != null && rules.contains(rule);
                }
                yield value.equals(settings.script());
            }
            default -> false;
        };
    }

    private Text label(String category, String value) {
        if ("mode".equals(category)) return Text.translatable("btt.mode." + value.toLowerCase(Locale.ROOT));
        if ("encounter".equals(category)) {
            if ("random".equals(value)) return Text.translatable("btt.admin.random");
            if ("none".equals(value)) return Text.translatable("btt.admin.none");
            return Text.translatable("btt.background." + value);
        }

        if ("random".equals(value)) return Text.translatable("btt.admin.random");
        if (value.startsWith("base:") || value.startsWith("rule:")) {
            Rule rule = Rule.byKey(value.substring(5));
            return rule == null ? Text.literal(value) : Text.translatable("btt.rule." + rule.key());
        }
        return Text.translatable("btt.script." + value);
    }

    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xB0000000);
        context.fill(left, top, left + panelWidth, top + 212, 0xF0362819);
        context.fill(left, top, left + panelWidth, top + 2, 0xFFE2B65B);
        context.drawTextWithShadow(textRenderer, title, left + 12, top + 14, 0xF0E8D8);
        if (!settings.selected().isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer, label(settings.category(), settings.selected()),
                    width / 2, top + 65, 0xE2B65B);
        }
        if (choices.isEmpty()) context.drawCenteredTextWithShadow(textRenderer,
                Text.translatable("btt.admin.no_other"), width / 2, top + 110, 0xC5AD7D);
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("btt.admin.ui_hint"),
                width / 2, top + 198, 0xC5AD7D);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override public boolean shouldPause() { return false; }
    @Override public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {}
    @Override public void tick() {
        if (client == null || client.world == null
                || GameWorldComponent.KEY.get(client.world).getGameStatus() != GameWorldComponent.GameStatus.INACTIVE) close();
    }

    private static final class PanelButton extends ButtonWidget {
        private final boolean selected;
        private PanelButton(int x, int y, int width, int height, Text message, boolean selected, PressAction action) {
            super(x, y, width, height, message, action, DEFAULT_NARRATION_SUPPLIER);
            this.selected = selected;
        }
        @Override protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            int border = !active ? 0xFF54422E : selected ? 0xFFE2B65B : isHovered() ? 0xFFC5A36C : 0xFF896735;
            context.fill(getX(), getY(), getX() + width, getY() + height, border);
            context.fill(getX() + 1, getY() + 1, getX() + width - 1, getY() + height - 1,
                    selected ? 0xFF785725 : isHovered() && active ? 0xFF674B28 : 0xFF49351F);
            drawScrollableText(context, MinecraftClient.getInstance().textRenderer, 3,
                    !active ? 0x9A8666 : selected ? 0xFFF0C9 : 0xF2DFC0);
        }
    }
}
