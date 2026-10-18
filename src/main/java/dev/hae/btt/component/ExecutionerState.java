package dev.hae.btt.component;

import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import dev.hae.btt.BeforeTheTerminal;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.role.Executioner;

public class ExecutionerState implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {
    public static final ComponentKey<ExecutionerState> KEY = ComponentRegistry.getOrCreate(Identifier.of(BeforeTheTerminal.MOD_ID, "executioner"), ExecutionerState.class);
    private final PlayerEntity player;
    public UUID target;
    public boolean won = false;

    public void reset() {
        this.target = null;
        this.won = false;
        this.sync();
    }

    public ExecutionerState(PlayerEntity player) {
        this.player = player;
        target = null;
    }

    @Override
    public boolean shouldSyncWith(net.minecraft.server.network.ServerPlayerEntity recipient) {
        return player.getUuid().equals(recipient.getUuid());
    }

    public void sync() {
        KEY.sync(this.player);
    }

    public void clientTick() {
    }

    public void serverTick() {


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
