package dev.hae.btt.client.mixin.game;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedHandledScreen;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.role.RoleDef;
import dev.hae.btt.role.RoleDefs;
import dev.hae.btt.role.Roles;
import dev.hae.btt.client.ui.AbilityKey;
import dev.hae.btt.client.ui.ActionPlayerWidget;
import dev.hae.btt.client.ui.select.SelectPlayerWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;










@Mixin(LimitedInventoryScreen.class)
public abstract class SelectScreenMixin extends LimitedHandledScreen<PlayerScreenHandler> {
    @Shadow @Final public ClientPlayerEntity player;

    public SelectScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    void bttAddGuessWidgets(CallbackInfo ci) {
        if (player == null || MinecraftClient.getInstance().world == null) return;
        if (!SeatAssignment.isBttMode(player.getWorld())) return;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(player.getWorld());
        if (!gwc.isRunning()) return;
        RoleDef def = RoleDefs.get(dev.hae.btt.role.Roles.effectiveRole(gwc, player));
        if (def == null || !dev.hae.btt.client.ui.AbilityKey.isUiRole(def.role)) return;
        boolean instant = dev.hae.btt.client.ui.AbilityKey.isInstant(def.role);
        boolean multiPick = dev.hae.btt.client.ui.AbilityKey.isMultiPick(def.role);
        boolean morph = dev.hae.btt.client.ui.AbilityKey.isMorphRole(def.role);
        if (morph) instant = true;

        SelectPlayerWidget.selectedPlayer = null;
        SelectPlayerWidget.instantMode = instant;
        if (multiPick || morph) dev.hae.btt.client.ui.MultiSelectState.reset();

        List<UUID> entries = new ArrayList<>(player.networkHandler.getPlayerUuids());
        entries.sort(java.util.Comparator.comparingInt(dev.hae.btt.component.GameState.KEY.get(player.getWorld())::numberOf));
        if (morph) entries.removeIf(uuid -> gwc.getRole(uuid) == null);
        entries.removeIf(uuid -> {
            var target = MinecraftClient.getInstance().world.getPlayerByUuid(uuid);
            if (target == null) return false;
            var state = dev.hae.btt.component.PlayerState.KEY.get(target);
            return def.role == Roles.VOODOO ? state.voodooPhase != 1 || !state.voodooOwner.equals(player.getUuidAsString())
                    : state.voodooPhase == 1;
        });

        if (multiPick && !morph) {

            entries.removeIf(uuid -> MinecraftClient.getInstance().world.getPlayerByUuid(uuid) == null);
        }
        int apart = 36;
        LimitedInventoryScreen self = (LimitedInventoryScreen) (Object) this;
        int x = self.width / 2 - entries.size() * apart / 2 + 9;
        int y = (self.height - 32) / 2 + 105;

        for (int i = 0; i < entries.size(); i++) {
            ActionPlayerWidget child = new ActionPlayerWidget(self, x + apart * i, y, entries.get(i),
                    player.networkHandler.getPlayerListEntry(entries.get(i)));
            addDrawableChild(child);
            child.visible = false;
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    void bttSwapWidgetVisibility(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        boolean ours = false;
        for (Element child : children()) {
            if (child instanceof ActionPlayerWidget) {
                ours = true;
                ((ActionPlayerWidget) child).visible = SelectPlayerWidget.selectedPlayer == null;
            }
        }
        if (ours && SelectPlayerWidget.instantMode) {
            for (Element child : children()) {
                if (child instanceof ActionPlayerWidget gpw) gpw.visible = true;
            }
        }

        RoleDef def = RoleDefs.get(dev.hae.btt.role.Roles.effectiveRole(
                GameWorldComponent.KEY.get(player.getWorld()), player));
        if (def != null && AbilityKey.isMorphRole(def.role)) {
            boolean second = !dev.hae.btt.client.ui.MultiSelectState.picks().isEmpty();
            for (Element child : children()) if (child instanceof ActionPlayerWidget widget) {
                var target = player.getWorld().getPlayerByUuid(widget.targetUUID);
                widget.visible = second || target != null
                        && dev.doctor4t.wathe.game.GameFunctions.isPlayerAliveAndSurvival(target);
            }
        }

        boolean multiPick = def != null && AbilityKey.isMultiPick(def.role);
        if (dev.hae.btt.encounter.Memory.isBlind(player)) {
            int count = multiPick ? dev.hae.btt.client.ui.MultiSelectState.needed(player) : 1;
            Text generic = Text.translatable("btt.memory.targets", Math.max(1, count));
            context.drawTextWithShadow(textRenderer, generic, width / 2 - textRenderer.getWidth(generic) / 2,
                    (height - 32) / 2 + 40, 0xFFFFFF);
            return;
        }
        if (def != null && AbilityKey.isAnyRole(def.role) && !multiPick) {
            Text desc = def.role == dev.hae.btt.role.Roles.HUNTER
                    && dev.hae.btt.component.PlayerState.KEY.get(player).hunterShot == 1
                    ? Text.translatable("btt.hud.hunter.used")
                    : Text.translatable("btt.hud." + def.role.identifier().getPath());

            Text state = bttRemoteState(def.role, player);
            desc = dev.hae.btt.client.ui.SkillText.prompt(desc,
                    dev.hae.btt.component.AbilityState.KEY.get(player).cooldown, state);
            context.drawTextWithShadow(textRenderer, desc,
                    width / 2 - textRenderer.getWidth(desc) / 2, (height - 32) / 2 + 40, def.role.color());
        }


        if (def != null && AbilityKey.isMultiPick(def.role)) {
            Text ready = def.role == Roles.JUDGE
                    ? Text.translatable("btt.hud.judge", dev.hae.btt.client.ui.MultiSelectState.needed(player))
                    : Text.translatable("btt.hud." + def.role.identifier().getPath());
            Text merged = dev.hae.btt.client.ui.SkillText.prompt(ready,
                    dev.hae.btt.component.AbilityState.KEY.get(player).cooldown, null);
            context.drawTextWithShadow(textRenderer, merged,
                    width / 2 - textRenderer.getWidth(merged) / 2, (height - 32) / 2 + 64, def.role.color());
        }
    }






    private static Text bttRemoteState(dev.doctor4t.wathe.api.Role role, ClientPlayerEntity self) {
        if (self == null || self.getWorld() == null) return null;
        dev.hae.btt.component.PlayerState pc = dev.hae.btt.component.PlayerState.KEY.get(self);
        if (role == Roles.NOVELIST) return Text.translatable("btt.hud.novelist.progress", pc.novelistHits,
                pc.novelistRequired);
        String key = null;
        String uuid = null;
        if (role == dev.hae.btt.role.Roles.JOURNALIST) {
            key = "btt.hud.journalist.target";
            uuid = pc.markedTarget;

        } else if (role == dev.hae.btt.role.Roles.STAR) {
            key = "btt.hud.star.target";
            uuid = pc.starMark;
        } else if (role == dev.hae.btt.role.Roles.GODFATHER) {
            key = "btt.hud.godfather.target";
            java.util.UUID revenge = dev.hae.btt.component.CurseState.KEY.get(self).target;
            uuid = revenge == null ? null : revenge.toString();
        } else if (role == dev.hae.btt.role.Roles.MEDIUM) {

            key = "btt.hud.medium.target";
            uuid = pc.mediumRetain;

        }
        if (key == null || uuid == null || uuid.isEmpty()) return null;
        net.minecraft.entity.player.PlayerEntity tracked;
        try {
            tracked = self.getWorld().getPlayerByUuid(java.util.UUID.fromString(uuid));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
        return tracked == null ? null : Text.translatable(key, tracked.getName().getString());
    }
}
