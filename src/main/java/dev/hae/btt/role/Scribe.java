package dev.hae.btt.role;
import net.minecraft.server.network.ServerPlayerEntity;
import dev.hae.btt.component.*;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.nbt.*;
import net.minecraft.text.Text;
public final class Scribe {
    private Scribe() {}
    public static void observe(ServerPlayerEntity actor, String name) {
        var world = actor.getServerWorld(); var game = GameWorldComponent.KEY.get(world);
        if (!game.isRunning()) return;
        for (var p : world.getPlayers()) {
            if (!game.isRole(p, Roles.SCRIBE) || !dev.doctor4t.wathe.game.GameFunctions.isPlayerAliveAndSurvival(p)
                    || AbilityState.KEY.get(p).cooldown > 0 || Drunk.isDrunk(p)) continue;
            var effects = GameState.KEY.get(world).roleEffects; String key = "scribe:" + p.getUuidAsString();
            var records = effects.getList(key, NbtElement.STRING_TYPE);
            records.add(NbtString.of(name)); if (records.size() > 4) records.remove(0); effects.put(key, records);
        }
    }
    public static void read(ServerPlayerEntity p) {
        var ability = AbilityState.KEY.get(p); if (ability.cooldown > 0) return;
        ability.setCooldown(600); if (Drunk.isDrunk(p)) return;
        var effects = GameState.KEY.get(p.getWorld()).roleEffects; String key = "scribe:" + p.getUuidAsString();
        var records = effects.getList(key, NbtElement.STRING_TYPE); var line = Text.empty();
        for (int i = 0; i < records.size(); i++) { if (i > 0) line.append(" / "); line.append(Text.translatable(records.getString(i))); }
        p.sendMessage(line.withColor(Roles.SCRIBE.color()), true); effects.remove(key);
    }
}
