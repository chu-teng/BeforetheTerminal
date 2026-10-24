package dev.hae.btt.perception;

import net.minecraft.nbt.NbtCompound;


public final class RoundView {
    private RoundView() {}
    public static NbtCompound publicState(NbtCompound saved) {
        NbtCompound view = new NbtCompound();
        for (String key : new String[]{"lastEnding", "winners", "background", "script",
                "letterNumbers", "letterProfiles", "epilogueType", "roundResultBtt"}) {
            if (saved.contains(key)) view.put(key, saved.get(key).copy());
        }
        return view;
    }
}
