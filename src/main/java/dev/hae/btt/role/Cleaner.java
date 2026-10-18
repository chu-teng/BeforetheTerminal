package dev.hae.btt.role;

import dev.doctor4t.wathe.game.GameConstants;
import dev.hae.btt.component.AbilityState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.EntityHitResult;









public final class Cleaner {
    private Cleaner() {}


    private static final double RANGE = 6.0;


    public static void purge(ServerPlayerEntity user) {
        AbilityState ability = AbilityState.KEY.get(user);
        if (ability.cooldown > 0) return;
        var hit = ProjectileUtil.getCollision(user,
                e -> e instanceof ItemEntity item && !item.getStack().isEmpty(), RANGE);
        if (!(hit instanceof EntityHitResult ehr) || !(ehr.getEntity() instanceof ItemEntity target)) {
            return;
        }
        target.discard();
        ability.setCooldown(GameConstants.getInTicks(2, 0));
        ability.sync();
    }
}