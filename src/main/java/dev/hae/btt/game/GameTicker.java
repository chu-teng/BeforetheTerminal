package dev.hae.btt.game;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import dev.hae.btt.component.PlayerState;
import dev.hae.btt.role.RoleTransitions;
import dev.hae.btt.role.Actor;
import dev.hae.btt.role.Arsonist;
import dev.hae.btt.role.Astral;
import dev.hae.btt.combat.DeathReasons;
import dev.hae.btt.role.Folklorist;
import dev.hae.btt.role.Guard;
import dev.hae.btt.role.Kidnapper;
import dev.hae.btt.role.ColdWave;
import dev.hae.btt.role.RoleDef;
import dev.hae.btt.role.RoleDefs;
import dev.hae.btt.role.Roles;
import dev.hae.btt.mechanic.Bomb;
import dev.hae.btt.mechanic.DelayedAbility;
import dev.hae.btt.mechanic.SpiritWalk;
import dev.hae.btt.trait.Traits;


public final class GameTicker {
    private GameTicker() {}
    private static final int MONEY_PASSIVE_CAP = 200;

    static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (var world : server.getWorlds()) {
                tickWorld(world);
                dev.hae.btt.perception.ObservationRefresh.tick(world);
            }
        });
    }

    static void tickWorld(ServerWorld serverWorld) {
        GameWorldComponent gwc = GameWorldComponent.KEY.get(serverWorld);
        for (ServerPlayerEntity player : serverWorld.getPlayers()) {
            if (gwc.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE)
                dev.hae.btt.map.MapEnhancements.tickJump(player, null);
            dev.hae.btt.role.Warden.tick(player);
            dev.hae.btt.role.Voodoo.tick(player);
            PlayerState state = PlayerState.KEY.get(player);
            if (state.consoleOpen && (!SeatAssignment.isBttMode(serverWorld) || !gwc.isRunning()
                    || !GameFunctions.isPlayerAliveAndSurvival(player) || !state.hasConsoleSession())) {
                state.closeConsole();
            }
        }
        if (!(gwc.getGameMode() instanceof BeforeTheTerminalGameMode)) return;
        dev.hae.btt.role.Smuggler.tickWorld(serverWorld);

        Debate.tick(serverWorld);
        if (!gwc.isRunning()) {

            SpiritWalk.abortAll(serverWorld.getPlayers());
            return;
        }

        dev.hae.btt.trait.TraitEffects.tickWorld(serverWorld);
        dev.hae.btt.role.Librarian.refreshListeners(serverWorld);
        dev.hae.btt.role.Harpy.tick(serverWorld);
        ColdWave.updateSanity(serverWorld);
        ColdWave.tickCorpseSources(serverWorld);
        for (var player : serverWorld.getPlayers()) {
            tickPlayer(player, serverWorld, gwc);
        }


        Bomb.tickWorld(serverWorld);


        for (var p : serverWorld.getPlayers()) {
            PlayerState c = PlayerState.KEY.get(p);
            if (c.engineerScanTicks <= 0) continue;
            if (!GameFunctions.isPlayerAliveAndSurvival(p)) {
                c.engineerScanTicks = 0;
                c.sync();
                continue;
            }
            c.engineerScanTicks--;
            c.sync();
        }


        for (var p : serverWorld.getPlayers()) {
            PlayerState c = PlayerState.KEY.get(p);
            if (c.thiefRevealTicks <= 0) continue;
            if (!GameFunctions.isPlayerAliveAndSurvival(p)) {
                c.thiefRevealTicks = 0;
                c.sync();
                continue;
            }
            c.thiefRevealTicks--;
            c.sync();
        }
    }

    private static void tickPlayer(ServerPlayerEntity player, ServerWorld serverWorld, GameWorldComponent gwc) {
        PlayerState pc = PlayerState.KEY.get(player);
        dev.hae.btt.role.CampItems.tick(player);
        if (pc.openingNoticeAt > 0 && serverWorld.getTime() >= pc.openingNoticeAt) {
            pc.openingNoticeAt = 0;
            var inherited = Roles.byId(pc.openingNoticeRole);
            if (inherited != null && GameFunctions.isPlayerAliveAndSurvival(player) && !dev.hae.btt.encounter.Memory.isBlind(player))
                player.sendMessage(net.minecraft.text.Text.translatable("btt.action.inherit.kit",
                        SeatAssignment.displayName(inherited).getString(), pc.openingNoticeLabel).withColor(inherited.color()), true);
            pc.openingNoticeRole = ""; pc.openingNoticeLabel = "";
        }
        pc.decrementDrunk();
        pc.decrementMute();
        pc.decrementVoicePitch();

        dev.hae.btt.map.MapEnhancements.tickGravity(player,
                dev.hae.btt.map.MapEnhancements.activeFor(serverWorld));



        dev.hae.btt.map.MapEnhancements.tickJump(player,
                dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(serverWorld).isRunning()
                        ? dev.hae.btt.map.MapEnhancements.activeFor(serverWorld) : null);



        if (GameFunctions.isPlayerAliveAndSurvival(player)
                && dev.doctor4t.wathe.cca.PlayerMoodComponent.KEY.get(player).getMood()
                        < dev.doctor4t.wathe.game.GameConstants.MID_MOOD_THRESHOLD) {
            pc.applyDrunk(2);
        }


        if (GameFunctions.isPlayerAliveAndSurvival(player)
                && dev.doctor4t.wathe.cca.PlayerMoodComponent.KEY.get(player).getMood() <= 0f
                && !Traits.has(player, Traits.HOLLOW)) {
            GameFunctions.killPlayer(player, true, null, DeathReasons.COLLAPSE);
        }

        if (Roles.hasCurrentRole(gwc, player, Roles.ASTRAL)) Astral.tick(player, gwc);

        ColdWave.tick(player, pc);
        Traits.tick(player);

        if (pc.starGlowTicks > 0 && --pc.starGlowTicks % 20 == 0) pc.sync();
        dev.hae.btt.role.Architect.tick(player);
        dev.hae.btt.role.Librarian.tickSpeaker(player);
        dev.hae.btt.game.Needs.tick(player);
        Traits.tickMood(player);
        Traits.tickTease(player);
        Arsonist.tickGasoline(player, pc);
        dev.hae.btt.item.PipeItem.tick(player, pc);

        if (GameFunctions.isPlayerAliveAndSurvival(player) && gwc.canUseKillerFeatures(player)) {
            int passive = dev.doctor4t.wathe.game.GameConstants.PASSIVE_MONEY_TICKER.apply(serverWorld.getTime());
            if (passive > 0) {
                var shop = dev.doctor4t.wathe.cca.PlayerShopComponent.KEY.get(player);
                if (shop.balance < MONEY_PASSIVE_CAP) shop.addToBalance(passive);
            }
        }
        DelayedAbility.tick(player, pc);
        Actor.tick(player, pc);
        Kidnapper.tick(player, pc);
        RoleTransitions.tick(player, pc);
        Guard.tick(player, pc);
        Folklorist.tick(player, pc);
        dev.hae.btt.role.Engineer.tick(player, pc);
        RoleDef d = RoleDefs.get(gwc.getRole(player));
        if (d != null) d.dispatchTick(player, serverWorld, gwc);
    }
}
