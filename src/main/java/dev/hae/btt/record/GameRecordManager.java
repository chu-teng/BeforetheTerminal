package dev.hae.btt.record;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameRoundEndComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.util.ShopEntry;
import dev.hae.btt.component.GameState;
import dev.hae.btt.game.BeforeTheTerminalGameMode;
import dev.hae.btt.record.replay.ReplayGenerator;
import dev.hae.btt.role.Roles;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;












public final class GameRecordManager {


    private static MatchRecord live = null;
    private static ServerWorld liveWorld = null;

    private static MatchRecord latest = null;

    private static final Set<UUID> present = new HashSet<>();

    private GameRecordManager() {}



    public static synchronized boolean hasActiveMatch() {
        return live != null && live.open();
    }

    public static synchronized boolean hasActiveMatch(ServerWorld world) {
        return world == liveWorld && hasActiveMatch();
    }


    public static synchronized void clear() {
        live = null;
        liveWorld = null;
        latest = null;
        present.clear();
    }

    public static synchronized @Nullable MatchRecord getCurrentMatch() {
        return live;
    }

    public static synchronized @Nullable MatchRecord getLastFinishedMatch() {
        return latest;
    }




    public static synchronized void startMatch(ServerWorld world, GameWorldComponent gameComponent) {
        if (live != null && live.open()) {
            endMatch(liveWorld);
        }
        Identifier mode = gameComponent.getGameMode() == null
                ? Identifier.of("unknown", "unknown") : gameComponent.getGameMode().identifier;
        Identifier effect = gameComponent.getMapEffect() == null
                ? Identifier.of("unknown", "unknown") : gameComponent.getMapEffect().identifier;
        live = new MatchRecord(UUID.randomUUID(), world.getRegistryKey().getValue(),
                mode, effect, world.getTime(), System.currentTimeMillis());
        liveWorld = world;
        present.clear();
    }


    public static synchronized void recordMatchStart(ServerWorld world, GameWorldComponent gameComponent) {
        if (!hasActiveMatch(world)) return;
        MatchRecord match = live;
        NbtCompound header = new NbtCompound();
        header.putString("game_mode", match.gameMode().toString());
        header.putString("map_effect", match.mapEffect().toString());
        header.putInt("player_count", world.getPlayers().size());
        header.putString("background", dev.hae.btt.component.GameState.KEY.get(world).background);
        emit(world, GameRecordTypes.MATCH_START, null, null, header);
        for (ServerPlayerEntity player : world.getPlayers()) {
            notePresence(player);
        }
    }


    public static synchronized void recordRoleSnapshot(ServerWorld world, GameWorldComponent gameComponent) {
        if (!hasActiveMatch(world)) return;
        MatchRecord match = live;
        for (Map.Entry<UUID, Role> entry : gameComponent.getRoles().entrySet()) {
            Role role = entry.getValue();
            if (role == null) continue;
            if (!match.markSnapshot(entry.getKey())) continue;
            ServerPlayerEntity player = world.getPlayerByUuid(entry.getKey()) instanceof ServerPlayerEntity sp ? sp : null;
            NbtCompound payload = new NbtCompound();
            payload.put("player", roleSnapshot(entry.getKey(), player, role));
            emit(world, GameRecordTypes.ROLE_ASSIGNED, null, null, payload);
        }
    }


    public static synchronized void endMatch(ServerWorld world) {
        if (!hasActiveMatch(world)) return;
        MatchRecord match = live;
        try {
            finish(world, match);
        } catch (Throwable t) {

            BeforeTheTerminalGameMode.LOGGER.error("[BTT] match recording failed at round end, continuing", t);
            match.close();
            latest = match;
            live = null;
            liveWorld = null;
            present.clear();
        }
    }

    private static void finish(ServerWorld world, MatchRecord match) {
        GameRoundEndComponent roundEnd = GameRoundEndComponent.KEY.get(world);
        GameFunctions.WinStatus winStatus = roundEnd.getWinStatus();

        NbtCompound summary = new NbtCompound();
        summary.putString("win_status", winStatus == null ? "NONE" : winStatus.name());

        summary.putString("btt_ending", GameState.KEY.get(world).lastEnding);
        emit(world, GameRecordTypes.MATCH_END, null, null, summary);



        for (GameRoundEndComponent.RoundEndData entry : roundEnd.getPlayers()) {
            UUID uuid = entry.player().getId();
            NbtCompound result = new NbtCompound();
            result.putUuid("player", uuid);
            result.putString("end_status", entry.wasDead() ? "DEAD" : "ALIVE");
            result.putBoolean("is_winner", roundEnd.didWin(uuid));
            emit(world, GameRecordTypes.PLAYER_RESULT, null, null, result);
        }

        match.close();
        latest = match;
        live = null;
        liveWorld = null;
        present.clear();

        if (match.events().isEmpty()) {
            BeforeTheTerminalGameMode.LOGGER.warn("[BTT] match recorded: {} —— **0 事件**（记录链路可能没接到；"
                    + "见 BttRecordHooks 的事件订阅）", match.id());
        } else {
            BeforeTheTerminalGameMode.LOGGER.info("[BTT] match recorded: {} ({} events, 不落盘 → 仅 /maps replay 可看)",
                    match.id(), match.events().size());
        }



        ReplayGenerator.generateAndSend(world, match);
    }



    public static void recordPlayerJoin(ServerPlayerEntity player) {
        if (!hasActiveMatch()) return;
        notePresence(player);
    }

    public static void recordPlayerLeave(ServerPlayerEntity player) {
        if (!hasActiveMatch(player.getServerWorld())) return;
        if (!present.remove(player.getUuid())) return;
        NbtCompound payload = new NbtCompound();
        putPos(payload, "pos", player.getPos());
        emit(player.getServerWorld(), GameRecordTypes.PLAYER_LEAVE, player, null, payload);
    }

    public static void recordDeath(ServerPlayerEntity victim, @Nullable ServerPlayerEntity killer,
                                   Identifier deathReason) {
        if (!hasActiveMatch()) return;
        NbtCompound payload = new NbtCompound();
        payload.putString("death_reason", deathReason.toString());
        emit(victim.getServerWorld(), GameRecordTypes.DEATH, killer, victim, payload);
    }

    public static void recordShopPurchase(ServerPlayerEntity player, ShopEntry entry, int index, int pricePaid) {
        if (!hasActiveMatch()) return;
        ItemStack stack = entry.stack();
        Identifier itemId = Registries.ITEM.getId(stack.getItem());
        NbtCompound payload = new NbtCompound();

        payload.putString("entry_id", itemId.toString());
        payload.putInt("index", index);
        payload.putInt("price", entry.price());
        payload.putInt("price_paid", pricePaid);
        payload.putString("item", itemId.toString());
        payload.putString("item_name", Text.Serialization.toJsonString(stack.getName(), player.getRegistryManager()));
        payload.putInt("balance_after", PlayerShopComponent.KEY.get(player).balance);
        emit(player.getServerWorld(), GameRecordTypes.SHOP_PURCHASE, player, null, payload);
    }

    public static void recordTaskComplete(ServerPlayerEntity player, String taskName) {
        if (!hasActiveMatch()) return;
        NbtCompound payload = new NbtCompound();
        payload.putString("task", taskName);
        emit(player.getServerWorld(), GameRecordTypes.TASK_COMPLETE, player, null, payload);
    }

    public static void recordSkillUse(ServerPlayerEntity player, Identifier skillId,
                                      @Nullable ServerPlayerEntity target, @Nullable NbtCompound extra) {
        if (!hasActiveMatch()) return;
        NbtCompound payload = extra == null ? new NbtCompound() : extra.copy();
        payload.putString("skill", skillId.toString());
        emit(player.getServerWorld(), GameRecordTypes.SKILL_USE, player, target, payload);
    }






    public static void recordRoleChange(ServerPlayerEntity actor, String fromRoleId, String toRoleId,
                                            boolean decoy, boolean technicalRole) {
        if (!hasActiveMatch() || actor == null) return;
        NbtCompound payload = new NbtCompound();
        payload.putString("from", fromRoleId == null ? "" : fromRoleId);
        payload.putString("to", toRoleId == null ? "" : toRoleId);
        payload.putBoolean("decoy", decoy);


        payload.putBoolean("technical", technicalRole);
        emit(actor.getServerWorld(), GameRecordTypes.ROLE_CHANGE, actor, null, payload);
    }







    public static void recordCampChange(ServerPlayerEntity actor, String camp,
                                        @Nullable ServerPlayerEntity source) {
        if (!hasActiveMatch() || actor == null || camp == null || camp.isEmpty()) return;
        NbtCompound payload = new NbtCompound();
        payload.putString("camp", camp);
        emit(actor.getServerWorld(), GameRecordTypes.CAMP_CHANGE, actor, source, payload);
    }






    public static void recordTalentUse(ServerPlayerEntity actor, String talent,
                                       @Nullable ServerPlayerEntity target) {
        if (!hasActiveMatch() || actor == null) return;
        NbtCompound payload = new NbtCompound();
        payload.putString("talent", talent);

        payload.putBoolean("drunk", dev.hae.btt.component.PlayerState.KEY.get(actor).isDrunk());
        emit(actor.getServerWorld(), GameRecordTypes.TALENT_USE, actor, target, payload);
    }

    public static void recordItemUse(ServerPlayerEntity player, Identifier itemId,
                                     @Nullable ServerPlayerEntity target, @Nullable NbtCompound extra) {
        if (!hasActiveMatch()) return;
        NbtCompound payload = extra == null ? new NbtCompound() : extra.copy();
        payload.putString("item", itemId.toString());
        emit(player.getServerWorld(), GameRecordTypes.ITEM_USE, player, target, payload);
    }





    public static void recordItemPickup(ServerPlayerEntity player, ItemStack stack) {
        if (!hasActiveMatch() || stack.isEmpty()) return;
        Identifier itemId = Registries.ITEM.getId(stack.getItem());
        NbtCompound payload = new NbtCompound();
        payload.putString("item", itemId.toString());
        payload.putString("item_name", Text.Serialization.toJsonString(stack.getName(), player.getRegistryManager()));
        payload.putInt("count", stack.getCount());
        emit(player.getServerWorld(), GameRecordTypes.ITEM_PICKUP, player, null, payload);
    }





    public static void recordItemDrop(ServerPlayerEntity player, ItemStack stack) {
        if (!hasActiveMatch() || stack.isEmpty()) return;
        Identifier itemId = Registries.ITEM.getId(stack.getItem());
        NbtCompound payload = new NbtCompound();
        payload.putString("item", itemId.toString());
        payload.putString("item_name", Text.Serialization.toJsonString(stack.getName(), player.getRegistryManager()));
        payload.putInt("count", stack.getCount());
        emit(player.getServerWorld(), GameRecordTypes.ITEM_DROP, player, null, payload);
    }





    public static void recordEpilogueStart(ServerWorld world, String type) {
        if (!hasActiveMatch()) return;
        NbtCompound payload = new NbtCompound();
        payload.putString("epilogue", type == null ? "SURVIVAL" : type);
        emit(world, GameRecordTypes.EPILOGUE_START, null, null, payload);
    }

    public static void recordGlobalEvent(ServerWorld world, Identifier eventId,
                                         @Nullable ServerPlayerEntity source, @Nullable NbtCompound extra) {
        if (!hasActiveMatch()) return;
        NbtCompound payload = extra == null ? new NbtCompound() : extra.copy();
        payload.putString("event", eventId.toString());
        emit(world, GameRecordTypes.GLOBAL_EVENT, source, null, payload);
    }



    public static EventBuilder event(String kind) {
        return new EventBuilder(kind);
    }

    public static final class EventBuilder {
        private final String kind;
        private ServerWorld world;
        private ServerPlayerEntity actor;
        private ServerPlayerEntity target;
        private final NbtCompound payload = new NbtCompound();

        private EventBuilder(String kind) {
            this.kind = kind;
        }

        public EventBuilder world(ServerWorld world) {
            this.world = world;
            return this;
        }

        public EventBuilder actor(ServerPlayerEntity actor) {
            this.actor = actor;
            if (this.world == null && actor != null) this.world = actor.getServerWorld();
            return this;
        }

        public EventBuilder target(ServerPlayerEntity target) {
            this.target = target;
            return this;
        }

        public EventBuilder put(String key, String value) {
            payload.putString(key, value);
            return this;
        }

        public EventBuilder putInt(String key, int value) {
            payload.putInt(key, value);
            return this;
        }

        public EventBuilder putBool(String key, boolean value) {
            payload.putBoolean(key, value);
            return this;
        }

        public EventBuilder putUuid(String key, UUID value) {
            payload.putUuid(key, value);
            return this;
        }

        public EventBuilder putBlockPos(String key, BlockPos pos) {
            GameRecordManager.putBlockPos(payload, key, pos);
            return this;
        }

        public void record() {
            if (!hasActiveMatch() || world == null) return;
            emit(world, kind, actor, target, payload);
        }
    }



    private static void notePresence(ServerPlayerEntity player) {
        if (!hasActiveMatch(player.getServerWorld())) return;
        if (!present.add(player.getUuid())) return;
        NbtCompound payload = new NbtCompound();
        putPos(payload, "pos", player.getPos());
        emit(player.getServerWorld(), GameRecordTypes.PLAYER_JOIN, player, null, payload);
    }


    private static void emit(ServerWorld world, String kind, @Nullable ServerPlayerEntity actor,
                             @Nullable ServerPlayerEntity target, @Nullable NbtCompound payload) {
        if (!hasActiveMatch(world)) return;
        MatchRecord match = live;
        NbtCompound body = payload == null ? new NbtCompound() : payload.copy();
        if (actor != null) body.putUuid("actor", actor.getUuid());
        if (target != null) body.putUuid("target", target.getUuid());
        if (world != null) body.putString("dimension", world.getRegistryKey().getValue().toString());
        match.append(kind, world == null ? 0 : world.getTime(), System.currentTimeMillis(), body);
    }


    private static NbtCompound roleSnapshot(UUID uuid, @Nullable ServerPlayerEntity player, Role role) {
        NbtCompound info = new NbtCompound();
        info.putUuid("uuid", uuid);
        if (player != null) info.putString("name", player.getGameProfile().getName());


        if (player != null) info.putInt("number", letterNumber(player.getServerWorld(), uuid));
        if (role != null) {
            info.putString("role", role.identifier().toString());
            Roles.Faction faction = Roles.factionOf(role);
            info.putString("faction", faction == null ? "UNKNOWN" : faction.name());
        }
        return info;
    }


    private static int letterNumber(@Nullable ServerWorld world, UUID uuid) {
        if (world == null || uuid == null) return 0;
        String csv = GameState.KEY.get(world).letterNumbers;
        if (csv == null || csv.isEmpty()) return 0;
        String key = uuid.toString();
        for (String part : csv.split(",")) {
            int colon = part.indexOf(':');
            if (colon <= 0) continue;
            if (key.equals(part.substring(0, colon))) {
                try {
                    return Integer.parseInt(part.substring(colon + 1));
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    static void putPos(NbtCompound data, String key, Vec3d pos) {
        data.putDouble(key + "X", pos.x);
        data.putDouble(key + "Y", pos.y);
        data.putDouble(key + "Z", pos.z);
    }

    static void putBlockPos(NbtCompound data, String key, BlockPos pos) {
        data.putInt(key + "X", pos.getX());
        data.putInt(key + "Y", pos.getY());
        data.putInt(key + "Z", pos.getZ());
    }






    public static NbtCompound toNbt(MatchRecord match) {
        NbtCompound root = new NbtCompound();
        root.putString("match_id", match.id().toString());
        root.putString("dimension", match.dimension().toString());
        root.putString("game_mode", match.gameMode().toString());
        root.putString("map_effect", match.mapEffect().toString());
        root.putLong("start_tick", match.startTick());
        root.putLong("start_ms", match.startMs());
        NbtList list = new NbtList();
        for (GameRecordEvent event : match.events()) {
            NbtCompound row = new NbtCompound();
            row.putInt("seq", event.seq());
            row.putString("type", event.type());
            row.putLong("tick", event.worldTick());
            row.putLong("ms", event.realTimeMs());
            row.put("data", event.data().copy());
            list.add(row);
        }
        root.put("events", list);
        return root;
    }
}
