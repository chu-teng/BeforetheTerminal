package dev.hae.btt.role;

import dev.hae.btt.combat.DeathReasons;

import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.item.ItemUse;
import dev.hae.btt.registry.BttItems;
import dev.hae.btt.mechanic.DelayedAbility;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


public final class Warden {
    private Warden() {}
    public static final int COOLDOWN = 2400;
    public static final int DECISION_TICKS = 300;

    private static List<String> members(PlayerState state) {
        return state.cageMembers.isEmpty() ? List.of() : Arrays.asList(state.cageMembers.split(","));
    }
    private static ServerPlayerEntity resolve(ServerWorld world, String id) {
        try { return world.getPlayerByUuid(UUID.fromString(id)) instanceof ServerPlayerEntity p ? p : null; }
        catch (IllegalArgumentException ignored) { return null; }
    }

    public static boolean select(ServerPlayerEntity caster, ServerPlayerEntity target, ItemStack cage) {
        var state = PlayerState.KEY.get(caster);
        var prisoner = PlayerState.KEY.get(target);
        if (!caster.isAlive() || caster.isSpectator() || !target.isAlive()
                || !GameFunctions.isPlayerAliveAndSurvival(target) || caster == target
                || caster.distanceTo(target) > 6.0 || !caster.canSee(target)
                || ItemUse.cooling(caster, BttItems.CAGE) || state.cagePhase != 0
                || !prisoner.cageOwner.isEmpty()) return false;
        var ids = new ArrayList<>(members(state));
        if (ids.contains(target.getUuidAsString()) || ids.size() >= 3) return false;
        ids.add(target.getUuidAsString());
        state.cageMembers = String.join(",", ids);
        prisoner.cageOwner = caster.getUuidAsString();
        prisoner.cageChooseDeath = false;
        prisoner.cageForfeit = false;
        prisoner.sync();
        cage.set(DataComponentTypes.CUSTOM_NAME, ids.size() < 3
                ? Text.translatable("item.btt.cage.selected", ids.size()) : Text.translatable("item.btt.cage"));
        if (ids.size() == 3) {
            ItemUse.applyCooldown(caster, BttItems.CAGE, COOLDOWN);
            var redirected = Puppeteer.redirectPicks(caster, ids.stream().map(UUID::fromString).toList());
            for (String id : ids) {
                var old = resolve(caster.getServerWorld(), id);
                if (old != null) { PlayerState.KEY.get(old).cageOwner = ""; PlayerState.KEY.get(old).sync(); }
            }
            for (UUID id : redirected) {
                var redirectedTarget = resolve(caster.getServerWorld(), id.toString());
                if (redirectedTarget == null || (!PlayerState.KEY.get(redirectedTarget).cageOwner.isEmpty()
                        && !PlayerState.KEY.get(redirectedTarget).cageOwner.equals(caster.getUuidAsString()))) {
                    clearGroup(caster); return true;
                }
            }
            state.cageMembers = String.join(",", redirected.stream().map(UUID::toString).toList());
            for (UUID id : redirected) {
                var selected = resolve(caster.getServerWorld(), id.toString());
                PlayerState.KEY.get(selected).cageOwner = caster.getUuidAsString();
                PlayerState.KEY.get(selected).sync();
            }
            state.cagePhase = 1;
            state.cageAt = caster.getServerWorld().getTime() + DelayedAbility.randomDelayTicks(caster.getRandom());
            dev.hae.btt.api.event.BttEvents.SkillUsed.EVENT.invoker().onSkillUsed(caster, null, Roles.WARDEN.identifier());
        }
        state.sync();
        return true;
    }

    public static boolean choose(ServerPlayerEntity player) {
        var state = PlayerState.KEY.get(player);
        if (!player.isAlive() || !GameFunctions.isPlayerAliveAndSurvival(player)
                || state.cageOwner.isEmpty() || player.getServerWorld().getTime() >= state.cageDeadline
                || state.cageDeadline == 0) return false;
        state.cageChooseDeath = !state.cageForfeit && !state.cageChooseDeath;
        renameChoice(player);
        state.sync();
        return true;
    }

    public static void died(ServerPlayerEntity player) {
        var state = PlayerState.KEY.get(player);
        if (state.cageDeadline == 0 || player.getWorld().getTime() > state.cageDeadline) return;
        state.cageForfeit = true; state.cageChooseDeath = false;
        renameChoice(player); state.sync();
    }

    private static void renameChoice(ServerPlayerEntity player) {
        String key = PlayerState.KEY.get(player).cageChooseDeath ? "item.btt.cage_choice.death" : "item.btt.cage_choice";
        for (int i = 0; i < player.getInventory().size(); i++) {
            var item = player.getInventory().getStack(i);
            if (item.isOf(BttItems.CAGE_CHOICE)) item.set(DataComponentTypes.CUSTOM_NAME, Text.translatable(key));
        }
        player.currentScreenHandler.syncState();
    }

    public static void tick(ServerPlayerEntity caster) {
        var state = PlayerState.KEY.get(caster);
        if (state.cageMembers.isEmpty()) return;
        var world = caster.getServerWorld();
        if (state.cagePhase < 2 && (!caster.isAlive() || caster.isSpectator())) { clearGroup(caster); return; }
        if (state.cagePhase == 0 || world.getTime() < state.cageAt) return;
        var targets = members(state).stream().map(id -> resolve(world, id)).filter(java.util.Objects::nonNull).toList();
        if (state.cagePhase == 1) {
            if (targets.size() != 3 || targets.stream().anyMatch(p -> !p.isAlive() || !GameFunctions.isPlayerAliveAndSurvival(p))) {
                clearGroup(caster); return;
            }
            state.cagePhase = 2;
            state.cageAt = world.getTime() + DECISION_TICKS;
            for (ServerPlayerEntity target : targets.stream().distinct().toList()) {
                var prisoner = PlayerState.KEY.get(target);
                prisoner.cageDeadline = state.cageAt;
                prisoner.cageChooseDeath = false;
                prisoner.cageForfeit = false;
                target.giveItemStack(new ItemStack(BttItems.CAGE_CHOICE));
                prisoner.sync();
            }
            state.sync();
            return;
        }
        var alive = targets.stream().distinct().filter(p -> p.isAlive() && GameFunctions.isPlayerAliveAndSurvival(p)).toList();
        boolean sacrifice = alive.stream().anyMatch(p -> PlayerState.KEY.get(p).cageChooseDeath && !PlayerState.KEY.get(p).cageForfeit);
        var victims = alive.stream().filter(p -> !sacrifice || PlayerState.KEY.get(p).cageChooseDeath && !PlayerState.KEY.get(p).cageForfeit).toList();
        clearGroup(caster);
        for (ServerPlayerEntity victim : victims) GameFunctions.killPlayer(victim, true, caster, DeathReasons.MAGIC);
    }

    private static void clearGroup(ServerPlayerEntity caster) {
        var state = PlayerState.KEY.get(caster);
        for (String id : members(state)) {
            var target = resolve(caster.getServerWorld(), id);
            if (target == null) continue;
            var prisoner = PlayerState.KEY.get(target);
            if (!caster.getUuidAsString().equals(prisoner.cageOwner)) continue;
            prisoner.cageOwner = ""; prisoner.cageDeadline = 0; prisoner.cageChooseDeath = false; prisoner.cageForfeit = false;
            removeChoices(target); prisoner.sync();
        }
        state.cageMembers = ""; state.cagePhase = 0; state.cageAt = 0;
        for (int i = 0; i < caster.getInventory().size(); i++) {
            var item = caster.getInventory().getStack(i);
            if (item.isOf(BttItems.CAGE)) item.remove(DataComponentTypes.CUSTOM_NAME);
        }
        caster.currentScreenHandler.syncState(); state.sync();
    }

    private static void removeChoices(ServerPlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            if (player.getInventory().getStack(i).isOf(BttItems.CAGE_CHOICE)) player.getInventory().removeStack(i);
        }
    }
    public static void clear(ServerWorld world) {
        for (ServerPlayerEntity p : world.getPlayers()) {
            clearGroup(p); var state = PlayerState.KEY.get(p);
            state.cageOwner = ""; state.cageDeadline = 0; state.cageChooseDeath = false; state.cageForfeit = false; removeChoices(p); state.sync();
        }
        for (var item : world.getEntitiesByType(EntityType.ITEM, e -> e.getStack().isOf(BttItems.CAGE_CHOICE))) item.discard();
    }
}
