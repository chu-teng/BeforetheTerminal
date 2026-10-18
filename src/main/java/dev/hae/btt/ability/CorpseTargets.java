package dev.hae.btt.ability;

import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.RaycastContext;


public final class CorpseTargets {
    private CorpseTargets() {}
    public static PlayerBodyEntity aimed(PlayerEntity player) {
        var start = player.getEyePos();
        var end = start.add(player.getRotationVec(1.0F).multiply(4.0));
        var block = player.getWorld().raycast(new RaycastContext(start, end,
                RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));
        end = block.getPos();
        var nearest = (PlayerBodyEntity) null;
        double distance = start.squaredDistanceTo(end);
        for (var body : player.getWorld().getEntitiesByClass(PlayerBodyEntity.class,
                player.getBoundingBox().expand(5.0), entity -> !entity.isRemoved())) {
            var hit = body.getBoundingBox().expand(0.15).raycast(start, end);
            if (hit.isEmpty()) continue;
            double candidate = start.squaredDistanceTo(hit.get());
            if (candidate <= distance) { distance = candidate; nearest = body; }
        }
        return nearest;
    }
}
