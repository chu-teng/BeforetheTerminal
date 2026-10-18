package dev.hae.btt.mixin.game;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.hae.btt.perception.NativeRoleView;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.spongepowered.asm.mixin.Mixin;


@Mixin(GameWorldComponent.class)
public abstract class GameWorldObservationMixin implements AutoSyncedComponent {
    @Override
    public void writeSyncPacket(RegistryByteBuf buf, ServerPlayerEntity recipient) {
        var game = (GameWorldComponent) (Object) this;
        NbtCompound saved = new NbtCompound();
        game.writeToNbt(saved, buf.getRegistryManager());
        buf.writeNbt(NativeRoleView.forPlayer(saved, game, recipient));
    }
}
