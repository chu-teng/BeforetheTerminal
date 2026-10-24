package dev.hae.btt.component;

import dev.hae.btt.BeforeTheTerminal;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;







public class MapVoteState implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<MapVoteState> KEY =
            ComponentRegistry.getOrCreate(Identifier.of(BeforeTheTerminal.MOD_ID, "mapvote"), MapVoteState.class);

    private final World world;


    public boolean active = false;

    public int ticks = 0;

    public boolean roulette = false;
    public int rouletteTicks = 0;

    public long rouletteUpdatedAt;

    public final List<String> ids = new ArrayList<>();
    public final List<String> names = new ArrayList<>();
    public final List<String> descs = new ArrayList<>();
    public final List<Integer> minimums = new ArrayList<>();
    public final List<Integer> maximums = new ArrayList<>();

    public int[] counts = new int[0];





    public boolean[] eligible = new boolean[0];

    public final Map<UUID, Integer> votes = new HashMap<>();

    public int selected = -1;

    public String lastSelected = "";




    public final List<dev.hae.btt.map.BttRoom> rooms = new ArrayList<>();

    public String roomsMapId = "";




    public dev.hae.btt.map.MapEnhancements enhancements = dev.hae.btt.map.MapEnhancements.NONE;

    public MapVoteState(World world) {
        this.world = world;
    }

    public void sync() {
        KEY.sync(world);




        if (world.getRegistryKey().equals(net.minecraft.world.World.OVERWORLD)
                && world instanceof net.minecraft.server.world.ServerWorld sw && sw.getServer() != null) {
            for (net.minecraft.server.world.ServerWorld other : sw.getServer().getWorlds()) {
                if (other == world) continue;
                MapVoteState mirrored = KEY.get(other);
                mirrored.copyVoteFrom(this);
                KEY.sync(other);
            }
        }

    }


    public void copyVoteFrom(MapVoteState other) {
        active = other.active;
        ticks = other.ticks;
        roulette = other.roulette;
        rouletteTicks = other.rouletteTicks;
        selected = other.selected;
        ids.clear();
        ids.addAll(other.ids);
        names.clear();
        names.addAll(other.names);
        descs.clear();
        minimums.clear(); maximums.clear();
        descs.addAll(other.descs);
        minimums.addAll(other.minimums); maximums.addAll(other.maximums);
        counts = other.counts == null ? new int[0] : other.counts.clone();
        eligible = other.eligible == null ? new boolean[0] : other.eligible.clone();
        votes.clear();
        votes.putAll(other.votes);
    }

    @Override
    public void serverTick() {
        if (!(world instanceof net.minecraft.server.world.ServerWorld sw)) return;
        if (!world.getRegistryKey().equals(World.OVERWORLD)) return;

        if (dev.hae.btt.map.MapVoting.tickPending(sw)) return;
        if (!active) return;
        dev.hae.btt.map.MapVoting.tick(sw);
    }

    public void reset() {
        active = false;
        ticks = 0;
        roulette = false;
        rouletteTicks = 0;
        ids.clear();
        names.clear();
        descs.clear();
        minimums.clear(); maximums.clear();
        counts = new int[0];
        eligible = new boolean[0];
        votes.clear();
        selected = -1;
        sync();
    }



    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup lookup) {
        active = tag.getBoolean("Active");
        ticks = tag.getInt("Ticks");
        roulette = tag.getBoolean("Roulette");
        rouletteTicks = tag.getInt("RouletteTicks");
        rouletteUpdatedAt = world.getTime();
        selected = tag.getInt("Selected");
        lastSelected = tag.getString("LastSelected");
        rooms.clear();
        rooms.addAll(dev.hae.btt.map.BttRoom.listFromNbt(tag));
        roomsMapId = tag.getString("RoomsMapId");
        enhancements = tag.contains("Enhancements", NbtElement.COMPOUND_TYPE)
                ? dev.hae.btt.map.MapEnhancements.fromNbt(tag.getCompound("Enhancements"))
                : dev.hae.btt.map.MapEnhancements.NONE;

        ids.clear();
        names.clear();
        descs.clear();
        minimums.clear(); maximums.clear();
        java.util.List<Boolean> okList = new ArrayList<>();
        if (tag.contains("Maps", NbtElement.LIST_TYPE)) {
            NbtList list = tag.getList("Maps", NbtElement.COMPOUND_TYPE);
            for (NbtElement e : list) {
                NbtCompound c = (NbtCompound) e;
                ids.add(c.getString("Id"));
                names.add(c.getString("Name"));
                descs.add(c.getString("Desc"));
                minimums.add(c.getInt("MinPlayers")); maximums.add(c.getInt("MaxPlayers"));
                okList.add(!c.contains("Ok") || c.getBoolean("Ok"));
            }
        }
        eligible = new boolean[okList.size()];
        for (int i = 0; i < okList.size(); i++) eligible[i] = okList.get(i);
        counts = tag.contains("Counts") ? tag.getIntArray("Counts") : new int[ids.size()];

        votes.clear();
        if (tag.contains("Votes", NbtElement.LIST_TYPE)) {
            NbtList list = tag.getList("Votes", NbtElement.COMPOUND_TYPE);
            for (NbtElement e : list) {
                NbtCompound c = (NbtCompound) e;
                if (c.containsUuid("P")) votes.put(c.getUuid("P"), c.getInt("I"));
            }
        }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup lookup) {
        tag.putBoolean("Active", active);
        tag.putInt("Ticks", ticks);
        tag.putBoolean("Roulette", roulette);
        tag.putInt("RouletteTicks", rouletteTicks);
        tag.putInt("Selected", selected);
        tag.putString("LastSelected", lastSelected);
        tag.put("Rooms", dev.hae.btt.map.BttRoom.listToNbt(rooms));
        tag.putString("RoomsMapId", roomsMapId);
        tag.put("Enhancements", enhancements.toNbt());

        NbtList list = new NbtList();
        for (int i = 0; i < ids.size(); i++) {
            NbtCompound c = new NbtCompound();
            c.putString("Id", ids.get(i));
            c.putString("Name", i < names.size() ? names.get(i) : ids.get(i));
            c.putString("Desc", i < descs.size() ? descs.get(i) : "");
            c.putInt("MinPlayers", i < minimums.size() ? minimums.get(i) : 6);
            c.putInt("MaxPlayers", i < maximums.size() ? maximums.get(i) : 22);
            c.putBoolean("Ok", i >= eligible.length || eligible[i]);
            list.add(c);
        }
        tag.put("Maps", list);
        tag.putIntArray("Counts", counts);

        NbtList voteList = new NbtList();
        for (Map.Entry<UUID, Integer> e : votes.entrySet()) {
            NbtCompound c = new NbtCompound();
            c.putUuid("P", e.getKey());
            c.putInt("I", e.getValue());
            voteList.add(c);
        }
        tag.put("Votes", voteList);
    }
}
