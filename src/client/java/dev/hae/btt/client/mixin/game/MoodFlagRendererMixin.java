package dev.hae.btt.client.mixin.game;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.MoodRenderer;
import dev.doctor4t.wathe.game.GameConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.hae.btt.BeforeTheTerminal;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.role.Roles;




















@Mixin(MoodRenderer.class)
public abstract class MoodFlagRendererMixin {

    @Shadow public static float moodOffset;
    @Shadow public static float moodRender;

    private static final Identifier MOOD_ENFORCER = Identifier.of(BeforeTheTerminal.MOD_ID, "hud/mood_enforcer_happy");
    private static final Identifier MOOD_MAD_FLIP = Identifier.of(BeforeTheTerminal.MOD_ID, "hud/mood_mad_happy");
    private static final Identifier MOOD_OUTSIDER = Identifier.of(BeforeTheTerminal.MOD_ID, "hud/mood_outsider");

    private static final Identifier MOOD_SOLO = Identifier.of(BeforeTheTerminal.MOD_ID, "hud/mood_solo");


    private static final int COLOR_SOLO = 0x00FFFF;
    private static final int COLOR_OUTSIDER = 0xFF00FF;


    private static boolean btt$happy() {
        return moodRender >= GameConstants.MID_MOOD_THRESHOLD;
    }

    private static Role btt$viewerRole() {
        var viewer = MinecraftClient.getInstance().player;
        if (viewer == null || !SeatAssignment.isBttMode(viewer.getWorld())) return null;
        return GameWorldComponent.KEY.get(viewer.getWorld()).getRole(viewer);
    }





    private static Role btt$viewerRealRole() {
        var viewer = MinecraftClient.getInstance().player;
        if (viewer == null || !SeatAssignment.isBttMode(viewer.getWorld())) return null;
        return Roles.realRole(GameWorldComponent.KEY.get(viewer.getWorld()), viewer);
    }


    private static Identifier btt$flag(Role role) {
        if (role == null) return null;

        var viewer = MinecraftClient.getInstance().player;
        Role.MoodType moodType = viewer == null ? role.getMoodType()
                : Roles.effectiveMoodType(GameWorldComponent.KEY.get(viewer.getWorld()), viewer);

        Roles.Kind kind = Roles.kindOf(role);
        Roles.Faction camp = viewer == null ? Roles.factionOf(role)
                : Roles.campOf(GameWorldComponent.KEY.get(viewer.getWorld()), viewer);
        if (moodType == Role.MoodType.REAL) {

            if (!btt$happy()) return null;
            if (kind == Roles.Kind.ENFORCER) return MOOD_ENFORCER;
            if (kind == Roles.Kind.MAD && btt$madFlipSider(role)) return MOOD_MAD_FLIP;
            return null;
        }

        if (camp == Roles.Faction.SOLO) return MOOD_SOLO;
        if (camp == Roles.Faction.OUTSIDER) return MOOD_OUTSIDER;
        return null;
    }


    private static boolean btt$madFlipSider(Role role) {
        return role == Roles.HERETIC || role == Roles.AMNESIAC || role == Roles.HERMIT;
    }










    private static int btt$barColor(Role role) {
        if (dev.hae.btt.encounter.Memory.isBlind(MinecraftClient.getInstance().player)) return 0x00FF00;
        if (role == null) return 0;
        var viewer = MinecraftClient.getInstance().player;
        Roles.Faction camp = viewer == null ? Roles.factionOf(role)
                : Roles.campOf(GameWorldComponent.KEY.get(viewer.getWorld()), viewer);
        if (camp == Roles.Faction.SOLO) return COLOR_SOLO;
        if (camp == Roles.Faction.OUTSIDER) return COLOR_OUTSIDER;

        if (camp == Roles.Faction.KILLER || role == Roles.LUNATIC) {
            return net.minecraft.util.math.MathHelper.hsvToRgb(0F, 1.0F, 0.6F);
        }
        return net.minecraft.util.math.MathHelper.hsvToRgb(moodRender / 3.0F, 1.0F, 1.0F);
    }

    @Inject(method = "renderCivilian", at = @At("TAIL"))
    private static void bttCivilianFlag(TextRenderer textRenderer, DrawContext context, float prevMood, CallbackInfo ci) {
        Role role = btt$viewerRole();
        var viewer = MinecraftClient.getInstance().player;
        if (role == null || viewer == null) return;
        if (Roles.effectiveMoodType(GameWorldComponent.KEY.get(viewer.getWorld()), viewer) != Role.MoodType.REAL) return;

        btt$overlay(context, btt$flag(role));
    }

    @Inject(method = "renderKiller", at = @At("TAIL"))
    private static void bttKillerFlag(TextRenderer textRenderer, DrawContext context, CallbackInfo ci) {
        Role role = btt$viewerRole();
        var viewer = MinecraftClient.getInstance().player;

        if (role == null || viewer == null) return;
        Role.MoodType moodType = Roles.effectiveMoodType(GameWorldComponent.KEY.get(viewer.getWorld()), viewer);
        if (moodType != Role.MoodType.FAKE && !Roles.isPlayingAs(
                GameWorldComponent.KEY.get(viewer.getWorld()), viewer, Roles.LUNATIC)) return;
        Identifier tex = btt$flag(role);


        if (tex == null) tex = MoodRenderer.MOOD_KILLER;
        btt$overlay(context, tex);
    }






    private static void btt$overlay(DrawContext context, Identifier tex) {
        if (dev.hae.btt.encounter.Memory.isBlind(MinecraftClient.getInstance().player)) return;
        if (tex == null) return;
        context.getMatrices().push();
        context.getMatrices().translate(0.0F, 3.0F * moodOffset, 0.0F);
        context.drawGuiTexture(tex, 5, 6, 14, 17);
        context.getMatrices().pop();
    }


    private static Role btt$barRole() {
        Role real = btt$viewerRealRole();
        return real != null ? real : btt$viewerRole();
    }

    @WrapOperation(method = {"renderCivilian", "renderKiller"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"))
    private static void btt$hideMemoryFlag(DrawContext context, Identifier texture, int x, int y, int width, int height,
                                          Operation<Void> original) {
        if (!dev.hae.btt.encounter.Memory.isBlind(MinecraftClient.getInstance().player))
            original.call(context, texture, x, y, width, height);
    }

    @WrapOperation(method = "renderCivilian", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawSprite(IIIIILnet/minecraft/client/texture/Sprite;FFFF)V"))
    private static void btt$hideMemoryArrow(DrawContext context, int x, int y, int z, int width, int height,
            net.minecraft.client.texture.Sprite sprite, float red, float green, float blue, float alpha, Operation<Void> original) {
        if (!dev.hae.btt.encounter.Memory.isBlind(MinecraftClient.getInstance().player))
            original.call(context, x, y, z, width, height, sprite, red, green, blue, alpha);
    }





    @ModifyExpressionValue(method = "renderCivilian",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;hsvToRgb(FFF)I"))
    private static int bttCivilianBarColor(int original, TextRenderer textRenderer, DrawContext context, float prevMood) {
        int color = btt$barColor(btt$barRole());
        return color == 0 ? original : color;
    }

    @ModifyExpressionValue(method = "renderKiller",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;hsvToRgb(FFF)I"))
    private static int bttKillerBarColor(int original, TextRenderer textRenderer, DrawContext context) {
        int color = btt$barColor(btt$barRole());
        return color == 0 ? original : color;
    }
}
