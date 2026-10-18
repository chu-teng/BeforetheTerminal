package dev.hae.btt.component;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import dev.hae.btt.BeforeTheTerminal;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class ScavengerState implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {
    public static final ComponentKey<ScavengerState> KEY = ComponentRegistry.getOrCreate(Identifier.of(BeforeTheTerminal.MOD_ID, "scavenger"), ScavengerState.class);
    private final PlayerEntity player;
    public int bodiesEaten = 0;
    public int bodiesRequired = 0;

    public void reset() {
        this.bodiesEaten = 0;
        this.bodiesRequired = 0;
        this.sync();
    }

    public ScavengerState(PlayerEntity player) {
        this.player = player;
        bodiesEaten = 0;
        bodiesRequired = 0;
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity recipient) {
        return recipient != null && player.getUuid().equals(recipient.getUuid());
    }

    public void clientTick() {
    }

    public void serverTick() {
        if (dev.hae.btt.game.SeatAssignment.isBttMode(player.getWorld())
                && dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(player.getWorld()).isRunning()) {


            bodiesRequired = (player.getWorld().getPlayers().size() + 2) / 3;
        }
        sync();
    }

    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putInt("bodiesEaten", this.bodiesEaten);
        tag.putInt("bodiesRequired", this.bodiesRequired);
    }

    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.bodiesEaten = tag.contains("bodiesEaten") ? tag.getInt("bodiesEaten") : 0;
        this.bodiesRequired = tag.contains("bodiesRequired") ? tag.getInt("bodiesRequired") : 0;
    }
}
