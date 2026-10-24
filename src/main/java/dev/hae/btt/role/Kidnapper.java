package dev.hae.btt.role;

import dev.hae.btt.combat.DeathReasons;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import dev.hae.btt.component.AbilityState;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.mechanic.Attachment;
import dev.hae.btt.voice.VoiceChatPlugin;
















public final class Kidnapper {
    private Kidnapper() {}

    public static final int COOLDOWN_TICKS = GameConstants.getInTicks(1, 0);
    private static final double RANGE_SQUARED = 36.0;

    public static void swallow(ServerPlayerEntity user, ServerPlayerEntity target, PlayerState targetPc) {
        if (!GameFunctions.isPlayerAliveAndSurvival(user) || !GameFunctions.isPlayerAliveAndSurvival(target)) return;
        if (targetPc.isSwallowed() || targetPc.isAttached()) return;
        AbilityState ability = AbilityState.KEY.get(user);
        if (ability.cooldown > 0) return;
        if (user.squaredDistanceTo(target) > RANGE_SQUARED || !user.canSee(target)) return;
        ability.setCooldown(COOLDOWN_TICKS);
        ability.sync();

        attachVictim(user, target, true);
    }


    public static void attachVictim(ServerPlayerEntity user, ServerPlayerEntity target, boolean privateVoice) {
        PlayerState targetPc = PlayerState.KEY.get(target);
        GameWorldComponent gwc = GameWorldComponent.KEY.get(user.getWorld());
        boolean passenger = Roles.isPassengerCampFor(gwc, target);
        PlayerMoodComponent mood = PlayerMoodComponent.KEY.get(target);

        float drain = Math.max(1, dev.hae.btt.game.Needs.count(target)) * GameConstants.MOOD_DRAIN;
        targetPc.beginDigestion(user.getUuidAsString(), mood.getMood(), drain, passenger);
        Attachment.attach(target, user, Attachment.KIND_KIDNAP);
        target.changeGameMode(GameMode.SPECTATOR);
        if (privateVoice) VoiceChatPlugin.joinStomach(user, target);
        else VoiceChatPlugin.leaveStomach(target);

        if (PlayerState.KEY.get(target).blackdeathPatient) BlackDeath.transferTo(user, target);

    }


    public static void tick(ServerPlayerEntity victim, PlayerState pc) {
        if (!pc.isSwallowed()) return;
        ServerPlayerEntity kidnapper = Attachment.hostOf(victim, victim.getServerWorld());
        Attachment.tick(victim, victim.getServerWorld());
        if (kidnapper == null || !GameFunctions.isPlayerAliveAndSurvival(kidnapper)) {
            release(victim, pc, kidnapper);
            return;
        }
        if (!pc.smugglerHost.isEmpty()) return;
        if (!pc.digestible) return;



        if (pc.digestTicks % 10 == 0) pc.sync();
        if (pc.digestTicks > 0 && --pc.digestTicks > 0) return;
        digest(victim, pc, kidnapper);
    }





    private static void digest(ServerPlayerEntity victim, PlayerState pc, ServerPlayerEntity kidnapper) {
        if (!GameFunctions.isPlayerAliveAndSurvival(victim)) {
            victim.changeGameMode(GameMode.ADVENTURE);
            victim.teleport(kidnapper.getServerWorld(), kidnapper.getX(), kidnapper.getY() + 1, kidnapper.getZ(),
                    victim.getYaw(), victim.getPitch());
        }
        Attachment.detach(victim);
        VoiceChatPlugin.leaveStomach(victim);
        PlayerMoodComponent.KEY.get(victim).setMood(0f);
        pc.clearDigestion();
        GameFunctions.killPlayer(victim, false, kidnapper, DeathReasons.DIGESTED);
    }


    public static void release(ServerPlayerEntity victim, PlayerState pc, ServerPlayerEntity kidnapper) {
        if (!pc.smugglerHost.isEmpty()) { Smuggler.release(victim, kidnapper); return; }
        releaseWithMood(victim, pc, kidnapper, Math.max(0f, pc.digestTicks * pc.digestDrain));
    }

    public static void releaseWithMood(ServerPlayerEntity victim, PlayerState pc, ServerPlayerEntity kidnapper, float remainingMood) {
        pc.clearDigestion();
        Attachment.detach(victim);
        VoiceChatPlugin.leaveStomach(victim);
        if (!GameFunctions.isPlayerAliveAndSurvival(victim)) {
            victim.changeGameMode(GameMode.ADVENTURE);
        }
        if (kidnapper != null && GameFunctions.isPlayerAliveAndSurvival(kidnapper)) {
            victim.teleport(kidnapper.getServerWorld(), kidnapper.getX(), kidnapper.getY() + 1, kidnapper.getZ(),
                    victim.getYaw(), victim.getPitch());
        }
        PlayerMoodComponent.KEY.get(victim).setMood(remainingMood);
    }


    public static void releaseAll(ServerWorld world) {
        for (ServerPlayerEntity p : world.getPlayers()) {
            PlayerState pc = PlayerState.KEY.get(p);
            if (pc.isSwallowed()) release(p, pc, Attachment.hostOf(p, world));
        }
    }

    public static boolean isSwallowed(ServerPlayerEntity player) {
        return PlayerState.KEY.get(player).isSwallowed();
    }
}
