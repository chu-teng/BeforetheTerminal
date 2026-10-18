package dev.hae.btt.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
















public final class SightGate {
    private SightGate() {}


    public static final double MAX_DISTANCE = 64.0;

    public static boolean canSee(Entity target) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity viewer = client.player;
        if (viewer == null || target == null || client.world == null) return false;
        if (target == viewer || target.isRemoved()) return false;

        if (target.isInvisibleTo(viewer)) return false;
        if (viewer.squaredDistanceTo(target) > MAX_DISTANCE * MAX_DISTANCE) return false;

        return viewer.canSee(target);
    }
}
