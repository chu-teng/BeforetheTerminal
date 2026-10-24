package dev.hae.btt.mechanic;

import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.UUID;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.game.GameEvents;
import dev.hae.btt.role.Arsonist;
import dev.hae.btt.combat.DeathReasons;
import dev.hae.btt.role.Roles;







public final class DelayedAbility {
    private DelayedAbility() {}


    public static final int DELAY_MIN_TICKS = 200;

    public static final int DELAY_SPREAD_TICKS = 401;


    public static int randomDelayTicks(net.minecraft.util.math.random.Random random) {
        return DELAY_MIN_TICKS + random.nextInt(DELAY_SPREAD_TICKS);
    }

    public static final String EXECUTIONER_NOTICE = "executioner_notice";
    public static final String COMEDIAN = "comedian";
    public static final String COLD_WAVE = "cold_wave";

    public static final String ISOLATION = "isolation";

    public static boolean hasPending(ServerPlayerEntity caster) {
        PlayerState pc = PlayerState.KEY.get(caster);
        return (pc.delayedTicks > 0 && !pc.delayedTarget.isEmpty())
                || (pc.delayedTicks2 > 0 && !pc.delayedTarget2.isEmpty());
    }


    public static boolean schedule(ServerPlayerEntity caster, String kind, ServerPlayerEntity target) {
        PlayerState pc = PlayerState.KEY.get(caster);

        if (pc.delayedTicks <= 0 || pc.delayedTarget.isEmpty()) {
            pc.delayedKind = kind;
            pc.delayedTarget = target.getUuid().toString();
            pc.delayedTicks = randomDelayTicks(caster.getRandom());
            return true;
        }
        if (pc.delayedTicks2 <= 0 || pc.delayedTarget2.isEmpty()) {
            pc.delayedKind2 = kind;
            pc.delayedTarget2 = target.getUuid().toString();
            pc.delayedTicks2 = randomDelayTicks(caster.getRandom());
            return true;
        }
        return false;
    }


    public static void tick(ServerPlayerEntity caster, PlayerState pc) {
        if (!caster.isAlive() || !GameFunctions.isPlayerAliveAndSurvival(caster)) {
            clear(pc);
            return;
        }
        tickSlot(caster, pc, true);
        tickSlot(caster, pc, false);
        return;
    }


    private static void tickSlot(ServerPlayerEntity caster, PlayerState pc, boolean first) {
        int ticks = first ? pc.delayedTicks : pc.delayedTicks2;
        if (ticks <= 0) return;
        String kind = first ? pc.delayedKind : pc.delayedKind2;
        String targetUuid = first ? pc.delayedTarget : pc.delayedTarget2;
        if (--ticks > 0) {
            if (first) pc.delayedTicks = ticks; else pc.delayedTicks2 = ticks;
            return;
        }
        if (first) clearSlot1(pc); else clearSlot2(pc);
        ServerPlayerEntity target = null;
        try {
            if (caster.getServerWorld().getPlayerByUuid(UUID.fromString(targetUuid)) instanceof ServerPlayerEntity s) {
                target = s;
            }
        } catch (IllegalArgumentException ignored) {
        }
        if (target == null || !target.isAlive() || !GameFunctions.isPlayerAliveAndSurvival(target)) {
            return;
        }
        if (EXECUTIONER_NOTICE.equals(kind)) dev.hae.btt.role.Executioner.notifyTarget(caster, target);
        else if (COMEDIAN.equals(kind)) applyComedian(caster, target);
        else if (ISOLATION.equals(kind)) applyIsolation(caster, target);
        else if (COLD_WAVE.equals(kind)) dev.hae.btt.role.ColdWave.activate(target);
    }





    private static void applyComedian(ServerPlayerEntity caster, ServerPlayerEntity target) {


        PlayerState targetPc = PlayerState.KEY.get(target);
        targetPc.partyUses++;
        if (targetPc.partyUses >= 2) {
            GameFunctions.killPlayer(target, true, caster, DeathReasons.HELIUM_SELF_DESTRUCT);
            return;
        }

        targetPc.applyVoicePitch(GameConstants.getInTicks(1, 0));
    }





    private static void applyIsolation(ServerPlayerEntity caster, ServerPlayerEntity target) {
        PlayerState.KEY.get(target).applyMute(GameConstants.getInTicks(0, 30));
        dev.hae.btt.component.GameState.KEY.get(target.getWorld()).roleEffects
                .putLong("isolated:" + target.getUuidAsString(), target.getWorld().getTime() + 600);
    }

    private static void clear(PlayerState pc) {
        clearSlot1(pc);
        clearSlot2(pc);
    }

    private static void clearSlot1(PlayerState pc) {
        pc.delayedKind = "";
        pc.delayedTarget = "";
        pc.delayedTicks = 0;
    }

    private static void clearSlot2(PlayerState pc) {
        pc.delayedKind2 = "";
        pc.delayedTarget2 = "";
        pc.delayedTicks2 = 0;
    }
}
