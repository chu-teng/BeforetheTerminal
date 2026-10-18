package dev.hae.btt.mixin.game;

import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.hae.btt.game.SeatAssignment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;


@Mixin(PlayerShopComponent.class)
public abstract class ShopObservationMixin implements AutoSyncedComponent {
    @Shadow @Final private PlayerEntity player;

    @Override
    public void writeSyncPacket(RegistryByteBuf buf, ServerPlayerEntity recipient) {
        var view = new NbtCompound();
        if (!SeatAssignment.isBttMode(player.getWorld()) || player.getUuid().equals(recipient.getUuid()))
            ((PlayerShopComponent) (Object) this).writeToNbt(view, buf.getRegistryManager());
        buf.writeNbt(view);
    }
}
