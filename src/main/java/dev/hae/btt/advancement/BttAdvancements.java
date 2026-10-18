package dev.hae.btt.advancement;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import dev.doctor4t.wathe.cca.WorldBlackoutComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.api.event.BttEvents;
import dev.hae.btt.combat.DeathReasons;
import dev.hae.btt.component.GameState;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.encounter.Backgrounds;
import dev.hae.btt.game.BeforeTheTerminalGameMode;
import dev.hae.btt.game.GameModes;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.role.Roles;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import java.util.*;


public final class BttAdvancements {
    private BttAdvancements() {}
    private static final Map<UUID, ServerWorld> HIDDEN_PICKS = new HashMap<>();
    private record Explosion(ServerPlayerEntity owner, Set<UUID> deaths) {}
    private static final ThreadLocal<Explosion> EXPLOSION = new ThreadLocal<>();
    private record Trap(ServerPlayerEntity victim, PlayerEntity owner, String kind) {}
    private static final ThreadLocal<Trap> TRAP = new ThreadLocal<>();

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> award(handler.player, "arrival"));
        BttEvents.GameEnded.EVENT.register(BttAdvancements::end);
        BttEvents.Death.EVENT.register(BttAdvancements::death);
        BttEvents.TaskCompleted.EVENT.register((player, task) -> {
            fact(player, "need");
            if (distinct(round(player), "needs", task) >= 8) fact(player, "all_needs");
        });
        BttEvents.DraftPick.EVENT.register((world, player, ordinal, role, hidden) -> {
            if (hidden) HIDDEN_PICKS.put(player, world);
        });
        BttEvents.SkillUsed.EVENT.register((user, target, skill) -> {
            if (target != null && Roles.kindOf(game(user).getRole(user)) == Roles.Kind.INFORMATION
                    && !Roles.isPassengerCampFor(game(user), target)) fact(user, "information");
        });
        BttEvents.ShopPurchase.EVENT.register((player, entry, index, price) -> {
            if (entry.stack().isOf(dev.doctor4t.wathe.index.WatheItems.BLACKOUT)) {
                NbtCompound state = worldRound(player.getServerWorld());
                state.putString("blackoutOwner", player.getUuidAsString());
                state.putInt("blackoutDeaths", 0);
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (var world : server.getWorlds()) tick(world);
        });
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPED.register(server -> HIDDEN_PICKS.clear());
    }

    private static GameWorldComponent game(PlayerEntity player) { return GameWorldComponent.KEY.get(player.getWorld()); }
    private static boolean active(ServerPlayerEntity player) {
        return SeatAssignment.isBttMode(player.getWorld()) && game(player).getGameStatus() == GameWorldComponent.GameStatus.ACTIVE
                && game(player).getRole(player) != null;
    }
    private static NbtCompound round(ServerPlayerEntity player) { return PlayerState.KEY.get(player).advancementRound; }
    private static NbtCompound worldRound(ServerWorld world) { return GameState.KEY.get(world).advancementRound; }
    private static int increment(NbtCompound data, String key) {
        int value = data.getInt(key) + 1; data.putInt(key, value); return value;
    }
    private static int distinct(NbtCompound data, String key, String value) {
        var values = new HashSet<>(Arrays.asList(data.getString(key).split(",")));
        values.remove(""); values.add(value); data.putString(key, String.join(",", values)); return values.size();
    }
    public static void award(ServerPlayerEntity player, String id) { criterion(player, id, "done"); }
    private static void criterion(ServerPlayerEntity player, String id, String criterion) {
        var entry = player.getServer().getAdvancementLoader().get(Identifier.of("btt", id));
        if (entry != null && entry.value().criteria().containsKey(criterion)) player.getAdvancementTracker().grantCriterion(entry, criterion);
    }
    public static void fact(ServerPlayerEntity player, String id) { if (active(player)) award(player, id); }
    private static void nextCriterion(ServerPlayerEntity player, String id) {
        var entry = player.getServer().getAdvancementLoader().get(Identifier.of("btt", id));
        if (entry == null) return;
        for (String key : player.getAdvancementTracker().getProgress(entry).getUnobtainedCriteria()) {
            player.getAdvancementTracker().grantCriterion(entry, key); break;
        }
    }
    public static void wreath(ServerPlayerEntity player, String id) {
        award(player, id); criterion(player, "all_wreaths", id);
    }

    public static void ready(ServerWorld world) {
        GameState.KEY.get(world).advancementRound = new NbtCompound();
        var game = GameWorldComponent.KEY.get(world);
        var unarmedPrincipals = Set.of(Roles.RIOT, Roles.LEVIATHAN, Roles.SCULPTOR, Roles.VORTEX);
        boolean noKnife = GameState.KEY.get(world).openingRoles.values().stream()
                .map(Roles::byId).anyMatch(unarmedPrincipals::contains);
        worldRound(world).putBoolean("unarmedPrincipal", noKnife);
        for (var player : world.getPlayers()) {
            if (game.getRole(player) == null) continue;
            PlayerState.KEY.get(player).advancementRound = new NbtCompound();
            var state = round(player);
            state.putBoolean("initialized", true);
            state.putString("camp", Roles.campOf(game, player).name());
            state.putString("startCamp", Roles.campOf(game, player).name());
            state.putString("role", game.getRole(player).identifier().toString());
            state.putBoolean("hiddenPick", HIDDEN_PICKS.remove(player.getUuid()) == world);
            state.putBoolean("guesser", guessesRoles(game.getRole(player)));
            if (GameModes.modeOf(game) == BeforeTheTerminalGameMode.Mode.DUEL
                    && (Roles.kindOfReal(game, player) == Roles.Kind.LONE || Roles.kindOfReal(game, player) == Roles.Kind.OUTSIDER)) award(player, "duel");
        }
        HIDDEN_PICKS.entrySet().removeIf(entry -> entry.getValue() == world);
    }
    private static boolean guessesRoles(Role role) {
        return role == Roles.NOVELIST || role == Roles.ASSASSIN || role == Roles.MESSIAH || role == Roles.GAMBLER;
    }
    public static void guess(ServerPlayerEntity player, boolean correct) {
        if (!active(player)) return;
        round(player).putBoolean("guesser", true);
        if (!correct) round(player).putBoolean("guessFailed", true);
    }
    public static void recruit(ServerPlayerEntity source, ServerPlayerEntity target) {
        if (!active(source) || source == target) return;
        distinct(round(source), "recruits", target.getUuidAsString());
        if (game(source).getRole(source) == Roles.MESSIAH
                && distinct(round(source), "faithful", target.getUuidAsString()) >= 3 && !round(source).getBoolean("guessFailed")) award(source, "messiah_five");
    }
    public static void revivedByVoodoo(ServerPlayerEntity caster) {
        if (active(caster) && increment(round(caster), "voodooRevives") >= 3) award(caster, "voodoo_three");
    }
    public static void succession(ServerPlayerEntity predecessor) { fact(predecessor, "legion_succession"); }
    public static void executionSanity(ServerPlayerEntity player, float before) {
        if (active(player) && before > 0 && dev.doctor4t.wathe.cca.PlayerMoodComponent.KEY.get(player).getMood() <= 0)
            round(player).putLong("executionCollapseUntil", player.getServerWorld().getTime() + 2);
    }
    public static void protection(ServerPlayerEntity caster, PlayerEntity target) {
        if (target != caster) fact(caster, "protection");
    }
    public static void blocked(PlayerEntity victim, PlayerEntity killer, Identifier reason, boolean consumed) {
        if (!(victim instanceof ServerPlayerEntity player) || !active(player)) return;
        if (consumed) award(player, "shield");
        if (dev.hae.btt.trait.TraitRules.execution(victim, killer, reason)) award(player, "exemption");
    }
    public static void trapDamage(ServerPlayerEntity victim, PlayerEntity owner, String kind) {
        var previous = TRAP.get(); TRAP.set(new Trap(victim, owner, kind));
        try { GameFunctions.killPlayer(victim, true, owner, GameConstants.DeathReasons.GENERIC); }
        finally { if (previous == null) TRAP.remove(); else TRAP.set(previous); }
    }
    public interface Scope extends AutoCloseable { @Override void close(); }
    public static Scope explosion(ServerPlayerEntity owner) {
        var previous = EXPLOSION.get(); EXPLOSION.set(new Explosion(owner, new HashSet<>()));
        return () -> { if (previous == null) EXPLOSION.remove(); else EXPLOSION.set(previous); };
    }
    public static void changedRole(ServerPlayerEntity player) {
        if (!active(player) || !round(player).getBoolean("initialized")) return;
        round(player).putBoolean("roleChanged", true);
        if (GameModes.modeOf(game(player)) == BeforeTheTerminalGameMode.Mode.MEMORY) award(player, "memory_change");
    }
    public static void changedCamp(ServerPlayerEntity player) {
        if (active(player) && round(player).getBoolean("initialized")) round(player).putBoolean("campChanged", true);
    }
    public static void psychoStarted(ServerPlayerEntity player) {
        if (active(player)) round(player).putInt("psychoKills", 0);
    }
    public static void derringer(ServerPlayerEntity player, boolean hit) {
        if (!active(player)) return;
        if (!hit) round(player).putInt("derringerHits", 0);
        else if (increment(round(player), "derringerHits") >= 3) award(player, "derringer_streak");
    }
    public static void debate(ServerWorld world, ServerPlayerEntity victim, Map<String, String> votes, int aliveBefore) {
        var game = GameWorldComponent.KEY.get(world);
        boolean correct = !Roles.isPassengerCampFor(game, victim);
        boolean unanimous = correct && world.getPlayers().stream().filter(GameFunctions::isPlayerAliveAndSurvival)
                .filter(p -> p != victim && game.getRole(p) != null).allMatch(p -> victim.getUuidAsString().equals(votes.get(p.getUuidAsString())));
        for (var player : world.getPlayers()) if (victim.getUuidAsString().equals(votes.get(player.getUuidAsString()))) {
            fact(player, "debate"); if (unanimous && aliveBefore > 1) fact(player, "unanimous");
        }
    }

    private static void death(ServerPlayerEntity victim, ServerPlayerEntity killer, Identifier reason) {
        if (!active(victim)) return;
        var world = victim.getServerWorld(); var game = game(victim); var state = round(victim);
        state.putBoolean("dead", true);
        if (DeathReasons.SELF_EXECUTION.equals(reason) && state.contains("misfireAt") && state.getLong("misfireAt") == world.getTime()) award(victim, "self_execution");
        if (DeathReasons.COLLAPSE.equals(reason) && state.contains("executionCollapseUntil") && state.getLong("executionCollapseUntil") >= world.getTime()
                && world.getPlayers().stream().anyMatch(p -> game.getRole(p) == Roles.COLD_WAVE)) {
            if (distinct(worldRound(world), "coldVictims", victim.getUuidAsString()) >= 3)
                for (var owner : world.getPlayers()) if (GameState.KEY.get(world).openingRoles.getOrDefault(owner.getUuid(), "").equals(Roles.COLD_WAVE.identifier().toString())
                        || game.getRole(owner) == Roles.COLD_WAVE) award(owner, "cold_three");
        }
        if (WorldBlackoutComponent.KEY.get(world).isBlackoutActive()) {
            int deaths = increment(worldRound(world), "blackoutDeaths");
            var owner = player(world, worldRound(world).getString("blackoutOwner"));
            if (owner != null && deaths >= 7) award(owner, "blackout_seven");
        }
        if (killer == null || killer == victim) return;
        var actor = round(killer);
        if (Roles.isKillerCampFor(game, killer)) {
            int kills = increment(actor, "kills");
            award(killer, "kill_one"); if (kills >= 3) award(killer, "kill_three"); if (kills >= 5) award(killer, "kill_five");
        }
        if (GameConstants.DeathReasons.POISON.equals(reason)) award(killer, "poison_kill");
        if (DeathReasons.MAGIC.equals(reason) && !Roles.isPassengerCampFor(game, victim)) award(killer, "magic_kill");
        if (game.getRole(killer) == Roles.ASTRAL && Roles.isKillerCampFor(game, victim)
                && distinct(actor, "astralVictims", victim.getUuidAsString()) >= 2) award(killer, "astral_two");
        if (DeathReasons.BLOOM.equals(reason)) award(killer, "bloom_kill");
        if (Roles.hasCurrentOrPriorRole(game, killer, Roles.LUNATIC) && Roles.kindOfReal(game, victim) == Roles.Kind.PRINCIPAL) award(killer, "lunatic_principal");
        if (PlayerPsychoComponent.KEY.get(killer).getPsychoTicks() > 0 && increment(actor, "psychoKills") >= 4) award(killer, "psycho_four");
        var explosion = EXPLOSION.get();
        if (explosion != null && explosion.owner == killer && GameConstants.DeathReasons.GRENADE.equals(reason)) {
            explosion.deaths.add(victim.getUuid()); if (explosion.deaths.size() >= 4) award(killer, "grenade_four");
        }
        var trap = TRAP.get();
        if (trap != null && trap.victim == victim && trap.owner == killer) {
            award(killer, "trap_kill");
            if (distinct(actor, "traps", trap.kind) >= 2) award(killer, "double_trap");
            if (Roles.isPassengerCampFor(game, killer) && !Roles.isPassengerCampFor(game, victim)) award(killer, "passenger_trap");
        }
        if (dev.hae.btt.trait.TraitRules.execution(victim, killer, reason)) {
            if (Roles.hasCurrentOrPriorRole(game, killer, Roles.MARIONETTE) && Roles.isPassengerCampFor(game, victim)) award(killer, "marionette_execution");
            if (Roles.isPassengerCampFor(game, killer)) {
                if (Roles.isPassengerCampFor(game, victim)) { award(killer, "misfire"); actor.putLong("misfireAt", world.getTime()); }
                else {
                    int executions = increment(actor, "executions"); award(killer, "execution");
                    if (executions >= 2) award(killer, "execution_two"); if (executions >= 3) award(killer, "execution_three");
                }
            }
        }
    }

    private static void tick(ServerWorld world) {
        var game = GameWorldComponent.KEY.get(world);
        if (!(game.getGameMode() instanceof BeforeTheTerminalGameMode) || game.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE) return;
        boolean blackout = WorldBlackoutComponent.KEY.get(world).isBlackoutActive();
        if (!blackout) { worldRound(world).putString("blackoutOwner", ""); worldRound(world).putInt("blackoutDeaths", 0); }
        long killers = world.getPlayers().stream().filter(p -> game.getRole(p) != null && Roles.isKillerCampFor(game, p)).count();
        long passengers = world.getPlayers().stream().filter(p -> game.getRole(p) != null && Roles.isPassengerCampFor(game, p)).count();
        for (var player : world.getPlayers()) {
            if (game.getRole(player) == null) continue;
            var state = round(player); var pc = PlayerState.KEY.get(player); boolean alive = GameFunctions.isPlayerAliveAndSurvival(player);
            if (state.getBoolean("dead") && alive) { award(player, "revival"); state.putBoolean("dead", false); }
            if (pc.isDrunk()) award(player, "drunk");
            if (pc.consoleOpen) award(player, "console");
            if (player.getVehicle() instanceof dev.doctor4t.wathe.block.entity.SeatEntity seat
                    && world.getBlockState(seat.getBlockPos()).isOf(dev.hae.btt.registry.BttBlocks.LOOKOUT_CHAIR)) award(player, "lookout");
            if (pc.isProjecting() && (game.getRole(player) == Roles.MUYUUBYOU || game.getRole(player) == Roles.ASTRAL)) award(player, "dream_walk");
            if (alive && !GameState.KEY.get(world).epilogueType.isEmpty()) award(player, "epilogue");
            String role = game.getRole(player).identifier().toString();
            if (!state.getString("role").equals(role)) {
                if (GameModes.modeOf(game) == BeforeTheTerminalGameMode.Mode.MEMORY) award(player, "memory_change");
                state.putBoolean("roleChanged", true); state.putString("role", role);
            }
            if (guessesRoles(game.getRole(player))) state.putBoolean("guesser", true);
            String camp = Roles.campOf(game, player).name();
            if (!state.getString("camp").equals(camp)) {
                state.putBoolean("campChanged", true); state.putString("camp", camp);
            }
            if ("TWINS".equals(pc.relType) && !camp.equals(state.getString("startCamp"))) {
                var partner = player(world, pc.partner);
                if (partner != null && Roles.campOf(game, partner) == Roles.campOf(game, player)) {
                    award(player, "twins_camp"); award(partner, "twins_camp");
                }
            }
            if (Roles.isKillerCampFor(game, player) && killers >= passengers) award(player, "parity");
            if (dev.hae.btt.encounter.EncounterRules.stormProtected(player)) award(player, "storm");
            if (PlayerPsychoComponent.KEY.get(player).getPsychoTicks() <= 0) state.putInt("psychoKills", 0);
            if (game.getRole(player) == Roles.MAJO && world.getPlayers().stream().filter(p -> p != player && game.getRole(p) != null)
                    .filter(p -> dev.hae.btt.faction.Camps.sharesRoleCamp(game, p, Roles.MAJO)).count() >= 2) award(player, "majo_two");
            if (game.getRole(player) == Roles.KIDNAPPER && world.getPlayers().stream()
                    .filter(p -> PlayerState.KEY.get(p).swallowedBy.equals(player.getUuidAsString())).count() >= 4) award(player, "stomach_four");
        }
    }

    private static void end(ServerWorld world, String ending) {
        if ("NONE".equals(ending) || ending.isEmpty()) return;
        var game = GameWorldComponent.KEY.get(world); var btt = GameState.KEY.get(world);
        var winners = new HashSet<>(Arrays.asList(btt.winners.split(",")));
        long passengers = world.getPlayers().stream().filter(GameFunctions::isPlayerAliveAndSurvival).filter(p -> Roles.isPassengerCampFor(game, p)).count();
        long killers = world.getPlayers().stream().filter(GameFunctions::isPlayerAliveAndSurvival).filter(p -> Roles.isKillerCampFor(game, p)).count();
        for (var player : world.getPlayers()) {
            if (game.getRole(player) == null) continue;
            var state = round(player); var pc = PlayerState.KEY.get(player);
            boolean alive = GameFunctions.isPlayerAliveAndSurvival(player), won = winners.contains(player.getUuidAsString());
            if (alive) award(player, "last_stop"); nextCriterion(player, "ten_rounds");
            if (GameModes.modeOf(game) != BeforeTheTerminalGameMode.Mode.SCRIPT) award(player, "modes");
            if (state.getBoolean("hiddenPick")) award(player, "hidden_draft");
            if (!won) continue;
            var camp = Roles.campOf(game, player); var kind = Roles.kindOfReal(game, player);
            var initialKind = Roles.kindOf(Roles.byId(btt.openingRoles.getOrDefault(player.getUuid(), "")));
            criterion(player, "all_camps", camp.name());
            if (!alive) award(player, "dead_winner");
            if (camp == Roles.Faction.PASSENGER) { award(player, "passenger_win"); if (alive && passengers == 1) award(player, "last_passenger"); }
            if (camp == Roles.Faction.KILLER) {
                award(player, "killer_win"); if (alive && killers == 1) award(player, "last_killer");
                if (!"KILLER".equals(state.getString("startCamp"))) award(player, "joined_killer");
                if (worldRound(world).getBoolean("unarmedPrincipal")) award(player, "unarmed_principal");
            }
            if (camp == Roles.Faction.OUTSIDER) {
                award(player, "outsider_win"); if (!"OUTSIDER".equals(state.getString("startCamp"))) award(player, "joined_outsider");
            }
            if (kind != null && kind.isNeutralKind() || initialKind != null && initialKind.isNeutralKind()) award(player, "neutral_win");
            if (camp == Roles.Faction.SOLO && ending.endsWith("_WIN")) award(player, "solo_win");
            if ((kind == Roles.Kind.MAD || initialKind == Roles.Kind.MAD) && state.getBoolean("campChanged")) award(player, "mad_conversion");
            if (state.getBoolean("guesser") && !state.getBoolean("guessFailed")) award(player, "perfect_guesses");
            if (kind == Roles.Kind.OUTSIDER && !state.getString("recruits").isEmpty()
                    && state.getString("recruits").split(",").length >= 5) award(player, "recruit_five");
            if (GameState.KEY.get(world).reborn.contains(player.getUuidAsString() + "=") && kind == Roles.Kind.LONE) award(player, "rebirth");
            if (ending.equals("BLACKDEATH_KILLER") && Roles.hasCurrentOrPriorRole(game, player, Roles.BLACKDEATH)) award(player, "black_death");
            if (ending.equals("HERETIC_PASSENGER") && Roles.hasCurrentOrPriorRole(game, player, Roles.HERETIC)) award(player, "heretic");
            var partner = player(world, pc.partner);
            if ("LOVER".equals(pc.relType) && ending.equals("LOVERS_WIN") && alive && partner != null
                    && GameFunctions.isPlayerAliveAndSurvival(partner) && winners.contains(partner.getUuidAsString())) award(player, "lovers");
            if ("ARCHENEMY".equals(pc.relType) && ending.equals("ARCHENEMY_WIN") && Roles.kindOfReal(game, player) == Roles.Kind.ACCOMPLICE) award(player, "archenemy");
            if ("FATED".equals(pc.relType) && state.getBoolean("roleChanged")) award(player, "rivals");
            if (Backgrounds.isActive(world, Backgrounds.DUPLICATES) && alive && world.getPlayers().stream().anyMatch(p -> p != player
                    && GameFunctions.isPlayerAliveAndSurvival(p) && winners.contains(p.getUuidAsString())
                    && Roles.realRole(game, p) == Roles.realRole(game, player))) award(player, "twins_role");
        }
    }
    private static ServerPlayerEntity player(ServerWorld world, String id) {
        try { return world.getPlayerByUuid(UUID.fromString(id)) instanceof ServerPlayerEntity player ? player : null; }
        catch (IllegalArgumentException ignored) { return null; }
    }
}
