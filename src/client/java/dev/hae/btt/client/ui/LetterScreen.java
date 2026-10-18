package dev.hae.btt.client.ui;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.hae.btt.component.GameState;
import dev.hae.btt.component.LetterState;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.network.LetterC2SPacket;
import dev.hae.btt.role.Roles;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

















public class LetterScreen extends Screen {
    @Override public boolean shouldPause() { return false; }
    private static final int COLS = 6;

    private static final int MIN_CELL_H = 44;
    private static final int MAX_CELL_H = 96;
    private static final int HEAD = 16;
    private static final int DEAD_COLOR = 0xE10000;
    private static final int DEAD_SHADOW = 0x550000;
    private static final int MISSING_COLOR = 0xFFCC00;
    private static final int MISSING_SHADOW = 0x554400;

    private static final int PASSENGER_GREEN = 0x4EDD35;
    private static final int KILLER_RED = 0xDD0000;
    private static final int NEUTRAL_CYAN = 0x00FFFF;
    private static final int COUNT_WHITE = 0xFFFFFF;


    private static final class Entry {
        final UUID uuid;
        final int number;
        final PlayerListEntry playerEntry;
        int status;

        boolean voted;

        Entry(UUID uuid, int number, PlayerListEntry playerEntry, int status) {
            this.uuid = uuid;
            this.number = number;
            this.playerEntry = playerEntry;
            this.status = status;
        }
    }

    private final List<Entry> entries;
    private final List<List<ClickableWidget>> pageWidgets = new ArrayList<>();
    private int page;
    private int pageCount = 1;
    private int rowsPerPage = 1;
    private int cellW = 60;
    private int cellH = MIN_CELL_H;

    private LetterScreen(List<Entry> entries) {
        super(Text.translatable("btt.letter.title"));
        this.entries = entries;
    }






    public static boolean open(MinecraftClient client) {
        if (client.player == null || client.world == null) return false;
        if (!SeatAssignment.isBttMode(client.world)) return false;
        if (!GameWorldComponent.KEY.get(client.world).isRunning()) return false;
        List<Entry> list = snapshot(client);
        if (list.isEmpty()) return false;
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(new dev.hae.btt.network.LetterC2SPacket(client.player.getUuid(), "", -2));
        client.setScreen(new LetterScreen(list));
        return true;
    }




    private static boolean voting(MinecraftClient client) {
        if (client == null || client.world == null) return false;
        return dev.hae.btt.component.DebateState.KEY.get(client.world).phase
                == dev.hae.btt.component.DebateState.PHASE_VOTING;
    }


    private static String myVote(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null) return "";
        if (!voting(client)) return "";
        String csv = dev.hae.btt.component.DebateState.KEY.get(client.world).votes;
        if (csv == null || csv.isEmpty()) return "";
        String me = client.player.getUuidAsString();
        for (String part : csv.split(",")) {
            int c = part.indexOf(':');
            if (c > 0 && part.substring(0, c).equals(me)) return part.substring(c + 1);
        }
        return "";
    }



    private static List<Entry> snapshot(MinecraftClient client) {
        GameState game = GameState.KEY.get(client.world);
        Map<UUID, Integer> numbers = parseNumbers(game == null ? "" : game.letterNumbers);
        LetterState state = LetterState.KEY.get(client.player);
        List<Entry> list = new ArrayList<>();
        for (Map.Entry<UUID, Integer> numbered : numbers.entrySet()) {
            PlayerListEntry ple = client.getNetworkHandler() == null
                    ? null : client.getNetworkHandler().getPlayerListEntry(numbered.getKey());
            if (ple == null) continue;
            String uuid = numbered.getKey().toString();

            int status = state == null ? LetterState.STATUS_ALIVE
                    : state.status.getOrDefault(uuid, LetterState.STATUS_ALIVE);
            Entry entry = new Entry(numbered.getKey(), numbered.getValue(), ple, status);
            entry.voted = uuid.equals(myVote(client));
            list.add(entry);
        }
        list.sort(Comparator.comparingInt(e -> e.number));
        return list;
    }

    private static Map<UUID, Integer> parseNumbers(String csv) {
        Map<UUID, Integer> out = new HashMap<>();
        if (csv == null || csv.isEmpty()) return out;
        for (String part : csv.split(",")) {
            int colon = part.indexOf(':');
            if (colon <= 0) continue;
            try {
                out.put(UUID.fromString(part.substring(0, colon)), Integer.parseInt(part.substring(colon + 1)));
            } catch (IllegalArgumentException ignored) {

            }
        }
        return out;
    }



    private int gridTop() {

        return 60;
    }

    @Override
    protected void init() {
        this.page = 0;
        this.pageWidgets.clear();
        this.cellW = MathHelper.clamp((this.width - 24) / COLS, 44, 110);

        int available = this.height - gridTop() - 26;
        int neededRows = Math.max(1, (this.entries.size() + COLS - 1) / COLS);
        int maxRows = Math.max(1, available / MIN_CELL_H);
        this.rowsPerPage = Math.min(neededRows, maxRows);
        this.cellH = MathHelper.clamp(available / this.rowsPerPage, MIN_CELL_H, MAX_CELL_H);

        MinecraftClient mc = MinecraftClient.getInstance();
        LetterState state = LetterState.KEY.get(mc.player);
        String voted = myVote(mc);
        for (Entry entry : this.entries) {
            if (state != null) {
                entry.status = state.status.getOrDefault(entry.uuid.toString(), LetterState.STATUS_ALIVE);
            }
            entry.voted = entry.uuid.toString().equals(voted);
        }
        int perPage = this.rowsPerPage * COLS;
        this.pageCount = Math.max(1, (this.entries.size() + perPage - 1) / perPage);
        int gridLeft = (this.width - this.cellW * COLS) / 2;

        for (int p = 0; p < this.pageCount; p++) {
            List<ClickableWidget> widgets = new ArrayList<>();
            int from = p * perPage;
            int to = Math.min(this.entries.size(), from + perPage);
            for (int idx = from; idx < to; idx++) {
                Entry entry = this.entries.get(idx);
                int inPage = idx - from;
                int cx = gridLeft + (inPage % COLS) * this.cellW;
                int cy = gridTop() + (inPage / COLS) * this.cellH;

                HeadButton head = new HeadButton(entry, cx + (this.cellW - HEAD) / 2, cy);
                head.visible = (p == 0);
                this.addDrawableChild(head);
                widgets.add(head);

                RoleButton selectedRole = new RoleButton(entry, cx + 2, cy + HEAD + 20, this.cellW - 4, 10);
                selectedRole.visible = (p == 0);
                this.addDrawableChild(selectedRole);
                widgets.add(selectedRole);
            }
            this.pageWidgets.add(widgets);
        }

        if (this.pageCount > 1) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("◀"), b -> turn(-1))
                    .dimensions(this.width / 2 - 56, this.height - 20, 20, 16).build());
            this.addDrawableChild(ButtonWidget.builder(Text.literal("▶"), b -> turn(1))
                    .dimensions(this.width / 2 + 36, this.height - 20, 20, 16).build());
        }
    }

    private void turn(int delta) {
        setPage(Math.floorMod(this.page + delta, this.pageCount));
    }

    private void setPage(int target) {
        int clamped = MathHelper.clamp(target, 0, this.pageCount - 1);
        if (clamped == this.page) return;
        this.page = clamped;
        for (int p = 0; p < this.pageWidgets.size(); p++) {
            boolean visible = (p == this.page);
            for (ClickableWidget widget : this.pageWidgets.get(p)) widget.visible = visible;
        }
    }

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
            default -> {
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }



    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xB0100C08);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 6, 0xFFF2B84B);

        Text premise = dev.hae.btt.client.util.AnnouncePremise.line();
        if (!premise.getString().isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, premise, this.width / 2, 17, 0xFFEFE3C8);
        }

        Text counts = countsLine(false);
        if (!counts.getString().isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, counts, this.width / 2, 28, 0xFFEFE3C8);
        }

        Text remaining = countsLine(true);
        if (!remaining.getString().isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, remaining, this.width / 2, 39, 0xFFEFE3C8);
        }
        if (this.pageCount > 1) {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable("btt.pick.page", this.page + 1, this.pageCount),
                    this.width / 2, this.height - 18, 0xFFF1D9B0);
        }
    }


    private static int categoryColor(String langKey) {
        if (langKey.startsWith("btt.guide.killers")) return KILLER_RED;
        if (langKey.startsWith("btt.guide.passengers")) return PASSENGER_GREEN;
        return NEUTRAL_CYAN;
    }






    private Text countsLine(boolean remaining) {
        var counts = SeatAssignment.categoryCounts(this.entries.size());
        if (counts.isEmpty()) return Text.empty();
        Map<String, Integer> marked = remaining ? markedByCategory() : Map.of();
        MutableText line = Text.empty();
        boolean first = true;
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            if (!first) line.append(" · ");
            first = false;
            int value = remaining ? e.getValue() - marked.getOrDefault(e.getKey(), 0) : e.getValue();
            line.append(Text.translatable(e.getKey()).withColor(categoryColor(e.getKey())))
                    .append(" ").append(Text.literal(String.valueOf(value)).withColor(COUNT_WHITE));
        }
        return Text.translatable(remaining ? "btt.letter.remaining" : "btt.letter.counts", line);
    }


    private Map<String, Integer> markedByCategory() {
        Map<String, Integer> out = new HashMap<>();
        for (Entry entry : this.entries) {
            String mark = markOf(entry);
            if (mark.isEmpty() || Roles.isCampValue(mark)) continue;
            String key = SeatAssignment.categoryKeyOf(Roles.resolve(mark));
            if (key != null) out.merge(key, 1, Integer::sum);
        }
        return out;
    }

    @Nullable
    private static Text markLabel(String mark) {
        if (mark == null || mark.isEmpty()) return null;
        String campKey = Roles.campLangKey(mark);
        if (campKey != null) return Text.translatable(campKey);
        Role role = Roles.resolve(mark);
        return role == null ? null : SeatAssignment.displayName(role);
    }

    private static int markColor(String mark) {
        int camp = Roles.campColor(mark);
        if (camp != -1) return camp;
        Role role = Roles.resolve(mark);
        return role == null ? 0xFF888888 : role.color();
    }


    private static String markOf(Entry entry) {
        LetterState state = LetterState.KEY.get(MinecraftClient.getInstance().player);
        return state == null ? "" : state.marks.getOrDefault(entry.uuid.toString(), "");
    }




    private class HeadButton extends ClickableWidget {
        private final Entry entry;

        HeadButton(Entry entry, int x, int y) {
            super(x, y, HEAD, HEAD, Text.literal(entry.playerEntry.getProfile().getName()));
            this.entry = entry;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {

            context.drawGuiTexture(dev.doctor4t.wathe.Wathe.id("gui/shop_slot"),
                    getX() - 7, getY() - 7, 30, 30);
            PlayerSkinDrawer.draw(context, entry.playerEntry.getSkinTextures().texture(), getX(), getY(), HEAD);
            drawStatusCross(context, this);
            if (this.isHovered()) {
                context.drawTooltip(textRenderer, Text.literal("#" + entry.number + " " + entry.playerEntry.getProfile().getName()),
                        mouseX, mouseY);
            }
            int centerX = getX() + HEAD / 2;
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("#" + entry.number), centerX, getY() + HEAD + 3, 0xFFB9A88C);
            String name = entry.playerEntry.getProfile().getName();
            if (textRenderer.getWidth(name) > cellW - 4) {
                name = textRenderer.trimToWidth(name, Math.max(8, cellW - 4 - textRenderer.getWidth("…"))) + "…";
            }
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(name), centerX, getY() + HEAD + 11, 0xFFEFE3C8);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (voting(mc)) {

                if (entry.voted) {
                    entry.voted = false;
                    ClientPlayNetworking.send(new LetterC2SPacket(entry.uuid, "", LetterState.STATUS_ALIVE));
                    return;
                }
                var target = mc.world == null ? null : mc.world.getPlayerByUuid(entry.uuid);
                if (target == null || target.isSpectator() || !target.isAlive()) return;
                for (Entry e : LetterScreen.this.entries) e.voted = false;
                entry.voted = true;
                ClientPlayNetworking.send(new LetterC2SPacket(entry.uuid, "", LetterState.STATUS_DEAD));
                return;
            }
            entry.status = LetterState.nextStatus(entry.status);
            ClientPlayNetworking.send(new LetterC2SPacket(entry.uuid, "", entry.status));
        }

        @Override
        protected void appendClickableNarrations(
                net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {
            this.appendDefaultNarrations(builder);
        }
    }


    private class RoleButton extends ClickableWidget {
        private final Entry entry;

        RoleButton(Entry entry, int x, int y, int width, int height) {
            super(x, y, width, height, Text.translatable("btt.pick.title"));
            this.entry = entry;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            String mark = markOf(this.entry);
            Text label = markLabel(mark);
            int color = label == null ? 0xFF6E6252 : markColor(mark);
            String text = label == null ? "—" : label.getString();
            if (textRenderer.getWidth(text) > getWidth()) {
                text = textRenderer.trimToWidth(text, Math.max(8, getWidth() - textRenderer.getWidth("…"))) + "…";
            }
            int fill = this.isHovered() ? 0x33FFFFFF : 0;
            if (fill != 0) context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), fill);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(text),
                    getX() + getWidth() / 2, getY() + 1, color);
            if (this.isHovered() && label != null) {
                context.drawTooltip(textRenderer, label, mouseX, mouseY);
            }
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            if (Screen.hasShiftDown()) {
                ClientPlayNetworking.send(new LetterC2SPacket(entry.uuid, LetterC2SPacket.CLEAR, -1));
                return;
            }
            RolePickScreen.openForLetter(MinecraftClient.getInstance(), entry.uuid, LetterScreen.this);
        }

        @Override
        protected void appendClickableNarrations(
                net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {
            this.appendDefaultNarrations(builder);
        }
    }





    private void drawStatusCross(DrawContext context, ClickableWidget head) {
        Entry entry = head instanceof HeadButton hb ? hb.entry : null;
        if (entry == null) return;

        if (entry.voted) {
            context.fill(head.getX(), head.getY(), head.getX() + HEAD, head.getY() + HEAD, 0x99AA0000);
        }
        if (entry.status == LetterState.STATUS_ALIVE) return;
        int color = entry.status == LetterState.STATUS_DEAD ? DEAD_COLOR : MISSING_COLOR;
        int shadow = entry.status == LetterState.STATUS_DEAD ? DEAD_SHADOW : MISSING_SHADOW;
        context.getMatrices().push();
        context.getMatrices().scale(2f, 1f, 1f);
        context.getMatrices().translate(head.getX() / 2f + 6, head.getY() + 8f, 0);
        context.drawText(this.textRenderer, "x", -this.textRenderer.getWidth("x") / 2, 0, color, false);
        context.drawText(this.textRenderer, "x", -this.textRenderer.getWidth("x") / 2, 1, shadow, false);
        context.getMatrices().pop();
    }
}
