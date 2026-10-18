package dev.hae.btt.mixin.game;

import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.perception.NativeEffectView;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;


@Mixin(PlayerPoisonComponent.class)
public abstract class PoisonObservationMixin implements AutoSyncedComponent {
    @Shadow @Final private PlayerEntity player;

    @Override
    public void writeSyncPacket(RegistryByteBuf buf, ServerPlayerEntity recipient) {
        var component = (AutoSyncedComponent) this;
        var saved = new NbtCompound();
        component.writeToNbt(saved, buf.getRegistryManager());
        boolean owner = player.getUuid().equals(recipient.getUuid());
        var view = !SeatAssignment.isBttMode(player.getWorld()) ? saved : NativeEffectView.poison(saved, owner);
        buf.writeNbt(view);
    }
}
