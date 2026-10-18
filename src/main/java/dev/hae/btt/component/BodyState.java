package dev.hae.btt.component;

import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import dev.hae.btt.BeforeTheTerminal;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.Component;





public class BodyState implements Component {
    public static final ComponentKey<BodyState> KEY =
            ComponentRegistry.getOrCreate(Identifier.of(BeforeTheTerminal.MOD_ID, "body"), BodyState.class);

    private final PlayerBodyEntity body;


    public boolean amnesiacUsed = false;

    public BodyState(PlayerBodyEntity body) {
        this.body = body;
    }

    public boolean isAmnesiacUsed() {
        return amnesiacUsed;
    }

    public void markAmnesiacUsed() {
        this.amnesiacUsed = true;
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putBoolean("amnesiacUsed", amnesiacUsed);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.amnesiacUsed = tag.contains("amnesiacUsed") && tag.getBoolean("amnesiacUsed");
    }
}
