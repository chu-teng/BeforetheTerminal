package dev.hae.btt.role;

import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import dev.hae.btt.component.*;
import net.minecraft.entity.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;


public final class Toymaker {
    private Toymaker() {}
    private static final String TRAPS = "bearTraps", CAPTIVES = "bearTrapCaptives";
    public static void poisonNext(ServerPlayerEntity player) {
        var ability = AbilityState.KEY.get(player);
        if (ability.cooldown > 0) return;
        ability.setCooldown(1200);
        if (!Drunk.isDrunk(player)) GameState.KEY.get(player.getWorld()).roleEffects.putBoolean("poisonTrap:" + player.getUuidAsString(), true);
    }
    public static void place(ServerPlayerEntity player) {
        var world = player.getServerWorld();
        var effects = GameState.KEY.get(world).roleEffects;

        double x = Math.floor(player.getX()) + 0.5;
        double y = Math.floor(player.getY());
        double z = Math.floor(player.getZ()) + 0.5;
        var marker = new ItemEntity(world, x, y + 0.25, z, new ItemStack(dev.hae.btt.registry.BttItems.BEAR_TRAP));
        marker.setPickupDelay(Integer.MAX_VALUE); marker.setNeverDespawn(); marker.setNoGravity(true); marker.setVelocity(Vec3d.ZERO);
        marker.setInvulnerable(true);
        marker.addCommandTag("btt_bear_trap"); world.spawnEntity(marker);
        var trap = new NbtCompound(); trap.putUuid("owner", player.getUuid()); trap.putUuid("marker", marker.getUuid());
        trap.putBoolean("left", false);
        trap.putDouble("x", x); trap.putDouble("y", y); trap.putDouble("z", z);
        String key = "poisonTrap:" + player.getUuidAsString(); trap.putBoolean("poison", effects.getBoolean(key)); effects.remove(key);
        var traps = effects.getList(TRAPS, NbtElement.COMPOUND_TYPE); traps.add(trap); effects.put(TRAPS, traps);
    }
    public static void tick(ServerWorld world) {
        var effects = GameState.KEY.get(world).roleEffects;
        var traps = effects.getList(TRAPS, NbtElement.COMPOUND_TYPE);
        var captives = effects.getCompound(CAPTIVES);
        for (int i = traps.size() - 1; i >= 0; i--) {
            var trap = traps.getCompound(i); var marker = world.getEntity(trap.getUuid("marker"));
            if (marker == null) {

                int cx = net.minecraft.util.math.MathHelper.floor(trap.getDouble("x")) >> 4;
                int cz = net.minecraft.util.math.MathHelper.floor(trap.getDouble("z")) >> 4;
                if (world.isChunkLoaded(cx, cz)) traps.remove(i);
                continue;
            }
            var pos = new Vec3d(trap.getDouble("x"), trap.getDouble("y"), trap.getDouble("z"));
            marker.setPosition(pos.x, pos.y + 0.25, pos.z); marker.setVelocity(Vec3d.ZERO);
            if (!trap.getBoolean("left")) {
                var owner = world.getPlayerByUuid(trap.getUuid("owner"));
                if (owner == null || owner.squaredDistanceTo(pos) > 0.64) { trap.putBoolean("left", true);  }
                continue;
            }
            for (var player : world.getPlayers()) {
                if (!GameFunctions.isPlayerAliveAndSurvival(player) || player.squaredDistanceTo(pos) > 0.64) continue;
                if (!player.isSneaking()) {
                    var caught = new NbtCompound(); caught.putInt("ticks", 200);
                    caught.putDouble("x", player.getX()); caught.putDouble("y", player.getY()); caught.putDouble("z", player.getZ());
                    caught.putFloat("yaw", player.getYaw()); caught.putFloat("pitch", player.getPitch());
                    captives.put(player.getUuidAsString(), caught);
                    PlayerState.KEY.get(player).trapped = true; PlayerState.KEY.get(player).sync();
                    if (trap.getBoolean("poison")) PlayerPoisonComponent.KEY.get(player).setPoisonTicks(800, trap.getUuid("owner"));
                }
                marker.discard(); traps.remove(i); break;
            }
        }
        for (String id : java.util.List.copyOf(captives.getKeys())) {
            var caught = captives.getCompound(id); var player = world.getPlayerByUuid(java.util.UUID.fromString(id));
            int ticks = caught.getInt("ticks");
            if (!(player instanceof ServerPlayerEntity p) || !GameFunctions.isPlayerAliveAndSurvival(p) || ticks <= 0) { if (player != null) { PlayerState.KEY.get(player).trapped = false; PlayerState.KEY.get(player).sync(); } captives.remove(id); continue; }
            p.teleport(world, caught.getDouble("x"), caught.getDouble("y"), caught.getDouble("z"), caught.getFloat("yaw"), caught.getFloat("pitch"));
            p.setVelocity(Vec3d.ZERO);
            var mood = PlayerMoodComponent.KEY.get(p); int tasks = dev.hae.btt.game.Needs.drainingCount(p);
            if (tasks > 0) mood.setMood(mood.getMood() - 3f * tasks * GameConstants.MOOD_DRAIN);
            caught.putInt("ticks", ticks - 1);
        }
        effects.put(CAPTIVES, captives);
    }
    public static void clear(ServerWorld world) {
        for (var item : world.getEntitiesByType(EntityType.ITEM, e -> e.getCommandTags().contains("btt_bear_trap"))) item.discard();
        for (var p : world.getPlayers()) if (PlayerState.KEY.get(p).trapped) { PlayerState.KEY.get(p).trapped = false; PlayerState.KEY.get(p).sync(); }
        var effects = GameState.KEY.get(world).roleEffects; effects.remove(TRAPS); effects.remove(CAPTIVES);
        for (String key : java.util.List.copyOf(effects.getKeys())) if (key.startsWith("poisonTrap:")) effects.remove(key);
    }
}
