package dev.hae.btt.component;

import dev.doctor4t.wathe.game.GameConstants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.UUID;
import dev.hae.btt.BeforeTheTerminal;

public class AbilityState implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {
    public static final ComponentKey<AbilityState> KEY = ComponentRegistry.getOrCreate(Identifier.of(BeforeTheTerminal.MOD_ID, "ability"), AbilityState.class);
    private final PlayerEntity player;
    public int cooldown = 0;

    public void reset() {
        this.cooldown = 0;
        this.sync();
    }

    public AbilityState(PlayerEntity player) {
        this.player = player;
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
        if (this.cooldown > 0) {
            --this.cooldown;

            this.sync();
        }
    }

    public void setCooldown(int ticks) {
        if (ticks > this.cooldown) dev.hae.btt.record.SkillUseRecording.cooldownSpent(player);
        this.cooldown = ticks;
        this.sync();
    }

    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putInt("cooldown", this.cooldown);
    }

    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.cooldown = tag.contains("cooldown") ? tag.getInt("cooldown") : 0;
    }
}
