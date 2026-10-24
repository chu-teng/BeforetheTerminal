package dev.hae.btt.role;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import dev.hae.btt.component.GameState;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.trait.TraitEffects;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;


public final class CampItems {
    private CampItems() {}

    public static void captureInitialWeapon(ServerPlayerEntity player) {
        var state = PlayerState.KEY.get(player);
        if (Roles.kindOf(Roles.byId(state.initialRole)) != Roles.Kind.ENFORCER) return;
        if (RoleDefs.hasItem(player, WatheItems.REVOLVER)) state.initialWeapon = Registries.ITEM.getId(WatheItems.REVOLVER).toString();
        else if (RoleDefs.hasItem(player, Items.BOW)) state.initialWeapon = "minecraft:bow";
    }

    public static void tick(ServerPlayerEntity player) {
        var game = GameWorldComponent.KEY.get(player.getWorld());
        if (game.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE) return;
        var faction = Roles.campOf(game, player);
        if (faction == null) return;
        var state = PlayerState.KEY.get(player);
        String camp = faction.name();
        if (!camp.equals(state.weaponCamp)) {
            boolean passenger = Roles.isPassengerCampFor(game, player);
            if (!state.initialWeapon.isEmpty()) {
                if (passenger && !state.weaponCamp.equals("PASSENGER")) {

                    removeAll(player, WatheItems.REVOLVER);
                } else if (!passenger && state.weaponCamp.equals("PASSENGER")) {
                    Item weapon = Registries.ITEM.get(Identifier.of(state.initialWeapon));
                    if (!RoleDefs.hasItem(player, weapon)) RoleDefs.giveWithInitialCd(player, weapon);
                }
            }
            state.weaponCamp = camp;
            state.sync();
        }
        String host = TraitEffects.hostFor(dev.hae.btt.faction.Camps.campRole(game, player));
        if (!state.epilogueCrowbarGiven && !host.isEmpty()
                && Roles.campOf(game, player) == Roles.Faction.OUTSIDER
                && host.equals(GameState.KEY.get(player.getWorld()).epilogueType)
                && GameFunctions.isPlayerAliveAndSurvival(player)) {
            state.epilogueCrowbarGiven = true;
            if (!RoleDefs.hasItem(player, WatheItems.CROWBAR)) RoleDefs.giveWithInitialCd(player, WatheItems.CROWBAR);
            state.sync();
        }
    }

    private static void removeAll(ServerPlayerEntity player, Item item) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            if (player.getInventory().getStack(i).isOf(item)) player.getInventory().removeStack(i);
        }
    }
}
