package dev.hae.btt.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.doctor4t.wathe.cca.MapVariablesWorldComponent;
import dev.hae.btt.map.BttMapEntry;
import dev.hae.btt.map.BttMaps;
import dev.hae.btt.map.BttRoom;
import dev.hae.btt.map.MapVoting;
import dev.hae.btt.map.Rooms;
import dev.hae.btt.role.RoleWeights;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
import java.util.List;















public final class BttCommands {
    private BttCommands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, environment) -> {
            var forceMapArg = CommandManager.argument("id", StringArgumentType.string())
                    .suggests((ctx, b) -> {
                        for (String id : BttMaps.all().stream().map(BttMapEntry::id).toList()) b.suggest(id);
                        return b.buildFuture();
                    })
                    .executes(ctx -> forceMap(ctx.getSource(), StringArgumentType.getString(ctx, "id")));

            var vote = CommandManager.literal("vote")
                    .executes(ctx -> startVote(ctx.getSource()))
                    .then(forceMapArg);

            var weights = CommandManager.literal("weights")
                    .executes(ctx -> showWeights(ctx.getSource()));

            var replay = CommandManager.literal("replay")
                    .executes(ctx -> showReplay(ctx.getSource()))

                    .then(CommandManager.literal("play")
                            .executes(ctx -> playReplay(ctx.getSource())))
                    .then(CommandManager.literal("stop")
                            .executes(ctx -> stopReplay(ctx.getSource())));

            var rotationClear = CommandManager.literal("clear")
                    .executes(ctx -> clearRotation(ctx.getSource()));
            var rotation = CommandManager.literal("rotation")
                    .executes(ctx -> showRotation(ctx.getSource()))
                    .then(rotationClear);




            var roomsList = CommandManager.literal("list").executes(ctx -> roomListCmd(ctx.getSource()));
            var roomsSet = CommandManager.literal("set")
                    .then(roomArg().executes(ctx -> roomSetCmd(ctx.getSource(),
                                    IntegerArgumentType.getInteger(ctx, "room"), -1))
                            .then(indexArg().executes(ctx -> roomSetCmd(ctx.getSource(),
                                    IntegerArgumentType.getInteger(ctx, "room"),
                                    IntegerArgumentType.getInteger(ctx, "index")))));
            var roomsRemove = CommandManager.literal("remove")
                    .then(roomArg().executes(ctx -> roomRemoveCmd(ctx.getSource(),
                                    IntegerArgumentType.getInteger(ctx, "room"), -1))
                            .then(indexArg().executes(ctx -> roomRemoveCmd(ctx.getSource(),
                                    IntegerArgumentType.getInteger(ctx, "room"),
                                    IntegerArgumentType.getInteger(ctx, "index")))));
            var roomsClear = CommandManager.literal("clear")
                    .executes(ctx -> roomClearCmd(ctx.getSource(), 0))
                    .then(roomArg().executes(ctx -> roomClearCmd(ctx.getSource(),
                            IntegerArgumentType.getInteger(ctx, "room"))));
            var roomsTp = CommandManager.literal("tp")
                    .then(roomArg().executes(ctx -> roomTpCmd(ctx.getSource(),
                                    IntegerArgumentType.getInteger(ctx, "room"), -1))
                            .then(indexArg().executes(ctx -> roomTpCmd(ctx.getSource(),
                                    IntegerArgumentType.getInteger(ctx, "room"),
                                    IntegerArgumentType.getInteger(ctx, "index")))));
            var roomsDump = CommandManager.literal("dump").executes(ctx -> roomDumpCmd(ctx.getSource()));
            var rooms = CommandManager.literal("rooms")
                    .executes(ctx -> roomListCmd(ctx.getSource()))
                    .then(roomsList).then(roomsSet).then(roomsRemove).then(roomsClear)
                    .then(roomsTp).then(roomsDump);



            var variablesDump = CommandManager.literal("dump").executes(ctx -> variablesDumpCmd(ctx.getSource()));
            var variablesCheck = CommandManager.literal("check").executes(ctx -> variablesCheckCmd(ctx.getSource()));
            var variables = CommandManager.literal("variables")
                    .executes(ctx -> variablesDumpCmd(ctx.getSource()))
                    .then(variablesDump)
                    .then(variablesCheck);
            var exportId = CommandManager.argument("id", StringArgumentType.string())
                    .executes(ctx -> exportMapCmd(ctx.getSource(), StringArgumentType.getString(ctx, "id")));
            var exportMap = CommandManager.literal("export").then(exportId);




            dispatcher.register(CommandManager.literal("maps")
                    .requires(src -> src.hasPermissionLevel(2))
                    .executes(ctx -> listMaps(ctx.getSource()))
                    .then(vote)
                    .then(weights)
                    .then(replay)
                    .then(rotation)
                    .then(rooms)
                    .then(variables)
                    .then(exportMap));




            dispatcher.register(CommandManager.literal("start")
                    .requires(src -> src.hasPermissionLevel(2))
                    .executes(ctx -> startHere(ctx.getSource(), 8))
                    .then(CommandManager.argument("minutes", IntegerArgumentType.integer(1, 60))
                            .executes(ctx -> startHere(ctx.getSource(),
                                    IntegerArgumentType.getInteger(ctx, "minutes")))));
        });
    }








    private static int startHere(ServerCommandSource src, int minutes) {
        ServerWorld world = src.getWorld();
        if (!dev.hae.btt.game.SeatAssignment.isBttMode(world)) {
            src.sendMessage(Text.literal("[BTT] 当前维度不是 BTT 局：先用 `/maps vote <id>` 或 "
                    + "`/execute in <维度> run wathe:mapVariables …` 切到目标地图的维度").formatted(Formatting.RED));
            return 0;
        }
        dev.doctor4t.wathe.cca.GameWorldComponent gwc =
                dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(world);
        dev.doctor4t.wathe.api.MapEffect effect = gwc.getMapEffect() != null
                ? gwc.getMapEffect() : dev.doctor4t.wathe.api.WatheMapEffects.HARPY_EXPRESS_DAY;
        dev.doctor4t.wathe.game.GameFunctions.startGame(world,
                dev.hae.btt.game.GameModes.modeForStart(gwc), effect,
                dev.doctor4t.wathe.game.GameConstants.getInTicks(minutes, 0));
        src.sendMessage(Text.literal("[BTT] 已按当前地图开局（维度 " + dimName(world)
                + "，特效 " + effect.identifier + "，" + minutes + " 分钟）")
                .formatted(Formatting.GREEN));
        return 1;
    }

    private static int listMaps(ServerCommandSource src) {
        if (BttMaps.count() == 0) {
            src.sendMessage(Text.literal("[BTT] 没有已注册的地图（数据包 `data/btt/maps/*.json`，"
                    + "或 Spark 的 `data/wathe/maps/*.json`／旧版 `data/wathe/areas/*.json`）；"
                    + "当前模式下不会有地图投票。").formatted(Formatting.YELLOW));
            return 0;
        }

        ServerWorld active = dev.hae.btt.map.MapDimensions.gameWorldOrOverworld(src.getServer());
        src.sendMessage(Text.literal("[BTT] 地图注册表（" + BttMaps.count() + "）；当前对局维度："
                + (active == null ? "?" : dimName(active))).formatted(Formatting.GOLD));
        for (BttMapEntry e : BttMaps.all()) {
            boolean loaded = dimensionLoaded(src, e.dimension());
            src.sendMessage(Text.literal(" - " + e.id() + "  " + e.displayName()
                    + "  [" + e.dimension() + "]  " + e.minPlayers() + "~" + e.maxPlayers()
                    + (e.mapEffect() == null || e.mapEffect().isEmpty() ? "" : "  fx=" + e.mapEffect())
                    + (e.rooms().isEmpty() ? "" : "  rooms=" + e.rooms().size())
                    + (e.spawnPos().isPresent() ? "" : "  变量=维度现值")
                    + (BttMaps.playableRange(e) ? "" : "  ⚠人数区间与 6~24 无交集")
                    + (loaded ? "" : "  ⚠维度未加载")).formatted(loaded ? Formatting.WHITE : Formatting.GRAY));
        }
        return BttMaps.count();
    }







    private static boolean dimensionLoaded(ServerCommandSource src, String dimension) {
        Identifier id = Identifier.tryParse(dimension);
        if (id == null) return false;
        MinecraftServer server = src.getServer();
        RegistryKey<net.minecraft.world.World> key = RegistryKey.of(RegistryKeys.WORLD, id);
        for (ServerWorld world : server.getWorlds()) {
            if (world.getRegistryKey().equals(key)) return true;
        }
        return false;
    }

    private static int startVote(ServerCommandSource src) {
        ServerWorld world = src.getWorld();

        MapVoting.startPreview(world);
        src.sendMessage(Text.literal("[BTT] 已请求发起地图投票（预览模式：不按人数过滤；需 ≥2 张可用地图，见 /maps）"));
        return 1;
    }

    private static int forceMap(ServerCommandSource src, String id) {
        String err = MapVoting.force(src.getWorld(), id);
        src.sendMessage(err == null
                ? Text.literal("[BTT] 已套用地图 " + id).formatted(Formatting.GREEN)
                : Text.literal("[BTT] 套用失败：" + err).formatted(Formatting.RED));
        return err == null ? 1 : 0;
    }





    private static int variablesDumpCmd(ServerCommandSource src) {
        ServerWorld world = src.getWorld();
        src.sendMessage(Text.literal("[BTT] " + dimName(world)
                + " 的地图变量（贴进 `data/btt/maps/<id>.json`）:").formatted(Formatting.GOLD));
        for (String line : variablesJson(world).split("\n")) src.sendMessage(Text.literal(line));
        return 1;
    }


    private static int exportMapCmd(ServerCommandSource src, String id) {
        ServerWorld world = src.getWorld();
        var st = roomsStateOf(src);

        src.sendMessage(Text.literal("[BTT] 存成 `data/wathe/maps/" + id + ".json`（Spark 标准格式；维度 = "
                + dimName(world) + "，人数区间默认 6~24）:").formatted(Formatting.GOLD));
        List<String> out = new ArrayList<>();
        out.add("{");
        out.add("  \"dimension\": \"" + dimName(world) + "\",");
        out.add("  \"display_name\": \"" + id + "\",");
        out.add("  \"description\": \"\",");
        out.add("  \"min_players\": 6,");
        out.add("  \"max_players\": 24,");

        for (String line : variablesJson(world).split("\n")) out.add(line);
        out.set(out.size() - 1, out.get(out.size() - 1) + ",");
        out.add("  \"enhancements\": {");
        if (!st.rooms.isEmpty()) {
            for (String line : BttRoom.listToJson(st.rooms).split("\n")) out.add("  " + line);
        }
        out.add("  }");
        out.add("}");
        for (String line : out) src.sendMessage(Text.literal(line));

        try {
            dev.hae.btt.map.BttMapEntry back = dev.hae.btt.map.BttMapEntry.fromSparkEntry(id,
                    com.google.gson.JsonParser.parseString(String.join("\n", out)).getAsJsonObject());
            boolean ok = id.equals(back.id()) && !back.dimension().isEmpty()
                    && back.rooms().size() == st.rooms.size();
            src.sendMessage(Text.literal((ok ? " ✅ 自检：条目可被读回（" : " ⚠ 自检失败：")
                    + back.dimension() + "，房间 " + back.rooms().size() + " 间）")
                    .formatted(ok ? Formatting.GREEN : Formatting.RED));
        } catch (Exception ex) {
            src.sendMessage(Text.literal(" ⚠ 自检失败：" + ex).formatted(Formatting.RED));
        }
        return 1;
    }


    private static String variablesJson(ServerWorld world) {
        MapVariablesWorldComponent v = MapVariablesWorldComponent.KEY.get(world);
        var spawn = v.getSpawnPos();
        var spec = v.getSpectatorSpawnPos();
        return "  \"variables\": {\n"
                + "    \"spawn_position\": " + posJson(spawn.pos.x, spawn.pos.y, spawn.pos.z, spawn.yaw, spawn.pitch) + ",\n"
                + "    \"spectator_spawn_position\": "
                + posJson(spec.pos.x, spec.pos.y, spec.pos.z, spec.yaw, spec.pitch) + ",\n"
                + "    \"ready_area\": " + boxJson(v.getReadyArea()) + ",\n"
                + "    \"play_area\": " + boxJson(v.getPlayArea()) + ",\n"
                + "    \"play_area_offset\": " + vecJson(v.getPlayAreaOffset()) + ",\n"
                + "    \"reset_template_area\": " + boxJson(v.getResetTemplateArea()) + ",\n"
                + "    \"reset_paste_offset\": " + vecJson(v.getResetPasteOffset()) + "\n"
                + "  }";
    }





    private static int variablesCheckCmd(ServerCommandSource src) {
        ServerWorld world = src.getWorld();
        MapVariablesWorldComponent v = MapVariablesWorldComponent.KEY.get(world);
        List<String> missing = new ArrayList<>();
        List<String> notes = new ArrayList<>();

        var spawn = v.getSpawnPos();
        boolean spawnSet = !isZeroPos(spawn.pos.x, spawn.pos.y, spawn.pos.z);
        if (!spawnSet) missing.add("spawn_position（0,0,0）");
        var spec = v.getSpectatorSpawnPos();
        if (isZeroPos(spec.pos.x, spec.pos.y, spec.pos.z)) missing.add("spectator_spawn_position（0,0,0）");
        if (isEmptyBox(v.getReadyArea())) missing.add("ready_area（零体积）");
        if (isEmptyBox(v.getPlayArea())) missing.add("play_area（零体积）");
        if (isEmptyBox(v.getResetTemplateArea())) missing.add("reset_template_area（零体积）");
        if (isZeroVec(v.getPlayAreaOffset())) notes.add("play_area_offset = (0,0,0)：开局不做游玩区位移（靠 rooms 落点时正常）");
        if (isZeroVec(v.getResetPasteOffset())) notes.add("reset_paste_offset = (0,0,0)：列车重置＝原地覆盖（若 reset 区没配好会覆盖原建筑）");

        if (spawnSet && !v.getReadyArea().contains(spawn.pos)) {
            notes.add("⚠ 出生点不在 ready_area 内 → 开局 `getReadyPlayerList` 不会算上站/传到该点的玩家");
        }
        var st = dev.hae.btt.component.MapVoteState.KEY.get(world);
        int outside = 0;
        for (BttRoom room : st.rooms) {
            for (BttRoom.Spawn s : room.spawnPoints()) {
                if (!v.getPlayArea().contains(new net.minecraft.util.math.Vec3d(s.x(), s.y(), s.z()))) outside++;
            }
        }
        if (outside > 0) notes.add("⚠ 有 " + outside + " 个房间出生点不在 play_area 内");

        int roomSpawns = 0;
        for (BttRoom room : st.rooms) roomSpawns += room.spawnPoints().size();
        if (st.rooms.isEmpty()) {
            notes.add("⚠ 该维度没有任何房间（rooms 为空）→ 开局不按房间铺人、回退 wathe 原版落点；"
                    + "补：站到落点执行 `/maps rooms set <房间号>`");
        } else if (roomSpawns == 0) {
            notes.add("⚠ 房间表里有 " + st.rooms.size() + " 间，但**一个出生点都没有** → 等同没配");
        } else if (roomSpawns < 6) {
            notes.add("⚠ 房间出生点只有 " + roomSpawns + " 个（< 最小 6 人）→ 多人局会出现多人同点");
        }

        String dim = dimName(world);
        List<String> entries = new ArrayList<>();
        List<String> entryNotes = new ArrayList<>();
        for (BttMapEntry e : BttMaps.all()) {
            if (!e.dimension().equals(dim)) continue;
            entries.add(e.id()
                    + (e.rooms().isEmpty() ? "" : "(rooms=" + e.rooms().size() + ")"));
            if (e.rooms().isEmpty()) {
                entryNotes.add("⚠ " + e.id() + "：条目里**没有 rooms** → 套用该地图后不会按房间落点"
                        + "（该图若需要房间，用 `/maps rooms` 配好后 `/maps export " + e.id() + "` 回写）");
            } else if (st.rooms.isEmpty()) {
                entryNotes.add("· " + e.id() + "：条目声明了 " + e.rooms().size()
                        + " 间房间（尚未套用到本维度 → 本维度运行时房间表为空）");
            }
            if (!BttMaps.playableRange(e)) {
                entryNotes.add("⚠ " + e.id() + " 的人数区间 " + e.minPlayers() + "~" + e.maxPlayers() + " 与 BTT 6–24 无交集");
            }
            String fx = e.mapEffect();
            if (fx == null || fx.isEmpty()) {
                entryNotes.add("· " + e.id() + "：map_effect 为空 → 沿用该维度当前特效");
            } else {
                net.minecraft.util.Identifier fid = Identifier.tryParse(fx);
                if (fid == null || dev.doctor4t.wathe.api.WatheMapEffects.MAP_EFFECTS.get(fid) == null) {
                    entryNotes.add("⚠ " + e.id() + "：map_effect「" + fx + "」不是已注册的特效");
                }
            }
            var enh = e.enhancements();
            if (enh != null && !enh.unconsumed().isEmpty()) {
                entryNotes.add("· " + e.id() + "：数据包里有 BTT 未消费的段 " + enh.unconsumed() + "（仅 Spark 生效）");
            }
            if (enh != null && !enh.baselineLocked().isEmpty()) {
                entryNotes.add("· " + e.id() + "：" + enh.baselineLocked()
                        + " 由 wathe 客户端写死（附加模组无法接管；这三张自带地图的取值与 wathe 内置相同，故无视觉差异）");
            }
        }
        src.sendMessage(Text.literal("[BTT] " + dim + " 地图变量检查：").formatted(Formatting.GOLD));
        if (missing.isEmpty()) {
            src.sendMessage(Text.literal(" ✅ 7 项均已设置（spawn / spectator / ready / play / reset / playOffset / pasteOffset）")
                    .formatted(Formatting.GREEN));
        } else {
            src.sendMessage(Text.literal(" ⚠ 缺失 " + missing.size() + " 项：" + String.join("、", missing))
                    .formatted(Formatting.RED));
        }
        for (String n : notes) src.sendMessage(Text.literal(" · " + n).formatted(Formatting.YELLOW));
        src.sendMessage(Text.literal(" 注册条目：" + (entries.isEmpty()
                ? "无（该维度没有 `data/btt/maps/*.json` 条目 → 不会出现在投票里）"
                : String.join("、", entries))).formatted(entries.isEmpty() ? Formatting.RED : Formatting.GRAY));
        for (String n : entryNotes) src.sendMessage(Text.literal(" " + n).formatted(
                n.startsWith("⚠") ? Formatting.RED : Formatting.GRAY));
        boolean hasWarn = !missing.isEmpty() || notes.stream().anyMatch(n -> n.startsWith("⚠"))
                || entryNotes.stream().anyMatch(n -> n.startsWith("⚠"));
        return hasWarn ? 0 : 1;
    }

    private static boolean isEmptyBox(Box b) {
        return Math.abs(b.maxX - b.minX) < 1.0E-6 || Math.abs(b.maxY - b.minY) < 1.0E-6
                || Math.abs(b.maxZ - b.minZ) < 1.0E-6;
    }

    private static boolean isZeroPos(double x, double y, double z) {
        return Math.abs(x) < 1.0E-6 && Math.abs(y) < 1.0E-6 && Math.abs(z) < 1.0E-6;
    }

    private static boolean isZeroVec(Vec3i v) {
        return v.getX() == 0 && v.getY() == 0 && v.getZ() == 0;
    }

    private static String posJson(double x, double y, double z, float yaw, float pitch) {
        return "{\"x\": " + num(x) + ", \"y\": " + num(y) + ", \"z\": " + num(z)
                + ", \"yaw\": " + num(yaw) + ", \"pitch\": " + num(pitch) + "}";
    }

    private static String boxJson(Box b) {
        return "{\"min_x\": " + num(b.minX) + ", \"min_y\": " + num(b.minY) + ", \"min_z\": " + num(b.minZ)
                + ", \"max_x\": " + num(b.maxX) + ", \"max_y\": " + num(b.maxY) + ", \"max_z\": " + num(b.maxZ) + "}";
    }

    private static String vecJson(Vec3i v) {
        return "{\"x\": " + v.getX() + ", \"y\": " + v.getY() + ", \"z\": " + v.getZ() + "}";
    }


    private static String num(double v) {
        return Double.toString(Math.round(v * 100.0) / 100.0);
    }

    private static String dimName(ServerWorld world) {
        return world.getRegistryKey().getValue().toString();
    }



    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<ServerCommandSource, Integer> roomArg() {
        return CommandManager.argument("room", IntegerArgumentType.integer(1));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<ServerCommandSource, Integer> indexArg() {
        return CommandManager.argument("index", IntegerArgumentType.integer(1));
    }


    private static dev.hae.btt.component.MapVoteState roomsStateOf(ServerCommandSource src) {
        var st = dev.hae.btt.component.MapVoteState.KEY.get(src.getWorld());
        if (!st.rooms.isEmpty() || src.getServer().getOverworld() == src.getWorld()) return st;
        var main = dev.hae.btt.component.MapVoteState.KEY.get(src.getServer().getOverworld());
        return main.rooms.isEmpty() ? st : main;
    }


    private static ServerWorld roomsWorldOf(ServerCommandSource src) {
        ServerWorld w = src.getWorld();
        ServerWorld main = src.getServer().getOverworld();
        if (w == main || !dev.hae.btt.component.MapVoteState.KEY.get(w).rooms.isEmpty()) return w;
        return main;
    }

    private static int roomListCmd(ServerCommandSource src) {
        var st = roomsStateOf(src);
        if (st.rooms.isEmpty()) {
            src.sendMessage(Text.literal("[BTT] " + dimName(roomsWorldOf(src)) + " 当前没有房间出生点配置。用法：站到目标点执行 "
                    + "/maps rooms set <房间号> —— 房间号从 1 开始，依次添加出生点；"
                    + "也可以用数据包 `data/btt/maps/<id>.json` 的 \"rooms\" 或 Spark `enhancements.rooms` 给初值。"
                    + "给别的维度配置：/execute in <维度> positioned <x y z> run maps rooms set <房间号>。")
                    .formatted(Formatting.YELLOW));
            return 0;
        }
        src.sendMessage(Text.literal("[BTT] 房间出生点（" + dimName(roomsWorldOf(src)) + "，" + st.rooms.size() + " 间"
                + (st.roomsMapId.isEmpty() ? "" : "，来源地图 " + st.roomsMapId) + "）:").formatted(Formatting.GOLD));
        for (int i = 0; i < st.rooms.size(); i++) {
            BttRoom r = st.rooms.get(i);
            StringBuilder sb = new StringBuilder(" - ").append(i + 1).append(". ").append(r.name())
                    .append("  出生点 ").append(r.spawnPoints().size()).append(" 个");
            for (int k = 0; k < r.spawnPoints().size(); k++) {
                var s = r.spawnPoints().get(k);
                sb.append("\n     [").append(k + 1).append("] ")
                        .append(String.format("%.2f %.2f %.2f", s.x(), s.y(), s.z()));
            }
            src.sendMessage(Text.literal(sb.toString()));
        }
        return st.rooms.size();
    }






    private static int roomSetCmd(ServerCommandSource src, int room, int index) {
        ServerPlayerEntity player = src.getPlayer();
        if (player == null) {
            src.sendMessage(Text.literal("[BTT] 该命令需要玩家执行（要记录你站的位置与朝向）。").formatted(Formatting.RED));
            return 0;
        }
        var st = roomsStateOf(src);
        while (st.rooms.size() < room) {
            st.rooms.add(new BttRoom("Room " + (st.rooms.size() + 1), 0, new ArrayList<>()));
        }
        BttRoom cur = st.rooms.get(room - 1);
        List<BttRoom.Spawn> spawns = new ArrayList<>(cur.spawnPoints());
        var at = src.getPosition();
        BttRoom.Spawn spawn = new BttRoom.Spawn(at.x, at.y, at.z,
                player.getYaw(), player.getPitch());
        if (index <= 0 || index > spawns.size()) spawns.add(spawn);
        else spawns.set(index - 1, spawn);
        st.rooms.set(room - 1, new BttRoom(cur.name(), cur.maxPlayers(), spawns));
        st.sync();
        src.sendMessage(Text.literal("[BTT] " + dimName(roomsWorldOf(src)) + "：房间 " + room
                + " 出生点已更新（共 " + spawns.size() + " 个）；"
                + "开局会按房间顺序铺人，落点就是这里的位置与朝向。").formatted(Formatting.GREEN));
        return 1;
    }

    private static int roomRemoveCmd(ServerCommandSource src, int room, int index) {
        var st = roomsStateOf(src);
        if (room < 1 || room > st.rooms.size()) {
            src.sendMessage(Text.literal("[BTT] 没有房间 " + room).formatted(Formatting.RED));
            return 0;
        }
        BttRoom cur = st.rooms.get(room - 1);
        List<BttRoom.Spawn> spawns = new ArrayList<>(cur.spawnPoints());
        if (spawns.isEmpty()) {
            src.sendMessage(Text.literal("[BTT] 房间 " + room + " 本来就没有出生点。").formatted(Formatting.YELLOW));
            return 0;
        }
        int idx = (index <= 0 || index > spawns.size()) ? spawns.size() : index;
        spawns.remove(idx - 1);
        st.rooms.set(room - 1, new BttRoom(cur.name(), cur.maxPlayers(), spawns));
        st.sync();
        src.sendMessage(Text.literal("[BTT] 已删除房间 " + room + " 的出生点 " + idx
                + "（剩 " + spawns.size() + " 个）"));
        return 1;
    }

    private static int roomClearCmd(ServerCommandSource src, int room) {
        var st = roomsStateOf(src);
        if (room <= 0) {
            st.rooms.clear();
            st.roomsMapId = "";
            st.sync();
            src.sendMessage(Text.literal("[BTT] 已清空全部房间出生点（开局恢复 wathe 原版落点）。"));
            return 1;
        }
        if (room > st.rooms.size()) {
            src.sendMessage(Text.literal("[BTT] 没有房间 " + room).formatted(Formatting.RED));
            return 0;
        }
        BttRoom cur = st.rooms.get(room - 1);
        st.rooms.set(room - 1, new BttRoom(cur.name(), cur.maxPlayers(), new ArrayList<>()));
        st.sync();
        src.sendMessage(Text.literal("[BTT] 已清空房间 " + room + " 的出生点。"));
        return 1;
    }

    private static int roomTpCmd(ServerCommandSource src, int room, int index) {
        ServerPlayerEntity player = src.getPlayer();
        if (player == null) {
            src.sendMessage(Text.literal("[BTT] 该命令需要玩家执行。").formatted(Formatting.RED));
            return 0;
        }
        var st = roomsStateOf(src);
        if (room < 1 || room > st.rooms.size()) {
            src.sendMessage(Text.literal("[BTT] 没有房间 " + room).formatted(Formatting.RED));
            return 0;
        }
        List<BttRoom.Spawn> spawns = st.rooms.get(room - 1).spawnPoints();
        if (spawns.isEmpty()) {
            src.sendMessage(Text.literal("[BTT] 房间 " + room + " 还没有出生点。").formatted(Formatting.YELLOW));
            return 0;
        }
        int idx = (index <= 0 || index > spawns.size()) ? 1 : index;
        ServerWorld target = roomsWorldOf(src);
        Rooms.teleport(player, target, spawns.get(idx - 1));
        src.sendMessage(Text.literal("[BTT] 已传送到 " + dimName(target) + " 房间 " + room
                + " 的出生点 " + idx + "。"));
        return 1;
    }

    private static int roomDumpCmd(ServerCommandSource src) {
        var st = roomsStateOf(src);
        if (st.rooms.isEmpty()) {
            src.sendMessage(Text.literal("[BTT] 没有可导出的房间配置。").formatted(Formatting.YELLOW));
            return 0;
        }
        src.sendMessage(Text.literal("[BTT] " + dimName(roomsWorldOf(src))
                + " 的房间片段 —— 贴进 `data/btt/maps/<id>.json`（与其它变量同级）:"));
        for (String line : BttRoom.listToJson(st.rooms).split("\n")) {
            src.sendMessage(Text.literal(line));
        }
        return 1;
    }

    private static int showWeights(ServerCommandSource src) {
        if (!RoleWeights.hasCustomWeights()) {
            src.sendMessage(Text.literal("[BTT] 角色权重全为默认（1）——池内均匀抽取。").formatted(Formatting.GRAY));
        } else {
            src.sendMessage(Text.literal("[BTT] 生效的角色权重: " + RoleWeights.describeCustomWeights()));
        }
        return 1;
    }






    private static int showReplay(ServerCommandSource src) {
        return playReplay(src);
    }


    private static int playReplay(ServerCommandSource src) {
        if (!(src.getEntity() instanceof net.minecraft.server.network.ServerPlayerEntity player)) {
            src.sendMessage(Text.literal("[BTT] 这个子命令要由玩家执行（回放是逐行发给玩家的）。").formatted(Formatting.YELLOW));
            return 0;
        }
        if (dev.hae.btt.record.replay.ReplayPlayer.isPlaying(player)) {
            src.sendMessage(Text.literal("[BTT] 你已经在看回放了（/maps replay stop 停止）。").formatted(Formatting.YELLOW));
            return 0;
        }
        return dev.hae.btt.record.replay.ReplayPlayer.start(player) ? 1 : 0;
    }


    private static int stopReplay(ServerCommandSource src) {
        if (!(src.getEntity() instanceof net.minecraft.server.network.ServerPlayerEntity player)) return 0;
        boolean stopped = dev.hae.btt.record.replay.ReplayPlayer.stop(player);
        src.sendMessage(Text.literal(stopped ? "[BTT] 回放已停止。" : "[BTT] 你当前没有在回放。")
                .formatted(Formatting.GRAY));
        return stopped ? 1 : 0;
    }


    private static int showRotation(ServerCommandSource src) {
        var rot = dev.hae.btt.component.RotationState.KEY.get(src.getWorld());
        var all = rot.all();
        if (all.isEmpty()) {
            src.sendMessage(Text.literal("[BTT] 轮换历史为空（还没打完一局，或已被清空）。").formatted(Formatting.GRAY));
            return 0;
        }
        src.sendMessage(Text.literal("[BTT] 轮换历史（窗口 " + dev.hae.btt.component.RotationState.WINDOW + " 局）："
                + "强度 " + dev.hae.btt.config.BttConfig.HANDLER.instance().rotationStrength).formatted(Formatting.GOLD));
        for (var e : all.entrySet()) {
            var player = src.getServer().getPlayerManager().getPlayer(e.getKey());
            String name = player != null ? player.getGameProfile().getName() : e.getKey().toString().substring(0, 8);
            StringBuilder sb = new StringBuilder(" - " + name + ": ");
            for (var g : e.getValue()) sb.append(g.bucket().name(), 0, 1).append(' ');
            src.sendMessage(Text.literal(sb.toString()));
        }
        return all.size();
    }

    private static int clearRotation(ServerCommandSource src) {
        dev.hae.btt.component.RotationState.KEY.get(src.getWorld()).clear();
        src.sendMessage(Text.literal("[BTT] 轮换历史已清空。").formatted(Formatting.YELLOW));
        return 1;
    }

}
