package dev.hae.btt.victory;

import dev.hae.btt.victory.Endings;
import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;


public record VictoryRequest(Endings.Ending ending, UUID actor) {
    public NbtCompound toNbt() {
        var tag = new NbtCompound();
        tag.putString("ending", ending.name());
        if (actor != null) tag.putUuid("actor", actor);
        return tag;
    }

    public static VictoryRequest fromNbt(NbtCompound tag) {
        var ending = Endings.byName(tag.getString("ending"));
        return ending == Endings.Ending.NONE ? null : new VictoryRequest(ending,
                tag.containsUuid("actor") ? tag.getUuid("actor") : null);
    }

    public static VictoryRequest choose(List<VictoryRequest> requests) {
        return requests.stream().filter(r -> Endings.isRelationEnding(r.ending())).findFirst()
                .orElseGet(() -> requests.isEmpty() ? null : requests.getFirst());
    }
}
