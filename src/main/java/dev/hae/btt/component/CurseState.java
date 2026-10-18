package dev.hae.btt.component;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import dev.hae.btt.BeforeTheTerminal;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import java.util.UUID;

public class CurseState implements AutoSyncedComponent {
    public static final ComponentKey<CurseState> KEY = ComponentRegistry.getOrCreate(Identifier.of(BeforeTheTerminal.MOD_ID, "curse"), CurseState.class);
    private final PlayerEntity player;
    public UUID target;

    public void reset() {
        this.target = null;
        this.sync();
    }

    public CurseState(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public boolean shouldSyncWith(net.minecraft.server.network.ServerPlayerEntity recipient) {
        return player.getUuid().equals(recipient.getUuid());
    }

    public void sync() {
        KEY.sync(this.player);
    }

    public void setTarget(UUID target) {
        this.target = target;
        this.sync();
    }

    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        if (target != null) tag.putUuid("target", target);
    }

    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.target = tag.containsUuid("target") ? tag.getUuid("target") : null;
    }
}
