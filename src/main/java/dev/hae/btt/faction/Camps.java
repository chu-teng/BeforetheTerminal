package dev.hae.btt.faction;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.role.Roles;
import net.minecraft.server.network.ServerPlayerEntity;


public final class Camps {
    private Camps() {}

    public static net.minecraft.text.MutableText name(Role role) {
        String path = role.identifier().getPath();
        return net.minecraft.text.Text.translatable("btt.campgroup." + path);
    }
    public static Role campRole(GameWorldComponent game, ServerPlayerEntity player) {
        if (dev.hae.btt.role.Werewolf.member(player)) return Roles.WEREWOLF;
        Role role = game.getRole(player);
        var state = PlayerState.KEY.get(player);
        if (Roles.campOf(game, player) == Roles.Faction.OUTSIDER) {
            if (state.isMessiahFollower()) return Roles.MESSIAH;
            if (state.isMajoRecruit()) return Roles.MAJO;
        }
        Role adopted = Roles.byId(state.joinedCampRole);
        return adopted != null && Roles.campOf(game, player) == Roles.factionOf(adopted) ? adopted : role;
    }
    public static boolean sharesRoleCamp(GameWorldComponent game, ServerPlayerEntity player, Role role) {
        return campRole(game, player) == role && Roles.campOf(game, player) == Roles.factionOf(role);
    }
    public static boolean sameCamp(GameWorldComponent game, ServerPlayerEntity left, ServerPlayerEntity right) {
        var faction = Roles.campOf(game, left);
        return faction == Roles.campOf(game, right)
                && (faction == Roles.Faction.PASSENGER || faction == Roles.Faction.KILLER
                    || campRole(game, left) == campRole(game, right));
    }

    public static void joinCamp(ServerPlayerEntity player, ServerPlayerEntity sourcePlayer) {
        var game = GameWorldComponent.KEY.get(player.getWorld());
        if (game.getRole(player) == Roles.BLACKDEATH) return;
        var source = PlayerState.KEY.get(sourcePlayer);
        var state = PlayerState.KEY.get(player);
        Role role = campRole(game, sourcePlayer);
        String camp = switch (Roles.campOf(game, sourcePlayer)) {
            case PASSENGER -> Roles.CAMP_PASSENGER;
            case KILLER -> Roles.CAMP_KILLER;
            case SOLO -> Roles.CAMP_SOLO;
            case OUTSIDER -> Roles.CAMP_OUTSIDER;
        };
        state.messiahFollower = source.isMessiahFollower() || role == Roles.MESSIAH;
        state.majoRecruit = source.isMajoRecruit() || role == Roles.MAJO;
        state.joinedCampRole = role == null ? "" : role.identifier().toString();
        state.setCampOverride(camp);
        state.sync();
        dev.hae.btt.record.GameRecordManager.recordCampChange(player, camp, sourcePlayer);
    }
}
