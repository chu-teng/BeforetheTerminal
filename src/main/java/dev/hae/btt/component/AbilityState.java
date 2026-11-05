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

    public int forceCost() { return 40 + Math.max(0, cooldown) / 80; }
    public boolean canForce() {
        var game = dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(player.getWorld());
        float mood = dev.doctor4t.wathe.cca.PlayerMoodComponent.KEY.get(player).getMood();
        return game.isRunning() && dev.hae.btt.game.SeatAssignment.isBttMode(player.getWorld())
                && dev.hae.btt.role.Roles.effectiveMoodType(game, player) == dev.doctor4t.wathe.api.Role.MoodType.REAL
                && mood >= GameConstants.MID_MOOD_THRESHOLD && mood + 0.00001f >= forceCost() / 100f;
    }
    public ForceScope force() { return new ForceScope(); }
    public final class ForceScope implements AutoCloseable {
        private final int previous;
        private final float before;
        private final boolean forcing;
        private ForceScope() {
            previous = cooldown;
            before = dev.doctor4t.wathe.cca.PlayerMoodComponent.KEY.get(player).getMood();
            forcing = previous > 0 && player instanceof ServerPlayerEntity p && dev.hae.btt.ability.AbilityRules.canAct(p) && canForce();
            if (forcing) { cooldown = 0; dev.doctor4t.wathe.cca.PlayerMoodComponent.KEY.get(player).setMood(before - (40 + previous / 80) / 100f); }
        }
        public void close() {
            if (!forcing) return;
            if (cooldown <= 0) { cooldown = previous; dev.doctor4t.wathe.cca.PlayerMoodComponent.KEY.get(player).setMood(before); }
            sync();
        }
    }

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
        if (this.cooldown > 0) --this.cooldown;
    }

    public void serverTick() {
        if (this.cooldown > 0) {
            --this.cooldown;


            if (this.cooldown % 20 == 0) this.sync();
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
