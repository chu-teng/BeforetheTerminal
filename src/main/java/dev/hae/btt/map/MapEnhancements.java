package dev.hae.btt.map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.hae.btt.component.MapVoteState;
import net.minecraft.block.BlockState;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;



















public record MapEnhancements(
        List<String> blacklistBlocks,
        List<String> blacklistTags,
        float walkSpeedMultiplier,
        float sprintSpeedMultiplier,
        float gravityMultiplier,
        Visual visual,
        Jump jump,
        Fog fog,

        java.util.Set<String> unconsumed,

        java.util.Set<String> baselineLocked) {


    public static final MapEnhancements NONE = new MapEnhancements(List.of(), List.of(), 1f, 1f, 1f, Visual.EMPTY,
            Jump.EMPTY, Fog.EMPTY, java.util.Set.of(), java.util.Set.of());


    private static final java.util.Set<String> CONSUMED = java.util.Set.of(
            "rooms", "interaction_blacklist", "movement", "gravity", "visual", "jump", "fog");

















    private static final java.util.Set<String> BASELINE_LOCKED = java.util.Set.of(
            "scenery", "visibility", "camera_shake", "snow_particles", "ambience");










    private static final java.util.Set<String> KNOWN_INAPPLICABLE = java.util.Set.of("special_roles");








    public record Jump(boolean allowed, float staminaCost) {

        public static final Jump EMPTY = new Jump(false, 0f);

        public boolean isDefault() {
            return !allowed && staminaCost == 0f;
        }

        public NbtCompound toNbt() {
            NbtCompound c = new NbtCompound();
            c.putBoolean("Allowed", allowed);
            c.putFloat("StaminaCost", staminaCost);
            return c;
        }

        public static Jump fromNbt(NbtCompound c) {
            if (c == null || c.getKeys().isEmpty()) return EMPTY;
            return new Jump(c.contains("Allowed") && c.getBoolean("Allowed"),
                    c.contains("StaminaCost") ? c.getFloat("StaminaCost") : 0f);
        }
    }









    public record Fog(Float start, Float endMoving, Float endStationary, Integer nightColor) {
        public static final Fog EMPTY = new Fog(null, null, null, null);

        public boolean isEmpty() {
            return start == null && endMoving == null && endStationary == null && nightColor == null;
        }


        public boolean hasDistances() {
            return start != null || endMoving != null || endStationary != null;
        }

        public NbtCompound toNbt() {
            NbtCompound c = new NbtCompound();
            if (start != null) c.putFloat("Start", start);
            if (endMoving != null) c.putFloat("EndMoving", endMoving);
            if (endStationary != null) c.putFloat("EndStationary", endStationary);
            if (nightColor != null) c.putInt("NightColor", nightColor);
            return c;
        }

        public static Fog fromNbt(NbtCompound c) {
            if (c == null || c.getKeys().isEmpty()) return EMPTY;
            return new Fog(
                    c.contains("Start") ? c.getFloat("Start") : null,
                    c.contains("EndMoving") ? c.getFloat("EndMoving") : null,
                    c.contains("EndStationary") ? c.getFloat("EndStationary") : null,
                    c.contains("NightColor") ? c.getInt("NightColor") : null);
        }
    }









    public record Visual(Boolean hud, Boolean snow, Boolean fog, Integer trainSpeed, String timeOfDay, Boolean staticMap) {
        public static final Visual EMPTY = new Visual(null, null, null, null, null, null);

        public boolean isEmpty() {
            return hud == null && snow == null && fog == null && trainSpeed == null && timeOfDay == null && staticMap == null;
        }

        public NbtCompound toNbt() {
            NbtCompound c = new NbtCompound();
            if (hud != null) c.putBoolean("Hud", hud);
            if (snow != null) c.putBoolean("Snow", snow);
            if (fog != null) c.putBoolean("Fog", fog);
            if (trainSpeed != null) c.putInt("TrainSpeed", trainSpeed);
            if (timeOfDay != null) c.putString("TimeOfDay", timeOfDay);
            if (staticMap != null) c.putBoolean("StaticMap", staticMap);
            return c;
        }

        public static Visual fromNbt(NbtCompound c) {
            if (c == null || c.getKeys().isEmpty()) return EMPTY;
            return new Visual(
                    c.contains("Hud") ? c.getBoolean("Hud") : null,
                    c.contains("Snow") ? c.getBoolean("Snow") : null,
                    c.contains("Fog") ? c.getBoolean("Fog") : null,
                    c.contains("TrainSpeed") ? c.getInt("TrainSpeed") : null,
                    c.contains("TimeOfDay") ? c.getString("TimeOfDay") : null,
                    c.contains("StaticMap") ? c.getBoolean("StaticMap") : null);
        }
    }


    private static final Identifier GRAVITY_MODIFIER_ID = Identifier.of("btt", "map_gravity");

    public boolean isDefault() {
        return blacklistBlocks.isEmpty() && blacklistTags.isEmpty()
                && walkSpeedMultiplier == 1f && sprintSpeedMultiplier == 1f && gravityMultiplier == 1f
                && (visual == null || visual.isEmpty()) && (jump == null || jump.isDefault())
                && (fog == null || fog.isEmpty());
    }




    public static MapEnhancements activeFor(World world) {
        if (world == null) return NONE;
        MapEnhancements own = MapVoteState.KEY.get(world).enhancements;
        if (own != null && !own.isDefault()) return own;
        return NONE;
    }




    public static boolean blocksInteraction(World world, BlockPos pos) {
        MapEnhancements e = activeFor(world);
        if (e.blacklistBlocks.isEmpty() && e.blacklistTags.isEmpty()) return false;
        BlockState state = world.getBlockState(pos);
        if (e.blacklistBlocks.contains(Registries.BLOCK.getId(state.getBlock()).toString())) return true;
        for (String tag : e.blacklistTags) {
            Identifier id = Identifier.tryParse(tag);
            if (id != null && state.isIn(TagKey.of(RegistryKeys.BLOCK, id))) return true;
        }
        return false;
    }




    public static void tickGravity(PlayerEntity player, MapEnhancements enhancement) {
        EntityAttributeInstance attr = player.getAttributeInstance(EntityAttributes.GENERIC_GRAVITY);
        if (attr == null) return;
        float target = (enhancement == null || enhancement.isDefault()) ? 1f : enhancement.gravityMultiplier();
        boolean present = false;
        for (EntityAttributeModifier m : attr.getModifiers()) {
            if (m.id().equals(GRAVITY_MODIFIER_ID)) {
                present = true;
                if (Math.abs(m.value() - (target - 1f)) < 1.0E-6) return;
                break;
            }
        }
        if (present) attr.removeModifier(GRAVITY_MODIFIER_ID);
        if (target != 1f) {
            attr.addTemporaryModifier(new EntityAttributeModifier(
                    GRAVITY_MODIFIER_ID, target - 1f, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }


    private static final Identifier DISABLE_JUMP_MODIFIER_ID = Identifier.of("btt", "map_disable_jump");






    public static void tickJump(PlayerEntity player, MapEnhancements enhancement) {
        EntityAttributeInstance attr = player.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH);
        if (attr == null) return;
        boolean allow = enhancement == null || enhancement.jump() == null || enhancement.jump().allowed()
                || dev.hae.btt.trait.Traits.has(player, dev.hae.btt.trait.Traits.ENERGETIC);
        boolean present = false;
        for (EntityAttributeModifier m : attr.getModifiers()) {
            if (m.id().equals(DISABLE_JUMP_MODIFIER_ID)) {
                present = true;
                break;
            }
        }
        if (!present && !allow) {
            attr.addTemporaryModifier(new EntityAttributeModifier(
                    DISABLE_JUMP_MODIFIER_ID, -1.0, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        } else if (present && allow) {
            attr.removeModifier(DISABLE_JUMP_MODIFIER_ID);
        }
    }


    public static void clearGravity(PlayerEntity player) {
        EntityAttributeInstance attr = player.getAttributeInstance(EntityAttributes.GENERIC_GRAVITY);
        if (attr == null) return;
        if (attr.hasModifier(GRAVITY_MODIFIER_ID)) attr.removeModifier(GRAVITY_MODIFIER_ID);
    }




    public static MapEnhancements fromJson(JsonObject o) {
        if (o == null) return NONE;
        List<String> blocks = List.of();
        List<String> tags = List.of();
        JsonObject blacklist = obj(o, "interaction_blacklist");
        if (blacklist != null) {
            blocks = strings(blacklist.get("blocks"));
            tags = strings(blacklist.get("block_tags"));
        }
        float walk = 1f;
        float sprint = 1f;
        JsonObject movement = obj(o, "movement");
        if (movement != null) {
            walk = num(movement, "walk_speed_multiplier", 1f);
            sprint = num(movement, "sprint_speed_multiplier", 1f);
        }
        float gravity = 1f;
        JsonObject gravityObj = obj(o, "gravity");
        if (gravityObj != null) gravity = num(gravityObj, "gravity_multiplier", 1f);
        java.util.Set<String> unconsumed = new java.util.LinkedHashSet<>();
        java.util.Set<String> baselineLocked = new java.util.LinkedHashSet<>();
        for (String key : o.keySet()) {
            if (CONSUMED.contains(key) || KNOWN_INAPPLICABLE.contains(key)) continue;
            if (BASELINE_LOCKED.contains(key)) baselineLocked.add(key);
            else unconsumed.add(key);
        }
        return new MapEnhancements(blocks, tags, walk, sprint, gravity, visualFromJson(obj(o, "visual")),
                jumpFromJson(obj(o, "jump")), fogFromJson(obj(o, "fog")),
                java.util.Set.copyOf(unconsumed), java.util.Set.copyOf(baselineLocked));
    }


    private static Fog fogFromJson(JsonObject j) {
        if (j == null) return Fog.EMPTY;
        return new Fog(floatOrNull(j, "start"), floatOrNull(j, "end_moving"), floatOrNull(j, "end_stationary"),
                colorOrNull(j, "night_color"));
    }


    private static Integer colorOrNull(JsonObject o, String key) {
        JsonElement e = o.get(key);
        if (e == null || !e.isJsonPrimitive()) return null;
        try {
            if (e.getAsJsonPrimitive().isNumber()) return (int) e.getAsLong();
            String s = e.getAsString().trim();
            if (s.startsWith("#")) s = s.substring(1);
            if (s.startsWith("0x") || s.startsWith("0X")) s = s.substring(2);
            return s.isEmpty() ? null : (int) Long.parseLong(s, 16);
        } catch (Exception ex) {
            return null;
        }
    }


    private static Jump jumpFromJson(JsonObject j) {
        if (j == null) return Jump.EMPTY;
        Boolean allowed = boolOrNull(j, "allowed");
        Float cost = floatOrNull(j, "stamina_cost");
        return new Jump(allowed != null && allowed, cost == null ? 0f : cost);
    }

    private static Float floatOrNull(JsonObject o, String key) {
        try {
            JsonElement e = o.get(key);
            return e != null && e.isJsonPrimitive() ? e.getAsFloat() : null;
        } catch (Exception ex) {
            return null;
        }
    }


    private static Visual visualFromJson(JsonObject v) {
        if (v == null) return Visual.EMPTY;
        return new Visual(
                boolOrNull(v, "hud"),
                boolOrNull(v, "snow"),
                boolOrNull(v, "fog"),
                intOrNull(v, "train_speed"),
                v.has("time_of_day") && v.get("time_of_day").isJsonPrimitive() ? v.get("time_of_day").getAsString() : null,
                boolOrNull(v, "static_map"));
    }

    private static Boolean boolOrNull(JsonObject o, String key) {
        JsonElement e = o.get(key);
        return e != null && e.isJsonPrimitive() ? e.getAsBoolean() : null;
    }

    private static Integer intOrNull(JsonObject o, String key) {
        try {
            JsonElement e = o.get(key);
            return e != null && e.isJsonPrimitive() ? e.getAsInt() : null;
        } catch (Exception ex) {
            return null;
        }
    }












    private static final java.util.Map<ServerWorld, Boolean> explicitEffects = new java.util.WeakHashMap<>();


    public static void markExplicitEffect(ServerWorld world) {
        explicitEffects.put(world, true);
    }


    public static boolean consumeExplicitEffect(ServerWorld world) {
        return explicitEffects.remove(world) != null;
    }

    public static void clearExplicitEffects() {
        explicitEffects.clear();
    }

    public static void register() {
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            for (ServerWorld world : server.getWorlds()) restoreLobbyVisual(world);
        });
        dev.doctor4t.wathe.api.event.GameEvents.ON_FINISH_FINALIZE.register((world, game) -> {
            if (world instanceof ServerWorld serverWorld
                    && game.getGameMode() instanceof dev.hae.btt.game.BeforeTheTerminalGameMode) restoreLobbyVisual(serverWorld);
        });
    }


    public static void restoreLobbyVisual(ServerWorld world) {
        var game = dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(world);
        if (game.isRunning() || !(game.getGameMode() instanceof dev.hae.btt.game.BeforeTheTerminalGameMode)) return;
        var train = dev.doctor4t.wathe.cca.TrainWorldComponent.KEY.get(world);
        var effect = game.getMapEffect();
        if (effect == dev.doctor4t.wathe.api.WatheMapEffects.HARPY_EXPRESS_NIGHT)
            train.setTimeOfDay(dev.doctor4t.wathe.cca.TrainWorldComponent.TimeOfDay.NIGHT);
        else if (effect == dev.doctor4t.wathe.api.WatheMapEffects.HARPY_EXPRESS_DAY)
            train.setTimeOfDay(dev.doctor4t.wathe.cca.TrainWorldComponent.TimeOfDay.DAY);
        else if (effect == dev.doctor4t.wathe.api.WatheMapEffects.HARPY_EXPRESS_SUNDOWN)
            train.setTimeOfDay(dev.doctor4t.wathe.cca.TrainWorldComponent.TimeOfDay.SUNDOWN);
        var entry = BttMaps.current(world);
        if (entry != null) applyVisual(world, entry.enhancements());
        train.setSpeed(0);
    }

    public static void applyVisual(net.minecraft.server.world.ServerWorld world, MapEnhancements enhancement) {
        if (enhancement == null || enhancement.visual() == null || enhancement.visual().isEmpty()) return;
        Visual v = enhancement.visual();
        var train = dev.doctor4t.wathe.cca.TrainWorldComponent.KEY.get(world);
        if (v.hud() != null) train.setHud(v.hud());
        if (v.snow() != null) train.setSnow(v.snow());
        if (v.fog() != null) train.setFog(v.fog());
        if (v.trainSpeed() != null) train.setSpeed(v.trainSpeed());
        if (v.timeOfDay() != null) {
            try {
                train.setTimeOfDay(dev.doctor4t.wathe.cca.TrainWorldComponent.TimeOfDay.valueOf(
                        v.timeOfDay().trim().toUpperCase(java.util.Locale.ROOT)));
            } catch (IllegalArgumentException ex) {
                dev.hae.btt.game.BeforeTheTerminalGameMode.LOGGER.warn(
                        "[BTT] unknown visual time_of_day '{}' (expected DAY/NIGHT/SUNDOWN)", v.timeOfDay());
            }
        }
    }

    private static JsonObject obj(JsonObject parent, String key) {
        JsonElement e = parent.get(key);
        return e != null && e.isJsonObject() ? e.getAsJsonObject() : null;
    }

    private static List<String> strings(JsonElement e) {
        if (e == null || !e.isJsonArray()) return List.of();
        JsonArray arr = e.getAsJsonArray();
        List<String> out = new ArrayList<>(arr.size());
        for (JsonElement el : arr) {
            if (el.isJsonPrimitive()) out.add(el.getAsString());
        }
        return List.copyOf(out);
    }

    private static float num(JsonObject o, String key, float def) {
        try {
            JsonElement e = o.get(key);
            return e != null && e.isJsonPrimitive() ? (float) e.getAsDouble() : def;
        } catch (Exception ex) {
            return def;
        }
    }



    public NbtCompound toNbt() {
        NbtCompound c = new NbtCompound();
        c.put("BlacklistBlocks", stringList(blacklistBlocks));
        c.put("BlacklistTags", stringList(blacklistTags));
        c.putFloat("WalkSpeed", walkSpeedMultiplier);
        c.putFloat("SprintSpeed", sprintSpeedMultiplier);
        c.putFloat("Gravity", gravityMultiplier);
        if (visual != null && !visual.isEmpty()) c.put("Visual", visual.toNbt());
        if (jump != null && !jump.isDefault()) c.put("Jump", jump.toNbt());
        if (fog != null && !fog.isEmpty()) c.put("Fog", fog.toNbt());
        return c;
    }

    public static MapEnhancements fromNbt(NbtCompound c) {
        if (c == null) return NONE;
        return new MapEnhancements(
                readStrings(c.getList("BlacklistBlocks", NbtElement.STRING_TYPE)),
                readStrings(c.getList("BlacklistTags", NbtElement.STRING_TYPE)),
                c.contains("WalkSpeed") ? c.getFloat("WalkSpeed") : 1f,
                c.contains("SprintSpeed") ? c.getFloat("SprintSpeed") : 1f,
                c.contains("Gravity") ? c.getFloat("Gravity") : 1f,
                Visual.fromNbt(c.contains("Visual", NbtElement.COMPOUND_TYPE) ? c.getCompound("Visual") : null),
                Jump.fromNbt(c.contains("Jump", NbtElement.COMPOUND_TYPE) ? c.getCompound("Jump") : null),
                Fog.fromNbt(c.contains("Fog", NbtElement.COMPOUND_TYPE) ? c.getCompound("Fog") : null),
                java.util.Set.of(), java.util.Set.of());
    }

    private static NbtList stringList(List<String> values) {
        NbtList list = new NbtList();
        for (String v : values) list.add(net.minecraft.nbt.NbtString.of(v));
        return list;
    }

    private static List<String> readStrings(NbtList list) {
        List<String> out = new ArrayList<>(list.size());
        for (NbtElement e : list) out.add(e.asString());
        return List.copyOf(out);
    }
}
