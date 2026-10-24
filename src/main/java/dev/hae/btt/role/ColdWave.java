package dev.hae.btt.role;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import dev.hae.btt.component.PlayerState;






public final class ColdWave {
    private ColdWave() {}

    public static boolean affectsSanity(net.minecraft.entity.player.PlayerEntity player) {
        var game = GameWorldComponent.KEY.get(player.getWorld());
        if (!dev.hae.btt.game.SeatAssignment.isBttMode(player.getWorld()) || !game.isRunning()
                || Roles.isPassengerCampFor(game, player)) return false;
        if (player.getWorld().isClient) return PlayerState.KEY.get(player).coldWaveSanity;
        return ((ServerWorld) player.getWorld()).getPlayers().stream().anyMatch(p ->
                GameFunctions.isPlayerAliveAndSurvival(p) && Roles.hasCurrentRole(game, p, Roles.COLD_WAVE));
    }

    public static void updateSanity(ServerWorld world) {
        for (var player : world.getPlayers()) {
            var state = PlayerState.KEY.get(player);
            boolean enabled = affectsSanity(player);
            if (state.coldWaveSanity == enabled) continue;
            state.coldWaveSanity = enabled;
            state.sync();
            dev.doctor4t.wathe.cca.PlayerMoodComponent.KEY.get(player).sync();
        }
    }


    static final int PATIENT_TICKS = GameConstants.getInTicks(1, 0);

    private static final double AURA_RANGE = 20.0;


    public static void diagnose(ServerPlayerEntity user, ServerPlayerEntity target) {
        var ability = dev.hae.btt.component.AbilityState.KEY.get(user);
        if (ability.cooldown > 0 || user == target || user.distanceTo(target) > 6) return;
        if (!dev.hae.btt.mechanic.DelayedAbility.schedule(user,
                dev.hae.btt.mechanic.DelayedAbility.COLD_WAVE, target)) return;
        ability.setCooldown(RoleDefs.CD_1MIN);
        ability.sync();
    }


    public static void activate(ServerPlayerEntity patient) {
        PlayerState pc = PlayerState.KEY.get(patient);
        pc.coldWaveTicks = PATIENT_TICKS;
        pc.sync();
        for (ServerPlayerEntity listener : patient.getServerWorld().getPlayers()) {
            listener.playSoundToPlayer(dev.hae.btt.registry.Sounds.COLD_WAVE_CONTAMINATION,
                    net.minecraft.sound.SoundCategory.PLAYERS, 1.0F, 1.0F);
            listener.sendMessage(Text.translatable("btt.action.cold_wave.contamination")
                    .withColor(Roles.COLD_WAVE.color()), true);
        }
    }


    public static void tick(ServerPlayerEntity patient, PlayerState pc) {
        if (pc.coldWaveTicks <= 0) return;
        if (!patient.isAlive() || !GameFunctions.isPlayerAliveAndSurvival(patient)
                || --pc.coldWaveTicks <= 0) {
            pc.coldWaveTicks = 0;
            pc.sync();
            return;
        }
        patient.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 20, 0, false, false, true));
        for (ServerPlayerEntity other : patient.getServerWorld().getPlayers()) {
            if (other == patient || !other.isAlive() || !GameFunctions.isPlayerAliveAndSurvival(other)) continue;
            if (other.squaredDistanceTo(patient) > AURA_RANGE * AURA_RANGE) continue;
            PlayerMoodComponent mood = PlayerMoodComponent.KEY.get(other);
            int tasks = dev.hae.btt.game.Needs.count(other);
            if (tasks > 0) mood.setMood(mood.getMood() - 2f * tasks * GameConstants.MOOD_DRAIN);
        }
    }


    public static void onConfirmedDeath(ServerPlayerEntity patient, boolean spawnBody) {
        PlayerState pc = PlayerState.KEY.get(patient);
        if (pc.coldWaveTicks <= 0) return;
        pc.coldWaveTicks = 0;
        pc.sync();
        if (!spawnBody) return;
        var state = dev.hae.btt.component.GameState.KEY.get(patient.getServerWorld());
        String id = patient.getUuidAsString();
        if (!java.util.Arrays.asList(state.coldWaveSources.split(",")).contains(id)) {
            state.coldWaveSources = state.coldWaveSources.isEmpty() ? id : state.coldWaveSources + "," + id;
            state.sync();
        }
    }


    public static void clear(ServerWorld world) {
        var state = dev.hae.btt.component.GameState.KEY.get(world);
        state.coldWaveSources = "";
        state.sync();
    }





    public static void tickCorpseSources(ServerWorld world) {
        if (!GameWorldComponent.KEY.get(world).isRunning()) return;
        dev.hae.btt.component.GameState st = dev.hae.btt.component.GameState.KEY.get(world);
        if (st.coldWaveSources.isEmpty()) return;
        for (String s : st.coldWaveSources.split(",")) {
            if (s.isEmpty()) continue;
            java.util.UUID id;
            try {
                id = java.util.UUID.fromString(s);
            } catch (IllegalArgumentException e) {
                continue;
            }
            for (ServerPlayerEntity player : world.getPlayers()) {
                if (!GameFunctions.isPlayerAliveAndSurvival(player)) continue;
                net.minecraft.util.math.Box box = net.minecraft.util.math.Box.of(player.getPos(),
                        AURA_RANGE * 2, AURA_RANGE * 2, AURA_RANGE * 2);
                boolean near = !world.getEntitiesByClass(dev.doctor4t.wathe.entity.PlayerBodyEntity.class, box,
                        b -> id.equals(b.getPlayerUuid())
                                && player.squaredDistanceTo(b) <= AURA_RANGE * AURA_RANGE).isEmpty();
                if (!near) continue;
                var mood = dev.doctor4t.wathe.cca.PlayerMoodComponent.KEY.get(player);
                mood.setMood(mood.getMood() - 2f * Math.max(1, dev.hae.btt.game.Needs.count(player)) * GameConstants.MOOD_DRAIN);
            }
        }
    }
}
