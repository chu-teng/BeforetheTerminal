package dev.hae.btt.map;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.hae.btt.BeforeTheTerminal;
import dev.hae.btt.game.BeforeTheTerminalGameMode;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;







































public final class BttMaps {
    private BttMaps() {}

    private static final String PATH = "maps";

    private static final String SPARK_NAMESPACE = "wathe";

    private static final String SPARK_LEGACY_PATH = "areas";

    private static final String LEGACY_ID = "legacy_overworld";
    private static final Map<String, BttMapEntry> MAPS = new LinkedHashMap<>();


    public static void register() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new Reloader());
    }


    public static List<BttMapEntry> all() {
        return new ArrayList<>(MAPS.values());
    }


    public static BttMapEntry current(net.minecraft.server.world.ServerWorld world) {
        String dimension = world.getRegistryKey().getValue().toString();
        var state = dev.hae.btt.component.MapVoteState.KEY.get(world);
        BttMapEntry selected = get(state.roomsMapId);
        if (selected != null && dimension.equals(selected.dimension())) return selected;
        selected = get(dev.hae.btt.component.MapVoteState.KEY.get(world.getServer().getOverworld()).lastSelected);
        if (selected != null && dimension.equals(selected.dimension())) return selected;
        List<BttMapEntry> matches = all().stream().filter(e -> dimension.equals(e.dimension())).toList();
        return matches.size() == 1 ? matches.getFirst() : null;
    }

    public static int count() {
        return MAPS.size();
    }

    public static BttMapEntry get(String id) {
        return MAPS.get(id);
    }





    public static String displayNameOfDimension(net.minecraft.util.Identifier dimension) {
        if (dimension == null) return null;
        for (BttMapEntry e : MAPS.values()) {
            if (dimension.equals(e.dimension())) return e.displayName();
        }
        return null;
    }






    public static boolean playableRange(BttMapEntry e) {
        return e.maxPlayers() >= dev.hae.btt.game.SeatAssignment.MIN_PLAYERS
                && e.minPlayers() <= dev.hae.btt.game.SeatAssignment.MAX_PLAYERS;
    }


    public static List<BttMapEntry> playable() {
        List<BttMapEntry> out = new ArrayList<>();
        for (BttMapEntry e : MAPS.values()) {
            if (playableRange(e)) out.add(e);
        }
        return out;
    }


    private static final Map<String, String> SOURCE_PACK = new LinkedHashMap<>();





    private static int packScore(String packId) {
        if (packId == null) return 0;
        if (packId.startsWith("file/")) return 2;
        return 1;
    }


    public static List<BttMapEntry> eligible(int playerCount) {
        List<BttMapEntry> out = new ArrayList<>();
        for (BttMapEntry e : MAPS.values()) {
            if (playableRange(e) && e.isEligible(playerCount)) out.add(e);
        }
        return out;
    }


    public static List<String> unavailableReasons(int playerCount) {
        List<String> out = new ArrayList<>();
        for (BttMapEntry e : MAPS.values()) {
            if (!playableRange(e)) {
                out.add(e.id() + " (人数区间 " + e.minPlayers() + "~" + e.maxPlayers() + " 与 BTT 6~22 无交集)");
                continue;
            }
            if (e.isEligible(playerCount)) continue;
            out.add(playerCount < e.minPlayers()
                    ? e.id() + " (min " + e.minPlayers() + ")"
                    : e.id() + " (max " + e.maxPlayers() + ")");
        }
        return out;
    }





    private static final class Reloader implements SimpleSynchronousResourceReloadListener {
        @Override
        public Identifier getFabricId() {
            return Identifier.of(BeforeTheTerminal.MOD_ID, "btt_maps");
        }

        @Override
        public void reload(ResourceManager manager) {
            MAPS.clear();
            SOURCE_PACK.clear();


            int nativeMaps = 0;
            int sparkMaps = loadSpark(manager);
            boolean legacy = false;


            if (sparkMaps == 0) legacy = loadSparkLegacy(manager);
            BeforeTheTerminalGameMode.LOGGER.info(
                    "[BTT] map registry loaded: {} map(s) [btt/maps={}, wathe/maps={}, wathe/areas={}]{}",
                    MAPS.size(), nativeMaps, sparkMaps, legacy ? 1 : 0,
                    MAPS.isEmpty() ? " (voting falls back to the current map)" : "");
        }


        private int loadSpark(ResourceManager manager) {
            int added = 0;
            Map<Identifier, Resource> found = manager.findResources(PATH,
                    id -> id.getNamespace().equals(SPARK_NAMESPACE) && id.getPath().endsWith(".json"));
            for (Identifier key : sortedKeys(found)) {
                try (InputStreamReader r = new InputStreamReader(found.get(key).getInputStream(), StandardCharsets.UTF_8)) {
                    JsonObject o = JsonParser.parseReader(r).getAsJsonObject();


                    if (!o.has("dimension")) {
                        BeforeTheTerminalGameMode.LOGGER.warn(
                                "[BTT] skipped spark map {}: no \"dimension\" field (旧版 areas 内容请放 data/wathe/areas/)", key);
                        continue;
                    }
                    String id = entryId(key);



                    if (o.has("btt_exclude") && o.get("btt_exclude").getAsBoolean()) {
                        BeforeTheTerminalGameMode.LOGGER.info(
                                "[BTT] map '{}' excluded from the vote (btt_exclude)", id);
                        continue;
                    }
                    putEntry(id, BttMapEntry.fromSparkEntry(id, o), found.get(key).getPackId(), key);
                    added++;
                } catch (Exception e) {
                    BeforeTheTerminalGameMode.LOGGER.error("[BTT] failed to load spark map config {}", key, e);
                }
            }
            return added;
        }


        private boolean loadSparkLegacy(ResourceManager manager) {
            Map<Identifier, Resource> found = manager.findResources(SPARK_LEGACY_PATH,
                    id -> id.getNamespace().equals(SPARK_NAMESPACE) && id.getPath().endsWith(".json"));
            for (Identifier key : sortedKeys(found)) {
                try (InputStreamReader r = new InputStreamReader(found.get(key).getInputStream(), StandardCharsets.UTF_8)) {
                    JsonObject o = JsonParser.parseReader(r).getAsJsonObject();
                    putEntry(LEGACY_ID, BttMapEntry.fromSparkLegacyAreas(LEGACY_ID, o),
                            found.get(key).getPackId(), key);
                    BeforeTheTerminalGameMode.LOGGER.info(
                            "[BTT] spark legacy areas config loaded from {} as overworld map '{}'", key, LEGACY_ID);
                    return true;
                } catch (Exception e) {
                    BeforeTheTerminalGameMode.LOGGER.error("[BTT] failed to load spark legacy areas {}", key, e);
                }
            }
            return false;
        }


        private static String entryId(Identifier key) {
            String file = key.getPath();
            return file.substring(PATH.length() + 1, file.length() - ".json".length());
        }


        private static void putEntry(String id, BttMapEntry entry, String packId, Identifier key) {
            String prevPack = SOURCE_PACK.get(id);
            if (MAPS.containsKey(id) && packScore(packId) <= packScore(prevPack)) {
                BeforeTheTerminalGameMode.LOGGER.info(
                        "[BTT] map id '{}' 重复：保留优先级更高的 {}（忽略 {}）", id, prevPack, key);
                return;
            }
            if (MAPS.containsKey(id)) {
                BeforeTheTerminalGameMode.LOGGER.info(
                        "[BTT] map id '{}' 被更高优先级资源包覆盖：{} → {}", id, prevPack, packId);
            }
            MAPS.put(id, entry);
            SOURCE_PACK.put(id, packId);
        }

        private static List<Identifier> sortedKeys(Map<Identifier, Resource> found) {
            List<Identifier> keys = new ArrayList<>(found.keySet());
            Collections.sort(keys);
            return keys;
        }
    }
}
