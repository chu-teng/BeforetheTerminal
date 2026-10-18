package dev.hae.btt.role;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.game.GameEvents;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.mixin.game.ShopBuyMixin;






public final class Decoy {
    private Decoy() {}

    public static void assignAll(ServerWorld world, GameWorldComponent gwc) {
        List<ServerPlayerEntity> all = new ArrayList<>(world.getPlayers());
        List<Role> assigned = new ArrayList<>();
        for (ServerPlayerEntity p : all) {
            Role r = gwc.getRole(p);
            if (r != null) assigned.add(r);
        }
        for (ServerPlayerEntity p : all) {
            Role role = gwc.getRole(p);
            RoleDef definition = RoleDefs.get(role);
            if (definition != null) definition.openRound(p, assigned);
        }
    }

    public static void marionette(ServerPlayerEntity player, List<Role> assigned) {
        Role decoy = pickUnplayed(civilianPool(), assigned, player);
        if (decoy == null) return;
        setDecoy(player, decoy);
        RoleTransitions.replace(player, decoy, 0, Roles.CAMP_KILLER, false);
        var definition = RoleDefs.get(decoy);
        if (definition != null) definition.dispatchEquipment(player);
        PlayerState.KEY.get(player).applySourceDrunk(player);
    }

    public static void lunatic(ServerPlayerEntity player, List<Role> assigned) {
        Role decoy = pickUnplayed(principalPool(), assigned, player);
        if (decoy == null) return;
        setDecoy(player, decoy);
        RoleTransitions.replace(player, decoy, 0, Roles.CAMP_PASSENGER, false);
    }


    public static boolean passengerFacade(PlayerEntity player) {
        return player != null && SeatAssignment.isBttMode(player.getWorld())
                && Roles.hasCurrentOrPriorRole(GameWorldComponent.KEY.get(player.getWorld()), player, Roles.MARIONETTE);
    }


    public static Role displayRole(PlayerEntity player, Role real) {
        Role decoy = byId(PlayerState.KEY.get(player).decoyRole);
        return decoy != null ? decoy : real;
    }


    public static boolean isDecoyed(PlayerEntity player) {
        return byId(PlayerState.KEY.get(player).decoyRole) != null;
    }




    private static List<Role> civilianPool() {
        List<Role> pool = new ArrayList<>();
        for (Role r : Roles.allRoles()) {
            if (Roles.factionOf(r) == Roles.Faction.PASSENGER && Roles.kindOf(r) != Roles.Kind.MAD
                    && r != Roles.EX_UNDERCOVER && Roles.isImplemented(r)) pool.add(r);
        }
        return pool;
    }


    private static List<Role> principalPool() {
        List<Role> pool = new ArrayList<>();
        for (Role r : Roles.allRoles()) {
            if (Roles.kindOf(r) == Roles.Kind.PRINCIPAL) pool.add(r);
        }
        return pool;
    }


    private static Role pickUnplayed(List<Role> pool, List<Role> assigned, ServerPlayerEntity player) {
        List<Role> free = new ArrayList<>();
        for (Role r : pool) {
            if (!assigned.contains(r)) free.add(r);
        }
        if (free.isEmpty()) return null;
        return free.get(player.getRandom().nextInt(free.size()));
    }

    private static void setDecoy(ServerPlayerEntity player, Role decoy) {
        if (decoy == null) return;
        PlayerState pc = PlayerState.KEY.get(player);
        pc.decoyRole = decoy.identifier().toString();
        pc.sync();
    }

    private static Role byId(String id) {
        return (id == null || id.isEmpty()) ? null : Roles.byId(id);
    }
}
