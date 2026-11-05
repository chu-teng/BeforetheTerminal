package dev.hae.btt.client.mixin.game;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.wathe.client.gui.MoodRenderer;
import dev.hae.btt.component.NeedsState;
import dev.hae.btt.game.SeatAssignment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import dev.hae.btt.client.ui.MoodNeedLine;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Mixin(MoodRenderer.class)
public abstract class NeedsHudMixin {

    @Shadow public static float moodOffset;

    @Shadow public static float moodTextWidth;


    private static final Map<String, MoodNeedLine> BTT_LINES = new LinkedHashMap<>();

    private static float bttBarLastWidth = 20.0F;





    @Inject(method = "renderHud", at = @At("TAIL"))
    private static void bttNeedsHud(PlayerEntity player, TextRenderer textRenderer, DrawContext context,
                                    RenderTickCounter tickCounter, CallbackInfo ci) {
        if (player == null || !SeatAssignment.isBttMode(player.getWorld())) return;
        NeedsState st = NeedsState.KEY.get(player);
        float delta = Math.max(0.0F, tickCounter.getLastFrameDuration());
        float fade = 1f - (float) Math.exp(-delta / 2f);
        float slide = 1f - (float) Math.exp(-delta / 3f);
        List<String> active = new ArrayList<>();
        if (st != null && st.kinds != null && !st.kinds.isEmpty()) {
            for (String k : st.kinds.split(",")) if (!k.isEmpty()) active.add(k);
        }


        for (String kind : active) {
            if (BTT_LINES.containsKey(kind)) continue;
            for (MoodNeedLine l : BTT_LINES.values()) l.index++;
            MoodNeedLine l = new MoodNeedLine();
            l.kind = kind;
            BTT_LINES.put(kind, l);
        }




        int watheLines = dev.doctor4t.wathe.cca.PlayerMoodComponent.KEY.get(player).tasks.size();


        List<String> toRemove = new ArrayList<>();
        for (MoodNeedLine l : BTT_LINES.values()) {
            boolean present = active.contains(l.kind);
            l.present = present;
            if (present) {



                boolean fake = dev.doctor4t.wathe.client.WatheClient.isKiller()
                        || dev.hae.btt.role.Roles.effectiveMoodType(
                        dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(player.getWorld()), player)
                        == dev.doctor4t.wathe.api.Role.MoodType.FAKE;
                l.text = Text.translatable("task." + (fake ? "fake" : "feel"))
                        .append(Text.translatable("task." + l.kind.toLowerCase()));
            }
            l.alpha = MathHelper.lerp(fade, l.alpha, present ? 1.0F : 0.0F);
            l.offset = MathHelper.lerp(slide, l.offset, watheLines + l.index);
            if (l.alpha < 0.075F) toRemove.add(l.kind);
        }
        for (String kind : toRemove) BTT_LINES.remove(kind);
        if (!toRemove.isEmpty()) {
            List<MoodNeedLine> list = new ArrayList<>(BTT_LINES.values());
            list.sort((a, b) -> Float.compare(a.offset, b.offset));
            for (int i = 0; i < list.size(); i++) list.get(i).index = i;
        }


        for (MoodNeedLine l : BTT_LINES.values()) {
            if (l.alpha < 0.075F) continue;
            int argb = MathHelper.packRgb(1f, 1f, 1f) | ((int) (l.alpha * 255.0F) << 24);
            context.getMatrices().push();
            context.getMatrices().translate(0, 10 * l.offset, 0);
            context.drawTextWithShadow(textRenderer, l.text, 22, 6, argb);
            context.getMatrices().pop();
        }






        boolean hasRows = !BTT_LINES.isEmpty();
        MoodNeedLine bottom = null;
        float ourMax = -1f;
        int widest = 0;
        for (MoodNeedLine l : BTT_LINES.values()) {
            if (l.offset > ourMax) {
                ourMax = l.offset;
                bottom = l;
            }
            int w = textRenderer.getWidth(l.text);
            if (w > widest) widest = w;
        }
        float target = hasRows ? Math.max(ourMax, 0f) : (watheLines > 0 ? watheLines - 1 : 0f);
        moodOffset = MathHelper.lerp(slide, moodOffset, target);

        if (hasRows) {
            int bottomWidth = bottom == null ? 0 : textRenderer.getWidth(bottom.text);
            int measured = bottomWidth > 8 ? bottomWidth : widest;
            if (measured > 8) bttBarLastWidth = measured;
            moodTextWidth = MathHelper.lerp(slide, moodTextWidth,
                    measured > 8 ? (float) measured : bttBarLastWidth);
        }
    }





    @WrapOperation(method = "renderHud", at = @At(value = "INVOKE", target = "Ljava/util/Map;isEmpty()Z"))
    private static boolean bttHasRows(Map<?, ?> renderers, Operation<Boolean> original) {
        if (!BTT_LINES.isEmpty()) return false;
        return original.call(renderers);
    }

    @WrapOperation(method = "renderHud", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/RenderTickCounter;getTickDelta(Z)F"))
    private static float btt$elapsedHudTime(RenderTickCounter counter, boolean ignoreFreeze,
                                            Operation<Float> original,
                                            @Local(argsOnly = true) PlayerEntity player) {
        return SeatAssignment.isBttMode(player.getWorld())
                ? Math.max(0.0F, counter.getLastFrameDuration()) : original.call(counter, ignoreFreeze);
    }

    @WrapOperation(method = "renderHud", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"))
    private static float btt$stableHudSmoothing(float amount, float start, float end,
                                               Operation<Float> original,
                                               @Local(argsOnly = true) PlayerEntity player) {
        if (SeatAssignment.isBttMode(player.getWorld())) amount = -(float) Math.expm1(-amount);
        return original.call(amount, start, end);
    }
}
