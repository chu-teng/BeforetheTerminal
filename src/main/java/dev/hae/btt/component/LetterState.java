package dev.hae.btt.component;

import dev.hae.btt.BeforeTheTerminal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentProvider;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import java.util.HashMap;
import java.util.Map;














public class LetterState implements AutoSyncedComponent {
    public static final ComponentKey<LetterState> KEY =
            ComponentRegistry.getOrCreate(Identifier.of(BeforeTheTerminal.MOD_ID, "letters"), LetterState.class);

    public static final int STATUS_ALIVE = 0;
    public static final int STATUS_DEAD = 1;
    public static final int STATUS_MISSING = 2;

    private final PlayerEntity owner;


    public final Map<String, String> marks = new HashMap<>();

    public final Map<String, Integer> status = new HashMap<>();

    public LetterState(PlayerEntity owner) {
        this.owner = owner;
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        return player != null && player.getUuid().equals(this.owner.getUuid());
    }


    public void sync() {
        if (this.owner instanceof ServerPlayerEntity serverPlayer) {
            KEY.syncWith(serverPlayer, (ComponentProvider) (Object) serverPlayer);
        }
    }


    public void setMark(String targetUuid, String value) {
        if (targetUuid == null) return;
        if (value == null || value.isEmpty()) this.marks.remove(targetUuid);
        else this.marks.put(targetUuid, value);
        sync();
    }





    public void markSelf(String rolePath) {
        if (rolePath == null || rolePath.isEmpty()) return;
        this.marks.put(this.owner.getUuidAsString(), rolePath);
        sync();
    }


    public void setStatus(String targetUuid, int value) {
        if (targetUuid == null || value < STATUS_ALIVE || value > STATUS_MISSING) return;
        if (value == STATUS_ALIVE) this.status.remove(targetUuid);
        else this.status.put(targetUuid, value);
        sync();
    }


    public static int nextStatus(int current) {
        return switch (current) {
            case STATUS_DEAD -> STATUS_MISSING;
            case STATUS_MISSING -> STATUS_ALIVE;
            default -> STATUS_DEAD;
        };
    }


    public void reset() {
        this.marks.clear();
        this.status.clear();
        sync();
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound marksTag = new NbtCompound();
        this.marks.forEach(marksTag::putString);
        tag.put("marks", marksTag);
        NbtCompound statusTag = new NbtCompound();
        this.status.forEach(statusTag::putInt);
        tag.put("status", statusTag);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.marks.clear();
        this.status.clear();
        NbtCompound marksTag = tag.getCompound("marks");
        for (String key : marksTag.getKeys()) this.marks.put(key, marksTag.getString(key));
        NbtCompound statusTag = tag.getCompound("status");
        for (String key : statusTag.getKeys()) this.status.put(key, statusTag.getInt(key));
    }
}
