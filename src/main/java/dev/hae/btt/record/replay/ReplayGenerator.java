package dev.hae.btt.record.replay;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.hae.btt.record.GameRecordEvent;
import dev.hae.btt.record.GameRecordManager;
import dev.hae.btt.record.GameRecordTypes;
import dev.hae.btt.record.MatchRecord;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;








public final class ReplayGenerator {
    private ReplayGenerator() {}


    public record PlayerInfo(String name, String roleTranslationKey, int roleColor, String roleId, int number) {}

    private static Map<UUID, PlayerInfo> cacheOf(MatchRecord match) {
        Map<UUID, PlayerInfo> cache = new HashMap<>();
        for (GameRecordEvent event : match.events()) {
            if (!GameRecordTypes.ROLE_ASSIGNED.equals(event.type())) continue;
            NbtCompound pd = event.data().getCompound("player");
            UUID uuid = pd.getUuid("uuid");
            String name = pd.getString("name");
            Identifier roleId = Identifier.tryParse(pd.getString("role"));
            Role role = roleId == null ? null : dev.hae.btt.role.Roles.byId(roleId.toString());
            String key = roleId == null ? "unknown"
                    : "announcement.role." + roleId.getNamespace() + "." + roleId.getPath();
            cache.put(uuid, new PlayerInfo(name, key, role == null ? 0xFFFFFF : role.color(),
                    roleId == null ? "" : roleId.toString(),
                    pd.contains("number") ? pd.getInt("number") : 0));
        }
        return cache;
    }


    public static List<Text> lines(MatchRecord match, ServerWorld world) {
        Map<UUID, PlayerInfo> cache = cacheOf(match);
        List<Text> out = new ArrayList<>();

        out.add(separator());
        out.add(Text.translatable("btt.replay.start").formatted(Formatting.GRAY));
        out.add(premise(match));
        NbtCompound start = firstData(match, GameRecordTypes.MATCH_START);
        if (start != null) {
            out.add(Text.translatable("btt.replay.header", start.getString("game_mode"),
                    mapLabel(match), start.getInt("player_count")).formatted(Formatting.GRAY));
        }

        NbtCompound end = firstData(match, GameRecordTypes.MATCH_END);
        if (end != null && !end.getString("btt_ending").isEmpty()) {
            String last = end.getString("btt_ending");

            out.add(Text.translatable("btt.replay.header.ending").formatted(Formatting.GRAY)
                    .append(endingName(last).copy().withColor(dev.hae.btt.victory.Endings.colorFor(last))));
        }
        out.add(Text.empty());


        out.addAll(rosterLines(cache));
        out.add(Text.empty());

        List<GameRecordEvent> events = new ArrayList<>(match.events());
        events.sort(Comparator.comparingInt(GameRecordEvent::seq));
        for (GameRecordEvent event : events) {

            if (GameRecordTypes.PLAYER_JOIN.equals(event.type())
                    || GameRecordTypes.MATCH_END.equals(event.type())
                    || GameRecordTypes.PLAYER_RESULT.equals(event.type())) continue;
            Text line = format(event, match, world, cache);
            if (line == null) continue;
            out.add(Text.literal("[" + formatTime(event.worldTick(), match.startTick()) + "] ")
                    .formatted(Formatting.GRAY).append(line));
        }


        out.add(Text.translatable("btt.replay.finish").formatted(Formatting.GRAY));
        out.add(separator());
        return out;
    }


    public static void generateAndSend(ServerWorld world, MatchRecord match) {

        dev.hae.btt.record.replay.ReplayPlayer.startAll(world.getServer(), eventLines(match, world));
    }


    public static List<Text> eventLines(MatchRecord match, ServerWorld world) {
        Map<UUID, PlayerInfo> cache = cacheOf(match);
        List<Text> out = new ArrayList<>();

        out.add(separator());
        out.add(Text.translatable("btt.replay.start").formatted(Formatting.GRAY));
        out.add(premise(match));
        List<GameRecordEvent> events = new ArrayList<>(match.events());
        events.sort(Comparator.comparingInt(GameRecordEvent::seq));
        for (GameRecordEvent event : events) {
            if (GameRecordTypes.PLAYER_JOIN.equals(event.type())
                    || GameRecordTypes.MATCH_END.equals(event.type())
                    || GameRecordTypes.PLAYER_RESULT.equals(event.type())) continue;
            Text line = format(event, match, world, cache);
            if (line == null) continue;
            out.add(Text.literal("[" + formatTime(event.worldTick(), match.startTick()) + "] ")
                    .formatted(Formatting.GRAY).append(line));
        }

        out.add(Text.translatable("btt.replay.finish").formatted(Formatting.GRAY));
        out.add(separator());
        return out;
    }

    private static Text premise(MatchRecord match) {
        var data = firstData(match, GameRecordTypes.MATCH_START);
        String mode = match.gameMode().getPath();
        if (mode.equals("before_the_terminal")) mode = "script";
        String background = data == null ? "" : data.getString("background");
        return Text.translatable("btt.replay.premise", Text.translatable("btt.mode." + mode),
                Text.translatable(background.isEmpty() ? "btt.admin.none" : "btt.background." + background)).formatted(Formatting.GRAY);
    }



    @Nullable
    private static Text format(GameRecordEvent event, MatchRecord match,
                               ServerWorld world, Map<UUID, PlayerInfo> cache) {
        ReplayEventFormatter custom = ReplayRegistry.eventKey(event.type());
        if (custom != null) return custom.format(event, match, world);

        NbtCompound d = event.data();
        UUID actor = d.containsUuid("actor") ? d.getUuid("actor") : null;
        UUID target = d.containsUuid("target") ? d.getUuid("target") : null;
        return switch (event.type()) {
            case GameRecordTypes.MATCH_START, GameRecordTypes.ROLE_ASSIGNED -> null;
            case GameRecordTypes.PLAYER_JOIN ->
                    Text.translatable("btt.replay.join", name(actor, cache)).formatted(Formatting.GRAY);
            case GameRecordTypes.PLAYER_LEAVE ->
                    Text.translatable("btt.replay.leave", name(actor, cache)).formatted(Formatting.GRAY);
            case GameRecordTypes.DEATH -> {
                Text victim = name(target, cache);

                Text reason = Text.translatable("death_reason." + shortId(d.getString("death_reason")))
                        .withColor(COLOR_DEATH_REASON);
                yield actor == null || actor.equals(target) || dev.hae.btt.combat.DeathReasons.isSuicide(Identifier.tryParse(d.getString("death_reason")))
                        ? Text.translatable("btt.replay.death.natural", victim, reason).formatted(Formatting.GRAY)
                        : Text.translatable("btt.replay.death", name(actor, cache), victim, reason)
                        .formatted(Formatting.GRAY);
            }

            case GameRecordTypes.TASK_COMPLETE -> null;
            case GameRecordTypes.SHOP_PURCHASE ->
                    Text.translatable("btt.replay.shop", name(actor, cache),
                            itemName(d, world)).formatted(Formatting.GRAY);

            case GameRecordTypes.SKILL_USE -> {
                boolean drunk = d.getBoolean("drunk");
                Text skill = namedSkill(d.getString("skill"));
                yield target == null
                        ? Text.translatable(drunk ? "btt.replay.skill.drunk" : "btt.replay.skill",
                        name(actor, cache), skill).formatted(Formatting.GRAY)
                        : Text.translatable(drunk ? "btt.replay.skill.target.drunk" : "btt.replay.skill.target",
                        name(actor, cache), name(target, cache),
                        skill).formatted(Formatting.GRAY);
            }

            case GameRecordTypes.ROLE_CHANGE -> roleChange(d, cache, actor);
            case GameRecordTypes.CAMP_CHANGE -> campChange(d, cache, actor, target);
            case GameRecordTypes.ITEM_USE ->
                    Text.translatable("btt.replay.item", name(actor, cache),
                            Text.literal("[" + shortId(d.getString("item")) + "]").withColor(COLOR_ITEM))
                            .formatted(Formatting.GRAY);

            case GameRecordTypes.ITEM_PICKUP ->
                    Text.translatable("btt.replay.pickup", name(actor, cache), itemName(d, world))
                            .formatted(Formatting.GRAY);

            case GameRecordTypes.ITEM_DROP ->
                    Text.translatable("btt.replay.drop", name(actor, cache), itemName(d, world))
                            .formatted(Formatting.GRAY);

            case GameRecordTypes.EPILOGUE_START -> epilogueLine(d.getString("epilogue"));

            case GameRecordTypes.TALENT_USE ->
                    Text.translatable(d.getBoolean("drunk") ? "btt.replay.talent.drunk" : "btt.replay.talent",
                                    name(actor, cache),
                                    Text.translatable("btt.talent." + d.getString("talent")))
                            .formatted(Formatting.GRAY);
            case GameRecordTypes.GLOBAL_EVENT ->
                    Text.translatable("btt.replay.global", shortId(d.getString("event")))
                            .formatted(Formatting.GRAY);
            case GameRecordTypes.MATCH_END ->
                    Text.translatable("btt.replay.end", endingName(d.getString("btt_ending")),
                                    d.getString("win_status"))
                            .withColor(dev.hae.btt.victory.Endings.colorFor(d.getString("btt_ending")));
            case GameRecordTypes.PLAYER_RESULT -> {
                UUID who = d.containsUuid("player") ? d.getUuid("player") : null;
                boolean win = d.getBoolean("is_winner");
                yield Text.translatable(win ? "btt.replay.result.win" : "btt.replay.result.lose",
                                name(who, cache), d.getString("end_status"))
                        .formatted(win ? Formatting.GREEN : Formatting.DARK_GRAY);
            }
            default -> Text.literal(event.type()).formatted(Formatting.DARK_GRAY);
        };
    }



    public static String formatTime(long tick, long startTick) {
        long totalSeconds = Math.max(0, (tick - startTick) / 20);
        return String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60);
    }


    public static Text formatPlayerName(UUID uuid, Map<UUID, PlayerInfo> cache) {
        PlayerInfo info = cache.get(uuid);
        if (info == null) return Text.literal(uuid == null ? "?" : uuid.toString().substring(0, 8));
        MutableText text = Text.literal((info.name() == null ? "?" : info.name()) + "(")
                .append(Text.translatable(info.roleTranslationKey()))
                .append(Text.literal(")"));
        return text.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(info.roleColor())));
    }

    private static Text name(UUID uuid, Map<UUID, PlayerInfo> cache) {
        return formatPlayerName(uuid, cache);
    }


    private static Text formatPlayerNameAs(UUID uuid, String roleId, Map<UUID, PlayerInfo> cache) {
        return formatPlayerNameAs(uuid, roleId, cache, false);
    }

    private static Text formatPlayerNameAs(UUID uuid, String roleId, Map<UUID, PlayerInfo> cache, boolean numbered) {
        Identifier id = Identifier.tryParse(roleId == null ? "" : roleId);
        if (uuid == null || id == null) return null;
        PlayerInfo info = cache.get(uuid);
        String nameStr = info == null || info.name() == null ? uuid.toString().substring(0, 8) : info.name();
        if (numbered && info != null && info.number() > 0) nameStr = Integer.toString(info.number());
        Role role = dev.hae.btt.role.Roles.byId(roleId);
        MutableText text = Text.literal(nameStr + "(")
                .append(Text.translatable("announcement.role." + id.getNamespace() + "." + id.getPath()))
                .append(Text.literal(")"));
        return text.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(role == null ? 0xFFFFFF : role.color())));
    }


    private static Text roleText(String roleId) {
        Identifier id = Identifier.tryParse(roleId == null ? "" : roleId);
        if (id == null) return null;
        Role role = dev.hae.btt.role.Roles.byId(roleId);
        return Text.translatable("announcement.role." + id.getNamespace() + "." + id.getPath())
                .withColor(role == null ? 0xFFFFFF : role.color());
    }









    private static Text roleChange(NbtCompound d, Map<UUID, PlayerInfo> cache, UUID actor) {
        String from = d.getString("from");
        String to = d.getString("to");
        if (d.getBoolean("decoy")) {
            Text who = formatPlayerNameAs(actor, from, cache, true);
            Text truth = roleText(to);
            if (who == null || truth == null) return null;
            return Text.translatable("btt.replay.decoy", who, truth).formatted(Formatting.GRAY);
        }


        if (d.getBoolean("technical")) {

            Text who = formatPlayerNameAs(actor, from, cache);
            Text ability = roleText(to);
            if (who != null && actor != null) updateCacheRole(cache, actor, to);
            if (who == null || ability == null) return null;
            return Text.translatable("btt.replay.technical_role", who, ability).formatted(Formatting.GRAY);
        }
        Text who = formatPlayerNameAs(actor, from, cache);
        Text truth = roleText(to);
        if (who != null && actor != null) updateCacheRole(cache, actor, to);
        if (who == null || truth == null) return null;
        return Text.translatable("btt.replay.role", who, truth).formatted(Formatting.GRAY);
    }


    private static void updateCacheRole(Map<UUID, PlayerInfo> cache, UUID actor, String roleId) {
        PlayerInfo info = cache.get(actor);
        Identifier id = Identifier.tryParse(roleId == null ? "" : roleId);
        if (info == null || id == null) return;
        Role role = dev.hae.btt.role.Roles.byId(roleId);
        cache.put(actor, new PlayerInfo(info.name(),
                "announcement.role." + id.getNamespace() + "." + id.getPath(),
                role == null ? 0xFFFFFF : role.color(), roleId, info.number()));
    }


    private static Text campChange(NbtCompound d, Map<UUID, PlayerInfo> cache, UUID actor, UUID source) {
        Text camp = campText(d.getString("camp"));
        if (camp == null || actor == null) return null;
        if (source != null) {
            return Text.translatable("btt.replay.camp.join", name(actor, cache), name(source, cache), camp)
                    .formatted(Formatting.GRAY);
        }
        return Text.translatable("btt.replay.camp.change", name(actor, cache), camp).formatted(Formatting.GRAY);
    }


    private static Text campText(String camp) {
        if (camp == null) return null;
        return switch (camp) {
            case dev.hae.btt.role.Roles.CAMP_KILLER ->
                    Text.translatable("btt.camp.killer").withColor(0xDD0000);
            case dev.hae.btt.role.Roles.CAMP_PASSENGER ->
                    Text.translatable("btt.camp.passenger").withColor(0x4EDD35);
            case dev.hae.btt.role.Roles.CAMP_SOLO ->
                    Text.translatable("btt.camp.lone").withColor(0x00FFFF);
            case "MAJO" -> Text.translatable("btt.campgroup.majo").withColor(dev.hae.btt.role.Roles.MAJO.color());
            case "MESSIAH" -> Text.translatable("btt.campgroup.messiah").withColor(dev.hae.btt.role.Roles.MESSIAH.color());
            case "KIDNAPPER" ->
                    Text.translatable("btt.campgroup.kidnapper").withColor(dev.hae.btt.role.Roles.KIDNAPPER.color());
            case "GARDENER" ->
                    Text.translatable("btt.campgroup.gardener").withColor(dev.hae.btt.role.Roles.GARDENER.color());
            case "ASTRAL" -> Text.translatable("btt.campgroup.astral").withColor(dev.hae.btt.role.Roles.ASTRAL.color());
            case "COLD_WAVE" ->
                    Text.translatable("btt.campgroup.cold_wave").withColor(dev.hae.btt.role.Roles.COLD_WAVE.color());
            default -> {
                var host = dev.hae.btt.role.RoleDefs.host(camp);
                yield host == null ? null : dev.hae.btt.faction.Camps.name(host.role)
                        .withColor(host.role.color());
            }
        };
    }


    private static String shortId(String id) {
        if (id == null || id.isEmpty()) return "unknown";
        Identifier i = Identifier.tryParse(id);
        return i == null ? id : i.getNamespace() + "." + i.getPath();
    }





    private static Text namedSkill(String id) {
        Identifier i = Identifier.tryParse(id == null ? "" : id);
        if (i == null) return Text.literal("unknown");
        return Text.translatable("btt.skill." + i.getPath());
    }

    private static Text itemName(NbtCompound data, ServerWorld world) {
        String json = data.getString("item_name");
        if (json != null && !json.isEmpty()) {
            try {
                Text t = Text.Serialization.fromJson(json, world.getRegistryManager());
                if (t != null) {

                    return Text.literal("[").withColor(COLOR_ITEM).append(t).append(Text.literal("]"));
                }
            } catch (Exception ignored) {

            }
        }
        return Text.literal("[" + shortId(data.getString("item")) + "]").withColor(COLOR_ITEM);
    }




    private static final int COLOR_DEATH_REASON = 0xFFFF5555;

    private static final int COLOR_ITEM = 0xFFFFAA00;

    private static final int ROSTER_PER_ROW = 6;


    private static Text separator() {
        return Text.literal("========================================").formatted(Formatting.GRAY);
    }






    private static List<Text> rosterLines(Map<UUID, PlayerInfo> cache) {
        List<Map.Entry<UUID, PlayerInfo>> rows = new ArrayList<>(cache.entrySet());
        rows.sort(Comparator.comparingInt((Map.Entry<UUID, PlayerInfo> e) -> playerNumber(e.getValue()))
                .thenComparing(e -> e.getValue().name() == null ? "" : e.getValue().name()));
        List<Text> out = new ArrayList<>();
        MutableText current = null;
        int inRow = 0;
        for (Map.Entry<UUID, PlayerInfo> e : rows) {
            MutableText cell = Text.literal(playerNumber(e.getValue()) + ".").formatted(Formatting.GRAY)
                    .append(formatPlayerName(e.getKey(), cache));
            if (current == null) {
                current = cell;
                inRow = 1;
            } else if (inRow < ROSTER_PER_ROW) {
                current.append(Text.literal("  ")).append(cell);
                inRow++;
            } else {
                out.add(current);
                current = cell;
                inRow = 1;
            }
        }
        if (current != null) out.add(current);
        return out;
    }


    private static int playerNumber(PlayerInfo info) {
        return info == null || info.number() <= 0 ? 999 : info.number();
    }


    private static String mapLabel(MatchRecord match) {
        String name = dev.hae.btt.map.BttMaps.displayNameOfDimension(match.dimension());
        return name == null || name.isEmpty() ? match.dimension().getPath() : name;
    }


    private static Text endingName(String lastEnding) {
        String key = dev.hae.btt.victory.Endings.keyOf(lastEnding);
        return key == null ? Text.literal(String.valueOf(lastEnding))
                : Text.translatable("btt.ending." + key);
    }


    private static Text epilogueLine(String type) {
        String key = switch (type == null ? "SURVIVAL" : type) {
            case "MAJO" -> "majo";
            case "MESSIAH" -> "messiah";
            case "KIDNAPPER" -> "kidnapper";
            case "GARDENER" -> "gardener";
            case "ASTRAL" -> "astral";
            case "COLD_WAVE" -> "cold_wave";
            default -> "survival";
        };
        int color = switch (key) {
            case "majo" -> dev.hae.btt.role.Roles.MAJO.color();
            case "messiah" -> dev.hae.btt.role.Roles.MESSIAH.color();
            case "kidnapper" -> dev.hae.btt.role.Roles.KIDNAPPER.color();
            case "gardener" -> dev.hae.btt.role.Roles.GARDENER.color();
            case "astral" -> dev.hae.btt.role.Roles.ASTRAL.color();
            case "cold_wave" -> dev.hae.btt.role.Roles.COLD_WAVE.color();
            default -> dev.hae.btt.game.BeforeTheTerminalGameMode.FACTION_PASSENGER_COLOR;
        };
        return Text.translatable("btt.replay.epilogue",
                Text.translatable("btt.epilogue." + key + ".title").withColor(color));
    }

    @Nullable
    private static NbtCompound firstData(MatchRecord match, String type) {
        for (GameRecordEvent e : match.events()) {
            if (type.equals(e.type())) return e.data();
        }
        return null;
    }
}
