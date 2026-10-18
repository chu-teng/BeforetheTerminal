package dev.hae.btt.role;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.hae.btt.component.AbilityState;
import dev.hae.btt.component.GameState;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.role.RoleTransitions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class Gambler {
    private Gambler() {}
    public static void initialize(ServerPlayerEntity player) {
        PlayerState state = PlayerState.KEY.get(player);
        var present = new java.util.HashSet<>(GameState.KEY.get(player.getWorld()).openingRoles.values());
        state.gamblerTarget = (int) GuessPool.candidates(Roles.GAMBLER).stream()
                .filter(role -> !present.contains(role.identifier().toString())).count() / 3;
        state.gamblerAttempted = "";
        state.gamblerGuesses = "";
        AbilityState.KEY.get(player).setCooldown(0);
        state.sync();
    }
    public static void guess(ServerPlayerEntity player, String id) {
        AbilityState ability = AbilityState.KEY.get(player);
        if (ability.cooldown > 0) return;
        var guessed = Roles.byId(id.contains(":") ? id : "btt:" + id);
        if (guessed == null || !Roles.isImplemented(guessed)) return;
        var state = PlayerState.KEY.get(player);
        String key = guessed.identifier().toString();
        var previous = new java.util.HashSet<>(java.util.List.of(state.gamblerAttempted.split(",")));
        if (previous.contains(key)) return;
        state.gamblerAttempted = state.gamblerAttempted.isEmpty() ? key : state.gamblerAttempted + "," + key;
        state.sync();
        boolean absent = !GameState.KEY.get(player.getWorld()).openingRoles.containsValue(key);
        dev.hae.btt.api.event.BttEvents.SkillUsed.EVENT.invoker().onSkillUsed(player, null, Roles.GAMBLER.identifier());
        dev.hae.btt.advancement.BttAdvancements.guess(player, absent && !Drunk.isDrunk(player));
        if (!absent || Drunk.isDrunk(player)) {
            ability.setCooldown(RoleDefs.CD_30S);
            ability.sync();
            return;
        }
        state.gamblerGuesses = state.gamblerGuesses.isEmpty() ? key : state.gamblerGuesses + "," + key;
        int progress = state.gamblerGuesses.split(",").length;
        state.sync();
        if (progress >= state.gamblerTarget) {
            state.clearPriorRole();
            state.setCampOverride("");
            RoleTransitions.takeOver(player, guessed);
        } else player.sendMessage(Text.translatable("btt.action.gambler.progress", progress, state.gamblerTarget)
                .withColor(Roles.GAMBLER.color()), true);
    }
}
