package dev.hae.btt.client.mixin.game;

import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.client.gui.RoleNameRenderer;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameFunctions;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.Identifier;
import dev.doctor4t.wathe.game.GameConstants;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.client.HarpymodloaderClient;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import dev.hae.btt.component.AbilityState;
import dev.hae.btt.BeforeTheTerminal;
import dev.hae.btt.client.BttClient;
import dev.hae.btt.component.CorpseState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RoleNameRenderer.class)
public abstract class CorpseInfoHudMixin {

    @Shadow private static float nametagAlpha;

    @Shadow private static Text nametag;

    @Inject(method = "renderHud", at = @At("TAIL"))
    private static void doctorRoleNameRenderer(TextRenderer renderer, ClientPlayerEntity player, DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (dev.hae.btt.encounter.Memory.isBlind(net.minecraft.client.MinecraftClient.getInstance().player)) return;
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.getWorld());
        if (BttClient.targetBody != null) {


            CorpseState esc = CorpseState.KEY.get(BttClient.targetBody);
            if (dev.hae.btt.combat.DeathReasons.ESCAPE.equals(esc.deathReason)) {
                Text escLine = Text.translatable("death_reason.btt.escape").withColor(Colors.RED);
                context.getMatrices().push();
                context.getMatrices().translate((float) context.getScaledWindowWidth() / 2.0F,
                        (float) context.getScaledWindowHeight() / 2.0F + 6.0F, 0.0F);
                context.getMatrices().scale(0.6F, 0.6F, 1.0F);
                context.drawTextWithShadow(renderer, escLine, -renderer.getWidth(escLine) / 2, 32, Colors.WHITE);
                context.getMatrices().pop();
                return;
            }


            boolean mortician = gameWorldComponent.isRole(player, dev.hae.btt.role.Roles.MORTICIAN);
            if (mortician) {
                CorpseState corpseState = CorpseState.KEY.get(BttClient.targetBody);
                if (!corpseState.observedPassengerKnown) return;
                boolean passenger = corpseState.observedPassenger;


                Text line = Text.translatable("hud.corpse.faction").withColor(Colors.RED)
                        .append(Text.translatable(passenger ? "hud.corpse.passenger" : "hud.corpse.not_passenger")
                                .withColor(passenger ? Colors.GREEN : Colors.RED));
                context.getMatrices().push();
                context.getMatrices().translate((float) context.getScaledWindowWidth() / 2.0F,
                        (float) context.getScaledWindowHeight() / 2.0F + 6.0F, 0.0F);
                context.getMatrices().scale(0.6F, 0.6F, 1.0F);
                context.drawTextWithShadow(renderer, line, -renderer.getWidth(line) / 2, 16, Colors.WHITE);
                context.getMatrices().pop();
            }


            if (CorpseState.KEY.get(BttClient.targetBody).observedAutopsy) {

                context.getMatrices().push();
                context.getMatrices().translate((float)context.getScaledWindowWidth() / 2.0F, (float)context.getScaledWindowHeight() / 2.0F + 6.0F, 0.0F);
                context.getMatrices().scale(0.6F, 0.6F, 1.0F);
                PlayerMoodComponent moodComponent = (PlayerMoodComponent) PlayerMoodComponent.KEY.get(MinecraftClient.getInstance().player);
                CorpseState bodyDeathReasonComponent = (CorpseState) CorpseState.KEY.get(BttClient.targetBody);



                int btt$ageSeconds = bodyDeathReasonComponent.observedAgeSeconds;
                Identifier btt$reason = bodyDeathReasonComponent.deathReason;
                Text name = Text.translatable("hud.doctor.death_info", btt$ageSeconds).append(Text.translatable("death_reason." + btt$reason.getNamespace()+ "." + btt$reason.getPath()));
                if (bodyDeathReasonComponent.scavenged) {
                    name = Text.literal("a".repeat(player.getRandom().nextBetween(12,26))).formatted(Formatting.OBFUSCATED);
                }
                context.drawTextWithShadow(renderer, name, -renderer.getWidth(name) / 2, 32, Colors.RED);
                Role foundRole = WatheRoles.CIVILIAN;
                for (Role role : WatheRoles.ROLES) {
                    if (role.identifier().equals(bodyDeathReasonComponent.technicalRole)) foundRole =role;
                }
                if ((WatheClient.isPlayerSpectatingOrCreative() || gameWorldComponent.isRole(MinecraftClient.getInstance().player, dev.hae.btt.role.Roles.DOCTOR) || gameWorldComponent.isRole(MinecraftClient.getInstance().player, dev.hae.btt.role.Roles.AGENT) || dev.hae.btt.trait.Traits.has(MinecraftClient.getInstance().player, dev.hae.btt.trait.Traits.ANATOMY))  && !bodyDeathReasonComponent.scavenged ) {
                    net.minecraft.text.MutableText roleInfo = Text.translatable("hud.doctor.role_info").withColor(Colors.RED)
                            .append(Harpymodloader.getRoleName(foundRole).withColor(foundRole.color()));


                    Role initialRole = dev.hae.btt.role.Roles.byId(bodyDeathReasonComponent.initialRole.toString());
                    if (initialRole != null && !initialRole.identifier().equals(foundRole.identifier())) {
                        roleInfo.append(Text.translatable("hud.corpse.first_role",
                                Harpymodloader.getRoleName(initialRole).withColor(initialRole.color())));
                    }
                    context.drawTextWithShadow(renderer, roleInfo, -renderer.getWidth(roleInfo) / 2, 48, Colors.WHITE);
                }



                context.getMatrices().pop();
                return;
            }
        }
    }
    @Inject(method = "renderHud", at = @At(value = "INVOKE", target = "Ldev/doctor4t/wathe/game/GameFunctions;isPlayerSpectatingOrCreative(Lnet/minecraft/entity/player/PlayerEntity;)Z"))
    private static void customRaycast(TextRenderer renderer, ClientPlayerEntity player, DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        float range = GameFunctions.isPlayerSpectatingOrCreative(player) ? 8.0F : 2.0F;
        HitResult line = ProjectileUtil.getCollision(player, (entity) -> entity instanceof PlayerBodyEntity, (double)range);
        BttClient.targetBody = null;
        if (line instanceof EntityHitResult ehr) {
            if (ehr.getEntity() instanceof PlayerBodyEntity playerBodyEntity) {
                BttClient.targetBody = playerBodyEntity;
            }
        }
    }
}
