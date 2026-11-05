package dev.hae.btt.component;

import dev.doctor4t.wathe.game.GameConstants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import dev.hae.btt.config.BttConfig;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.UUID;
import dev.hae.btt.BeforeTheTerminal;

public class ConfigState implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<ConfigState> KEY = ComponentRegistry.getOrCreate(Identifier.of(BeforeTheTerminal.MOD_ID, "config"), ConfigState.class);
    public boolean insaneSeesMorphs = true;
    private final World world;

    public void reset() {
        this.sync();
    }

    public ConfigState(World world) {
        this.world = world;
    }

    public void sync() {
        KEY.sync(this.world);
    }

    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        insaneSeesMorphs = BttConfig.HANDLER.instance().insanePlayersSeeMorphs;

        tag.putBoolean("insaneSeesMorphs", this.insaneSeesMorphs);
    }

    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        if (tag.contains("insaneSeesMorphs"))   this.insaneSeesMorphs = tag.getBoolean("insaneSeesMorphs");
    }

    @Override
    public void serverTick() {
    }
}
