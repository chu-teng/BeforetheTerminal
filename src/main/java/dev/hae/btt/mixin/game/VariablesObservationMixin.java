package dev.hae.btt.mixin.game;

import dev.doctor4t.wathe.cca.PlayerVariablesComponent;
import dev.hae.btt.game.SeatAssignment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;


@Mixin(PlayerVariablesComponent.class)
public abstract class VariablesObservationMixin implements AutoSyncedComponent {
    @Shadow @Final private PlayerEntity player;

    @Override
    public void writeSyncPacket(RegistryByteBuf buf, ServerPlayerEntity recipient) {
        var view = new NbtCompound();
        if (!SeatAssignment.isBttMode(player.getWorld()) || player.getUuid().equals(recipient.getUuid()))
            ((PlayerVariablesComponent) (Object) this).writeToNbt(view, buf.getRegistryManager());
        buf.writeNbt(view);
    }
}
