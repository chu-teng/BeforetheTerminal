package dev.hae.btt.client.mixin.game;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import dev.hae.btt.component.AbilityState;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.client.BttClient;
import dev.hae.btt.client.ui.AbilityKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;




@Mixin(InGameHud.class)
public abstract class AbilityHudMixin {
    @Inject(method = "setOverlayMessage", at = @At("HEAD"), cancellable = true)
    private void bttMemoryOverlay(Text message, boolean tinted, CallbackInfo ci) {
        if (dev.hae.btt.encounter.Memory.isBlind(MinecraftClient.getInstance().player)
                && !dev.hae.btt.encounter.Memory.isGuessFeedback(message)) ci.cancel();
    }
    @Shadow
    public abstract TextRenderer getTextRenderer();

    @Inject(method = "render", at = @At("TAIL"))
    private void bttAbilityHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        var client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        if (!SeatAssignment.isBttMode(client.world)) return;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(client.world);
        if (!gwc.isRunning()) return;
        Role role = gwc.getRole(client.player);
        if (dev.hae.btt.encounter.Memory.isBlind(client.player)) {
            Role effective = dev.hae.btt.role.Roles.effectiveRole(gwc, client.player);
            if (effective == null || !(AbilityKey.hasTip(effective) || AbilityKey.isAnyRole(effective)
                    || effective == dev.hae.btt.role.Roles.STOWAWAY || effective == dev.hae.btt.role.Roles.THIEF)) return;
            if (AbilityKey.isAnyRole(effective)) return;
            int cooldown = AbilityState.KEY.get(client.player).cooldown;
            Text bind = AbilityKey.isAnyRole(effective) ? client.options.inventoryKey.getBoundKeyLocalizedText()
                    : BttClient.abilityBind.getBoundKeyLocalizedText();
            Text generic = cooldown > 0 ? Text.translatable("btt.memory.cooldown", (cooldown + 19) / 20)
                    : Text.translatable(AbilityKey.isNearbyRole(effective) ? "btt.memory.nearby" : "btt.memory.use", bind);
            TextRenderer renderer = getTextRenderer();
            context.drawTextWithShadow(renderer, generic, context.getScaledWindowWidth() - renderer.getWidth(generic),
                    context.getScaledWindowHeight() - renderer.fontHeight, 0xFFFFFF);
            return;
        }
        if (role == null || !AbilityKey.hasTip(role)) return;

        AbilityState ability = AbilityState.KEY.get(client.player);
        Text bind = BttClient.abilityBind.getBoundKeyLocalizedText();
        Text aimedName = AbilityKey.aimedName(client);
        Text line = Text.translatable("btt.tip." + role.identifier().getPath(), bind, aimedName);


        if (ability.cooldown > 0 && role != dev.hae.btt.role.Roles.ACTOR) {
            line = (gwc.getRole(client.player) == dev.hae.btt.role.Roles.HUNTER
                    && dev.hae.btt.component.PlayerState.KEY.get(client.player).hunterShot == 1)
                    ? Text.translatable("btt.hud.hunter.used")
                    : Text.translatable("tip.btt.cooldown", ability.cooldown / 20);
        }

        var ownState = dev.hae.btt.component.PlayerState.KEY.get(client.player);
        if (role == dev.hae.btt.role.Roles.KIDNAPPER) {
            long count = client.world.getPlayers().stream().filter(p ->
                    dev.hae.btt.component.PlayerState.KEY.get(p).swallowedBy.equals(client.player.getUuidAsString())).count();
            if (count > 0) line = dev.hae.btt.client.ui.SkillText.prompt(
                    Text.translatable("btt.hud.kidnapper.belly", bind, aimedName, count), ability.cooldown, null);
        }
        if (role == dev.hae.btt.role.Roles.ANGEL) {
            line = ownState.angelTarget.isEmpty()
                    ? Text.translatable("btt.tip.angel", bind, aimedName, ownState.angelShields)
                    : Text.translatable("btt.hud.angel.target", bttPlayerName(client, ownState.angelTarget), ownState.angelShields);
        }
        if (role == dev.hae.btt.role.Roles.COLONEL) {
            line = Text.translatable(ownState.colonelFeigning ? "btt.hud.colonel.feigning" : "btt.tip.colonel", BttClient.abilityBind.getBoundKeyLocalizedText());
        }
        if (role == dev.hae.btt.role.Roles.GAMBLER && ability.cooldown <= 0) {
            int progress = ownState.gamblerGuesses.isEmpty() ? 0 : ownState.gamblerGuesses.split(",").length;
            line = Text.translatable("btt.tip.gambler", BttClient.abilityBind.getBoundKeyLocalizedText(),
                    Math.max(0, ownState.gamblerTarget - progress));
        }
        if (role == dev.hae.btt.role.Roles.CRAFTSMAN && !ownState.craftItem.isEmpty()) {
            long remaining = Math.max(0, ownState.craftReadyAt - client.world.getTime());
            line = remaining > 0 ? Text.translatable("btt.hud.craftsman.preparing", (remaining + 19) / 20)
                    : Text.translatable("btt.hud.craftsman.gift", bind, aimedName);
        }
        if (role == dev.hae.btt.role.Roles.MUYUUBYOU && ownState.isProjecting()) {
            line = Text.translatable("btt.hud.muyuubyou.wake", BttClient.abilityBind.getBoundKeyLocalizedText());
        }
        if ((role == dev.hae.btt.role.Roles.ACTOR || role == dev.hae.btt.role.Roles.SHOUJO)
                && ownState.isFakeDead()) {
            line = Text.translatable("btt.hud.fake_dead.stand", BttClient.abilityBind.getBoundKeyLocalizedText());
        }




        if (role == dev.hae.btt.role.Roles.LEECH) {
            String host = dev.hae.btt.component.PlayerState.KEY.get(client.player).parasiteHost;
            if (!host.isEmpty()) {
                line = dev.hae.btt.client.ui.SkillText.ongoing(
                        Text.translatable("btt.hud.leech.host", bttPlayerName(client, host)), ability.cooldown);
            }
        }



        if (role == dev.hae.btt.role.Roles.ASTRAL) {
            var astral = dev.hae.btt.component.PlayerState.KEY.get(client.player);
            java.util.List<String> dream = bttRoleNames(client, gwc,
                    astral.astralPassenger, astral.astralPassenger2, astral.astralKiller);
            Object[] args = new Object[dream.size() + 1];
            args[0] = BttClient.abilityBind.getBoundKeyLocalizedText();
            for (int i = 0; i < dream.size(); i++) args[i + 1] = dream.get(i);
            line = Text.translatable("btt.hud.astral."
                    + (astral.isProjecting() ? "wake" : "dream") + dream.size(), args);
        }



        if (role == dev.hae.btt.role.Roles.EXECUTIONER) {
            var ex = dev.hae.btt.component.PlayerState.KEY.get(client.player);
            String suffix = ex.executionerExecuted >= 1 ? "1" : "2";
            line = ex.executionerTarget.isEmpty()
                    ? Text.translatable("btt.hud.executioner.idle" + suffix, bind, aimedName)
                    : Text.translatable("btt.hud.executioner.target" + suffix,
                            bttPlayerName(client, ex.executionerTarget));
            if (ability.cooldown > 0) {
                line = line.copy().append(Text.translatable("btt.hud.cooldown_suffix", ability.cooldown / 20));
            }
        }
        if (role == dev.hae.btt.role.Roles.ENGINEER) {
            int status = dev.hae.btt.component.PlayerState.KEY.get(client.player).engineerCameraStatus;
            if (status != 0) {
                line = Text.translatable(status == 1 ? "btt.hud.engineer.active" : "btt.hud.engineer.unavailable",
                        BttClient.abilityBind.getBoundKeyLocalizedText());
            }
        }



        if (role == dev.hae.btt.role.Roles.ARCHITECT) {
            var arch = dev.hae.btt.component.PlayerState.KEY.get(client.player);
            if (arch.passageDim.isEmpty()) {
                line = ability.cooldown > 0
                        ? Text.translatable("btt.hud.architect.passage_cooldown", ability.cooldown / 20)
                        : Text.translatable("btt.hud.architect.passage_ready", bind);
            } else if (Math.max(arch.passageTicks, ability.cooldown) > 0) {
                line = Text.translatable("btt.hud.architect.teleport_cooldown", Math.max(arch.passageTicks, ability.cooldown) / 20);
            } else {
                line = Text.translatable("btt.hud.architect.teleport_ready", bind);
            }
        }




        String stateKey = bttNearbyStateKey(role);
        if (stateKey != null) {
            String uuid = bttNearbyStateUuid(client, role);
            if (uuid != null && !uuid.isEmpty()) {
                String name = bttPlayerName(client, uuid);
                if (!"?".equals(name)) line = dev.hae.btt.client.ui.SkillText.ongoing(
                        Text.translatable(stateKey, name), ability.cooldown);
            }
        }

        TextRenderer tr = getTextRenderer();
        int drawY = context.getScaledWindowHeight() - tr.getWrappedLinesHeight(line, 999999);
        context.drawTextWithShadow(tr, line, context.getScaledWindowWidth() - tr.getWidth(line), drawY, role.color());

    }






    private static String bttNearbyStateKey(Role role) {
        if (role == dev.hae.btt.role.Roles.BODYGUARD) return "btt.hud.bodyguard.target";

        if (role == dev.hae.btt.role.Roles.IMP) return "btt.hud.imp.target";
        return null;
    }


    private static String bttNearbyStateUuid(MinecraftClient client, Role role) {
        var pc = dev.hae.btt.component.PlayerState.KEY.get(client.player);
        if (role == dev.hae.btt.role.Roles.BODYGUARD) return pc.guardTarget;
        if (role == dev.hae.btt.role.Roles.IMP) return pc.impMark;
        return null;
    }


    private static String bttPlayerName(MinecraftClient client, String uuid) {
        try {
            var p = client.world == null ? null
                    : client.world.getPlayerByUuid(java.util.UUID.fromString(uuid));
            return p == null ? "?" : p.getName().getString();
        } catch (IllegalArgumentException e) {
            return "?";
        }
    }





    private static java.util.List<String> bttRoleNames(MinecraftClient client, GameWorldComponent gwc,
                                                           String... uuids) {
        java.util.List<String> out = new java.util.ArrayList<>(uuids.length);
        if (client.world == null) return out;
        for (String uuid : uuids) {
            if (uuid == null || uuid.isEmpty()) continue;
            net.minecraft.entity.player.PlayerEntity p;
            try {
                p = client.world.getPlayerByUuid(java.util.UUID.fromString(uuid));
            } catch (IllegalArgumentException e) {
                continue;
            }
            if (p == null) continue;
            Role dreamRole = gwc.getRole(p);
            if (dreamRole == null) continue;
            out.add(org.agmas.harpymodloader.Harpymodloader.getRoleName(dreamRole).getString());
        }
        return out;
    }
}
