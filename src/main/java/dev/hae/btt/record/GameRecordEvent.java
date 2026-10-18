package dev.hae.btt.record;

import net.minecraft.nbt.NbtCompound;

import java.util.UUID;











public record GameRecordEvent(
        UUID matchId,
        int seq,
        String type,
        long worldTick,
        long realTimeMs,
        NbtCompound data
) {
    public GameRecordEvent {

        data = data == null ? new NbtCompound() : data.copy();
    }
}
