package dev.hae.btt.perception;

import net.minecraft.nbt.NbtCompound;
import java.util.UUID;


public final class DebateView {
    private DebateView() {}

    public static NbtCompound forPlayer(NbtCompound saved, UUID observer) {
        NbtCompound view = new NbtCompound();
        for (String key : new String[]{"phase", "ticksLeft", "reportedBody", "reporter"}) {
            if (saved.contains(key)) view.put(key, saved.get(key).copy());
        }
        String prefix = observer + ":";
        String ballot = java.util.Arrays.stream(saved.getString("votes").split(","))
                .filter(vote -> vote.startsWith(prefix)).findFirst().orElse("");
        view.putString("votes", ballot);
        return view;
    }
}
