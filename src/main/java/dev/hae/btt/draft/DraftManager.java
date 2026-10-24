package dev.hae.btt.draft;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.MapVariablesWorldComponent;
import dev.hae.btt.BeforeTheTerminal;
import dev.hae.btt.api.event.BttEvents;
import dev.hae.btt.component.DraftState;
import dev.hae.btt.game.BeforeTheTerminalGameMode;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.registry.BttItems;
import dev.hae.btt.role.Roles;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

















public final class DraftManager {
    private DraftManager() {}


    public static final int TURN_TICKS = 10 * 20;


    private static int turnTicks() {
        int seconds = dev.hae.btt.config.BttConfig.HANDLER.instance().draftTurnSeconds;
        return Math.max(3, seconds) * 20;
    }

    private static final int GOLD = 0xFFD700;


    private static SeatAssignment.Roster roster = null;
    private static DraftPool pool = null;
    private static List<UUID> order = List.of();
    private static int index = -1;
    private static final Map<UUID, Role> picks = new HashMap<>();
    private static DraftPool.Hand currentHand = null;
    private static int ticksLeft = 0;

    private static boolean resultReady = false;

    private static boolean started = false;

    private static ServerWorld draftWorld = null;

    private static ServerWorld pendingWorld = null;
    private static dev.hae.btt.game.BeforeTheTerminalGameMode pendingMode = null;
    private static dev.doctor4t.wathe.api.MapEffect pendingEffect = null;
    private static int pendingTime = 0;

    private static UUID lastPicker;
    private static int openingTicks;
    private static Text lastAnnouncement = Text.empty();
    private static final Map<UUID, Text> personalAnnouncements = new HashMap<>();
    private static final Random RANDOM = new Random();





    public static boolean isDraftMode(ServerWorld world) {
        return world != null && dev.hae.btt.game.GameModes.modeOf(
                GameWorldComponent.KEY.get(world)) == dev.hae.btt.game.BeforeTheTerminalGameMode.Mode.DRAFT;
    }

    public static boolean isActive() {
        return started && !resultReady;
    }

    public static boolean isResultReady() {
        return resultReady;
    }


    public static boolean belongsToAnotherWorld(ServerWorld world) {
        return started && draftWorld != world;
    }

    public static boolean hasSession(ServerWorld world) {
        return started && draftWorld == world;
    }

    public static boolean isActiveFor(ServerWorld world) {
        return hasSession(world) && isActive();
    }


    public static void tick(ServerWorld world) {
        if (!isActive() || world != draftWorld) {
            DraftState stale = DraftState.KEY.get(world);
            if (stale.active) { stale.active = false; stale.sync(); }
            return;
        }
        if (ticksLeft > 0 && --ticksLeft <= 0) {

            autoPickFaceUp(world);
            return;
        }
        DraftState state = DraftState.KEY.get(world);
        state.ticksLeft = Math.max(0, ticksLeft);
        if (openingTicks > 0) { openingTicks--; return; }
        if (state.ticksLeft % 20 == 0) {
            state.sync();
            for (ServerPlayerEntity p : world.getPlayers()) {
                Text line = order.get(index).equals(p.getUuid())
                        ? Text.translatable("btt.draft.remaining", (ticksLeft + 19) / 20)
                        : Text.translatable("btt.draft.current", index + 1);
                p.sendMessage(line, true);
            }
        }
    }


    public static boolean start(ServerWorld world) {
        if (started) return !resultReady;
        draftWorld = world;
        lastAnnouncement = Text.empty();
        lastPicker = null; openingTicks = 60;
        personalAnnouncements.clear();
        started = true;

        List<ServerPlayerEntity> participants = readyPlayers(world);
        List<UUID> ids = new ArrayList<>(participants.stream().map(ServerPlayerEntity::getUuid).toList());
        SeatAssignment.Roster built = SeatAssignment.buildRoster(ids);
        if (built == null) {
            BeforeTheTerminalGameMode.LOGGER.warn("[BTT] draft: roster not buildable for {} players, falling back to random", ids.size());
            resultReady = true;
            roster = null;
            return false;
        }
        roster = built;
        pool = new DraftPool(roster.seats(), RANDOM);

        List<UUID> draftable = new ArrayList<>();
        for (UUID id : ids) {
            if (!roster.forced().containsKey(id)) draftable.add(id);
        }
        Collections.shuffle(draftable, RANDOM);
        order = List.copyOf(draftable);
        picks.clear();
        index = -1;
        resultReady = false;

        DraftState state = DraftState.KEY.get(world);
        state.knownRoles = "";
        state.active = true;
        state.totalTurns = order.size();
        state.ordinal = 0;
        state.pendingCount = pool.size();
        state.ticksLeft = 0;
        state.sync();


        for (int i = 0; i < order.size(); i++) {
            ServerPlayerEntity p = world.getPlayerByUuid(order.get(i)) instanceof ServerPlayerEntity sp ? sp : null;
            if (p != null) p.sendMessage(Text.translatable("btt.draft.your_order", i + 1)
                    .withColor(GOLD), true);
        }
        BeforeTheTerminalGameMode.LOGGER.info("[BTT] draft started: {} participants, {} roles in the deck, order={}",
                order.size(), pool.size(), order.size());
        nextTurn(world);
        return true;
    }


    public static void rememberStart(ServerWorld world, dev.hae.btt.game.BeforeTheTerminalGameMode mode,
                                     dev.doctor4t.wathe.api.MapEffect effect, int time) {
        pendingWorld = world;
        pendingMode = mode;
        pendingEffect = effect != null ? effect
                : GameWorldComponent.KEY.get(world).getMapEffect();
        pendingTime = time;
    }


    public static void resumeStart() {
        if (pendingWorld == null || pendingMode == null) return;
        ServerWorld world = pendingWorld;
        var mode = pendingMode;
        var effect = pendingEffect;
        int time = pendingTime;
        pendingWorld = null;
        pendingMode = null;
        pendingEffect = null;
        pendingTime = 0;


        BeforeTheTerminalGameMode.LOGGER.info(
                "[BTT] draft finished → resuming the deferred start ({}), ready-area players now = {} (drafted = {})",
                mode.identifier, readyPlayers(world).size(), picks.size() + roster.forced().size());
        dev.doctor4t.wathe.game.GameFunctions.startGame(world, mode, effect, time);
    }


    private static void nextTurn(ServerWorld world) {
        DraftState state = DraftState.KEY.get(world);
        index++;
        if (index >= order.size()) {
            finish(world);
            return;
        }
        ServerPlayerEntity current = world.getPlayerByUuid(order.get(index)) instanceof ServerPlayerEntity sp ? sp : null;
        if (current == null || !current.isAlive()) {

            nextTurn(world);
            return;
        }
        currentHand = pool.deal();
        ticksLeft = turnTicks();
        state.ordinal = index + 1;
        state.ticksLeft = ticksLeft;
        state.pendingCount = pool.size();
        state.sync();
        giveSlips(current, currentHand);

    }


    public static void pick(ServerWorld world, ServerPlayerEntity player, int slot) {
        if (!isActive() || world != draftWorld || player.getServerWorld() != draftWorld) return;
        if (index < 0 || index >= order.size()) return;
        if (!order.get(index).equals(player.getUuid())) return;
        if (currentHand == null) return;
        Role chosen = currentHand.at(slot);
        if (chosen == null) return;
        boolean hidden = currentHand.isHiddenSlot(slot);
        commit(world, player, chosen, hidden);
    }


    private static void autoPickFaceUp(ServerWorld world) {
        ServerPlayerEntity current = index >= 0 && index < order.size()
                && world.getPlayerByUuid(order.get(index)) instanceof ServerPlayerEntity sp ? sp : null;
        if (current == null || currentHand == null || currentHand.faceUp().isEmpty()) {
            nextTurn(world);
            return;
        }
        int pick = RANDOM.nextInt(currentHand.faceUp().size());
        commit(world, current, currentHand.faceUp().get(pick), false);
    }

    private static void commit(ServerWorld world, ServerPlayerEntity player, Role role, boolean hidden) {
        if (!pool.take(role)) return;
        hidden = hidden || role == Roles.HERETIC || role == Roles.BLACKDEATH;
        picks.put(player.getUuid(), role);
        clearSlips(player);
        currentHand = null;
        DraftState state = DraftState.KEY.get(world);
        if (!hidden) state.knownRoles += (state.knownRoles.isEmpty() ? "" : ",") + role.identifier();
        state.pendingCount = pool.size();
        state.sync();
        BttEvents.DraftPick.EVENT.invoker().onDraftPick(world, player.getUuid(), index + 1, role, hidden);

        Text visibleRole = SeatAssignment.displayName(role).copy().withColor(role.color());
        Text announce = Text.translatable("btt.draft.picked", index + 1,
                hidden ? Text.translatable("btt.draft.hidden").withColor(0xAAAAAA) : visibleRole).withColor(0xFFFFFF);
        lastAnnouncement = announce;
        lastPicker = player.getUuid();
        personalAnnouncements.put(player.getUuid(), Text.translatable("btt.draft.mine", visibleRole).withColor(0xFFFFFF));
        for (ServerPlayerEntity p : world.getPlayers()) {
            p.sendMessage(p == player ? personalAnnouncements.get(player.getUuid()) : announce, false);
            p.playSoundToPlayer(dev.hae.btt.registry.Sounds.PUBLIC_NOTICE,
                    net.minecraft.sound.SoundCategory.PLAYERS, 0.6f, 1.0f);
        }
        nextTurn(world);
    }

    private static void finish(ServerWorld world) {
        DraftState state = DraftState.KEY.get(world);
        state.active = false;
        state.ordinal = 0;
        state.ticksLeft = 0;
        state.pendingCount = 0;
        state.sync();
        resultReady = true;
        BeforeTheTerminalGameMode.LOGGER.info("[BTT] draft finished: {} picks", picks.size());
        resumeStart();
    }


    public static Result consume(ServerWorld world) {
        if (world != draftWorld) return null;
        if (!resultReady) return null;
        Result r = roster == null ? null : new Result(roster, Map.copyOf(picks));
        reset();
        return r;
    }


    public record Result(SeatAssignment.Roster roster, Map<UUID, Role> picks) {}


    public static void reset() {
        if (draftWorld != null) dev.hae.btt.game.ServerSettings.cancel(draftWorld);
        draftWorld = null;
        roster = null;
        pool = null;
        order = List.of();
        index = -1;
        picks.clear();
        currentHand = null;
        ticksLeft = 0;
        resultReady = false;
        started = false;
        pendingWorld = null;
        pendingMode = null;
        pendingEffect = null;
        pendingTime = 0;
        lastAnnouncement = Text.empty();
        personalAnnouncements.clear();
    }

    public static void reset(ServerWorld world) {
        if (draftWorld == world) reset();
    }


    public static List<ServerPlayerEntity> readyPlayers(ServerWorld world) {
        MapVariablesWorldComponent areas = MapVariablesWorldComponent.KEY.get(world);
        return world.getPlayers(p -> areas.getReadyArea().contains(p.getPos()) && !p.isSpectator());
    }



    private static void giveSlips(ServerPlayerEntity player, DraftPool.Hand hand) {
        player.getInventory().clear();
        for (int slot = 0; slot < hand.slotCount() && slot < 9; slot++) {
            Role role = hand.at(slot);
            boolean hidden = hand.isHiddenSlot(slot);
            ItemStack stack = new ItemStack(BttItems.SLIP);
            stack.set(DataComponentTypes.CUSTOM_NAME, (hidden
                    ? Text.translatable("btt.draft.slip.hidden", slot + 1)
                    : Text.translatable("btt.draft.slip", slot + 1,
                    SeatAssignment.displayName(role).getString()).withColor(role.color()))
                    .styled(style -> style.withItalic(false)));
            if (hidden) {
                stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                        Text.translatable("btt.draft.slip.hidden.lore"))));
            } else {
                stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                        Text.translatable("announcement.goal." + role.identifier().getNamespace()
                                + "." + role.identifier().getPath()))));
            }
            player.getInventory().setStack(slot, stack);
        }
        player.currentScreenHandler.syncState();
    }

    private static void clearSlips(ServerPlayerEntity player) {
        for (int slot = 0; slot < 9; slot++) {
            if (player.getInventory().getStack(slot).isOf(BttItems.SLIP)) {
                player.getInventory().setStack(slot, ItemStack.EMPTY);
            }
        }
        player.currentScreenHandler.syncState();
    }
}
