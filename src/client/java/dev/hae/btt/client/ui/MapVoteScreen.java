package dev.hae.btt.client.ui;

import dev.hae.btt.component.MapVoteState;
import dev.hae.btt.network.MapVoteC2SPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;










public class MapVoteScreen extends Screen {

    private static boolean autoOpened = false;
    private static final int ROW_HEIGHT = 26;
    private static final int ROW_GAP = 4;
    private static final int MAX_WIDTH = 320;



    private static final int BAR_GOLD = 0xFFF2B84B;
    private static final int BAR_GOLD_TRACK = 0x33F2B84B;

    private static final int ROW_ROLLING = 0xCC4A3A16;

    private static final int ROULETTE_TICKS = 6 * 20;

    private static final int ROULETTE_SNAP_TICKS = 12;

    public MapVoteScreen() {
        super(Text.translatable("btt.mapvote.title"));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }






    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }
        if (dev.hae.btt.client.BttClient.abilityBind != null
                && dev.hae.btt.client.BttClient.abilityBind.matchesKey(keyCode, scanCode)) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private MapVoteState state() {
        if (client == null || client.world == null) return null;
        return MapVoteState.KEY.get(client.world);
    }

    private int rowTop(int index) {
        int total = 0;
        MapVoteState st = state();
        if (st != null) total = st.ids.size();
        int blockHeight = total * ROW_HEIGHT + Math.max(0, total - 1) * ROW_GAP;
        return (this.height - blockHeight) / 2 + 20 + index * (ROW_HEIGHT + ROW_GAP);
    }







    private static int rollIndex(float remaining, int selected, int total) {
        if (total <= 0) return -1;
        if (remaining <= ROULETTE_SNAP_TICKS) return Math.floorMod(selected, total);
        float progress = Math.min(1f, (remaining - ROULETTE_SNAP_TICKS) / (ROULETTE_TICKS - ROULETTE_SNAP_TICKS));
        int steps = (int) Math.ceil(total * 4f * progress * progress * progress);
        return Math.floorMod(selected - steps, total);
    }





    private static int[] pool(MapVoteState st) {
        java.util.List<Integer> out = new java.util.ArrayList<>();
        for (int i = 0; i < st.ids.size(); i++) {
            if (i >= st.eligible.length || st.eligible[i]) out.add(i);
        }
        if (out.isEmpty()) {
            for (int i = 0; i < st.ids.size(); i++) out.add(i);
        }
        int[] arr = new int[out.size()];
        for (int i = 0; i < arr.length; i++) arr[i] = out.get(i);
        return arr;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        this.renderBackground(ctx, mouseX, mouseY, delta);
        MapVoteState st = state();
        ctx.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xF2DFC0);
        if (st == null || !st.active || st.ids.isEmpty()) {
            ctx.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable("btt.mapvote.idle").formatted(Formatting.GRAY),
                    this.width / 2, this.height / 2, 0xAAAAAA);
            return;
        }

        int seconds = Math.max(0, st.ticks / 20);
        Text hint = st.roulette
                ? Text.translatable("btt.mapvote.roulette").formatted(Formatting.GOLD)
                : Text.translatable("btt.mapvote.hint", seconds).formatted(Formatting.GRAY);
        ctx.drawCenteredTextWithShadow(this.textRenderer, hint, this.width / 2, 34, 0xC5AD7D);

        int x0 = (this.width - MAX_WIDTH) / 2;
        int own = st.votes.getOrDefault(this.client.player == null ? null : this.client.player.getUuid(), -1);
        int totalVotes = 0;
        for (int c : st.counts) totalVotes += Math.max(0, c);


        int[] pool = pool(st);
        int rolling = -1;
        if (st.roulette) {
            int selPos = 0;
            for (int i = 0; i < pool.length; i++) {
                if (pool[i] == st.selected) { selPos = i; break; }
            }
            float remaining = Math.max(0f, st.rouletteTicks - (client.world.getTime() - st.rouletteUpdatedAt) - delta);
            rolling = pool[rollIndex(remaining, selPos, pool.length)];
        }
        boolean landed = st.roulette && st.rouletteTicks <= ROULETTE_SNAP_TICKS;

        for (int i = 0; i < st.ids.size(); i++) {
            int y = rowTop(i);

            boolean ok = i >= st.eligible.length || st.eligible[i];
            boolean mine = i == own;
            boolean selected = st.roulette && i == st.selected;
            boolean rolled = i == rolling;

            int bg = !ok ? 0xFF2B2118
                    : rolled ? ROW_ROLLING
                    : selected && landed ? 0xCC745329 : 0xAA49351F;
            ctx.fill(x0, y, x0 + MAX_WIDTH, y + ROW_HEIGHT, bg);
            if (mine) {
                ctx.fill(x0 - 1, y - 1, x0 + MAX_WIDTH + 1, y, BAR_GOLD);
                ctx.fill(x0 - 1, y + ROW_HEIGHT, x0 + MAX_WIDTH + 1, y + ROW_HEIGHT + 1, BAR_GOLD);
                ctx.fill(x0 - 1, y - 1, x0, y + ROW_HEIGHT + 1, BAR_GOLD);
                ctx.fill(x0 + MAX_WIDTH, y - 1, x0 + MAX_WIDTH + 1, y + ROW_HEIGHT + 1, BAR_GOLD);
            }
            if (rolled) {

                ctx.fill(x0, y, x0 + MAX_WIDTH, y + 1, BAR_GOLD);
                ctx.fill(x0, y + ROW_HEIGHT - 1, x0 + MAX_WIDTH, y + ROW_HEIGHT, BAR_GOLD);
                ctx.fill(x0, y, x0 + 1, y + ROW_HEIGHT, BAR_GOLD);
                ctx.fill(x0 + MAX_WIDTH - 1, y, x0 + MAX_WIDTH, y + ROW_HEIGHT, BAR_GOLD);
            }
            ctx.drawTextWithShadow(this.textRenderer, st.names.get(i), x0 + 6, y + 4, ok ? 0xFFFFFF : 0x666666);
            if (i < st.descs.size() && !st.descs.get(i).isEmpty()) {
                ctx.drawTextWithShadow(this.textRenderer,
                        Text.literal(st.descs.get(i)).formatted(Formatting.GRAY), x0 + 6, y + 15,
                        ok ? 0x999999 : 0x4A4A4A);
            }
            int votes = i < st.counts.length ? Math.max(0, st.counts[i]) : 0;
            if (!ok) {

                Text reason = Text.translatable("btt.mapvote.ineligible").formatted(Formatting.DARK_GRAY);
                ctx.drawTextWithShadow(this.textRenderer, reason,
                        x0 + MAX_WIDTH - 6 - this.textRenderer.getWidth(reason), y + 4, 0x777777);
                continue;
            }
            String label = votes + (mine ? "  ✔" : "");
            ctx.drawTextWithShadow(this.textRenderer, label,
                    x0 + MAX_WIDTH - 6 - this.textRenderer.getWidth(label), y + 4, mine ? 0xFFFFCC55 : 0xC5AD7D);

            ctx.fill(x0 + 6, y + ROW_HEIGHT - 4, x0 + MAX_WIDTH - 6, y + ROW_HEIGHT - 2, BAR_GOLD_TRACK);
            if (totalVotes > 0) {
                int bar = (int) ((MAX_WIDTH - 12) * (votes / (float) totalVotes));
                ctx.fill(x0 + 6, y + ROW_HEIGHT - 4, x0 + 6 + bar, y + ROW_HEIGHT - 2,
                        selected && landed ? 0xFFFFFF66 : BAR_GOLD);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        MapVoteState st = state();
        if (st != null && st.active && !st.roulette && button == 0) {
            int x0 = (this.width - MAX_WIDTH) / 2;
            if (mouseX >= x0 && mouseX <= x0 + MAX_WIDTH) {
                for (int i = 0; i < st.ids.size(); i++) {

                    if (i < st.eligible.length && !st.eligible[i]) continue;
                    int y = rowTop(i);
                    if (mouseY >= y && mouseY <= y + ROW_HEIGHT) {
                        ClientPlayNetworking.send(new MapVoteC2SPacket(i));
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }


    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, 0x88000000);
    }


    public List<String> debugIds() {
        MapVoteState st = state();
        return st == null ? List.of() : st.ids;
    }





    public static void tick(net.minecraft.client.MinecraftClient client) {
        if (client == null || client.world == null || client.player == null) return;
        MapVoteState st = MapVoteState.KEY.get(client.world);
        if (st == null) return;
        if (!st.active) {
            autoOpened = false;
            if (client.currentScreen instanceof MapVoteScreen) client.setScreen(null);
            return;
        }




        if (!autoOpened) {
            autoOpened = true;
            if (!(client.currentScreen instanceof MapVoteScreen)) client.setScreen(new MapVoteScreen());
        }
    }
}
