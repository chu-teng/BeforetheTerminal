package dev.hae.btt.mixin.game;
import net.minecraft.server.network.ServerItemCooldownManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(ServerItemCooldownManager.class)
public interface ServerCooldownOwnerAccessor {
    @Accessor("player") ServerPlayerEntity btt$owner();
}
