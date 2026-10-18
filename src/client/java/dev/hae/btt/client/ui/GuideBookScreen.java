package dev.hae.btt.client.ui;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;


public class GuideBookScreen extends Screen {


    private static final int MAX_BOOK_WIDTH = 400;
    private static final int TAB_WIDTH = 48;
    private static final int TAB_HEIGHT = 17;
    private static final int TAB_GAP = 2;
    private static final int PAGE_PADDING = 12;
    private static final int LINE_HEIGHT = 11;

    private static final int GAP_HEADING = 6;

    private static final int GAP_HEADING_BODY = 2;

    private static final int GAP_PARAGRAPH = 4;


    private static final int PAPER = 0xFFFFF0C9;
    private static final int PAPER_EDGE = 0xFFC5A36C;
    private static final int INK = 0xFF392B1B;
    private static final int MUTED = 0xFF8A765D;
    private static final int ROW_HOVER = 0x334C2E25;
    private static final int ROW_SELECTED = 0x553C211C;
    private static final int ACCENT = 0xFFA57228;
    private static final int TAB_TEXT = 0xFFF1D9B0;
    private static final int TAB_SELECTED_BG = 0xFF745329;
    private static final int TAB_BG = 0xFF49351F;
    private static final int BUTTON_BG = 0xFF745329;
    private static final int BUTTON_DISABLED = 0xFF9C8A70;
    private static final int BACKDROP = 0xB0000000;

    private final List<GuideContent.Chapter> chapters = GuideContent.build();


    private int tab = -1;
    private int sectionIndex;
    private int page;
    private int sectionScroll;
    private List<String> sections = new ArrayList<>();

    private record Rendered(Text text, int spaceBefore) {}

    private List<Rendered> contentLines = new ArrayList<>();
    private final List<Integer> pageStarts = new ArrayList<>();
    private int pageCount = 1;

    public GuideBookScreen() {
        super(Text.translatable("btt.guide.title"));
    }



    private int bookWidth() {
        return Math.min(MAX_BOOK_WIDTH, Math.max(100, this.width - 140));
    }

    private int bookLeft() {
        return (this.width - bookWidth()) / 2;
    }

    private int bookTop() {
        return 34;
    }

    private int bookHeight() {

        return Math.min(this.height - bookTop() - 40, 400);
    }

    private int pageWidth() {
        return (bookWidth() - PAGE_PADDING * 3) / 2;
    }






    private int sectionColumnX() {

        return bookLeft() + bookWidth() + 6;
    }

    private int textPageX() {
        return bookLeft() + PAGE_PADDING;
    }

    private int pageTop() {
        return bookTop() + 32;
    }

    private int pageHeight() {
        return Math.max(LINE_HEIGHT * 2, bookHeight() - 64);
    }

    private int visibleSections() {


        return Math.max(1, pageHeight() / (TAB_HEIGHT + TAB_GAP));
    }


    private int sectionRowY(int index, int start) {
        return bookTop() + (index - start) * (TAB_HEIGHT + TAB_GAP);
    }


    private int tabX(int index) {
        return bookLeft() - TAB_WIDTH - 6;
    }

    private int tabY(int index) {
        return bookTop() + index * (TAB_HEIGHT + TAB_GAP);
    }

    @Override public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {}



    @Override
    protected void init() {
        if (this.tab >= 0) {
            selectTab(this.tab, true);
            return;
        }
        if (btt$jumpToOwnRole()) return;
        btt$openFirstSection();
    }





    private void btt$openFirstSection() {
        for (int i = 0; i < chapters.size(); i++) {
            if (!chapters.get(i).id().equals("rules")) continue;
            selectTab(i, true);
            this.sectionIndex = 0;
            this.page = 0;
            reflow();
            return;
        }
        reflow();
    }






    private boolean btt$jumpToOwnRole() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.world == null) return false;
        if (!dev.hae.btt.game.SeatAssignment.isBttMode(client.world)) return false;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(client.world);
        if (!gwc.isRunning()) return false;
        Role own = dev.hae.btt.role.Roles.byId(
                dev.hae.btt.component.PlayerState.KEY.get(client.player).observedGuideRole);
        if (own == null) return false;
        String[] loc = GuideContent.locate(own);
        if (loc == null) return false;
        int tabIndex = -1;
        for (int i = 0; i < chapters.size(); i++) {
            if (chapters.get(i).id().equals(loc[0])) {
                tabIndex = i;
                break;
            }
        }
        if (tabIndex < 0) return false;
        selectTab(tabIndex, true);
        int idx = this.sections.indexOf(loc[1]);
        if (idx < 0) return true;
        this.sectionIndex = idx;
        this.page = 0;
        reflow();
        return true;
    }

    private void selectTab(int index, boolean keepEntry) {
        this.tab = Math.max(0, Math.min(index, chapters.size() - 1));
        if (!keepEntry) this.sectionIndex = 0;
        this.page = 0;
        this.sectionScroll = 0;
        GuideContent.Chapter chapter = chapters.get(this.tab);
        this.sections = new ArrayList<>();
        for (GuideContent.Line line : chapter.lines()) {

            if (line.kind() == GuideContent.Kind.SECTION) this.sections.add(line.text());
        }
        reflow();
    }


    private void reflow() {
        List<Rendered> lines = new ArrayList<>();
        int wrapWidth = Math.max(40, bookWidth() - PAGE_PADDING * 2 - 8);
        if (this.tab < 0) {

            lines.add(new Rendered(Text.translatable("btt.guide.preface.title").withColor(ACCENT), 0));
            for (String line : GuideContent.preface()) {
                if (!line.isEmpty()) addWrapped(lines, line, wrapWidth, INK, GAP_PARAGRAPH, false);
            }
        } else if (!sections.isEmpty()) {

            List<GuideContent.Line> chapterLines = chapters.get(tab).lines();
            String want = sections.get(Math.min(sectionIndex, sections.size() - 1));
            boolean inside = false;
            GuideContent.Kind prev = null;
            for (GuideContent.Line line : chapterLines) {
                if (line.kind() == GuideContent.Kind.SECTION) {
                    inside = line.text().equals(want);

                    if (inside) prev = null;
                    continue;
                }
                if (!inside) continue;
                if (line.text().isBlank()) continue;
                int gap = prev == null ? 0
                        : line.kind() == GuideContent.Kind.HEADING ? GAP_HEADING
                        : prev == GuideContent.Kind.HEADING ? GAP_HEADING_BODY
                        : GAP_PARAGRAPH;
                addWrapped(lines, line.text(), wrapWidth, line.color(), gap,
                        line.kind() == GuideContent.Kind.HEADING);
                prev = line.kind();
            }
        } else {
            for (GuideContent.Line line : chapters.get(tab).lines()) {
                String text = line.text();
                Text styled = Text.literal(text).withColor(line.color());
                if (!text.isEmpty()) addWrapped(lines, text, wrapWidth, line.color(), GAP_PARAGRAPH, false);
            }
        }
        this.contentLines = lines;
        paginate();
        this.pageCount = Math.max(1, pageStarts.size());
        this.page = Math.max(0, Math.min(this.page, this.pageCount - 1));
    }


    private void addWrapped(List<Rendered> out, String text, int wrapWidth, int color,
                            int spaceBefore, boolean bold) {
        List<Text> wrapped = wrapText(text, wrapWidth, color, bold);
        for (int i = 0; i < wrapped.size(); i++) {
            out.add(new Rendered(wrapped.get(i), i == 0 ? spaceBefore : 0));
        }
    }


    private void paginate() {
        pageStarts.clear();
        pageStarts.add(0);
        if (contentLines.isEmpty()) return;
        int height = pageHeight();
        int used = LINE_HEIGHT;
        for (int i = 1; i < contentLines.size(); i++) {
            int need = contentLines.get(i).spaceBefore() + LINE_HEIGHT;
            if (used + need > height) {
                pageStarts.add(i);
                used = LINE_HEIGHT;
            } else {
                used += need;
            }
        }
    }



    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {


        super.render(context, mouseX, mouseY, delta);
        context.fill(0, 0, this.width, this.height, BACKDROP);
        int left = bookLeft();
        int top = bookTop();
        int w = bookWidth();
        int h = bookHeight();

        context.fill(left + 3, top + 4, left + w + 3, top + h + 4, 0x55000000);
        context.fill(left, top, left + w, top + h, PAPER);
        context.fill(left - 1, top - 1, left + w + 1, top, PAPER_EDGE);
        context.fill(left - 1, top + h, left + w + 1, top + h + 1, PAPER_EDGE);
        context.fill(left - 1, top, left, top + h, PAPER_EDGE);
        context.fill(left + w, top, left + w + 1, top + h, PAPER_EDGE);


        context.fill(left + PAGE_PADDING, top + 25, left + w - PAGE_PADDING, top + 26, PAPER_EDGE);
        context.fill(left + PAGE_PADDING, top + h - 28, left + w - PAGE_PADDING, top + h - 27, PAPER_EDGE);
        String heading = tab >= 0 ? chapters.get(tab).title().getString() : title.getString();
        if (!sections.isEmpty()) heading += " / " + entryName(sections.get(sectionIndex));
        heading = textRenderer.trimToWidth(heading, w - PAGE_PADDING * 2 - 24);
        context.drawText(textRenderer, heading, left + PAGE_PADDING, top + 10, INK, false);
        renderNavigation(context, mouseX, mouseY);

        for (int i = 0; i < chapters.size(); i++) {
            int x = tabX(i);
            int y = tabY(i);
            boolean selected = i == tab;
            drawButton(context, x, y, chapters.get(i).title().getString(), selected,
                    inside(mouseX, mouseY, x, y, TAB_WIDTH, TAB_HEIGHT));
        }

        renderSectionButtons(context, mouseX, mouseY);
        renderTextPage(context);
        renderClose(context, mouseX, mouseY);
    }

    private void renderSectionButtons(DrawContext context, int mouseX, int mouseY) {
        int x = sectionColumnX();
        if (sections.isEmpty()) {

            return;
        }
        int start = Math.min(sectionScroll, Math.max(0, sections.size() - visibleSections()));
        for (int i = start; i < Math.min(sections.size(), start + visibleSections()); i++) {
            int rowY = sectionRowY(i, start);

            drawButton(context, x, rowY, entryName(sections.get(i)), i == sectionIndex,
                    inside(mouseX, mouseY, x, rowY, TAB_WIDTH, TAB_HEIGHT));
        }
    }

    private void renderTextPage(DrawContext context) {
        int x = textPageX();
        int start = pageStarts.get(Math.min(page, pageStarts.size() - 1));
        int end = page >= pageStarts.size() - 1 ? contentLines.size() : pageStarts.get(page + 1);
        int y = pageTop();
        for (int i = start; i < end; i++) {
            Rendered line = contentLines.get(i);
            if (i != start) y += line.spaceBefore();
            context.drawText(this.textRenderer, line.text(), x + 3, y, INK, false);
            y += LINE_HEIGHT;
        }
        Text footer = Text.literal((page + 1) + " / " + pageCount);
        context.drawText(this.textRenderer, footer,
                bookLeft() + (bookWidth() - this.textRenderer.getWidth(footer)) / 2,
                navY() + 3, MUTED, false);
    }

    private void renderClose(DrawContext context, int mouseX, int mouseY) {

        int x = bookLeft() + bookWidth() - 18;
        int y = bookTop() + 2;
        boolean hovered = inside(mouseX, mouseY, x - 2, y - 2, 16, 16);
        context.fill(x - 2, y - 2, x + 14, y + 14, hovered ? ACCENT : BUTTON_BG);
        context.drawText(this.textRenderer, "x", x + 4, y + 2, TAB_TEXT, false);
    }

    private void renderNavigation(DrawContext context, int mouseX, int mouseY) {
        for (int direction : new int[]{-1, 1}) {
            int x = direction < 0 ? navPrevX() : navNextX();
            boolean enabled = direction < 0 ? page > 0 : page + 1 < pageCount;
            boolean hovered = inside(mouseX, mouseY, x, navY(), 24, 14);
            context.fill(x, navY(), x + 24, navY() + 14,
                    !enabled ? PAPER_EDGE : hovered ? ACCENT : BUTTON_BG);
            context.drawText(textRenderer, direction < 0 ? "<" : ">", x + 9, navY() + 3,
                    enabled ? TAB_TEXT : MUTED, false);
        }
    }

    private int navY() {
        return bookTop() + bookHeight() - PAGE_PADDING - 14;
    }

    private int navPrevX() {
        return bookLeft() + PAGE_PADDING;
    }

    private int navNextX() {
        return bookLeft() + bookWidth() - PAGE_PADDING - 24;
    }


    private void drawButton(DrawContext context, int x, int y, String rawLabel, boolean selected, boolean hovered) {
        context.fill(x, y, x + TAB_WIDTH, y + TAB_HEIGHT,
                selected ? TAB_SELECTED_BG : (hovered ? ACCENT : TAB_BG));
        String label = rawLabel;
        if (this.textRenderer.getWidth(label) > TAB_WIDTH - 4) {
            label = this.textRenderer.trimToWidth(label, TAB_WIDTH - 4);
        }
        context.drawText(this.textRenderer, label,
                x + (TAB_WIDTH - this.textRenderer.getWidth(label)) / 2, y + 6, TAB_TEXT, false);
    }

    private static String entryName(String raw) {
        if (raw.contains("：")) return raw.substring(0, raw.indexOf('：'));
        if (raw.contains(": ")) return raw.substring(0, raw.indexOf(": "));
        return raw;
    }


    private List<Text> wrapText(String text, int maxWidth, int color, boolean bold) {
        List<Text> out = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            String candidate = line.toString() + c;
            if (this.textRenderer.getWidth(candidate) > maxWidth && line.length() > 0) {
                out.add(style(Text.literal(line.toString()).withColor(color), bold));
                line.setLength(0);
            }
            line.append(c);
        }
        if (line.length() > 0) out.add(style(Text.literal(line.toString()).withColor(color), bold));
        if (out.isEmpty()) out.add(style(Text.literal("").withColor(color), bold));
        return out;
    }


    private static Text style(Text text, boolean bold) {
        return bold ? text.copy().formatted(Formatting.BOLD) : text;
    }

    private static boolean inside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }



    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mx = (int) mouseX;
        int my = (int) mouseY;
        if (inside(mx, my, navPrevX(), navY(), 24, 14)) {
            page = Math.max(0, page - 1);
            return true;
        }
        if (inside(mx, my, navNextX(), navY(), 24, 14)) {
            page = Math.min(pageCount - 1, page + 1);
            return true;
        }
        for (int i = 0; i < chapters.size(); i++) {
            if (inside(mx, my, tabX(i), tabY(i), TAB_WIDTH, TAB_HEIGHT)) {
                selectTab(i, false);
                return true;
            }
        }
        if (inside(mx, my, bookLeft() + bookWidth() - 18, bookTop() + 2, 18, 18)) {
            this.close();
            return true;
        }
        if (!sections.isEmpty()) {
            int start = Math.min(sectionScroll, Math.max(0, sections.size() - visibleSections()));
            for (int i = start; i < Math.min(sections.size(), start + visibleSections()); i++) {

                int rowY = sectionRowY(i, start);
                if (inside(mx, my, sectionColumnX(), rowY, TAB_WIDTH, TAB_HEIGHT)) {
                    sectionIndex = i;
                    page = 0;
                    reflow();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int mx = (int) mouseX;

        if (mx >= sectionColumnX()) {
            if (!sections.isEmpty()) {
                sectionScroll = Math.max(0, Math.min(Math.max(0, sections.size() - visibleSections()),
                        sectionScroll - (int) Math.signum(verticalAmount)));
            }
        } else if (mx >= textPageX()) {
            page = Math.max(0, Math.min(pageCount - 1, page - (int) Math.signum(verticalAmount)));
        }
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
