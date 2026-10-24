package dev.hae.btt.client.ui;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.PlayerListEntry;
import dev.hae.btt.component.AbilityState;
import dev.hae.btt.client.ui.select.SelectPlayerWidget;

import java.util.UUID;






public class ActionPlayerWidget extends SelectPlayerWidget {
    public ActionPlayerWidget(LimitedInventoryScreen screen, int x, int y, UUID targetUUID, PlayerListEntry targetPlayerEntry) {
        super(screen, x, y, targetUUID, targetPlayerEntry, a -> {
            if (AbilityState.KEY.get(screen.player).cooldown > 0) return;

            screen.player.playSound(dev.hae.btt.registry.Sounds.UI_KEY_E, 1.0F, 1.0F);



            GameWorldComponent gwc = GameWorldComponent.KEY.get(screen.player.getWorld());
            dev.doctor4t.wathe.api.Role actingRole = dev.hae.btt.role.Roles.effectiveRole(gwc, screen.player);
            if (AbilityKey.isMorphRole(actingRole)) {
                if (MultiSelectState.picks().isEmpty()) {
                    var target = screen.player.getWorld().getPlayerByUuid(targetUUID);
                    if (target == null || !dev.doctor4t.wathe.game.GameFunctions.isPlayerAliveAndSurvival(target)) return;
                    MultiSelectState.picks().add(targetUUID);
                } else {
                    ClientPlayNetworking.send(new dev.hae.btt.network.MultiSelectC2SPacket(
                            java.util.List.of(MultiSelectState.picks().getFirst(), targetUUID)));
                    MultiSelectState.reset();
                    screen.close();
                }
                return;
            }
            if (AbilityKey.isMultiPick(actingRole)) {

                if (actingRole != dev.hae.btt.role.Roles.PROPHET && MultiSelectState.isPicked(targetUUID)) {
                    MultiSelectState.picks().remove(targetUUID);
                } else {
                    MultiSelectState.picks().add(targetUUID);
                }
                int need = MultiSelectState.needed(screen.player);
                if (need > 0 && MultiSelectState.picks().size() >= need) {
                    ClientPlayNetworking.send(new dev.hae.btt.network.MultiSelectC2SPacket(
                            java.util.List.copyOf(MultiSelectState.picks())));
                    MultiSelectState.reset();
                    screen.close();
                }
                return;
            }



            if (AbilityKey.isPickRole(actingRole)) {
                RolePickScreen.openForAbility(net.minecraft.client.MinecraftClient.getInstance(),
                        actingRole, targetUUID, screen);
                return;
            }
            if (instantMode) {
                ClientPlayNetworking.send(new dev.hae.btt.network.SelectC2SPacket(targetUUID, ""));
                screen.close();
                return;
            }
            SelectPlayerWidget.selectedPlayer = targetUUID;
        });
    }

    @Override
    protected void renderWidget(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);

        if (MultiSelectState.isPicked(this.targetUUID)) {
            drawHighlight(context, this.getX(), this.getY());
        }
    }
}
