package dev.hae.btt.client.ui;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.hae.btt.encounter.Backgrounds;
import dev.hae.btt.game.BeforeTheTerminalGameMode;
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
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public final class AdminSettingsScreen extends Screen {
    private static final int PAGE_SIZE = 12;
    private final AdminMenuS2CPacket settings;
    private final BeforeTheTerminalGameMode.Mode mode;
    private final List<String> choices = new ArrayList<>();
    private int page;
    private int panelWidth;
    private int left;
    private int top;

    public AdminSettingsScreen(AdminMenuS2CPacket settings) {
        super(Text.translatable("btt.admin.title"));
        this.settings = settings;
        this.mode = BeforeTheTerminalGameMode.Mode.valueOf(settings.mode());
        switch (settings.category()) {
            case "mode" -> { for (var mode : BeforeTheTerminalGameMode.Mode.displayOrder()) choices.add(mode.name()); }
            case "script" -> {
                if (mode.usesScript()) {
                    choices.add("random");
                    for (var script : Scripts.ALL) choices.add(script.id());
                }
            }
            case "encounter" -> {
                choices.addAll(List.of("random", "none"));
                Backgrounds.IMPLEMENTED.stream().sorted().forEach(choices::add);
            }
        }
    }

    @Override protected void init() {
        panelWidth = Math.min(360, width - 24);
        left = (width - panelWidth) / 2;
        top = Math.max(8, (height - 212) / 2);
        int columnWidth = (panelWidth - 32) / 3;
        String[] categories = {"mode", "script", "encounter"};
        for (int i = 0; i < categories.length; i++) {
            String category = categories[i];
            addDrawableChild(new PanelButton(left + 12 + i * columnWidth, top + 33, columnWidth - 4, 23,
                    Text.translatable("btt.admin." + category), category.equals(settings.category()),
                    button -> ClientPlayNetworking.send(new AdminSettingC2SPacket(category, ""))));
        }
        int begin = page * PAGE_SIZE;
        for (int i = begin; i < Math.min(choices.size(), begin + PAGE_SIZE); i++) {
            String value = choices.get(i);
            int index = i - begin;
            var button = new PanelButton(left + 12 + index % 3 * columnWidth, top + 83 + index / 3 * 23,
                    columnWidth - 4, 20, label(value), value.equals(settings.selected()),
                    ignored -> ClientPlayNetworking.send(new AdminSettingC2SPacket(settings.category(), value)));
            button.active = !"encounter".equals(settings.category()) || ServerSettings.modeAllows(mode, value);
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

    private Text label(String value) {
        if ("mode".equals(settings.category())) return Text.translatable("btt.mode." + value.toLowerCase(Locale.ROOT));
        if ("random".equals(value)) return Text.translatable("btt.admin.random");
        if ("none".equals(value)) return Text.translatable("script".equals(settings.category()) ? "btt.admin.no_script" : "btt.admin.none");
        return Text.translatable(("script".equals(settings.category()) ? "btt.script." : "btt.background.") + value);
    }

    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xB0000000);
        context.fill(left, top, left + panelWidth, top + 212, 0xF0362819);
        context.fill(left, top, left + panelWidth, top + 2, 0xFFE2B65B);
        context.drawTextWithShadow(textRenderer, title, left + 12, top + 14, 0xF0E8D8);
        context.drawCenteredTextWithShadow(textRenderer, label(settings.selected()), width / 2, top + 65, 0xE2B65B);
        if (choices.isEmpty()) context.drawCenteredTextWithShadow(textRenderer,
                Text.translatable("btt.admin.no_script"), width / 2, top + 110, 0xC5AD7D);
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("btt.admin.ui_hint"), width / 2, top + 186, 0xC5AD7D);
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
