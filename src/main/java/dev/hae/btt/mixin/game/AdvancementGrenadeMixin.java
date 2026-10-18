package dev.hae.btt.mixin.game;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.doctor4t.wathe.entity.GrenadeEntity;
import dev.hae.btt.advancement.BttAdvancements;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(GrenadeEntity.class)
public abstract class AdvancementGrenadeMixin {
    @WrapMethod(method = "onCollision")
    private void bttExplosion(HitResult hit, Operation<Void> original) {
        var grenade = (GrenadeEntity) (Object) this;
        if (!(grenade.getOwner() instanceof ServerPlayerEntity owner)) { original.call(hit); return; }
        try (var scope = BttAdvancements.explosion(owner)) { original.call(hit); }
    }
}
