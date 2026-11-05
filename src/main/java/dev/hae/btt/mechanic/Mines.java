package dev.hae.btt.mechanic;

import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.component.AbilityState;
import dev.hae.btt.component.GameState;
import dev.hae.btt.component.PlayerState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import java.util.UUID;


public final class Mines {
    private Mines() {}
    private static final String KEY = "mines";
    public static void place(ServerPlayerEntity player) { place(player, false); }
    public static void placeGas(ServerPlayerEntity player) { place(player, true); }
    private static void place(ServerPlayerEntity player, boolean gas) {
        var ability = AbilityState.KEY.get(player);
        if (ability.cooldown > 0) return;
        ability.setCooldown(2400);
        if (PlayerState.KEY.get(player).isDrunk()) return;
        var world = player.getServerWorld();
        var marker = new ItemEntity(world, player.getX(), player.getY(), player.getZ(), new ItemStack(gas ? dev.hae.btt.registry.BttItems.POISON_GAS_GRENADE : Items.TNT));
        marker.setPickupDelay(32767); marker.setNeverDespawn(); marker.setNoGravity(true); marker.setVelocity(Vec3d.ZERO);
        marker.setCustomName(Text.translatable(gas ? "btt.gas_package" : "btt.mine"));
        marker.addCommandTag("btt_mine"); world.spawnEntity(marker);
        var saved = new NbtCompound(); saved.putBoolean("gas", gas); saved.putUuid("owner", player.getUuid()); saved.putUuid("marker", marker.getUuid());
        saved.putDouble("x", player.getX()); saved.putDouble("y", player.getY()); saved.putDouble("z", player.getZ());
        var effects = GameState.KEY.get(world).roleEffects;
        var packages = effects.getList(KEY, NbtElement.COMPOUND_TYPE); packages.add(saved); effects.put(KEY, packages);
    }
    public static void tick(ServerWorld world) {
        var effects = GameState.KEY.get(world).roleEffects;
        var packages = effects.getList(KEY, NbtElement.COMPOUND_TYPE);
        for (int i = packages.size() - 1; i >= 0; i--) {
            var saved = packages.getCompound(i);
            var marker = world.getEntity(saved.getUuid("marker"));
            if (marker == null) { packages.remove(i); continue; }
            var pos = new Vec3d(saved.getDouble("x"), saved.getDouble("y"), saved.getDouble("z"));
            marker.setPosition(pos); marker.setVelocity(Vec3d.ZERO);
            var owner = world.getPlayerByUuid(saved.getUuid("owner"));
            if (!saved.getBoolean("armed")) {
                if (owner == null || owner.squaredDistanceTo(pos) > 4) { saved.putBoolean("armed", true);  }
                continue;
            }
            if (world.getPlayers().stream().noneMatch(p -> GameFunctions.isPlayerAliveAndSurvival(p) && p.squaredDistanceTo(pos) <= 4)) continue;
            if (saved.getBoolean("gas")) {
                var cloud = new dev.hae.btt.entity.PoisonGasCloudEntity(dev.hae.btt.registry.BttEntities.POISON_GAS_CLOUD, world);
                cloud.setPosition(pos);
                cloud.setOwnerUuid(saved.getUuid("owner"));
                world.spawnEntity(cloud);
                marker.discard(); packages.remove(i);
                continue;
            }
            world.playSound(null, marker.getBlockPos(), dev.doctor4t.wathe.index.WatheSounds.ITEM_GRENADE_EXPLODE, net.minecraft.sound.SoundCategory.PLAYERS, 3f, 1f);
            world.spawnParticles(net.minecraft.particle.ParticleTypes.EXPLOSION, pos.x, pos.y, pos.z, 2, 0, 0, 0, 0);
            var victims = world.getPlayers().stream().filter(p -> GameFunctions.isPlayerAliveAndSurvival(p) && p.squaredDistanceTo(pos) <= 4).toList();
            marker.discard(); packages.remove(i);
            for (var victim : victims) GameFunctions.killPlayer(victim, true, owner instanceof ServerPlayerEntity p ? p : null, dev.hae.btt.combat.DeathReasons.BOMB);
        }
    }
    public static void clear(ServerWorld world) {
        for (var item : world.getEntitiesByType(EntityType.ITEM, item -> item.getCommandTags().contains("btt_mine"))) item.discard();
        GameState.KEY.get(world).roleEffects.remove(KEY);
    }
}
