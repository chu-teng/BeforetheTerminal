package dev.hae.btt.game;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.combat.DeathReasons;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.role.Roles;
import net.minecraft.server.network.ServerPlayerEntity;


public final class ScriptObjectives {
    private ScriptObjectives() {}
    public static void complete(ServerPlayerEntity actor) {
        var state = PlayerState.KEY.get(actor);
        if (state.soloObjectiveComplete) return;
        state.soloObjectiveComplete = true;
        state.applyPermanentDrunk();
        actor.giveItemStack(new net.minecraft.item.ItemStack(dev.doctor4t.wathe.index.WatheItems.REVOLVER));
        state.sync();
    }
}
