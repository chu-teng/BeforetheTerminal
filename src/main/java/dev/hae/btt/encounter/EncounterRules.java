package dev.hae.btt.encounter;

import dev.hae.btt.game.SeatAssignment;

import dev.hae.btt.game.Scripts;

import dev.doctor4t.wathe.cca.GameTimeComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.component.GameState;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.role.RoleTransitions;
import dev.hae.btt.registry.BttItems;
import dev.hae.btt.combat.DeathReasons;
import dev.hae.btt.role.GuessPool;
import dev.hae.btt.role.Roles;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;


public final class EncounterRules {
    private EncounterRules() {}

    public static boolean apply(ServerWorld world, List<ServerPlayerEntity> players) {
        var game = GameWorldComponent.KEY.get(world);
        var state = GameState.KEY.get(world);
        if (Backgrounds.isActive(world, Backgrounds.INTRIGUE)) {
            var accomplices = players.stream().filter(p -> Roles.kindOf(Roles.realRole(game, p)) == Roles.Kind.ACCOMPLICE).toList();
            var enforcers = players.stream().filter(p -> Roles.kindOf(Roles.realRole(game, p)) == Roles.Kind.ENFORCER).toList();
            if (accomplices.isEmpty() || enforcers.isEmpty()) return false;
            var a = accomplices.get(world.random.nextInt(accomplices.size()));
            var b = enforcers.get(world.random.nextInt(enforcers.size()));
            PlayerState.KEY.get(a).setCampOverride(Roles.CAMP_PASSENGER);
            PlayerState.KEY.get(b).setCampOverride(Roles.CAMP_KILLER);
        } else if (Backgrounds.STORM.equals(state.background)) {
            var pool = GuessPool.catalogue(Scripts.byId(state.script)).stream()
                    .filter(r -> Roles.isPassengerCamp(r) && Roles.kindOf(r) != Roles.Kind.MAD).toList();
            if (pool.isEmpty()) return false;
            var protectedRole = pool.get(world.random.nextInt(pool.size()));
            state.stormRole = protectedRole.identifier().toString();
            var present = players.stream().filter(other -> Roles.realRole(game, other) == protectedRole).toList();
            for (var p : players) {
                boolean killer = Roles.isKillerCampFor(game, p);
                List<Text> lore = killer ? (present.isEmpty()
                        ? List.of(Text.translatable("btt.background.storm.absent", SeatAssignment.displayName(protectedRole), SeatAssignment.displayName(protectedRole)))
                        : present.stream().<Text>map(other -> Text.translatable("btt.background.storm.role_present", other.getDisplayName(), SeatAssignment.displayName(protectedRole))).toList())
                        : List.of(Text.translatable("btt.background.storm.role", SeatAssignment.displayName(protectedRole)));
                p.giveItemStack(slip("btt.background.storm.slip", lore));
            }
            state.sync();
        }
        return true;
    }

    private static ItemStack slip(String name, List<Text> lines) {
        var stack = new ItemStack(BttItems.SLIP);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.translatable(name).styled(style -> style.withItalic(false)));
        stack.set(DataComponentTypes.LORE, new LoreComponent(lines));
        return stack;
    }
    public static boolean stormProtected(ServerPlayerEntity player) {
        var game = GameWorldComponent.KEY.get(player.getWorld());
        var role = game.getRole(player);
        return game.isRunning() && Backgrounds.isActive(player.getWorld(), Backgrounds.STORM)
                && role != null && role.identifier().toString().equals(GameState.KEY.get(player.getWorld()).stormRole);
    }
    public static boolean execution(Identifier reason) {
        return GameConstants.DeathReasons.GUN.equals(reason) || GameConstants.DeathReasons.KNIFE.equals(reason)
                || DeathReasons.ARROW.equals(reason) || DeathReasons.EXECUTION.equals(reason);
    }

    public static void confirmedDeath(ServerPlayerEntity player) {
        confirmedDeath(player, GameConstants.DeathReasons.GENERIC);
    }

    public static void confirmedDeath(ServerPlayerEntity player, Identifier reason) {
        var world = player.getServerWorld();
        var game = GameWorldComponent.KEY.get(world);
        var state = GameState.KEY.get(world);
        if (game.isRole(player, Roles.AVIATEUR) && !PlayerState.KEY.get(player).aviateurUsed
                && !dev.hae.btt.game.GameEvents.aviateurNotMurder(reason)) return;
        boolean sixPaths = Backgrounds.isActive(world, Backgrounds.REBIRTH) && Roles.isPassengerCampFor(game, player)
                && !state.reborn.contains(player.getUuid().toString())
                && Arrays.stream(state.reborn.split(";")).filter(s -> !s.isEmpty()).count() < 2;
        if (!sixPaths) return;
        var excluded = new HashSet<>(state.openingRoles.values());
        for (String entry : state.reborn.split(";")) {
            int split = entry.indexOf('=');
            if (split >= 0) excluded.add(entry.substring(split + 1));
        }
        var candidates = Roles.allRoles().stream().filter(Roles::isImplemented)
                .filter(r -> Roles.kindOf(r) == Roles.Kind.LONE && !excluded.contains(r.identifier().toString()))
                .filter(r -> game.getRoles().values().stream().noneMatch(current -> current == r)).toList();
        if (candidates.isEmpty()) return;
        var next = candidates.get(world.random.nextInt(candidates.size()));
        state.reborn += player.getUuid() + "=" + next.identifier() + ";";
        state.rebirthPending += player.getUuid() + "=" + next.identifier() + ";";
    }

    private static void revive(ServerWorld world) {
        var state = GameState.KEY.get(world);
        String pending = state.rebirthPending;
        state.rebirthPending = "";
        for (String entry : pending.split(";")) {
            var parts = entry.split("=", 2);
            if (parts.length != 2) continue;
            ServerPlayerEntity player;
            try { player = world.getServer().getPlayerManager().getPlayer(UUID.fromString(parts[0])); }
            catch (IllegalArgumentException invalid) { continue; }
            var role = Roles.byId(parts[1]);
            if (player == null || player.getWorld() != world || role == null) continue;
            if (!player.isAlive()) {

                var saved = new NbtCompound();
                var notes = new NbtCompound();
                PlayerState.KEY.get(player).writeToNbt(saved, world.getRegistryManager());
                dev.hae.btt.component.LetterState.KEY.get(player).writeToNbt(notes, world.getRegistryManager());
                player = world.getServer().getPlayerManager().respawnPlayer(player, false, net.minecraft.entity.Entity.RemovalReason.KILLED);
                PlayerState.KEY.get(player).readFromNbt(saved, world.getRegistryManager());
                dev.hae.btt.component.LetterState.KEY.get(player).readFromNbt(notes, world.getRegistryManager());
                dev.hae.btt.component.LetterState.KEY.sync(player);
            }
            var pc = PlayerState.KEY.get(player);
            player.getInventory().clear();
            pc.clearPriorRole();
            pc.fakeDead = false;
            pc.setMessiahFollower(false);
            pc.majoRecruit = false;
            pc.joinedCampRole = "";
            pc.setCampOverride("");
            player.changeGameMode(net.minecraft.world.GameMode.ADVENTURE);
            player.setHealth(player.getMaxHealth());
            player.teleport(world, pc.roomX, pc.roomY, pc.roomZ, player.getYaw(), player.getPitch());
            dev.hae.btt.component.AbilityState.KEY.get(player).setCooldown(0);
            RoleTransitions.takeOver(player, role);
            dev.doctor4t.wathe.cca.PlayerMoodComponent.KEY.get(player).setMood(1.0f);
            player.sendMessage(Text.translatable("btt.background.rebirth.return", SeatAssignment.displayName(role)).withColor(role.color()), true);
        }
    }


    public static boolean tick(ServerWorld world) {
        revive(world);
        var state = GameState.KEY.get(world);
        if (!Backgrounds.DREAM.equals(state.background)) return false;
        var game = GameWorldComponent.KEY.get(world);
        if (state.dreamDeadline == 0 && !state.dreamUsed) {
            var alive = world.getPlayers().stream().filter(GameFunctions::isPlayerAliveAndSurvival).filter(p -> game.getRole(p) != null).toList();
            if (alive.size() != 3) return false;
            state.dreamUsed = true;
            state.dreamDeadline = world.getTime() + 15 * 20;
            state.dreamVoters = String.join(",", alive.stream().map(p -> p.getUuid().toString()).toList());
            state.dreamNo = "";
            for (var player : alive) giveDreamSlip(player);
            state.sync();
        }
        if (state.dreamDeadline == 0) return false;
        GameTimeComponent.KEY.get(world).setTime(Math.max(20, GameTimeComponent.KEY.get(world).getTime()));
        if (world.getTime() < state.dreamDeadline) return true;
        state.dreamDeadline = 0;
        int no = state.dreamNo.isEmpty() ? 0 : state.dreamNo.split(",").length;
        if (no < 2) for (String id : state.dreamVoters.split(",")) {
            try { var voter = world.getPlayerByUuid(java.util.UUID.fromString(id));
                if (voter instanceof net.minecraft.server.network.ServerPlayerEntity p && !java.util.Arrays.asList(state.dreamNo.split(",")).contains(id)) dev.hae.btt.advancement.BttAdvancements.fact(p, "dream_restart");
            } catch (IllegalArgumentException ignored) {}
        }
        if (no >= 2) return false;
        for (UUID uuid : state.openingRoles.keySet()) {
            var player = world.getServer().getPlayerManager().getPlayer(uuid);
            if (player == null || player.getWorld() != world) continue;
            if (!player.isAlive()) {
                var container = ((org.ladysnake.cca.api.v3.component.ComponentProvider) player).getComponentContainer();
                var saved = container.toTag(new NbtCompound(), world.getRegistryManager());
                var inventory = player.getInventory().writeNbt(new net.minecraft.nbt.NbtList());
                player = world.getServer().getPlayerManager().respawnPlayer(player, false, net.minecraft.entity.Entity.RemovalReason.KILLED);
                ((org.ladysnake.cca.api.v3.component.ComponentProvider) player).getComponentContainer().fromTag(saved, world.getRegistryManager());
                player.getInventory().readNbt(inventory);
                var restored = ((org.ladysnake.cca.api.v3.component.ComponentProvider) player).getComponentContainer();
                for (var key : restored.keys())
                    if (key.get(player) instanceof org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent) key.sync(player);
            }
            var pc = PlayerState.KEY.get(player);
            player.changeGameMode(net.minecraft.world.GameMode.ADVENTURE);
            player.setHealth(player.getMaxHealth());
            player.teleport(world, pc.roomX, pc.roomY, pc.roomZ, player.getYaw(), player.getPitch());
            pc.sync();
            dev.hae.btt.component.AbilityState.KEY.sync(player);
            dev.hae.btt.component.LetterState.KEY.sync(player);
        }
        state.dreamVoters = "";
        state.dreamNo = "";
        state.sync();
        GameTimeComponent.KEY.get(world).setTime(2 * 60 * 20);
        GameWorldComponent.KEY.get(world).sync();
        return true;
    }

    private static void giveDreamSlip(ServerPlayerEntity player) {
        var stack = slip("btt.background.dream.yes", List.of(Text.translatable("btt.background.dream.question")));
        var data = new NbtCompound();
        data.putBoolean("btt_dream", true);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(data));
        player.giveItemStack(stack);
    }
    public static boolean useSlip(ServerPlayerEntity player, ItemStack stack) {
        var data = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (!stack.isOf(BttItems.SLIP) || data == null || !data.copyNbt().getBoolean("btt_dream")) return false;
        var state = GameState.KEY.get(player.getWorld());
        java.util.function.BooleanSupplier allowed = () -> Backgrounds.isActive(player.getWorld(), Backgrounds.DREAM)
                && state.dreamDeadline > player.getWorld().getTime() && Arrays.asList(state.dreamVoters.split(",")).contains(player.getUuid().toString());
        if (!allowed.getAsBoolean()) return true;
        boolean yes = Arrays.asList(state.dreamNo.split(",")).contains(player.getUuid().toString());
        vote(player, yes);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.translatable(yes ? "btt.background.dream.yes" : "btt.background.dream.no").styled(style -> style.withItalic(false)));
        player.currentScreenHandler.syncState();
        return true;
    }
    private static void vote(ServerPlayerEntity player, boolean yes) {
        var state = GameState.KEY.get(player.getWorld());
        var no = new LinkedHashSet<>(Arrays.asList(state.dreamNo.split(",")));
        no.remove("");
        if (yes) no.remove(player.getUuid().toString()); else no.add(player.getUuid().toString());
        state.dreamNo = String.join(",", no);
    }
}
