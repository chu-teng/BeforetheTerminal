package dev.hae.btt.client.ui;

import dev.doctor4t.wathe.api.Role;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.network.LetterC2SPacket;
import dev.hae.btt.network.SelectC2SPacket;
import dev.hae.btt.role.GuessPool;
import dev.hae.btt.role.Roles;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;















public class RolePickScreen extends Screen {
    private static final int ROW_H = 23;
    private static final int BTN_H = 20;
    private static final int COL_GAP = 4;
    private static final int FOOTER = 28;


    public record Choice(String value, Text label, int color) {
        public static Choice of(Role role) {
            return new Choice(role.identifier().getPath(), SeatAssignment.displayName(role), role.color());
        }
    }

    private sealed interface Row permits HeaderRow, ChoiceRow {}

    private record HeaderRow(Text text) implements Row {}

    private record ChoiceRow(List<Choice> choices) implements Row {}


    private final List<Choice> camps;
    private final List<Choice> identities;
    private final Consumer<Choice> onPick;
    @Nullable
    private final Screen returnTo;

    private List<Row> rows = List.of();
    private List<List<Row>> pages = List.of();
    private final List<List<ChoiceButton>> pageButtons = new ArrayList<>();
    private int columns = 8;
    private int rowsPerPage = 5;
    private int page;

    public RolePickScreen(Text title, List<Choice> camps, List<Choice> identities,
                          Consumer<Choice> onPick, @Nullable Screen returnTo) {
        super(title);
        this.camps = List.copyOf(camps);
        this.identities = List.copyOf(identities);
        this.onPick = onPick;
        this.returnTo = returnTo;
    }









    public static void openForAbility(MinecraftClient client, Role ability, UUID target, @Nullable Screen returnTo) {
        if (client.player == null || client.world == null) return;
        List<Choice> pool = GuessPool.candidates(ability, GuessPool.scriptOf(client.world))
                .stream().filter(role -> ability != Roles.GAMBLER || !java.util.Arrays.asList(
                        dev.hae.btt.component.PlayerState.KEY.get(client.player).gamblerAttempted.split(","))
                        .contains(role.identifier().toString())).map(Choice::of).toList();
        client.setScreen(new RolePickScreen(Text.translatable("btt.pick.title"), List.of(), pool,
                choice -> {
                    ClientPlayNetworking.send(new SelectC2SPacket(target, choice.value()));
                    client.setScreen(null);
                }, returnTo));
    }





    public static void openForLetter(MinecraftClient client, UUID target, @Nullable Screen returnTo) {
        if (client.player == null || client.world == null) return;
        List<Choice> camps = new ArrayList<>();
        for (String camp : Roles.LETTER_CAMPS) {
            String key = Roles.campLangKey(camp);
            camps.add(new Choice(camp, Text.translatable(key == null ? "btt.camp.passenger" : key),
                    Roles.campColor(camp)));
        }
        List<Choice> catalogue = GuessPool.catalogue(GuessPool.scriptOf(client.world))
                .stream().map(Choice::of).toList();
        client.setScreen(new RolePickScreen(Text.translatable("btt.pick.title"), camps, catalogue,
                choice -> {
                    ClientPlayNetworking.send(new LetterC2SPacket(target, choice.value(), -1));
                    client.setScreen(returnTo);
                }, returnTo));
    }



    private int gridTop() {
        return 34;
    }


    private static int evenColumns(int width) {
        int c = MathHelper.clamp(width / 72, 6, 10);
        return (c % 2 == 0) ? c : c - 1;
    }

    private static int buttonWidth(int width, int columns) {
        int usable = width - 40 - (columns - 1) * COL_GAP;
        return Math.max(40, Math.min(72, usable / columns));
    }

    private static List<Row> buildRows(List<Choice> camps, List<Choice> identities, int columns) {
        List<Row> out = new ArrayList<>();

        if (!camps.isEmpty()) {
            out.add(new HeaderRow(Text.translatable("btt.pick.camp")));
            List<Choice> campBuffer = new ArrayList<>();
            for (Choice camp : camps) {
                campBuffer.add(camp);
                if (campBuffer.size() >= columns) {
                    out.add(new ChoiceRow(List.copyOf(campBuffer)));
                    campBuffer.clear();
                }
            }
            if (!campBuffer.isEmpty()) out.add(new ChoiceRow(List.copyOf(campBuffer)));
        }
        var client = MinecraftClient.getInstance();
        if (!camps.isEmpty() && client.world != null
                && dev.hae.btt.game.GameModes.modeOf(dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(client.world))
                    == dev.hae.btt.game.BeforeTheTerminalGameMode.Mode.DRAFT) {
            out.add(new HeaderRow(Text.translatable("btt.pick.known")));
            var known = java.util.Arrays.stream(dev.hae.btt.component.DraftState.KEY.get(client.world).knownRoles.split(","))
                    .map(Roles::byId).filter(java.util.Objects::nonNull).distinct()
                    .sorted(java.util.Comparator.comparingInt(dev.hae.btt.game.Scripts::number))
                    .map(Choice::of).toList();
            for (int i = 0; i < known.size(); i += columns)
                out.add(new ChoiceRow(known.subList(i, Math.min(i + columns, known.size()))));
        }

        List<Choice> buffer = new ArrayList<>();
        String currentCategory = null;
        for (Choice choice : identities.stream().sorted(java.util.Comparator.comparing(c -> {
            Role role = Roles.resolve(c.value());
            return dev.hae.btt.game.Scripts.number(role);
        })).toList()) {
            Role role = Roles.resolve(choice.value());
            String category = role == null ? "btt.guide.neutrals.title" : categoryKey(role);
            if (!category.equals(currentCategory)) {
                if (!buffer.isEmpty()) {
                    out.add(new ChoiceRow(List.copyOf(buffer)));
                    buffer.clear();
                }
                currentCategory = category;
                out.add(new HeaderRow(Text.translatable(category)));
            }
            buffer.add(choice);
            if (buffer.size() >= columns) {
                out.add(new ChoiceRow(List.copyOf(buffer)));
                buffer.clear();
            }
        }
        if (!buffer.isEmpty()) out.add(new ChoiceRow(List.copyOf(buffer)));
        return List.copyOf(out);
    }

    private static List<List<Row>> paginate(List<Row> rows, int rowsPerPage) {
        List<List<Row>> out = new ArrayList<>();
        List<Row> current = new ArrayList<>();
        for (Row row : rows) {
            if (row instanceof HeaderRow && current.size() == rowsPerPage - 1) {
                out.add(List.copyOf(current)); current.clear();
            }
            current.add(row);
            if (current.size() >= rowsPerPage) {
                out.add(List.copyOf(current));
                current.clear();
            }
        }
        if (!current.isEmpty()) out.add(List.copyOf(current));
        if (out.isEmpty()) out.add(List.of());
        return List.copyOf(out);
    }


    private static String categoryKey(Role role) {
        Roles.Kind kind = Roles.kindOf(role);
        if (kind == null) return "btt.guide.neutrals.title";
        return switch (kind) {
            case ENFORCER -> "btt.guide.passengers.执法乘客.title";



            case CIVILIAN -> "btt.guide.passengers.平民乘客.title";
            case PRINCIPAL -> "btt.guide.killers.主犯凶手.title";
            case ACCOMPLICE -> "btt.guide.killers.从犯凶手.title";
            case LONE -> "btt.guide.neutrals.独行中立.title";
            case OUTSIDER -> "btt.guide.neutrals.外人中立.title";
            case MAD -> "btt.guide.neutrals.狂人中立.title";
        };
    }

    @Override
    protected void init() {
        this.columns = evenColumns(this.width);


        this.rowsPerPage = Math.max(3, (this.height - gridTop() - FOOTER) / ROW_H);
        this.rows = buildRows(this.camps, this.identities, this.columns);
        this.pages = paginate(this.rows, this.rowsPerPage);
        this.page = MathHelper.clamp(this.page, 0, this.pages.size() - 1);

        this.pageButtons.clear();
        int btnW = buttonWidth(this.width, this.columns);
        int gridW = this.columns * btnW + (this.columns - 1) * COL_GAP;
        int gridLeft = (this.width - gridW) / 2;
        for (int p = 0; p < this.pages.size(); p++) {
            List<ChoiceButton> buttons = new ArrayList<>();
            int rowIndex = 0;
            for (Row row : this.pages.get(p)) {
                int y = gridTop() + rowIndex * ROW_H;
                rowIndex++;
                if (row instanceof ChoiceRow choiceRow) {
                    for (int c = 0; c < choiceRow.choices().size(); c++) {
                        ChoiceButton button = new ChoiceButton(choiceRow.choices().get(c),
                                gridLeft + c * (btnW + COL_GAP), y, btnW);
                        button.visible = (p == this.page);
                        this.addDrawableChild(button);
                        buttons.add(button);
                    }
                }
            }
            this.pageButtons.add(buttons);
        }

        int footerY = this.height - 22;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("◀"), b -> turn(-1))
                .dimensions(this.width / 2 - 56, footerY, 20, 16).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("▶"), b -> turn(1))
                .dimensions(this.width / 2 + 36, footerY, 20, 16).build());
    }

    private void turn(int delta) {
        setPage(Math.floorMod(this.page + delta, this.pages.size()));
    }

    private void setPage(int target) {
        int clamped = MathHelper.clamp(target, 0, this.pages.size() - 1);
        if (clamped == this.page) return;
        this.page = clamped;
        for (int p = 0; p < this.pageButtons.size(); p++) {
            boolean visible = (p == this.page);
            for (ChoiceButton button : this.pageButtons.get(p)) button.visible = visible;
        }
    }

    @Override public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {}

    @Override public boolean shouldPause() { return false; }



    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        switch (keyCode) {
            case 263 -> {
                turn(-1);
                return true;
            }
            case 262 -> {
                turn(1);
                return true;
            }
            case 268 -> {
                setPage(0);
                return true;
            }
            case 269 -> {
                setPage(this.pages.size() - 1);
                return true;
            }
            default -> {
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }


    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(this.returnTo);
    }



    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xC02B2013);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFE2CD9B);

        context.drawCenteredTextWithShadow(this.textRenderer, btt$scriptLine(),
                this.width / 2, 21, 0xFFC5AD7D);

        int rowIndex = 0;
        for (Row row : this.pages.get(this.page)) {
            int y = gridTop() + rowIndex * ROW_H;
            rowIndex++;
            if (row instanceof HeaderRow header) {
                context.drawTextWithShadow(this.textRenderer, header.text(), 20, y + 4, 0xFFEFE3C8);
            }
        }

        if (this.pages.size() > 1) {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable("btt.pick.page", this.page + 1, this.pages.size()),
                    this.width / 2, this.height - 18, 0xFFF1D9B0);
        }
        if (this.camps.isEmpty() && this.identities.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("btt.pick.empty"),
                    this.width / 2, this.height / 2, 0xFFAA8866);
        }
    }


    private Text btt$scriptLine() {
        if (client == null || client.world == null) return Text.empty();
        dev.hae.btt.game.Scripts.Script script = dev.hae.btt.game.Scripts.byId(
                dev.hae.btt.component.GameState.KEY.get(this.client.world).script);
        if (script == null) return Text.empty();
        return Text.translatable("btt.script.announce", Text.translatable("btt.script." + script.id()));
    }


    private class ChoiceButton extends ClickableWidget {
        private final Choice choice;

        ChoiceButton(Choice choice, int x, int y, int width) {
            super(x, y, width, BTN_H, choice.label());
            this.choice = choice;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            int background = this.isHovered() ? 0xFF745329 : 0xFF49351F;
            context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), background);
            context.fill(getX(), getY(), getX() + getWidth(), getY() + 1, 0x55FFFFFF);
            Text label = textRenderer.getWidth(getMessage()) <= getWidth() - 4
                    ? getMessage()
                    : Text.literal(textRenderer.trimToWidth(getMessage().getString(),
                            Math.max(8, getWidth() - 4 - textRenderer.getWidth("…"))) + "…");
            context.drawCenteredTextWithShadow(textRenderer, label,
                    getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, this.choice.color());
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            if (onPick != null) onPick.accept(this.choice);
        }

        @Override
        protected void appendClickableNarrations(
                net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {
            this.appendDefaultNarrations(builder);
        }
    }
}
