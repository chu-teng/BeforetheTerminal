package dev.hae.btt.client.mixin.game;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.RoleNameRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import dev.hae.btt.game.BeforeTheTerminalGameMode;
import dev.hae.btt.role.Roles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;












@Mixin(RoleNameRenderer.class)
public abstract class AgentHudMixin {


    @Shadow private static PlayerEntity target;


    @Shadow private static float nametagAlpha;

    @com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod(method = "renderHud")
    private static void bttVotingInspect(TextRenderer renderer, ClientPlayerEntity player, DrawContext context,
                                          RenderTickCounter counter,
                                          com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original) {
        if (!dev.hae.btt.game.Debate.isVoting(player.getWorld())) {
            original.call(renderer, player, context, counter);
            return;
        }
        float range = dev.doctor4t.wathe.game.GameFunctions.isPlayerSpectatingOrCreative(player) ? 8f : 2f;
        var hit = net.minecraft.entity.projectile.ProjectileUtil.getCollision(player, entity -> entity instanceof PlayerEntity, range);
        if (!(hit instanceof net.minecraft.util.hit.EntityHitResult entityHit)
                || !(entityHit.getEntity() instanceof PlayerEntity looked) || looked == player) return;
        String names = dev.hae.btt.component.PlayerState.KEY.get(looked).observedVoters;
        Text voters = names.isEmpty() ? Text.translatable("btt.debate.voters.none") : Text.literal(names);
        Text line = Text.translatable("btt.debate.voters", voters);
        context.getMatrices().push();
        context.getMatrices().translate(context.getScaledWindowWidth() / 2f,
                context.getScaledWindowHeight() / 2f + 6f, 0f);
        context.getMatrices().scale(0.6f, 0.6f, 1f);
        context.drawTextWithShadow(renderer, line, -renderer.getWidth(line) / 2, 16, 0xFFFFFF);
        context.getMatrices().pop();
    }


    @Inject(method = "renderHud", at = @At("HEAD"), cancellable = true)
    private static void bttHidePreGameNames(TextRenderer renderer, ClientPlayerEntity player, DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        GameWorldComponent gwc = GameWorldComponent.KEY.get(player.getWorld());
        if (!(gwc.getGameMode() instanceof BeforeTheTerminalGameMode)) return;
        if (!gwc.isRunning()) ci.cancel();
    }

    @Inject(method = "renderHud", at = @At("TAIL"))
    private static void bttAgentInspect(TextRenderer renderer, ClientPlayerEntity player, DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (dev.hae.btt.encounter.Memory.isBlind(net.minecraft.client.MinecraftClient.getInstance().player)) return;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(player.getWorld());
        if (!(gwc.getGameMode() instanceof BeforeTheTerminalGameMode)) return;
        if (!gwc.isRunning()) return;



        PlayerEntity looked = target;
        if (looked == null || looked == player || nametagAlpha <= 0.05f) return;

        var observed = dev.hae.btt.component.PlayerState.KEY.get(looked).observedInspectRole;
        dev.doctor4t.wathe.api.Role role = Roles.byId(observed);
        if (role == null) return;


        Text roleName = Text.translatable("announcement.role."
                + role.identifier().getNamespace() + "." + role.identifier().getPath()).withColor(role.color());
        Text selectedRole = Text.translatable("btt.inspect.hud", roleName).formatted(Formatting.YELLOW);

        context.getMatrices().push();
        context.getMatrices().translate(context.getScaledWindowWidth() / 2.0F, context.getScaledWindowHeight() / 2.0F + 6.0F, 0.0F);
        context.getMatrices().scale(0.6F, 0.6F, 1.0F);
        context.drawTextWithShadow(renderer, selectedRole, -renderer.getWidth(selectedRole) / 2, 48, 0xFFFFFF);
        context.getMatrices().pop();
    }

}
