package dev.hae.btt.record;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;








public final class MatchRecord {

    private final UUID id;
    private final Identifier dimension;
    private final Identifier gameMode;
    private final Identifier mapEffect;
    private final long startTick;
    private final long startMs;

    private final List<GameRecordEvent> events = new ArrayList<>();

    private final Set<UUID> snapshotted = new HashSet<>();

    private int cursor = 0;
    private boolean open = true;

    MatchRecord(UUID id, Identifier dimension, Identifier gameMode, Identifier mapEffect,
                long startTick, long startMs) {
        this.id = id;
        this.dimension = dimension;
        this.gameMode = gameMode;
        this.mapEffect = mapEffect;
        this.startTick = startTick;
        this.startMs = startMs;
    }

    public UUID id() {
        return id;
    }

    public Identifier dimension() {
        return dimension;
    }

    public Identifier gameMode() {
        return gameMode;
    }

    public Identifier mapEffect() {
        return mapEffect;
    }

    public long startTick() {
        return startTick;
    }

    public long startMs() {
        return startMs;
    }

    public List<GameRecordEvent> events() {
        return Collections.unmodifiableList(events);
    }

    public boolean open() {
        return open;
    }


    void close() {
        this.open = false;
    }


    boolean markSnapshot(UUID uuid) {
        return snapshotted.add(uuid);
    }

    void append(String kind, long worldTick, long wallClockMs, NbtCompound payload) {
        events.add(new GameRecordEvent(id, cursor++, kind, worldTick, wallClockMs, payload));
    }
}
