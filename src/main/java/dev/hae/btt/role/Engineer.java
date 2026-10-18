package dev.hae.btt.role;

import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.component.AbilityState;
import dev.hae.btt.component.PlayerState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MarkerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;














public final class Engineer {
    private Engineer() {}


    private record Camera(ServerWorld world, UUID entity) {}
    private static final Map<UUID, Camera> CAMERAS = new HashMap<>();
    private static final String CAMERA_TAG = "btt:engineer_camera";

    private static final Map<String, Long> LAST_REPORT = new HashMap<>();
    private static final double RANGE = 8.0;
    private static final int REPORT_COOLDOWN_TICKS = 200;


    public static void clearAll() {
        CAMERAS.clear();
        LAST_REPORT.clear();
    }


    public static void register() {
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof MarkerEntity && entity.getCommandTags().contains(CAMERA_TAG)
                    && CAMERAS.values().stream().noneMatch(camera -> camera.world() == world
                    && camera.entity().equals(entity.getUuid()))) entity.discard();
        });
    }

    public static void clear(ServerWorld world) {
        var iterator = CAMERAS.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (entry.getValue().world() != world) continue;
            discardCamera(entry.getValue());
            var owner = world.getPlayerByUuid(entry.getKey());
            if (owner != null) updateCameraStatus(PlayerState.KEY.get(owner), 0);
            iterator.remove();
        }
    }

    private static void discardCamera(Camera camera) {
        Entity entity = camera.world().getEntity(camera.entity());
        if (entity != null) entity.discard();
        String prefix = camera.entity() + ":";
        LAST_REPORT.keySet().removeIf(key -> key.startsWith(prefix));
    }


    public static void use(ServerPlayerEntity user, int color) {
        ServerWorld world = user.getServerWorld();
        Camera existing = CAMERAS.get(user.getUuid());
        if (existing != null) {
            discardCamera(existing);
            CAMERAS.remove(user.getUuid());
            PlayerState pc = PlayerState.KEY.get(user);
            pc.engineerCameraStatus = 0;
            pc.sync();
            return;
        }
        AbilityState ability = AbilityState.KEY.get(user);
        if (ability.cooldown > 0) return;
        MarkerEntity camera = new MarkerEntity(EntityType.MARKER, world);
        camera.setPosition(user.getX(), user.getY(), user.getZ());
        camera.setInvisible(true);
        camera.addCommandTag(CAMERA_TAG);
        CAMERAS.put(user.getUuid(), new Camera(world, camera.getUuid()));
        if (!world.spawnEntity(camera)) {
            CAMERAS.remove(user.getUuid());
            return;
        }
        ability.setCooldown(dev.doctor4t.wathe.game.GameConstants.getInTicks(1, 0));
        ability.sync();
        PlayerState pc = PlayerState.KEY.get(user);
        pc.engineerCameraStatus = 1;
        pc.sync();
    }

    private static void updateCameraStatus(PlayerState pc, int status) {
        if (pc.engineerCameraStatus == status) return;
        pc.engineerCameraStatus = status;
        pc.sync();
    }


    public static void tick(ServerPlayerEntity user, PlayerState pc) {
        Camera camera = CAMERAS.get(user.getUuid());
        if (camera == null) {
            updateCameraStatus(pc, 0);
            return;
        }
        ServerWorld world = user.getServerWorld();
        UUID camId = camera.entity();
        Entity cam = camera.world() == world ? world.getEntity(camId) : null;
        updateCameraStatus(pc, cam instanceof MarkerEntity ? 1 : 2);
        if (!(cam instanceof MarkerEntity)) return;

        if (pc.isDrunk()) return;
        for (ServerPlayerEntity p : world.getPlayers()) {
            if (!GameFunctions.isPlayerAliveAndSurvival(p)) continue;
            if (p.squaredDistanceTo(cam) > RANGE * RANGE) continue;
            String key = camId + ":" + p.getUuid();
            long now = world.getTime();
            Long last = LAST_REPORT.get(key);
            if (last != null && now - last < REPORT_COOLDOWN_TICKS) continue;
            LAST_REPORT.put(key, now);
            user.sendMessage(Text.translatable("btt.action.engineer.camera.report",
                    p.getName().getString()).withColor(Roles.ENGINEER.color()), true);
        }
    }
}
