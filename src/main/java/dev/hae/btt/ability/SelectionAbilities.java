package dev.hae.btt.ability;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.network.ServerPlayerEntity;
import dev.hae.btt.component.AbilityState;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.role.Actor;
import dev.hae.btt.role.Drunk;
import dev.hae.btt.role.Folklorist;
import dev.hae.btt.role.Puppeteer;
import dev.hae.btt.role.RoleDefs;
import dev.hae.btt.role.Roles;


public final class SelectionAbilities {
    private SelectionAbilities() {}
    private static boolean validSingle(Role role) {
        var definition = RoleDefs.get(role);
        return definition != null && (definition.hasTargeted() || definition.hasNearby());
    }
    private static boolean validMulti(ServerPlayerEntity user, Role role, java.util.List<java.util.UUID> picks) {
        if (role == Roles.ACTOR) return picks.size() == 2;
        var definition = RoleDefs.get(role);
        return definition != null && definition.validMultiTargets(user, picks);
    }

    public static void select(ServerPlayerEntity user, java.util.UUID selectedTarget, String guess) {
        if (!SeatAssignment.isBttMode(user.getWorld())) return;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(user.getWorld());
        if (!gwc.isRunning()) return;
        if (!GameFunctions.isPlayerAliveAndSurvival(user)) return;
        var definition = RoleDefs.get(Roles.effectiveRole(gwc, user));
        if (definition != null && definition.chooseRole(user, guess)) return;




        boolean btt$drunkGate = PlayerState.KEY.get(user).isDrunk()
                && !Drunk.unaffectedByDrunk(Roles.effectiveRole(gwc, user));


        java.util.UUID skillTargetUuid = selectedTarget;
        ServerPlayerEntity skillTarget = skillTargetUuid == null ? null
                : (user.getServerWorld().getPlayerByUuid(skillTargetUuid) instanceof ServerPlayerEntity sp
                ? sp : null);
        if (skillTarget == user) skillTarget = null;
        dev.hae.btt.api.event.BttEvents.SkillUsed.EVENT.invoker().onSkillUsed(
                user, skillTarget, Roles.effectiveRole(gwc, user).identifier());
        if (definition != null && definition.hasSelf()) { definition.useSelf(user); return; }
        if (btt$drunkGate
                && !(Roles.effectiveRole(gwc, user) == Roles.HUNTER)
                && !(Roles.effectiveRole(gwc, user) == Roles.SNAKE_CHARMER)
                && !(Roles.effectiveRole(gwc, user) == Roles.NOVELIST)
                && !(Roles.effectiveRole(gwc, user) == Roles.ASSASSIN)
                && !(Roles.effectiveRole(gwc, user) == Roles.MESSIAH)) {
            AbilityRules.consumeDrunkCooldown(user, gwc);
            return;
        }
        Folklorist.onAnyoneAbility(user, gwc);
        var ordinaryDefinition = RoleDefs.get(Roles.effectiveRole(gwc, user));
        if (ordinaryDefinition != null && ordinaryDefinition.hasUntargeted()) {
            ordinaryDefinition.useUntargeted(user, guess);
            return;
        }

        if (Roles.effectiveRole(gwc, user) == Roles.SHOUJO) {
            Actor.toggle(user, PlayerState.KEY.get(user), Roles.SHOUJO);
            return;
        }
        if (!(user.getServerWorld().getPlayerByUuid(selectedTarget) instanceof ServerPlayerEntity target)) return;
        if (!validSingle(Roles.effectiveRole(gwc, user))) return;
        boolean readyForTarget = definition != null && definition.hasNearby()
                ? definition.nearbyReady(user) : AbilityState.KEY.get(user).cooldown <= 0;
        if (!readyForTarget) return;

        target = Puppeteer.applyRedirect(user, target);
        if (!dev.hae.btt.ability.AbilityRules.inTargetRange(user, target)) return;
        if (definition != null && definition.hasNearby()) {
            if (!definition.nearbyReady(user) || user.distanceTo(target) > 6.0
                    || !target.isAlive() || !GameFunctions.isPlayerAliveAndSurvival(target)) return;
            if (dev.hae.btt.role.Goon.interceptTarget(user, target)) {
                interceptedCost(user, gwc);
                return;
            }
            definition.useNearby(user, target);
            return;
        }
        if (validSingle(Roles.effectiveRole(gwc, user)) && AbilityState.KEY.get(user).cooldown <= 0 && dev.hae.btt.role.Goon.interceptTarget(user, target)) {
            interceptedCost(user, gwc);
            return;
        }
        if (definition != null && definition.hasTargeted()) { definition.useTargeted(user, target, guess); return; }

    }

    public static void multi(ServerPlayerEntity user, java.util.List<java.util.UUID> picks) {
        if (!SeatAssignment.isBttMode(user.getWorld())) return;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(user.getWorld());
        if (!gwc.isRunning()) return;
        if (!GameFunctions.isPlayerAliveAndSurvival(user) || AbilityState.KEY.get(user).cooldown > 0
                || !validMulti(user, Roles.effectiveRole(gwc, user), picks)) return;
        for (var uuid : picks) if (gwc.getRole(uuid) == null) return;
        picks = Puppeteer.redirectPicks(user, picks);

        var targets = Roles.effectiveRole(gwc, user) == Roles.ACTOR
                ? picks.subList(0, 1) : picks;
        for (var uuid : targets) {
            var selected = user.getServerWorld().getPlayerByUuid(uuid);
            if (selected instanceof ServerPlayerEntity target && dev.hae.btt.role.Goon.interceptTarget(user, target)) {
                dev.hae.btt.api.event.BttEvents.SkillUsed.EVENT.invoker().onSkillUsed(user, target, Roles.effectiveRole(gwc, user).identifier());
                interceptedCost(user, gwc);
                return;
            }
        }
        if ((Roles.effectiveRole(gwc, user) == Roles.ACTOR)) {
            if (!GameFunctions.isPlayerAliveAndSurvival(user) || picks.size() != 2
                    || AbilityState.KEY.get(user).cooldown > 0) return;
            var redirected = picks.get(0);
            if (redirected == null) return;
            var recipient = user.getServerWorld().getPlayerByUuid(redirected);
            var appearance = user.getServerWorld().getPlayerByUuid(picks.get(1));
            if (!(recipient instanceof ServerPlayerEntity living) || appearance == null
                    || !GameFunctions.isPlayerAliveAndSurvival(living) || gwc.getRole(living) == null
                    || gwc.getRole(appearance) == null) return;
            dev.hae.btt.api.event.BttEvents.SkillUsed.EVENT.invoker().onSkillUsed(user, living, Roles.ACTOR.identifier());
            AbilityState.KEY.get(user).setCooldown(60 * 20);
            if (Drunk.isDrunk(user)) return;
            dev.hae.btt.component.DisguiseState.KEY.get(living).startMorph(appearance.getUuid(), 30 * 20);
            Folklorist.onAnyoneAbility(user, gwc);
            return;
        }

        boolean btt$drunk = PlayerState.KEY.get(user).isDrunk()
                && !Drunk.unaffectedByDrunk(Roles.effectiveRole(gwc, user));


        dev.hae.btt.api.event.BttEvents.SkillUsed.EVENT.invoker().onSkillUsed(
                user, null, Roles.effectiveRole(gwc, user).identifier());
        if (btt$drunk && !(Roles.effectiveRole(gwc, user) == Roles.CANNIBAL)) return;
        Folklorist.onAnyoneAbility(user, gwc);

        var definition = RoleDefs.get(Roles.effectiveRole(gwc, user));
        if (definition != null) definition.useMultiTargeted(user, gwc, picks);
    }




    private static void interceptedCost(ServerPlayerEntity user, GameWorldComponent gwc) {
        Role role = Roles.effectiveRole(gwc, user);
        if (role == Roles.HUNTER) {
            PlayerState.KEY.get(user).hunterShot = 1;
            PlayerState.KEY.get(user).sync();
            AbilityState.KEY.get(user).setCooldown(20 * 60 * 60);
            AbilityState.KEY.get(user).sync();
            return;
        }
        if (role == Roles.ACTOR || role == Roles.LEECH || role == Roles.MEDIUM) return;
        int cost = Drunk.fizzleCost(role);
        if (cost <= 0) cost = RoleDefs.CD_1MIN;
        AbilityState.KEY.get(user).setCooldown(cost);
        AbilityState.KEY.get(user).sync();
    }

}
