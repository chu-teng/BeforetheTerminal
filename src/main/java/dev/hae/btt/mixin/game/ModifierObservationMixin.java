package dev.hae.btt.mixin.game;

import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.perception.NativeRoleView;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;


@Mixin(WorldModifierComponent.class)
public abstract class ModifierObservationMixin implements AutoSyncedComponent {
    @Shadow @Final private World world;

    @Override
    public void writeSyncPacket(RegistryByteBuf buf, ServerPlayerEntity recipient) {
        var saved = new NbtCompound();
        ((WorldModifierComponent) (Object) this).writeToNbt(saved, buf.getRegistryManager());
        if (SeatAssignment.isBttMode(world) && !NativeRoleView.fullObserver(recipient)) {
            for (String key : saved.getKeys()) {
                NbtList own = new NbtList();
                for (var entry : saved.getList(key, NbtElement.INT_ARRAY_TYPE))
                    if (NbtHelper.toUuid(entry).equals(recipient.getUuid())) own.add(entry.copy());
                saved.put(key, own);
            }
        }
        buf.writeNbt(saved);
    }
}
