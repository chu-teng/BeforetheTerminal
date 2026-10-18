package dev.hae.btt.component;

import dev.hae.btt.BeforeTheTerminal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;












public class NeedsState implements AutoSyncedComponent {

    public static final ComponentKey<NeedsState> KEY =
            ComponentRegistry.getOrCreate(net.minecraft.util.Identifier.of(BeforeTheTerminal.MOD_ID, "needs"),
                    NeedsState.class);

    private final PlayerEntity player;

    public String kinds = "";

    public String ticksCsv = "";

    public String hitsCsv = "";

    public int nextIn = 0;

    public NeedsState(PlayerEntity player) {
        this.player = player;
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void writeSyncPacket(net.minecraft.network.RegistryByteBuf buf,
                                net.minecraft.server.network.ServerPlayerEntity recipient) {
        NbtCompound saved = new NbtCompound();
        writeToNbt(saved, buf.getRegistryManager());
        if (!player.getUuid().equals(recipient.getUuid())) {
            saved = new NbtCompound();
            if (dev.hae.btt.perception.ObservationRefresh.canSeeNeedCondition(recipient)
                    && kinds != null && !kinds.isEmpty() && kinds.split(",").length >= 3)
                saved.putString("kinds", "OBSERVED,OBSERVED,OBSERVED");
        }
        buf.writeNbt(saved);
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        this.kinds = tag.contains("kinds") ? tag.getString("kinds") : "";
        this.ticksCsv = tag.contains("ticksCsv") ? tag.getString("ticksCsv") : "";
        this.hitsCsv = tag.contains("hitsCsv") ? tag.getString("hitsCsv") : "";
        this.nextIn = tag.getInt("nextIn");
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        tag.putString("kinds", this.kinds == null ? "" : this.kinds);
        tag.putString("ticksCsv", this.ticksCsv == null ? "" : this.ticksCsv);
        tag.putString("hitsCsv", this.hitsCsv == null ? "" : this.hitsCsv);
        tag.putInt("nextIn", this.nextIn);
    }
}
