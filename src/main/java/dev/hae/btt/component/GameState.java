package dev.hae.btt.component;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.hae.btt.BeforeTheTerminal;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.hae.btt.game.BeforeTheTerminalGameMode;
import dev.hae.btt.victory.Endings;
import dev.hae.btt.role.Locksmith;
import dev.hae.btt.role.RoleDefs;


public class GameState implements AutoSyncedComponent {
    public static final ComponentKey<GameState> KEY =
            ComponentRegistry.getOrCreate(Identifier.of(BeforeTheTerminal.MOD_ID, "game"), GameState.class);

    private final World world;
    public NbtCompound advancementRound = new NbtCompound();

    public String lastEnding = "NONE";

    public String epilogueType = "";

    public String epiloguePlayed = "";

    public int misfireCount = 0;
    public long lastExecutionAt = 0;




    public String winners = "";






    public String pendingEnding = "";
    public final java.util.List<dev.hae.btt.victory.VictoryRequest> pendingVictories = new java.util.ArrayList<>();

    public void clearVictoryRequests() {
        pendingEnding = "";
        pendingVictories.clear();
    }


    public String coldWaveSources = "";





    public String background = "";





    public String script = "";
    public final java.util.Map<java.util.UUID, String> openingRoles = new java.util.LinkedHashMap<>();
    public String stormRole = "";
    public String reborn = "";
    public boolean traitResonance = false;
    public String traitEpilogueHost = "";
    public String traitEpilogueOwner = "";
    public String rebirthPending = "";
    public boolean dreamUsed = false;
    public long dreamDeadline = 0;
    public String dreamVoters = "";
    public String dreamNo = "";








    public String letterNumbers = "";
    public int numberOf(java.util.UUID uuid) {
        String prefix = uuid.toString() + ":";
        for (String entry : letterNumbers.split(",")) {
            if (entry.startsWith(prefix)) {
                try { return Integer.parseInt(entry.substring(prefix.length())); }
                catch (NumberFormatException ignored) { break; }
            }
        }
        return Integer.MAX_VALUE;
    }

    public GameState(World world) {
        this.world = world;
    }

    public void sync() {
        KEY.sync(world);
    }

    public boolean isBttRoundRunning() {
        GameWorldComponent game = GameWorldComponent.KEY.get(world);
        return game.getGameMode() instanceof BeforeTheTerminalGameMode && game.isRunning();
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, @NotNull RegistryWrapper.WrapperLookup registryLookup) {
        tag.put("advancementRound", advancementRound.copy());
        tag.putString("lastEnding", this.lastEnding);
        tag.putString("winners", this.winners);
        tag.putString("pendingEnding", this.pendingEnding);
        var requests = new net.minecraft.nbt.NbtList();
        for (var request : pendingVictories) {
            requests.add(request.toNbt());
        }
        tag.put("pendingVictories", requests);
        tag.putString("coldWaveSources", this.coldWaveSources);
        tag.putString("epilogueType", this.epilogueType);
        tag.putString("epiloguePlayed", this.epiloguePlayed);
        tag.putInt("misfireCount", this.misfireCount);
        tag.putLong("lastExecutionAt", this.lastExecutionAt);

        tag.putString("background", this.background);
        tag.putString("script", this.script);
        NbtCompound roster = new NbtCompound();
        openingRoles.forEach((uuid, role) -> roster.putString(uuid.toString(), role));
        tag.put("openingRoles", roster);
        tag.putString("stormRole", stormRole);
        tag.putString("reborn", reborn);
        tag.putBoolean("traitResonance", traitResonance);
        tag.putString("traitEpilogueHost", traitEpilogueHost);
        tag.putString("traitEpilogueOwner", traitEpilogueOwner);
        tag.putString("rebirthPending", rebirthPending);
        tag.putBoolean("dreamUsed", dreamUsed);
        tag.putLong("dreamDeadline", dreamDeadline);
        tag.putString("dreamVoters", dreamVoters);
        tag.putString("dreamNo", dreamNo);
        tag.putString("letterNumbers", this.letterNumbers);
    }

    @Override
    public void writeSyncPacket(net.minecraft.network.RegistryByteBuf buf,
                                net.minecraft.server.network.ServerPlayerEntity recipient) {
        NbtCompound saved = new NbtCompound();
        writeToNbt(saved, buf.getRegistryManager());
        buf.writeNbt(dev.hae.btt.perception.RoundView.publicState(saved));
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, @NotNull RegistryWrapper.WrapperLookup registryLookup) {
        advancementRound = tag.getCompound("advancementRound").copy();
        if (tag.contains("lastEnding")) this.lastEnding = tag.getString("lastEnding");
        if (tag.contains("winners")) this.winners = tag.getString("winners");
        this.pendingEnding = tag.contains("pendingEnding") ? tag.getString("pendingEnding") : "";
        pendingVictories.clear();
        for (var element : tag.getList("pendingVictories", net.minecraft.nbt.NbtElement.COMPOUND_TYPE)) {
            var request = dev.hae.btt.victory.VictoryRequest.fromNbt((NbtCompound) element);
            if (request != null) pendingVictories.add(request);
        }
        this.coldWaveSources = tag.contains("coldWaveSources") ? tag.getString("coldWaveSources") : "";
        if (tag.contains("epilogueType")) this.epilogueType = tag.getString("epilogueType");
        if (tag.contains("epiloguePlayed")) this.epiloguePlayed = tag.getString("epiloguePlayed");
        this.lastExecutionAt = tag.getLong("lastExecutionAt");
        if (tag.contains("misfireCount")) this.misfireCount = tag.getInt("misfireCount");

        this.background = tag.contains("background") ? tag.getString("background") : "";
        this.script = tag.contains("script") ? tag.getString("script") : "";
        openingRoles.clear();
        NbtCompound roster = tag.getCompound("openingRoles");
        for (String uuid : roster.getKeys()) {
            try { openingRoles.put(java.util.UUID.fromString(uuid), roster.getString(uuid)); }
            catch (IllegalArgumentException ignored) {}
        }
        stormRole = tag.getString("stormRole");
        reborn = tag.getString("reborn");
        traitResonance = tag.getBoolean("traitResonance");
        traitEpilogueHost = tag.getString("traitEpilogueHost");
        traitEpilogueOwner = tag.getString("traitEpilogueOwner");
        rebirthPending = tag.getString("rebirthPending");
        dreamUsed = tag.getBoolean("dreamUsed");
        dreamDeadline = tag.getLong("dreamDeadline");
        dreamVoters = tag.getString("dreamVoters");
        dreamNo = tag.getString("dreamNo");
        this.letterNumbers = tag.contains("letterNumbers") ? tag.getString("letterNumbers") : "";
    }
}
