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
            if (Traits.has(player, Traits.TRUST)) PlayerState.KEY.get(player).traitTrustAt = world.getTime() + 100;
            if (Traits.has(player, Traits.UPRISING))
                dev.hae.btt.role.RoleDefs.giveWithInitialCd(player, WatheItems.KNIFE);
        }
    }

    public static boolean speaking(PlayerEntity player) {
        return PlayerState.KEY.get(player).speakingUntil > player.getWorld().getTime();
    }

    public static void tick(ServerPlayerEntity player) {
        if (!TraitRules.active(player)) return;
        var pc = PlayerState.KEY.get(player);
        if (pc.traitTrustAt <= 0 || player.getWorld().getTime() < pc.traitTrustAt) return;
        pc.traitTrustAt = 0;
        var game = GameWorldComponent.KEY.get(player.getWorld());
        var candidates = player.getServerWorld().getPlayers().stream()
                .filter(p -> p != player && game.getRole(p) != null && GameFunctions.isPlayerAliveAndSurvival(p)).toList();
        var same = candidates.stream().filter(p -> dev.hae.btt.faction.Camps.sameCamp(game, p, player)).toList();
        var pool = same.isEmpty() ? candidates : same;
        if (!pool.isEmpty()) {
            var recipient = pool.get(player.getRandom().nextInt(pool.size()));
            recipient.sendMessage(Text.translatable("btt.trait.trust.notice", GameState.KEY.get(player.getWorld()).numberOf(player.getUuid())), true);
        }
        pc.sync();
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
            PlayerState.KEY.get(target).applyPermanentDrunk();
        }
    }

    public static String hostFor(dev.doctor4t.wathe.api.Role role) {
        return dev.hae.btt.role.RoleDefs.hostFor(role);
    }

    public static void tickWorld(ServerWorld world) {
        var state = GameState.KEY.get(world);
        if (GameWorldComponent.KEY.get(world).getGameStatus() != GameWorldComponent.GameStatus.ACTIVE
                || state.epilogueType.isEmpty()) return;
        if (!state.traitResonance && world.getPlayers().stream().anyMatch(p -> GameFunctions.isPlayerAliveAndSurvival(p)
                && Traits.has(p, Traits.RESONANT))) {
            state.traitResonance = true;
            state.sync();
        }
        if (state.traitResonance) for (var player : world.getPlayers())
            if (GameFunctions.isPlayerAliveAndSurvival(player))
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 40, 0, false, false));
    }
}
