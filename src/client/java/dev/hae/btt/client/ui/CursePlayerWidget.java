package dev.hae.btt.client.ui;

import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import dev.hae.btt.client.ui.select.SelectPlayerWidget;
import dev.hae.btt.component.CurseState;
import dev.hae.btt.network.DisguiseC2SPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.world.World;
import java.util.UUID;

public class CursePlayerWidget extends SelectPlayerWidget {
    public CursePlayerWidget(LimitedInventoryScreen screen, int x, int y, UUID targetUUID,
                             PlayerListEntry entry, World world, int index) {
        super(screen, x, y, targetUUID, entry, button -> {
            if (cooldown() <= 0) ClientPlayNetworking.send(new DisguiseC2SPacket(targetUUID));
        });
    }
    @Override protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);
        if (targetUUID.equals(CurseState.KEY.get(screen.player).target)) drawHighlight(context, getX(), getY());
    }
}
