package dev.hae.btt.role;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.component.AbilityState;
import dev.hae.btt.component.GameState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import java.util.List;
import java.util.UUID;


public final class Harpy {
    private Harpy() {}
    private static final String PREFIX = "harpyShroud:";
    public static final int DELAY = 1200;
    public static final int COOLDOWN = 2400;

    public static void register() {
        dev.hae.btt.api.event.BttEvents.Death.EVENT.register((victim, killer, reason) -> {
            var effects = GameState.KEY.get(victim.getWorld()).roleEffects;
            for (String key : List.copyOf(effects.getKeys())) {
                if (key.startsWith(PREFIX) && effects.getCompound(key).getString("first").equals(victim.getUuidAsString()))
                    effects.remove(key);
            }
        });
    }

    public static void shroud(ServerPlayerEntity caster, GameWorldComponent game, List<UUID> picks) {
        var ability = AbilityState.KEY.get(caster);
        if (ability.cooldown > 0 || picks.size() != 2 || picks.get(0).equals(picks.get(1))) return;
        var world = caster.getServerWorld();
        var first = world.getPlayerByUuid(picks.get(0));
        var second = world.getPlayerByUuid(picks.get(1));
        if (!(first instanceof ServerPlayerEntity a) || !(second instanceof ServerPlayerEntity b)
                || !GameFunctions.isPlayerAliveAndSurvival(a) || !GameFunctions.isPlayerAliveAndSurvival(b)) return;
        ability.setCooldown(COOLDOWN);
        if (Drunk.isDrunk(caster)) return;
        var mark = new NbtCompound();
        mark.putString("first", a.getUuidAsString());
        mark.putString("second", b.getUuidAsString());
        mark.putInt("ticks", DELAY);
        GameState.KEY.get(world).roleEffects.put(PREFIX + caster.getUuidAsString(), mark);
        for (var listener : world.getPlayers()) {
            listener.sendMessage(net.minecraft.text.Text.translatable("btt.action.harpy.shroud",
                    dev.hae.btt.game.SeatAssignment.nameOf(a)).withColor(Roles.HARPY.color()), true);
            listener.playSoundToPlayer(dev.hae.btt.registry.Sounds.HARPY_SHROUD,
                    net.minecraft.sound.SoundCategory.PLAYERS, 1, 1);
        }
    }

    public static void tick(ServerWorld world) {
        var effects = GameState.KEY.get(world).roleEffects;
        for (String key : List.copyOf(effects.getKeys())) {
            if (!key.startsWith(PREFIX)) continue;
            var mark = effects.getCompound(key);
            var first = resolve(world, mark.getString("first"));
            if (first == null || !(GameFunctions.isPlayerAliveAndSurvival(first)
                    || dev.hae.btt.component.PlayerState.KEY.get(first).isSwallowed())) {
                effects.remove(key); continue;
            }
            int remaining = mark.getInt("ticks") - 1;
            if (remaining > 0) { mark.putInt("ticks", remaining); continue; }
            effects.remove(key);
            var victim = resolve(world, mark.getString("second"));
            var caster = resolve(world, key.substring(PREFIX.length()));
            if (victim != null && dev.hae.btt.component.PlayerState.KEY.get(victim).isSwallowed())
                Kidnapper.release(victim, dev.hae.btt.component.PlayerState.KEY.get(victim),
                        dev.hae.btt.mechanic.Attachment.hostOf(victim, world));
            if (victim != null && GameFunctions.isPlayerAliveAndSurvival(victim))
                GameFunctions.killPlayer(victim, true, caster, dev.hae.btt.combat.DeathReasons.MAGIC);
        }
    }

    private static ServerPlayerEntity resolve(ServerWorld world, String value) {
        try { return world.getPlayerByUuid(UUID.fromString(value)) instanceof ServerPlayerEntity p ? p : null; }
        catch (IllegalArgumentException ignored) { return null; }
    }
}
