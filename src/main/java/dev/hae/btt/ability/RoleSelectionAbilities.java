package dev.hae.btt.ability;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import dev.hae.btt.component.AbilityState;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.victory.Endings;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.role.RoleTransitions;
import dev.hae.btt.registry.Sounds;
import dev.hae.btt.combat.DeathReasons;
import dev.hae.btt.role.Drunk;
import dev.hae.btt.role.RoleDefs;
import dev.hae.btt.role.Roles;


public final class RoleSelectionAbilities {
    private RoleSelectionAbilities() {}



    public static void novelist(ServerPlayerEntity user, ServerPlayerEntity target, String guess) {
        GameWorldComponent gwc = GameWorldComponent.KEY.get(user.getWorld());
        Role guessed = gwc.getRole(target);

        AbilityState ability = AbilityState.KEY.get(user);
        if (ability.cooldown > 0) return;

        if (Drunk.isDrunk(user)) {
            dev.hae.btt.advancement.BttAdvancements.guess(user, false);
            GameFunctions.killPlayer(user, true, user, DeathReasons.FAILURE);
            return;
        }
        dev.hae.btt.advancement.BttAdvancements.guess(user, guessed != null && Roles.resolve(guess) == guessed);
        if (guessed != null && Roles.resolve(guess) == guessed) {
            PlayerState userComp = PlayerState.KEY.get(user);
            int hits = ++userComp.novelistHits;
            userComp.sync();

            for (ServerPlayerEntity listener : user.getServerWorld().getPlayers()) {
                listener.playSoundToPlayer(Sounds.NOVELIST_CORRECT,
                        net.minecraft.sound.SoundCategory.PLAYERS, 1.0F, 1.0F);
            }

            broadcast(user, Text.translatable("btt.action.novelist.correct").withColor(AbilityRules.roleColor(user)));

            if (hits * 3 >= user.getServerWorld().getPlayers().size()) {
                dev.hae.btt.victory.SoloWin.request(user.getServerWorld(), Endings.Ending.NOVELIST_WIN, user);
            }
        } else {

            GameFunctions.killPlayer(user, true, user, DeathReasons.FAILURE);
        }
    }



    public static void messiah(ServerPlayerEntity user, ServerPlayerEntity target, String guess) {
        GameWorldComponent gwc = GameWorldComponent.KEY.get(user.getWorld());

        AbilityState ability = AbilityState.KEY.get(user);
        if (ability.cooldown > 0) return;
        AbilityRules.setCooldown(ability, RoleDefs.CD_1MIN);


        if (Drunk.isDrunk(user)) {
            dev.hae.btt.advancement.BttAdvancements.guess(user, false);
            target.sendMessage(Text.translatable("btt.action.messiah.reveal", user.getName().getString())
                    .formatted(net.minecraft.util.Formatting.GRAY), true);
            return;
        }
        Role guessed = gwc.getRole(target);
        dev.hae.btt.advancement.BttAdvancements.guess(user, guessed != null && Roles.resolve(guess) == guessed);
        if (guessed != null && Roles.resolve(guess) == guessed) {
            if (!PlayerState.KEY.get(target).isMessiahFollower()) dev.hae.btt.advancement.BttAdvancements.recruit(user, target);
            PlayerState.KEY.get(target).setMessiahFollower(true);

            dev.hae.btt.record.GameRecordManager.recordCampChange(target, "MESSIAH", user);

            target.sendMessage(Text.translatable("btt.action.messiah.joined")
                    .formatted(net.minecraft.util.Formatting.GRAY), true);
        } else {

            target.sendMessage(Text.translatable("btt.action.messiah.reveal", user.getName().getString())
                    .formatted(net.minecraft.util.Formatting.GRAY), true);
        }
    }



    public static void assassin(ServerPlayerEntity user, ServerPlayerEntity target, String guess) {
        GameWorldComponent gwc = GameWorldComponent.KEY.get(user.getWorld());
        Role guessed = gwc.getRole(target);

        AbilityState ability = AbilityState.KEY.get(user);
        if (ability.cooldown > 0) return;


        if (!dev.hae.btt.role.GuessPool.isAllowed(Roles.ASSASSIN, Roles.resolve(guess),
                dev.hae.btt.role.GuessPool.scriptOf(user.getWorld()))) {
            return;
        }
        AbilityRules.setCooldown(ability, GameConstants.getInTicks(1, 0));

        if (Drunk.isDrunk(user)) {
            dev.hae.btt.advancement.BttAdvancements.guess(user, false);
            user.sendMessage(Text.translatable("btt.action.assassin.fail", target.getName()).withColor(AbilityRules.roleColor(user)), true);
            target.sendMessage(Text.translatable("btt.action.assassin.miss", user.getName().getString())
                    .formatted(net.minecraft.util.Formatting.GRAY), true);
            return;
        }

        dev.hae.btt.advancement.BttAdvancements.guess(user, guessed != null && Roles.resolve(guess) == guessed);
        if (guessed != null && Roles.resolve(guess) == guessed) {
            user.sendMessage(Text.translatable("btt.action.assassin.hit", target.getName()).withColor(AbilityRules.roleColor(user)), true);
            GameFunctions.killPlayer(target, true, user, DeathReasons.MAGIC);
        } else {

            user.sendMessage(Text.translatable("btt.action.assassin.fail", target.getName())
                    .withColor(AbilityRules.roleColor(user)), true);
            target.sendMessage(Text.translatable("btt.action.assassin.miss", user.getName().getString())
                    .formatted(net.minecraft.util.Formatting.GRAY), true);
        }
    }





    public static void sculptor(ServerPlayerEntity user, ServerPlayerEntity target, String guess) {
        GameWorldComponent gwc = GameWorldComponent.KEY.get(user.getWorld());

        AbilityState ability = AbilityState.KEY.get(user);
        if (ability.cooldown > 0) return;

        Role chosen = Roles.resolve(guess);
        Role targetRole = gwc.getRole(target);
        if (chosen == null || targetRole == null) return;
        boolean targetOk = Roles.isPassengerCamp(targetRole) || Roles.isKillerCamp(targetRole);

        boolean chosenOk = dev.hae.btt.role.GuessPool.isAllowed(Roles.SCULPTOR, chosen,
                dev.hae.btt.role.GuessPool.scriptOf(user.getWorld()));
        if (!targetOk || !chosenOk) {

            return;
        }
        AbilityRules.setCooldown(ability, RoleDefs.CD_2MIN);
        if (!gwc.getAllWithRole(chosen).isEmpty()) {
            user.sendMessage(Text.translatable("btt.action.sculptor.present",
                            SeatAssignment.displayName(chosen).getString())
                    .withColor(AbilityRules.roleColor(user)), true);
            return;
        }
        String camp = Roles.campNameOf(targetRole);
        RoleTransitions.takeOver(target, chosen);
        if (!camp.isEmpty()) {
            PlayerState tc = PlayerState.KEY.get(target);
            tc.setCampOverride(camp);
            tc.sync();
        }
        user.sendMessage(Text.translatable("btt.action.sculptor.changed",
                        target.getName().getString(), SeatAssignment.displayName(chosen).getString())
                .withColor(AbilityRules.roleColor(user)), true);
    }

    private static void broadcast(ServerPlayerEntity any, Text text) {
        for (ServerPlayerEntity p : any.getServerWorld().getPlayers()) {
            p.sendMessage(text, true);
        }
    }





    public static void bartender(ServerPlayerEntity user, String guess) {
        AbilityState ability = AbilityState.KEY.get(user);
        if (ability.cooldown > 0) return;
        Role chosen = Roles.resolve(guess);
        AbilityRules.setCooldown(ability, GameConstants.getInTicks(2, 0));
        if (chosen == null) return;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(user.getWorld());
        for (ServerPlayerEntity p : user.getServerWorld().getPlayers()) {
            if (!chosen.equals(gwc.getRole(p))) continue;
            PlayerState.KEY.get(p).applyDrunk(GameConstants.getInTicks(2, 0));
        }

    }


    public static void philosopher(ServerPlayerEntity user, String guess) {
        AbilityState ability = AbilityState.KEY.get(user);
        if (ability.cooldown > 0) return;

        Role chosen = Roles.resolve(guess);

        if (!dev.hae.btt.role.GuessPool.isAllowed(Roles.PHILOSOPHER, chosen,
                dev.hae.btt.role.GuessPool.scriptOf(user.getWorld()))) {

            return;
        }
        AbilityRules.setCooldown(ability, RoleDefs.CD_1MIN);
        for (ServerPlayerEntity target : user.getServerWorld().getPlayers()) {
            if (!GameFunctions.isPlayerAliveAndSurvival(target) || Roles.realRole(GameWorldComponent.KEY.get(user.getWorld()), target) != chosen) continue;
            PlayerState state = PlayerState.KEY.get(target);
            state.applySourceDrunk(user);
        }
        RoleTransitions.takeOver(user, chosen);
        user.sendMessage(Text.translatable("btt.action.philosopher.become",
                        SeatAssignment.displayName(chosen).getString())
                .withColor(chosen.color()), true);
    }
}
