package dev.hae.btt.component;

import dev.hae.btt.BeforeTheTerminal;
import dev.hae.btt.rotation.GameEntry;
import dev.hae.btt.rotation.RoleCategory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;







public class RotationState implements org.ladysnake.cca.api.v3.component.Component {
    public static final ComponentKey<RotationState> KEY =
            ComponentRegistry.getOrCreate(Identifier.of(BeforeTheTerminal.MOD_ID, "rotation"), RotationState.class);


    public static final int WINDOW = 5;

    private final World world;
    private final Map<UUID, List<GameEntry>> history = new HashMap<>();

    public RotationState(World world) {
        this.world = world;
    }


    public List<GameEntry> window(UUID uuid) {
        return history.getOrDefault(uuid, List.of());
    }


    public void record(Map<UUID, GameEntry> entries) {
        for (Map.Entry<UUID, GameEntry> e : entries.entrySet()) {
            List<GameEntry> list = history.computeIfAbsent(e.getKey(), k -> new ArrayList<>());
            list.add(e.getValue());
            while (list.size() > WINDOW) list.remove(0);
        }
    }

    public void clear() {
        history.clear();
    }

    public Map<UUID, List<GameEntry>> all() {
        return Collections.unmodifiableMap(history);
    }



    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup lookup) {
        history.clear();
        if (!tag.contains("Players", NbtElement.LIST_TYPE)) return;
        NbtList players = tag.getList("Players", NbtElement.COMPOUND_TYPE);
        for (NbtElement pe : players) {
            NbtCompound pc = (NbtCompound) pe;
            UUID uuid = pc.getUuid("U");
            List<GameEntry> list = new ArrayList<>();
            if (pc.contains("E", NbtElement.LIST_TYPE)) {
                for (NbtElement ee : pc.getList("E", NbtElement.COMPOUND_TYPE)) {
                    NbtCompound ec = (NbtCompound) ee;
                    RoleCategory cat;
                    try {
                        cat = RoleCategory.values()[ec.getInt("C")];
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        cat = RoleCategory.CIVILIAN;
                    }

                    if (!ec.contains("N", NbtElement.INT_TYPE)) continue;
                    list.add(new GameEntry(cat, ec.getString("R"),
                            ec.getInt("N"), ec.getInt("K"), ec.getInt("V"), ec.getInt("M")));
                }
            }
            history.put(uuid, list);
        }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup lookup) {
        NbtList players = new NbtList();
        for (Map.Entry<UUID, List<GameEntry>> e : history.entrySet()) {
            NbtCompound pc = new NbtCompound();
            pc.putUuid("U", e.getKey());
            NbtList list = new NbtList();
            for (GameEntry g : e.getValue()) {
                NbtCompound ec = new NbtCompound();
                ec.putInt("C", g.bucket().ordinal());
                ec.putInt("N", g.roster());
                ec.putInt("K", g.killerSeats());
                ec.putInt("V", g.vigilanteSeats());
                ec.putInt("M", g.neutralSeats());
                ec.putString("R", g.roleId() == null ? "" : g.roleId());
                list.add(ec);
            }
            pc.put("E", list);
            players.add(pc);
        }
        tag.put("Players", players);
    }
}
