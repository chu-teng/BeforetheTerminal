package dev.hae.btt.component;

import dev.hae.btt.BeforeTheTerminal;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;









public class DebateState implements AutoSyncedComponent {

    public static final ComponentKey<DebateState> KEY =
            ComponentRegistry.getOrCreate(net.minecraft.util.Identifier.of(BeforeTheTerminal.MOD_ID, "debate"),
                    DebateState.class);


    public static final int PHASE_IDLE = 0;
    public static final int PHASE_MEETING = 1;
    public static final int PHASE_VOTING = 2;

    public static final int MEETING_TICKS = 20 * 60;
    public static final int VOTING_TICKS = 20 * 30;

    private final World world;

    public int phase = PHASE_IDLE;

    public int ticksLeft = 0;

    public String reportedBody = "";

    public String reporter = "";

    public int lastMeetingAlive = -1;

    public String votes = "";

    public String tieDrunk = "";

    public DebateState(World world) {
        this.world = world;
    }

    public void sync() {
        KEY.sync(this.world);
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        this.phase = tag.getInt("phase");
        this.ticksLeft = tag.getInt("ticksLeft");
        this.reportedBody = tag.contains("reportedBody") ? tag.getString("reportedBody") : "";
        this.reporter = tag.contains("reporter") ? tag.getString("reporter") : "";
        this.lastMeetingAlive = tag.contains("lastMeetingAlive") ? tag.getInt("lastMeetingAlive") : -1;
        this.votes = tag.contains("votes") ? tag.getString("votes") : "";
        this.tieDrunk = tag.contains("tieDrunk") ? tag.getString("tieDrunk") : "";
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        tag.putInt("phase", this.phase);
        tag.putInt("ticksLeft", this.ticksLeft);
        tag.putString("reportedBody", this.reportedBody == null ? "" : this.reportedBody);
        tag.putString("reporter", this.reporter == null ? "" : this.reporter);
        tag.putInt("lastMeetingAlive", this.lastMeetingAlive);
        tag.putString("votes", this.votes == null ? "" : this.votes);
        tag.putString("tieDrunk", this.tieDrunk == null ? "" : this.tieDrunk);
    }

    @Override
    public void writeSyncPacket(net.minecraft.network.RegistryByteBuf buf,
                                net.minecraft.server.network.ServerPlayerEntity recipient) {
        NbtCompound saved = new NbtCompound();
        writeToNbt(saved, buf.getRegistryManager());
        buf.writeNbt(dev.hae.btt.perception.DebateView.forPlayer(saved, recipient.getUuid()));
    }


    public void reset() {
        phase = PHASE_IDLE;
        ticksLeft = 0;
        reportedBody = "";
        reporter = "";
        lastMeetingAlive = -1;
        votes = "";
        tieDrunk = "";
    }
}
