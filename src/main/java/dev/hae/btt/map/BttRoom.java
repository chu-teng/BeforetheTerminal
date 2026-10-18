package dev.hae.btt.map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.ArrayList;
import java.util.List;















public record BttRoom(String name, int maxPlayers, List<Spawn> spawnPoints) {


    public int capacity() {
        return maxPlayers > 0 ? maxPlayers : Math.max(1, spawnPoints.size());
    }


    public record Spawn(double x, double y, double z, float yaw, float pitch) {
        public NbtCompound toNbt() {
            NbtCompound c = new NbtCompound();
            c.putDouble("x", x);
            c.putDouble("y", y);
            c.putDouble("z", z);
            c.putFloat("yaw", yaw);
            c.putFloat("pitch", pitch);
            return c;
        }

        public static Spawn fromNbt(NbtCompound c) {
            return new Spawn(c.getDouble("x"), c.getDouble("y"), c.getDouble("z"),
                    c.getFloat("yaw"), c.getFloat("pitch"));
        }

        public String toJson() {
            return "{\"x\": " + round(x) + ", \"y\": " + round(y) + ", \"z\": " + round(z)
                    + ", \"yaw\": " + round(yaw) + ", \"pitch\": " + round(pitch) + "}";
        }

        private static String round(double v) {
            return Double.toString(Math.round(v * 100.0) / 100.0);
        }
    }


    public static List<BttRoom> parse(JsonObject mapJson) {
        List<BttRoom> out = new ArrayList<>();
        if (mapJson == null) return out;
        JsonElement e = mapJson.get("rooms");
        if (e == null || !e.isJsonArray()) return out;
        int n = 0;
        for (JsonElement roomEl : e.getAsJsonArray()) {
            n++;
            if (!roomEl.isJsonObject()) continue;
            JsonObject roomObj = roomEl.getAsJsonObject();
            String name = roomObj.has("name") && roomObj.get("name").isJsonPrimitive()
                    ? roomObj.get("name").getAsString() : ("Room " + n);
            int maxPlayers = roomObj.has("max_players") && roomObj.get("max_players").isJsonPrimitive()
                    ? roomObj.get("max_players").getAsInt() : 0;
            List<Spawn> spawns = new ArrayList<>();
            JsonElement sp = roomObj.get("spawn_points");
            if (sp != null && sp.isJsonArray()) {
                JsonArray arr = sp.getAsJsonArray();
                for (JsonElement s : arr) {
                    if (!s.isJsonObject()) continue;
                    JsonObject o = s.getAsJsonObject();
                    spawns.add(new Spawn(num(o, "x"), num(o, "y"), num(o, "z"),
                            (float) num(o, "yaw"), (float) num(o, "pitch")));
                }
            }
            out.add(new BttRoom(name, maxPlayers, spawns));
        }
        return out;
    }

    private static double num(JsonObject o, String key) {
        try {
            JsonElement e = o.get(key);
            return e != null && e.isJsonPrimitive() ? e.getAsDouble() : 0.0;
        } catch (Exception ex) {
            return 0.0;
        }
    }



    public static NbtList listToNbt(List<BttRoom> rooms) {
        NbtList list = new NbtList();
        for (BttRoom room : rooms) {
            NbtCompound c = new NbtCompound();
            c.putString("Name", room.name());
            c.putInt("MaxPlayers", room.maxPlayers());
            NbtList spawns = new NbtList();
            for (Spawn s : room.spawnPoints()) spawns.add(s.toNbt());
            c.put("SpawnPoints", spawns);
            list.add(c);
        }
        return list;
    }

    public static List<BttRoom> listFromNbt(NbtCompound tag) {
        List<BttRoom> out = new ArrayList<>();
        if (tag == null || !tag.contains("Rooms", NbtElement.LIST_TYPE)) return out;
        for (NbtElement e : tag.getList("Rooms", NbtElement.COMPOUND_TYPE)) {
            NbtCompound c = (NbtCompound) e;
            List<Spawn> spawns = new ArrayList<>();
            if (c.contains("SpawnPoints", NbtElement.LIST_TYPE)) {
                for (NbtElement s : c.getList("SpawnPoints", NbtElement.COMPOUND_TYPE)) {
                    spawns.add(Spawn.fromNbt((NbtCompound) s));
                }
            }
            out.add(new BttRoom(c.getString("Name"), c.getInt("MaxPlayers"), spawns));
        }
        return out;
    }


    public static String listToJson(List<BttRoom> rooms) {
        StringBuilder sb = new StringBuilder("  \"rooms\": [\n");
        for (int r = 0; r < rooms.size(); r++) {
            BttRoom room = rooms.get(r);
            sb.append("    { \"name\": \"").append(room.name()).append("\"");
            if (room.maxPlayers() > 0) sb.append(", \"max_players\": ").append(room.maxPlayers());
            sb.append(", \"spawn_points\": [");
            for (int i = 0; i < room.spawnPoints().size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(room.spawnPoints().get(i).toJson());
            }
            sb.append("] }").append(r + 1 < rooms.size() ? "," : "").append('\n');
        }
        return sb.append("  ]").toString();
    }
}
