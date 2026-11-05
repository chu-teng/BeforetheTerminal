package dev.hae.btt.role;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import dev.hae.btt.component.GameState;
import dev.hae.btt.component.PlayerState;
import net.minecraft.server.network.ServerPlayerEntity;

public final class Jester {
    private Jester() {}
    public static boolean begin(ServerPlayerEntity jester, ServerPlayerEntity target) {
        if (target == null || Drunk.isDrunk(jester)) return false;
        var game = GameWorldComponent.KEY.get(jester.getWorld());
        if (!game.isRole(jester, Roles.JESTER)) return false;
        var psycho = PlayerPsychoComponent.KEY.get(jester);
        if (psycho.getPsychoTicks() > 0) return false;
        var state = PlayerState.KEY.get(jester); state.jesterExecutioner = target.getUuidAsString(); state.sync();
        psycho.startPsycho(); psycho.setPsychoTicks(RoleDefs.CD_2MIN);
        psycho.setArmour(GameState.KEY.get(jester.getWorld()).misfireCount);
        jester.giveItemStack(new net.minecraft.item.ItemStack(WatheItems.BAT));
        for (int i = 0; i < jester.getInventory().size(); i++)
            if (jester.getInventory().getStack(i).isOf(WatheItems.BAT)) { jester.getInventory().selectedSlot = i; break; }
        dev.hae.btt.combat.DeathResolution.shieldBreak(jester, target);
        dev.hae.btt.record.GameRecordManager.recordTalentUse(jester, "jester_psycho", null);
        if (!GameFunctions.isPlayerAliveAndSurvival(target)) complete(jester);
        return true;
    }
    private static void complete(ServerPlayerEntity player) {
        var state = PlayerState.KEY.get(player); state.jesterExecutioner = ""; state.sync();
        dev.hae.btt.victory.SoloWin.request(player.getServerWorld(), dev.hae.btt.victory.Endings.Ending.JESTER_WIN, player);
    }
    public static void death(ServerPlayerEntity victim, ServerPlayerEntity killer, net.minecraft.util.Identifier reason) {
        for (var player : victim.getServerWorld().getPlayers())
            if (PlayerPsychoComponent.KEY.get(player).getPsychoTicks() > 0
                    && victim.getUuidAsString().equals(PlayerState.KEY.get(player).jesterExecutioner)) complete(player);
    }
}
