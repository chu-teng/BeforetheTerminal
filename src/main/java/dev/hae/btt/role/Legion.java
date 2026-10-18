package dev.hae.btt.role;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.registry.BttItems;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;


public final class Legion {
    private Legion() {}

    public static boolean recruit(ServerPlayerEntity source, ServerPlayerEntity victim) {
        if (source.getWorld() != victim.getWorld() || Drunk.isDrunk(source)) return false;
        var poison = PlayerPoisonComponent.KEY.get(victim);
        poison.reset();
        poison.poisoner = null;
        poison.sync();
        dev.hae.btt.advancement.BttAdvancements.recruit(source, victim);
        dev.hae.btt.faction.Camps.joinCamp(victim, source);
        return true;
    }

    public static void died(ServerPlayerEntity victim, ServerPlayerEntity killer, Identifier reason) {
        var game = GameWorldComponent.KEY.get(victim.getWorld());
        for (int slot = 0; slot < victim.getInventory().size(); slot++)
            if (victim.getInventory().getStack(slot).isOf(BttItems.POISON_NEEDLE)) victim.getInventory().removeStack(slot);
        var candidates = victim.getServerWorld().getPlayers().stream().filter(p -> p != victim
                && GameFunctions.isPlayerAliveAndSurvival(p) && dev.hae.btt.faction.Camps.sharesRoleCamp(game, p, Roles.LEGION)).toList();
        if (candidates.isEmpty()) return;
        dev.hae.btt.advancement.BttAdvancements.succession(victim);
        var next = candidates.get(victim.getRandom().nextInt(candidates.size()));
        var state = PlayerState.KEY.get(next);
        state.clearPriorRole();
        RoleTransitions.takeOver(next, Roles.LEGION);
        state.joinedCampRole = Roles.LEGION.identifier().toString();
        state.sync();
        for (int slot = 0; slot < next.getInventory().size(); slot++)
            if (next.getInventory().getStack(slot).isOf(BttItems.POISON_NEEDLE)) next.getInventory().removeStack(slot);
        RoleDefs.giveWithInitialCd(next, BttItems.POISON_NEEDLE);
    }
}
