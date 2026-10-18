package dev.hae.btt.ability;

import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.hae.btt.component.CorpseState;
import dev.hae.btt.component.ScavengerState;
import net.minecraft.sound.SoundCategory;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import dev.hae.btt.component.AbilityState;
import dev.hae.btt.component.BodyState;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.role.RoleTransitions;
import dev.hae.btt.registry.Sounds;
import dev.hae.btt.role.Drunk;
import dev.hae.btt.role.Roles;


public final class CorpseAbilities {
    private CorpseAbilities() {}
    public static void use(ServerPlayerEntity user, java.util.UUID bodyUuid) {
        if (!dev.hae.btt.ability.AbilityRules.canAct(user)) return;
        if (!SeatAssignment.isBttMode(user.getWorld())) return;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(user.getWorld());
        if (!gwc.isRunning()) return;
        var body = CorpseTargets.aimed(user);
        if (body == null || !body.getUuid().equals(bodyUuid)) return;
        if (Roles.effectiveRole(gwc, user) == Roles.THIEF) { scavenge(user, bodyUuid); return; }


        if ((Roles.effectiveRole(gwc, user) == Roles.AMNESIAC)) {
            if (BodyState.KEY.get(body).isAmnesiacUsed()) return;
            Role dead = Roles.byId(CorpseState.KEY.get(body).technicalRole.toString());
            if (dead == null) return;
            if (Drunk.isDrunk(user)) { AbilityRules.consumeDrunkCooldown(user, gwc); return; }
            AbilityRules.recordUse(user, null, Roles.AMNESIAC);
            dev.hae.btt.advancement.BttAdvancements.fact(user, "corpse_use");
            BodyState.KEY.get(body).markAmnesiacUsed();
            RoleTransitions.takeOver(user, dead);
            user.getServerWorld().playSound(null, body.getBlockPos(), Sounds.SCAVENGE,
                    net.minecraft.sound.SoundCategory.PLAYERS, 1.0F, 1.0F);
            user.sendMessage(RoleTransitions.tookOverText(dead), true);
            return;
        }

        if ((Roles.effectiveRole(gwc, user) == Roles.CANNIBAL)) {
            Role dead = Roles.byId(CorpseState.KEY.get(body).technicalRole.toString());

            if (!Roles.isInfoOrSupportPassenger(dead) || BodyState.KEY.get(body).isAmnesiacUsed()) return;
            AbilityState ability = AbilityState.KEY.get(user);
            if (ability.cooldown > 0) return;
            ability.setCooldown(GameConstants.getInTicks(1, 0));
            ability.sync();

            if (Drunk.isDrunk(user)) return;
            AbilityRules.recordUse(user, null, Roles.CANNIBAL);
            dev.hae.btt.advancement.BttAdvancements.fact(user, "corpse_use");
            BodyState.KEY.get(body).markAmnesiacUsed();



            RoleTransitions.replace(user, dead, GameConstants.getInTicks(1, 0), null, false);

            user.getServerWorld().playSound(null, body.getBlockPos(), Sounds.SCAVENGE,
                    net.minecraft.sound.SoundCategory.PLAYERS, 1.0F, 1.0F);
            user.sendMessage(Text.translatable("btt.action.cannibal.learn",
                            SeatAssignment.displayName(dead).getString())
                    .withColor(Roles.CANNIBAL.color()), true);
            return;
        }

    }

    public static void scavenge(ServerPlayerEntity user, java.util.UUID bodyUuid) {
        if (!dev.hae.btt.ability.AbilityRules.canAct(user)) return;
        if (!SeatAssignment.isBttMode(user.getWorld())) return;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(user.getWorld());
        if (!gwc.isRunning()) return;
        if (!Roles.hasCurrentRole(gwc, user, Roles.THIEF)) return;
        var body = CorpseTargets.aimed(user);
        if (body == null || !body.getUuid().equals(bodyUuid)) return;
        AbilityState ability = AbilityState.KEY.get(user);
        if (ability.cooldown > 0) return;
        CorpseState death = CorpseState.KEY.get(body);
        if (death.scavenged) return;

        ability.setCooldown(GameConstants.getInTicks(0, 10));
        ability.sync();
        if (Drunk.isDrunk(user)) {
            AbilityRules.recordUse(user, null, Roles.THIEF);
            return;
        }
        ScavengerState scavenger = ScavengerState.KEY.get(user);
        scavenger.bodiesEaten++;
        AbilityRules.recordUse(user, null, Roles.THIEF);
        scavenger.sync();
        dev.hae.btt.advancement.BttAdvancements.fact(user, "corpse_use");
        death.scavenged = true;
        user.getServerWorld().playSound(null, body.getBlockPos(), Sounds.SCAVENGE,
                SoundCategory.PLAYERS, 1.0F, 1.0F);
        PlayerState.KEY.get(user).setThiefReveal(GameConstants.getInTicks(0, 10));

        long players = user.getWorld().getPlayers().size();
        if (scavenger.bodiesEaten * 3 >= players) {

            dev.hae.btt.victory.SoloWin.request(user.getServerWorld(),
                    dev.hae.btt.victory.Endings.Ending.THIEF_WIN, user);
        }
    }

}
