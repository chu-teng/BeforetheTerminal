package dev.hae.btt.map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.List;
import java.util.Optional;













public record BttMapEntry(
        String id,
        String dimension,
        String displayName,
        String description,
        int minPlayers,
        int maxPlayers,
        String mapEffect,
        Optional<Vec3d> spawnPos,
        Optional<Float> spawnYaw,
        Optional<Float> spawnPitch,
        Optional<Vec3d> spectatorSpawnPos,
        Optional<Box> readyArea,
        Optional<Vec3i> playAreaOffset,
        Optional<Box> playArea,
        Optional<Box> resetTemplateArea,
        Optional<Vec3i> resetPasteOffset,

        List<BttRoom> rooms,

        MapEnhancements enhancements
) {

    public boolean isEligible(int playerCount) {
        return playerCount >= minPlayers && playerCount <= maxPlayers;
    }






    public static BttMapEntry fromJson(String id, JsonObject o) {
        String dimension = str(o, "dimension", "minecraft:overworld");
        String display = str(o, "display_name", id);
        String desc = str(o, "description", "");
        int min = num(o, "min_players", 0);
        int max = num(o, "max_players", 100);
        String effect = str(o, "map_effect", "");
        JsonObject v = o.has("variables") && o.get("variables").isJsonObject()
                ? o.getAsJsonObject("variables") : new JsonObject();

        return new BttMapEntry(
                id, dimension, display, desc, min, max, effect,
                vec3d(v, "spawn_position"),
                fnum(v, "spawn_position", "yaw"),
                fnum(v, "spawn_position", "pitch"),
                vec3d(v, "spectator_spawn_position"),
                box(v, "ready_area"),
                vec3i(v, "play_area_offset"),
                box(v, "play_area"),
                box(v, "reset_template_area"),
                vec3i(v, "reset_paste_offset"),
                BttRoom.parse(o),

                MapEnhancements.fromJson(obj(o, "enhancements").orElse(o)));
    }




















    public static BttMapEntry fromSparkEntry(String id, JsonObject o) {
        JsonObject enhancements = obj(o, "enhancements").orElseGet(JsonObject::new);
        JsonObject vars = obj(o, "variables").orElseGet(JsonObject::new);
        List<BttRoom> rooms = BttRoom.parse(o);
        if (rooms.isEmpty()) rooms = BttRoom.parse(enhancements);
        return new BttMapEntry(id,
                str(o, "dimension", "minecraft:overworld"),
                str(o, "display_name", id),
                str(o, "description", ""),
                num(o, "min_players", 0),
                num(o, "max_players", 100),
                str(o, "map_effect", ""),
                vec3d(vars, "spawn_position"),
                fnum(vars, "spawn_position", "yaw"),
                fnum(vars, "spawn_position", "pitch"),
                vec3d(vars, "spectator_spawn_position"),
                box(vars, "ready_area"),
                vec3i(vars, "play_area_offset"),
                box(vars, "play_area"),
                box(vars, "reset_template_area"),
                vec3i(vars, "reset_paste_offset"),
                rooms,
                MapEnhancements.fromJson(enhancements));
    }





    public static BttMapEntry fromSparkLegacyAreas(String id, JsonObject o) {
        return new BttMapEntry(id, "minecraft:overworld", "Overworld", "",
                0, 100, "",
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                BttRoom.parse(o),
                MapEnhancements.fromJson(o));
    }



    private static String str(JsonObject o, String key, String def) {
        JsonElement e = o.get(key);
        return e != null && e.isJsonPrimitive() ? e.getAsString() : def;
    }

    private static int num(JsonObject o, String key, int def) {
        try {
            JsonElement e = o.get(key);
            return e != null && e.isJsonPrimitive() ? e.getAsInt() : def;
        } catch (Exception ex) {
            return def;
        }
    }

    private static Optional<JsonObject> obj(JsonObject o, String key) {
        JsonElement e = o.get(key);
        return e != null && e.isJsonObject() ? Optional.of(e.getAsJsonObject()) : Optional.empty();
    }

    private static Optional<Vec3d> vec3d(JsonObject parent, String key) {
        return obj(parent, key).map(b -> new Vec3d(d(b, "x", 0), d(b, "y", 0), d(b, "z", 0)));
    }

    private static Optional<Vec3i> vec3i(JsonObject parent, String key) {
        return obj(parent, key).map(b -> new Vec3i((int) d(b, "x", 0), (int) d(b, "y", 0), (int) d(b, "z", 0)));
    }

    private static Optional<Float> fnum(JsonObject parent, String key, String field) {
        return obj(parent, key).filter(b -> b.has(field)).map(b -> (float) b.get(field).getAsDouble());
    }

    private static Optional<Box> box(JsonObject parent, String key) {
        return obj(parent, key).map(b -> new Box(
                d(b, "min_x", 0), d(b, "min_y", 0), d(b, "min_z", 0),
                d(b, "max_x", 0), d(b, "max_y", 0), d(b, "max_z", 0)));
    }

    private static double d(JsonObject o, String key, double def) {
        try {
            JsonElement e = o.get(key);
            return e != null && e.isJsonPrimitive() ? e.getAsDouble() : def;
        } catch (Exception ex) {
            return def;
        }
    }
}
