package dev.hae.btt.component;

import dev.doctor4t.wathe.Wathe;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.util.ShopEntry;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import dev.hae.btt.BeforeTheTerminal;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.UUID;

public class DisguiseState implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {
    public static final ComponentKey<DisguiseState> KEY = ComponentRegistry.getOrCreate(Identifier.of(BeforeTheTerminal.MOD_ID, "disguise"), DisguiseState.class);
    private final PlayerEntity player;
    public UUID disguise;
    public int morphTicks = 0;

    public void reset() {
        this.stopMorph();
        this.sync();
    }

    public DisguiseState(PlayerEntity player) {
        this.player = player;
    }

    public void sync() {
        KEY.sync(this.player);
    }

    public void clientTick() {
    }

    public void serverTick() {
        if (this.morphTicks > 0 && disguise != null) {
            if (!dev.doctor4t.wathe.game.GameFunctions.isPlayerAliveAndSurvival(player)
                    || player.getWorld().getPlayerByUuid(disguise) == null) {
                stopMorph();
                sync();
                return;
            }
            if (--this.morphTicks == 0) {
                this.stopMorph();
            }

            this.sync();
        }
        if (this.morphTicks < 0) {
            this.morphTicks++;
            this.sync();
        }
    }

    public boolean startMorph(UUID id) {
        return startMorph(id, GameConstants.getInTicks(0,35));
    }

    public boolean startMorph(UUID id, int durationTicks) {
        setMorphTicks(durationTicks);
        disguise = id;
        this.sync();
        return true;
    }

    public void stopMorph() {
        this.morphTicks = 0;
        this.disguise = player.getUuid();
    }

    public int getMorphTicks() {
        return this.morphTicks;
    }

    public void setMorphTicks(int ticks) {
        this.morphTicks = ticks;
        this.sync();
    }

    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putInt("morphTicks", this.morphTicks);
        if (disguise == null) disguise = player.getUuid();
        tag.putUuid("disguise", this.disguise);
    }

    @Override
    public void writeSyncPacket(net.minecraft.network.RegistryByteBuf buf,
                                net.minecraft.server.network.ServerPlayerEntity recipient) {
        NbtCompound saved = new NbtCompound();
        writeToNbt(saved, buf.getRegistryManager());
        if (!player.getUuid().equals(recipient.getUuid())) saved.putInt("morphTicks", morphTicks > 0 ? 1 : 0);
        buf.writeNbt(saved);
    }

    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.morphTicks = tag.contains("morphTicks") ? tag.getInt("morphTicks") : 0;
        this.disguise = tag.contains("disguise") ? tag.getUuid("disguise") : player.getUuid();
    }
}
