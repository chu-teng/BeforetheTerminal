package dev.hae.btt.mixin.game;

import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
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


@Mixin(PlayerPsychoComponent.class)
public abstract class PsychoObservationMixin implements AutoSyncedComponent {
    @Shadow @Final private PlayerEntity player;

    @org.spongepowered.asm.mixin.injection.Inject(method = "startPsycho", at = @org.spongepowered.asm.mixin.injection.At("RETURN"))
    private void bttPsychoSession(org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && player instanceof ServerPlayerEntity owner)
            dev.hae.btt.advancement.BttAdvancements.psychoStarted(owner);
    }

    @Override
    public void writeSyncPacket(RegistryByteBuf buf, ServerPlayerEntity recipient) {
        var component = (AutoSyncedComponent) this;
        var saved = new NbtCompound();
        component.writeToNbt(saved, buf.getRegistryManager());
        boolean owner = player.getUuid().equals(recipient.getUuid());
        var view = !SeatAssignment.isBttMode(player.getWorld()) ? saved : NativeEffectView.psycho(saved, owner);
        buf.writeNbt(view);
    }
}
