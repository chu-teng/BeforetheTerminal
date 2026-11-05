package dev.hae.btt.trait;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import dev.hae.btt.component.GameState;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.role.Roles;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Identifier;
import java.util.List;


public final class TraitEffects {
    private TraitEffects() {}

    public static void initialize(ServerWorld world, List<ServerPlayerEntity> players) {
        var game = GameWorldComponent.KEY.get(world);
        for (ServerPlayerEntity owner : players) if (Traits.has(owner, Traits.BOND)) {
            var candidates = players.stream().filter(p -> p != owner && Roles.isPassengerCampFor(game, p)).toList();
            if (!candidates.isEmpty()) dev.hae.btt.faction.Camps.joinCamp(candidates.get(owner.getRandom().nextInt(candidates.size())), owner);
        }
        for (ServerPlayerEntity player : players) {
            if (Traits.has(player, Traits.TRUST)) {
                var candidates = players.stream().filter(p -> p != player && Roles.isPassengerCampFor(game, p)).toList();
                if (!candidates.isEmpty()) dev.hae.btt.component.LetterState.KEY.get(candidates.get(player.getRandom().nextInt(candidates.size()))).setMark(player.getUuidAsString(), "PASSENGER");
            }
            if (Traits.has(player, Traits.PREMEDITATION)) for (var other : players)
                if (other != player && Roles.isKillerCampFor(game, other)) mark(player, other, game);
            if (Traits.has(player, Traits.WITNESS)) {
                var candidates = players.stream().filter(p -> Roles.kindOfReal(game, p) == Roles.Kind.ACCOMPLICE || Roles.kindOfReal(game, p) == Roles.Kind.PRINCIPAL).toList();
                if (!candidates.isEmpty()) dev.hae.btt.component.LetterState.KEY.get(player).setMark(candidates.get(player.getRandom().nextInt(candidates.size())).getUuidAsString(), "KILLER");
            }
        }
    }

    public static boolean speaking(PlayerEntity player) {
        return PlayerState.KEY.get(player).speakingUntil > player.getWorld().getTime();
    }

    private static void mark(ServerPlayerEntity viewer, ServerPlayerEntity target, GameWorldComponent game) {
        var role = Roles.realRole(game, target);
        if (role != null) dev.hae.btt.component.LetterState.KEY.get(viewer).setMark(target.getUuidAsString(), role.identifier().getPath());
    }

    public static void onDeath(ServerPlayerEntity victim, PlayerEntity killer, Identifier reason) {
        if (!TraitRules.active(victim)) return;
        if (killer instanceof ServerPlayerEntity player && player != victim
                && Traits.has(player, Traits.PLAYFUL) && speaking(victim))
            PlayerShopComponent.KEY.get(player).addToBalance(100);
        if (killer instanceof ServerPlayerEntity player && player != victim && Traits.has(victim, Traits.WISH))
            dev.hae.btt.faction.Camps.joinCamp(player, victim);
        if (!Traits.has(victim, Traits.CRUSH)) return;
        var game = GameWorldComponent.KEY.get(victim.getWorld());
        var targets = victim.getServerWorld().getPlayers().stream().filter(p -> p != victim && game.getRole(p) != null
                && GameFunctions.isPlayerAliveAndSurvival(p) && Roles.isPassengerCampFor(game, p)).toList();
        if (!targets.isEmpty()) {
            var target = targets.get(victim.getRandom().nextInt(targets.size()));
            PlayerState.KEY.get(target).applyDrunk(2 * 60 * 20);
        }
    }

    public static String hostFor(dev.doctor4t.wathe.api.Role role) {
        return dev.hae.btt.role.RoleDefs.hostFor(role);
    }

    public static void tickWorld(ServerWorld world) {
        var state = GameState.KEY.get(world);
        if (GameWorldComponent.KEY.get(world).getGameStatus() == GameWorldComponent.GameStatus.ACTIVE) {
            dev.hae.btt.registry.Sounds.tickEpilogue(world);
        }
        if (GameWorldComponent.KEY.get(world).getGameStatus() != GameWorldComponent.GameStatus.ACTIVE
                || state.epilogueType.isEmpty()) {
            if (state.traitResonance) {
                for (var player : world.getPlayers()) player.removeStatusEffect(StatusEffects.GLOWING);
                state.traitResonance = false;
                state.sync();
            }
            return;
        }
        for (var player : world.getPlayers()) {
            String key = "uprising:" + player.getUuidAsString();
            if (GameFunctions.isPlayerAliveAndSurvival(player) && Traits.has(player, Traits.UPRISING)
                    && !state.roleEffects.getBoolean(key)) {
                state.roleEffects.putBoolean(key, true);
                PlayerState.KEY.get(player).monkShields += 3;
                PlayerState.KEY.get(player).sync();
            }
        }
        if (!state.traitResonance && world.getPlayers().stream().anyMatch(p -> GameFunctions.isPlayerAliveAndSurvival(p)
                && Traits.has(p, Traits.RESONANT))) {
            state.traitResonance = true;
            state.sync();
        }
        if (state.traitResonance) for (var player : world.getPlayers()) {
            if (GameFunctions.isPlayerAliveAndSurvival(player)) {
                var effect = player.getStatusEffect(StatusEffects.GLOWING);
                if (effect == null || effect.getDuration() <= 20)
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 40, 0, false, false));
            } else {
                player.removeStatusEffect(StatusEffects.GLOWING);
            }
        }
    }
}
