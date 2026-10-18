package dev.hae.btt.ability;

import dev.hae.btt.component.CurseState;
import dev.hae.btt.role.Folklorist;
import dev.hae.btt.role.Puppeteer;
import java.util.List;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import dev.hae.btt.component.AbilityState;
import java.util.ArrayList;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.combat.DeathReasons;
import dev.hae.btt.role.Drunk;
import dev.hae.btt.role.RoleDefs;
import dev.hae.btt.role.Roles;
import dev.hae.btt.mechanic.DelayedAbility;


public final class TargetedAbilities {
    private TargetedAbilities() {}



    public static void hunter(ServerPlayerEntity user, ServerPlayerEntity target) {
        GameWorldComponent gwc = GameWorldComponent.KEY.get(user.getWorld());
        if (PlayerState.KEY.get(user).hunterShot == 1) {
            return;
        }
        PlayerState.KEY.get(user).hunterShot = 1;

        AbilityState huntAbility = AbilityState.KEY.get(user);
        huntAbility.setCooldown(20 * 60 * 60);
        huntAbility.sync();


        if (Drunk.isDrunk(user)) {
            user.sendMessage(Text.translatable("btt.action.hunter.miss", target.getName()).withColor(AbilityRules.roleColor(user)), true);
            return;
        }
        Role targetRole = gwc.getRole(target);
        if (targetRole != null && Roles.kindOf(targetRole) == Roles.Kind.PRINCIPAL) {


            user.sendMessage(Text.translatable("btt.action.hunter.success", target.getName()).withColor(AbilityRules.roleColor(user)), true);

            GameFunctions.killPlayer(target, true, user, DeathReasons.MAGIC);
        } else {
            user.sendMessage(Text.translatable("btt.action.hunter.miss", target.getName()).withColor(AbilityRules.roleColor(user)), true);
        }
    }



    public static void snakeCharmer(ServerPlayerEntity user, ServerPlayerEntity target) {
        GameWorldComponent gwc = GameWorldComponent.KEY.get(user.getWorld());
        AbilityState ability = AbilityState.KEY.get(user);
        if (ability.cooldown > 0) return;
        AbilityRules.setCooldown(ability, GameConstants.getInTicks(1, 0));


        if (Drunk.isDrunk(user)) {
            user.sendMessage(Text.translatable("btt.action.snake_charmer.wrong", target.getName()).withColor(AbilityRules.roleColor(user)), true);
            return;
        }
        Role targetRole = gwc.getRole(target);
        if (targetRole != null && Roles.kindOf(targetRole) == Roles.Kind.PRINCIPAL) {
            Role mine = gwc.getRole(user);
            swapInventories(user, target);
            swapBalances(user, target);
            gwc.addRole(user.getUuid(), targetRole);
            gwc.addRole(target.getUuid(), mine);
            gwc.sync();

            dev.hae.btt.trait.relationship.Relationships.onCampChanged(user);
            dev.hae.btt.trait.relationship.Relationships.onCampChanged(target);

            AbilityState newCharmer = AbilityState.KEY.get(target);
            newCharmer.setCooldown(RoleDefs.CD_1MIN);
            newCharmer.sync();

            PlayerState.KEY.get(target).applyPermanentDrunk(); PlayerState.KEY.get(target).sync();

            user.sendMessage(Text.translatable("btt.action.snake_charmer.swap",
                    target.getName().getString()).withColor(AbilityRules.roleColor(user)), true);
        } else {

            user.sendMessage(Text.translatable("btt.action.snake_charmer.wrong", target.getName()).withColor(AbilityRules.roleColor(user)), true);
        }
    }







    public static void virginHonor(ServerPlayerEntity user, ServerPlayerEntity target) {
        AbilityState ability = AbilityState.KEY.get(user);
        if (ability.cooldown > 0) return;
        if (target == user) return;
        if (user.distanceTo(target) > 6) return;
        PlayerState targetState = PlayerState.KEY.get(target);
        if (targetState.virginHonored) return;
        targetState.virginHonored = true;
        targetState.sync();
        AbilityRules.setCooldown(ability, RoleDefs.CD_1MIN);
    }





    public static void starMark(ServerPlayerEntity user, ServerPlayerEntity target) {
        AbilityState ability = AbilityState.KEY.get(user);
        if (ability.cooldown > 0) return;
        PlayerState pc = PlayerState.KEY.get(user);
        if (pc.starMark.equals(target.getUuidAsString())) return;
        pc.starMark = target.getUuidAsString();
        pc.sync();
        AbilityRules.setCooldown(ability, RoleDefs.CD_30S);
    }


    public static void revenge(ServerPlayerEntity user, ServerPlayerEntity target) {
        AbilityState ability = AbilityState.KEY.get(user);
        if (ability.cooldown > 0) return;
        AbilityRules.setCooldown(ability, RoleDefs.CD_1MIN);
        dev.hae.btt.component.CurseState.KEY.get(user).setTarget(target.getUuid());

    }

    public static void journalist(ServerPlayerEntity user, ServerPlayerEntity target) {
        AbilityState ability = AbilityState.KEY.get(user);
        if (ability.cooldown > 0) return;
        AbilityRules.setCooldown(ability, RoleDefs.CD_1MIN);
        PlayerState c = PlayerState.KEY.get(user);
        c.markedTarget = target.getUuid().toString();
        c.sync();

    }

    public static void isolation(ServerPlayerEntity user, ServerPlayerEntity target) {
        AbilityState ability = AbilityState.KEY.get(user);
        if (ability.cooldown > 0) return;
        if (user.distanceTo(target) > 6) return;
        if (!DelayedAbility.schedule(user, DelayedAbility.ISOLATION, target)) return;
        AbilityRules.setCooldown(ability, RoleDefs.CD_30S);

    }



    public static void detective(ServerPlayerEntity user, ServerPlayerEntity target) {
        GameWorldComponent gwc = GameWorldComponent.KEY.get(user.getWorld());
        AbilityState ability = AbilityState.KEY.get(user);
        if (ability.cooldown > 0) return;

        if (user.distanceTo(target) > 6) return;
        AbilityRules.setCooldown(ability, GameConstants.getInTicks(1, 0));

        boolean killed = Drunk.invertIfDrunk(user, PlayerState.KEY.get(target).hasKilled > 0);
        user.sendMessage(Text.translatable(killed
                        ? "btt.action.detective.killed" : "btt.action.detective.clean",
                target.getName().getString()).withColor(AbilityRules.roleColor(user)), true);
    }




    public static void investigator(ServerPlayerEntity user, ServerPlayerEntity target) {
        GameWorldComponent gwc = GameWorldComponent.KEY.get(user.getWorld());
        AbilityState ability = AbilityState.KEY.get(user);
        if (ability.cooldown > 0) return;
        if (user.distanceTo(target) > 6) return;
        Role targetRole = gwc.getRole(target);
        if (targetRole == null) return;

        java.util.List<Role> passengers = new ArrayList<>();
        java.util.List<Role> killers = new ArrayList<>();
        for (ServerPlayerEntity p : user.getServerWorld().getPlayers()) {
            Role r = gwc.getRole(p);
            if (r == null || r == targetRole) continue;
            if (Roles.isKillerCamp(r)) killers.add(r);
            else if (Roles.isPassengerCamp(r)) passengers.add(r);
        }
        Role shownPassenger;
        Role shownKiller;

        boolean drunkLie = Drunk.isDrunk(user);
        if (!drunkLie && Roles.isKillerCamp(targetRole)) {
            shownKiller = targetRole;
            shownPassenger = randomOf(passengers, user);
        } else if (!drunkLie && Roles.isPassengerCamp(targetRole)) {
            shownPassenger = targetRole;
            shownKiller = randomOf(killers, user);
        } else {

            shownPassenger = randomOf(passengers, user);
            shownKiller = randomOf(killers, user);
        }
        if (shownPassenger == null || shownKiller == null) return;
        AbilityRules.setCooldown(ability, RoleDefs.CD_1MIN);
        if (shownPassenger == Roles.HERETIC || shownKiller == Roles.HERETIC) dev.hae.btt.advancement.BttAdvancements.fact(user, "heretic_hint");
        boolean passengerFirst = user.getRandom().nextBoolean();
        Role first = passengerFirst ? shownPassenger : shownKiller;
        Role second = passengerFirst ? shownKiller : shownPassenger;
        user.sendMessage(Text.translatable("btt.action.investigator.result", target.getName(),
                        SeatAssignment.displayName(first).getString(), SeatAssignment.displayName(second).getString())
                .withColor(AbilityRules.roleColor(user)), true);
    }



    public static void pharmacist(ServerPlayerEntity user, ServerPlayerEntity target) {
        AbilityState ability = AbilityState.KEY.get(user);
        if (ability.cooldown > 0) return;
        if (user.distanceTo(target) > 6) return;
        AbilityRules.setCooldown(ability, GameConstants.getInTicks(1, 0));
        PlayerPoisonComponent poison = PlayerPoisonComponent.KEY.get(target);
        if (poison.poisonTicks > 0) poison.reset();
        else PlayerMoodComponent.KEY.get(target).setMood(1.0f);
    }


    public static void lawyer(ServerPlayerEntity user, ServerPlayerEntity target) {
        AbilityState ability = AbilityState.KEY.get(user);
        if (ability.cooldown > 0) return;
        if (user.distanceTo(target) > 6) return;
        if (target == user) return;
        if (!PlayerState.KEY.get(target).applyLawyerShield()) {
            return;
        }
        dev.hae.btt.advancement.BttAdvancements.protection(user, target);
        AbilityRules.setCooldown(ability, GameConstants.getInTicks(2, 0));
    }


    private static Role randomOf(java.util.List<Role> pool, ServerPlayerEntity user) {
        return pool.isEmpty() ? null : pool.get(user.getRandom().nextInt(pool.size()));
    }


    private static void swapInventories(ServerPlayerEntity a, ServerPlayerEntity b) {
        var ia = a.getInventory();
        var ib = b.getInventory();
        int size = Math.min(ia.size(), ib.size());
        for (int i = 0; i < size; i++) {
            ItemStack sa = ia.getStack(i).copy();
            ItemStack sb = ib.getStack(i).copy();
            ia.setStack(i, sb);
            ib.setStack(i, sa);
        }
        ia.markDirty();
        ib.markDirty();
    }


    private static void swapBalances(ServerPlayerEntity a, ServerPlayerEntity b) {
        PlayerShopComponent pa = PlayerShopComponent.KEY.get(a);
        PlayerShopComponent pb = PlayerShopComponent.KEY.get(b);
        int ba = pa.balance;
        int bb = pb.balance;
        pa.setBalance(bb);
        pb.setBalance(ba);
    }

    public static void witch(ServerPlayerEntity user, java.util.UUID selectedTarget) {
        if (!AbilityRules.canAct(user)) return;
        var world = user.getWorld();
        GameWorldComponent gwc = GameWorldComponent.KEY.get(world);
        if (!Roles.hasCurrentRole(gwc, user, Roles.WITCH)) return;
        AbilityState ability = AbilityState.KEY.get(user);

        if (!gwc.isRunning() || !dev.doctor4t.wathe.game.GameFunctions.isPlayerAliveAndSurvival(user) || ability.cooldown > 0) return;

        java.util.UUID target = Puppeteer.redirectUuid(user, selectedTarget);
        if (target == null || world.getPlayerByUuid(target) == null) return;

        if (Roles.hasCurrentRole(gwc, user, Roles.WITCH)) {
            if (ability.cooldown > 0) return;
            ability.setCooldown(GameConstants.getInTicks(0, 30));
            ability.sync();


            if (Drunk.isDrunk(user)) return;
            if (dev.hae.btt.role.Goon.interceptTarget(user, targetPlayer(world, target))) {
                AbilityRules.recordUse(user, targetPlayer(world, target), Roles.WITCH);
                return;
            }
            CurseState.KEY.get(user).setTarget(target);
            Folklorist.onAnyoneAbility(user, gwc);
            AbilityRules.recordUse(user, targetPlayer(world, target), Roles.WITCH);
            return;
        }

    }

    private static ServerPlayerEntity targetPlayer(net.minecraft.world.World world, java.util.UUID uuid) {
        if (uuid == null || world == null) return null;
        return world.getPlayerByUuid(uuid) instanceof ServerPlayerEntity sp ? sp : null;
    }

}
