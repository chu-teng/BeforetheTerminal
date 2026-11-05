package dev.hae.btt.role;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.component.AbilityState;
import dev.hae.btt.component.GameState;
import dev.hae.btt.component.PlayerState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.util.List;
import java.util.UUID;


public final class Investigations {
    private Investigations() {}
    public static void death(ServerPlayerEntity victim, ServerPlayerEntity killer, Identifier reason) {
        var game = GameWorldComponent.KEY.get(victim.getWorld());
        var facts = GameState.KEY.get(victim.getWorld()).roleEffects;
        facts.putString("killer:" + victim.getUuidAsString(), killer == null ? "" : killer.getUuidAsString());
    }
    public static void barberDeath(ServerPlayerEntity victim, ServerPlayerEntity killer, Identifier reason) {
        var game = GameWorldComponent.KEY.get(victim.getWorld());
        var facts = GameState.KEY.get(victim.getWorld()).roleEffects;
        var kind = Roles.kindOfReal(game, victim);
        if (kind != null && kind.isNeutralKind()) {
            for (var player : victim.getServerWorld().getPlayers())
                if (GameFunctions.isPlayerAliveAndSurvival(player) && game.isRole(player, Roles.BARBER) && !Drunk.isDrunk(player))
                {
                    facts.putBoolean("barber:" + player.getUuidAsString(), true);
                    PlayerState.KEY.get(player).sync();
                    player.sendMessage(Text.translatable("btt.action.barber.ready").withColor(Roles.BARBER.color()), true);
                }
        }
    }
    public static void morticianDeath(ServerPlayerEntity victim, ServerPlayerEntity killer, Identifier reason) {
        var game = GameWorldComponent.KEY.get(victim.getWorld());
        if (victim.getServerWorld().getPlayers().stream().anyMatch(p ->
                GameFunctions.isPlayerAliveAndSurvival(p) && game.isRole(p, Roles.MORTICIAN) && !Drunk.isDrunk(p) && dev.hae.btt.role.Werewolf.matchesKind(game, p, victim, Roles.Kind.ACCOMPLICE))) {
            for (var player : victim.getServerWorld().getPlayers()) {
                PlayerState.KEY.get(player).applyDrunk(RoleDefs.CD_30S);
                PlayerState.KEY.get(player).sync();
            }
        }
    }
    public static void deduce(ServerPlayerEntity user, GameWorldComponent game, List<UUID> picks) {
        var first = user.getServerWorld().getPlayerByUuid(picks.get(0));
        var second = user.getServerWorld().getPlayerByUuid(picks.get(1));
        if (first == null || second == null) return;
        boolean success = !GameFunctions.isPlayerAliveAndSurvival(first)
                && second.getUuidAsString().equals(GameState.KEY.get(user.getWorld()).roleEffects.getString("killer:" + first.getUuidAsString()));
        success = Drunk.invertIfDrunk(user, success);
        var ability = AbilityState.KEY.get(user); ability.setCooldown(RoleDefs.CD_1MIN); ability.sync();
        user.sendMessage(Text.translatable(success ? "btt.action.detective.deduction.success" : "btt.action.detective.deduction.failed",
                first.getName().getString(), second.getName().getString()).withColor(Roles.DETECTIVE.color()), true);
    }
    public static void barber(ServerPlayerEntity user, GameWorldComponent game, List<UUID> picks) {
        var facts = GameState.KEY.get(user.getWorld()).roleEffects;
        String key = "barber:" + user.getUuidAsString();
        if (!facts.getBoolean(key)) return;
        var left = user.getServerWorld().getPlayerByUuid(picks.get(0));
        var right = user.getServerWorld().getPlayerByUuid(picks.get(1));
        if (!(left instanceof ServerPlayerEntity a) || !(right instanceof ServerPlayerEntity b)) return;
        var ra = game.getRole(a); var rb = game.getRole(b);
        if (ra == null || rb == null || Roles.kindOf(ra).isNeutralKind() || Roles.kindOf(rb).isNeutralKind()) return;
        facts.putBoolean(key, false);
        PlayerState.KEY.get(user).sync();
        String ca = Roles.campName(Roles.campOf(game, a)), cb = Roles.campName(Roles.campOf(game, b));
        game.addRole(a, rb); game.addRole(b, ra); game.sync();
        PlayerState.KEY.get(a).setCampOverride(ca); PlayerState.KEY.get(b).setCampOverride(cb);
        PlayerState.KEY.get(a).sync(); PlayerState.KEY.get(b).sync();
        RoleTransitions.reannounce(a, rb); RoleTransitions.reannounce(b, ra);
        dev.hae.btt.record.GameRecordManager.recordRoleChange(a, ra.identifier().toString(), rb.identifier().toString(), false, false);
        dev.hae.btt.record.GameRecordManager.recordRoleChange(b, rb.identifier().toString(), ra.identifier().toString(), false, false);
    }
}
