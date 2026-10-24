package dev.hae.btt.mechanic;

import dev.hae.btt.component.PlayerState;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.UUID;










public final class Attachment {
    private Attachment() {}


    public static final String KIND_BLACKDEATH = "BLACKDEATH";
    public static final String KIND_KIDNAP = "KIDNAP";
    public static final String KIND_MEDIUM = "MEDIUM";


    private static final int INVISIBILITY_TICKS = Integer.MAX_VALUE / 4;


    public static void attach(ServerPlayerEntity attached, ServerPlayerEntity host, String kind) {
        if (attached == null || host == null) return;
        PlayerState.KEY.get(attached).setAttached(host.getUuidAsString(), kind);
        attached.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, INVISIBILITY_TICKS, 0, false, false, false));
        if (attached.getCameraEntity() != host) attached.setCameraEntity(host);
    }


    public static void detach(ServerPlayerEntity attached) {
        if (attached == null) return;
        PlayerState.KEY.get(attached).clearAttached();
        attached.removeStatusEffect(StatusEffects.INVISIBILITY);
        attached.setCameraEntity(attached);
    }

    public static boolean isAttached(ServerPlayerEntity player) {
        return PlayerState.KEY.get(player).isAttached();
    }

    public static String kindOf(ServerPlayerEntity player) {
        return PlayerState.KEY.get(player).attachKind;
    }


    public static ServerPlayerEntity hostOf(ServerPlayerEntity attached, ServerWorld world) {
        String uuid = PlayerState.KEY.get(attached).attachedTo;
        if (uuid.isEmpty()) return null;
        try {
            return world.getPlayerByUuid(UUID.fromString(uuid)) instanceof ServerPlayerEntity s ? s : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }


    public static void tick(ServerPlayerEntity attached, ServerWorld world) {
        if (!isAttached(attached)) return;
        ServerPlayerEntity host = hostOf(attached, world);


        if (host == null
                || !dev.doctor4t.wathe.game.GameFunctions.isPlayerAliveAndSurvival(host)
                || !dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(world).isRunning()) {
            detach(attached);
            return;
        }
        if (attached.getCameraEntity() != host) attached.setCameraEntity(host);
        if (!attached.hasStatusEffect(StatusEffects.INVISIBILITY)) {
            attached.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, INVISIBILITY_TICKS, 0, false, false, false));
        }


        if (attached.getServerWorld() != host.getServerWorld()
                || attached.squaredDistanceTo(host) > (PlayerState.KEY.get(attached).smugglerHost.isEmpty() ? 16.0 : 0.0001)) {
            attached.teleport(host.getServerWorld(), host.getX(), host.getY(), host.getZ(),
                    attached.getYaw(), attached.getPitch());
        }
    }
}
