package dev.hae.btt.ability;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import dev.hae.btt.component.AbilityState;
import java.util.ArrayList;
import dev.hae.btt.registry.Sounds;
import dev.hae.btt.combat.DeathReasons;
import dev.hae.btt.role.Drunk;
import dev.hae.btt.role.RoleDefs;
import dev.hae.btt.role.Roles;


public final class MultiTargetAbilities {
    private MultiTargetAbilities() {}








    public static void prophet(ServerPlayerEntity user, GameWorldComponent gwc, java.util.List<java.util.UUID> picks) {
        AbilityState ability = AbilityState.KEY.get(user);
        if (ability.cooldown > 0) return;
        java.util.List<java.util.UUID> chosen = picks;
        if (chosen.size() != 3) return;
        AbilityRules.setCooldown(ability, RoleDefs.CD_1MIN);
        boolean found = false;
        for (java.util.UUID uuid : chosen) {
            if (!(user.getServerWorld().getPlayerByUuid(uuid) instanceof ServerPlayerEntity p)) continue;

            if (Roles.kindOfReal(gwc, p) == Roles.Kind.PRINCIPAL) {
                found = true;
                break;
            }
        }
        user.sendMessage(Text.translatable(found
                        ? "btt.action.prophet.found"
                        : "btt.action.prophet.none", targetNames(user, chosen))
                .withColor(AbilityRules.roleColor(user)), true);
    }










    public static void tailor(ServerPlayerEntity user, GameWorldComponent gwc, java.util.List<java.util.UUID> picks) {
        AbilityState ability = AbilityState.KEY.get(user);
        if (ability.cooldown > 0) return;
        java.util.List<java.util.UUID> chosen = picks;
        if (chosen.size() != 2) return;
        AbilityRules.setCooldown(ability, RoleDefs.CD_1MIN);
        int passengers = 0;
        for (java.util.UUID uuid : chosen) {
            if (user.getServerWorld().getPlayerByUuid(uuid) instanceof ServerPlayerEntity p
                    && Roles.isPassengerCampFor(gwc, p)) {
                passengers++;
            }
        }
        boolean exactlyOne = Drunk.invertIfDrunk(user, passengers == 1);
        user.sendMessage(Text.translatable(exactlyOne
                        ? "btt.action.tailor.one"
                        : "btt.action.tailor.not_one", targetNames(user, chosen.subList(0, 1)), targetNames(user, chosen.subList(1, 2)))
                .withColor(AbilityRules.roleColor(user)), true);
    }



    public static void rigger(ServerPlayerEntity user, GameWorldComponent gwc, java.util.List<java.util.UUID> picks) {
        AbilityState riggerAbility = AbilityState.KEY.get(user);
        if (riggerAbility.cooldown > 0) return;
        java.util.List<ServerPlayerEntity> riggerTargets = twoAlive(user, picks);
        if (riggerTargets.size() < 2 && !(picks.size() == 2 && picks.get(0).equals(picks.get(1)))) return;
        AbilityRules.setCooldown(riggerAbility, RoleDefs.CD_1MIN);
        for (ServerPlayerEntity target : riggerTargets) {

            target.getServerWorld().playSound(null, target.getBlockPos(), Sounds.RIGGER_BIND,
                    net.minecraft.sound.SoundCategory.PLAYERS, 1.0F, 1.0F);
            target.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                    net.minecraft.entity.effect.StatusEffects.SLOWNESS,
                    GameConstants.getInTicks(0, 30), 250, false, false));
        }

    }



    public static void magician(ServerPlayerEntity user, java.util.List<java.util.UUID> picks) {
        AbilityState ability = AbilityState.KEY.get(user);
        if (ability.cooldown > 0) return;
        java.util.List<ServerPlayerEntity> targets = twoAlive(user, picks);
        if (targets.size() == 1 && picks.size() == 2 && picks.get(0).equals(picks.get(1))) {
            AbilityRules.setCooldown(ability, RoleDefs.CD_1MIN);
            return;
        }
        if (targets.size() < 2) return;
        ServerPlayerEntity a = targets.get(0);
        ServerPlayerEntity b = targets.get(1);
        var world = user.getServerWorld();

        if (!world.isSpaceEmpty(a) || !world.isSpaceEmpty(b)) {
            return;
        }
        var posA = a.getPos();
        var posB = b.getPos();
        a.refreshPositionAfterTeleport(posB.x, posB.y, posB.z);
        b.refreshPositionAfterTeleport(posA.x, posA.y, posA.z);
        AbilityRules.setCooldown(ability, RoleDefs.CD_1MIN);

        for (ServerPlayerEntity moved : targets) {
            moved.getServerWorld().playSound(null, moved.getBlockPos(), Sounds.SWAPPER_SWAP,
                    net.minecraft.sound.SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
        user.sendMessage(Text.translatable("btt.action.swapper.swapped",
                        a.getName().getString(), b.getName().getString())
                .withColor(AbilityRules.roleColor(user)), true);
    }











    public static void judge(ServerPlayerEntity user, GameWorldComponent gwc, java.util.List<java.util.UUID> picks) {
        AbilityState ability = AbilityState.KEY.get(user);
        if (ability.cooldown > 0) return;
        java.util.Set<java.util.UUID> accused = new java.util.LinkedHashSet<>(picks);
        java.util.Set<java.util.UUID> killers = new java.util.LinkedHashSet<>();
        for (ServerPlayerEntity p : user.getServerWorld().getPlayers()) {

            if (Roles.isKillerSeatFor(gwc, p)) killers.add(p.getUuid());
        }
        AbilityRules.setCooldown(ability, GameConstants.getInTicks(1, 0));
        if (killers.isEmpty() || !accused.equals(killers)) {
            user.sendMessage(Text.translatable("btt.action.judge.rejected").withColor(Roles.JUDGE.color()), true);
            return;
        }
        int executed = 0;
        for (java.util.UUID uuid : killers) {
            if (!(user.getServerWorld().getPlayerByUuid(uuid) instanceof ServerPlayerEntity victim)) continue;
            if (!GameFunctions.isPlayerAliveAndSurvival(victim)) continue;
            GameFunctions.killPlayer(victim, true, user, DeathReasons.MAGIC);
            if (!GameFunctions.isPlayerAliveAndSurvival(victim)) executed++;
        }
        user.sendMessage(Text.translatable("btt.action.judge.success", executed)
                .withColor(Roles.JUDGE.color()), true);
    }


    private static java.util.List<ServerPlayerEntity> twoAlive(ServerPlayerEntity user,
                                                               java.util.List<java.util.UUID> picks) {
        java.util.List<ServerPlayerEntity> targets = new java.util.ArrayList<>(2);
        for (java.util.UUID uuid : picks) {
            if (targets.size() >= 2) break;
            if (uuid == null) continue;
            if (!(user.getServerWorld().getPlayerByUuid(uuid) instanceof ServerPlayerEntity p)) continue;
            if (!GameFunctions.isPlayerAliveAndSurvival(p)) continue;
            if (!targets.contains(p)) targets.add(p);
        }
        return targets;
    }
    private static Text targetNames(ServerPlayerEntity user, java.util.List<java.util.UUID> picks) {
        var names = Text.empty();
        for (var uuid : picks) {
            var player = user.getServerWorld().getPlayerByUuid(uuid);
            if (player == null) continue;
            if (!names.getString().isEmpty()) names.append(", ");
            names.append(player.getName());
        }
        return names;
    }

}
