package dev.hae.btt.perception;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.component.CorpseState;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.role.Roles;
import dev.hae.btt.trait.Traits;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;


public final class CorpseView {
    private CorpseView() {}
    private static final Identifier[] BTT_DECOY_REASONS = {
            GameConstants.DeathReasons.GENERIC,
            GameConstants.DeathReasons.KNIFE,
            GameConstants.DeathReasons.GUN,
            GameConstants.DeathReasons.BAT,
            GameConstants.DeathReasons.GRENADE,
            GameConstants.DeathReasons.POISON,
            GameConstants.DeathReasons.FELL_OUT_OF_TRAIN,
            dev.hae.btt.combat.DeathReasons.MAGIC,
            dev.hae.btt.combat.DeathReasons.BOMB,
            dev.hae.btt.combat.DeathReasons.FLYING_SWORD,
            dev.hae.btt.combat.DeathReasons.COLLAPSE,
            dev.hae.btt.combat.DeathReasons.DAGGER,
    };


    public static NbtCompound forPlayer(CorpseState corpse, ServerPlayerEntity viewer) {
        NbtCompound view = new NbtCompound();
        var body = corpse.playerBodyEntity;
        var game = GameWorldComponent.KEY.get(body.getWorld());
        boolean observer = NativeRoleView.fullObserver(viewer);
        boolean blind = !observer && dev.hae.btt.encounter.Memory.isBlind(viewer);
        boolean autopsy = !blind && (game.isRole(viewer, Roles.DOCTOR) || game.isRole(viewer, Roles.DUCE)
                || Traits.has(viewer, Traits.ANATOMY) || GameFunctions.isPlayerSpectatingOrCreative(viewer));
        boolean faction = !blind && game.isRole(viewer, Roles.MORTICIAN);
        boolean escape = dev.hae.btt.combat.DeathReasons.ESCAPE.equals(corpse.deathReason);
        boolean drunk = !observer && PlayerState.KEY.get(viewer).isDrunk();
        Identifier reason = escape ? corpse.deathReason : GameConstants.DeathReasons.GENERIC;
        Role role = WatheRoles.CIVILIAN, first = role;
        int ageOffset = 0;
        if (autopsy && !escape && (observer || !corpse.scavenged)) {
            reason = corpse.deathReason;
            role = Roles.byId(corpse.technicalRole.toString());
            if (role == null) role = WatheRoles.CIVILIAN;
            first = Roles.byId(corpse.initialRole.toString());
            if (first == null || Roles.kindOf(first) == null) first = role;
            if (drunk) {
                reason = BTT_DECOY_REASONS[decoy(body, "reason", BTT_DECOY_REASONS.length)];
                ageOffset = decoy(body, "age", 121) - 60;
                var pool = new java.util.ArrayList<>(Roles.allRoles());
                pool.remove(role);
                if (!pool.isEmpty()) role = pool.get(decoy(body, "role", pool.size()));
                pool = new java.util.ArrayList<>(Roles.allRoles());
                pool.remove(role);
                if (!pool.isEmpty()) first = pool.get(decoy(body, "firstrole", pool.size()));
            }
        }
        boolean passenger = corpse.passengerAtDeath
                && !(!observer && corpse.neutralAtDeath && dev.hae.btt.role.Werewolf.active(game, viewer));
        if (drunk) passenger = decoy(body, "corpse", 2) == 1;
        view.putString("deathReason", reason.toString());
        view.putString("technicalRole", role.identifier().toString());
        view.putString("initialRole", first.identifier().toString());
        view.putBoolean("scavenged", !observer && corpse.scavenged);
        view.putBoolean("observedAutopsy", autopsy);
        view.putBoolean("observedPassengerKnown", faction);
        view.putBoolean("observedPassenger", faction && passenger);
        int age = body.age / 20;
        if (drunk) age = Math.max(1, age + ageOffset);
        view.putInt("observedAgeSeconds", autopsy && !escape && (observer || !corpse.scavenged) ? age : 0);
        return view;
    }


    public static void observe(ServerPlayerEntity viewer) {
        var game = GameWorldComponent.KEY.get(viewer.getWorld());
        if (!game.isRunning() || game.getRole(viewer) != Roles.DOCTOR) return;
        var body = dev.hae.btt.ability.CorpseTargets.aimed(viewer);
        if (body == null) return;
        var view = forPlayer(CorpseState.KEY.get(body), viewer);
        String heretic = Roles.HERETIC.identifier().toString();
        if (heretic.equals(view.getString("technicalRole")) || heretic.equals(view.getString("initialRole")))
            dev.hae.btt.advancement.BttAdvancements.fact(viewer, "heretic_hint");
    }

    private static int decoy(PlayerBodyEntity body, String salt, int bound) {
        if (bound <= 0) return 0;
        long seed = body.getUuid().getMostSignificantBits() ^ body.getUuid().getLeastSignificantBits()
                ^ ((long) salt.hashCode() * 0x9E3779B97F4A7C15L);
        return new java.util.Random(seed).nextInt(bound);
    }
}
