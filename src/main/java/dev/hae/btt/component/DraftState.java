package dev.hae.btt.component;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.hae.btt.BeforeTheTerminal;
import dev.hae.btt.draft.DraftManager;
import dev.hae.btt.game.SeatAssignment;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;











public class DraftState implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<DraftState> KEY =
            ComponentRegistry.getOrCreate(Identifier.of(BeforeTheTerminal.MOD_ID, "draft"), DraftState.class);

    private final World world;


    public boolean active = false;

    public int ordinal = 0;

    public int totalTurns = 0;

    public int ticksLeft = 0;

    public int pendingCount = 0;

    public DraftState(World world) {
        this.world = world;
    }

    public void sync() {
        KEY.sync(world);
    }

    @Override
    public void serverTick() {
        if (!(world instanceof ServerWorld sw)) return;

        DraftManager.tick(sw);
    }



    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup lookup) {
        active = tag.getBoolean("Active");
        ordinal = tag.getInt("Ordinal");
        totalTurns = tag.getInt("Total");
        ticksLeft = tag.getInt("Ticks");
        pendingCount = tag.getInt("Pending");
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup lookup) {
        tag.putBoolean("Active", active);
        tag.putInt("Ordinal", ordinal);
        tag.putInt("Total", totalTurns);
        tag.putInt("Ticks", ticksLeft);
        tag.putInt("Pending", pendingCount);
    }
}
