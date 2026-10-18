package dev.hae.btt.client.mixin.game;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedHandledScreen;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import dev.hae.btt.component.ConfigState;
import dev.hae.btt.BeforeTheTerminal;
import dev.hae.btt.client.ui.CursePlayerWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mixin(LimitedInventoryScreen.class)
public abstract class WitchScreenMixin extends LimitedHandledScreen<PlayerScreenHandler>{
    @Shadow @Final public ClientPlayerEntity player;

    public WitchScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }
    @Inject(method = "render", at = @At("HEAD"))
    void renderCurseText(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.getWorld());
        ConfigState configWorldComponent = (ConfigState) ConfigState.KEY.get(player.getWorld());
        if (gameWorldComponent.isRole(player,dev.hae.btt.role.Roles.WITCH)) {
            int y = (height- 32) / 2;
            int x = width / 2;
            java.util.UUID cursedUuid = dev.hae.btt.component.CurseState.KEY.get(player).target;
            var cursed = cursedUuid == null ? null : player.getWorld().getPlayerByUuid(cursedUuid);
            Text line = dev.hae.btt.client.ui.SkillText.prompt(Text.translatable("btt.hud.witch"),
                    dev.hae.btt.component.AbilityState.KEY.get(player).cooldown,
                    cursed == null ? null : Text.translatable("btt.hud.witch.target", cursed.getName().getString()));
            if (dev.hae.btt.encounter.Memory.isBlind(player)) line = Text.translatable("btt.memory.targets", 1);
            context.drawTextWithShadow(textRenderer, line, x - textRenderer.getWidth(line) / 2, y + 40,
                    dev.hae.btt.role.Roles.WITCH.color());
        }
    }

    @Inject(method = "init", at = @At("HEAD"))
    void renderCurseHeads(CallbackInfo ci) {
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.getWorld());
        if (gameWorldComponent.isRole(player,dev.hae.btt.role.Roles.WITCH)) {
            List<UUID> entries = new ArrayList<>(MinecraftClient.getInstance().player.networkHandler.getPlayerUuids());
            entries.sort(java.util.Comparator.comparingInt(dev.hae.btt.component.GameState.KEY.get(MinecraftClient.getInstance().world)::numberOf));

            int apart = 36;
            int x = width / 2 - (entries.size()) * apart / 2 + 9;
            int shouldBeY = (height - 32) / 2;
            int y = shouldBeY + 80;

            for(int i = 0; i < entries.size(); ++i) {
                CursePlayerWidget child = new CursePlayerWidget(((LimitedInventoryScreen)(Object)this), x + apart * i, y, entries.get(i), MinecraftClient.getInstance().player.networkHandler.getPlayerListEntry(entries.get(i)), player.getWorld(), i);
                addDrawableChild(child);
            }
        }
    }

}
