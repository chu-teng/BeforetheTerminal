package dev.hae.btt.component;

import dev.hae.btt.BeforeTheTerminal;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;








public class ShopState implements AutoSyncedComponent {

    public static final ComponentKey<ShopState> KEY =
            ComponentRegistry.getOrCreate(net.minecraft.util.Identifier.of(BeforeTheTerminal.MOD_ID, "shop"),
                    ShopState.class);

    private final World world;

    public String exclusive = "";

    public ShopState(World world) {
        this.world = world;
    }

    public void sync() {
        KEY.sync(this.world);
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        this.exclusive = tag.contains("exclusive") ? tag.getString("exclusive") : "";
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        tag.putString("exclusive", this.exclusive == null ? "" : this.exclusive);
    }
}
