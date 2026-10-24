package dev.hae.btt.role;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import dev.hae.btt.component.AbilityState;

import java.util.UUID;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.victory.Endings;
import dev.hae.btt.game.GameEvents;
import dev.hae.btt.network.ActionReceiver;
import dev.hae.btt.registry.Sounds;
import dev.hae.btt.mechanic.DelayedAbility;














public final class Arsonist {
    private Arsonist() {}


    private static final double RANGE_SQUARED = 9.0;

    static final int INITIAL_CD_TICKS = 10 * 20;


    public static int baseCooldownTicks(int playerCount) {

        return GameConstants.getInTicks(0, 10);
    }


    public static void douse(ServerPlayerEntity user, GameWorldComponent gwc) {
        if (!GameFunctions.isPlayerAliveAndSurvival(user)) return;
        AbilityState ability = AbilityState.KEY.get(user);
        if (ability.cooldown > 0) return;

        ServerPlayerEntity target = null;
        double nearest = RANGE_SQUARED;
        for (UUID uuid : gwc.getRoles().keySet()) {
            if (uuid.equals(user.getUuid())) continue;
            if (!(user.getServerWorld().getPlayerByUuid(uuid) instanceof ServerPlayerEntity p)) continue;
            if (!GameFunctions.isPlayerAliveAndSurvival(p)) continue;
            if (PlayerState.KEY.get(p).isDoused()) continue;
            double distance = user.squaredDistanceTo(p);
            if (distance >= nearest) continue;
            if (!user.canSee(p)) continue;
            nearest = distance;
            target = p;
        }

        if (target == null) return;

        if (Goon.interceptTarget(user, target)) {
            setCd(ability, baseCooldownTicks(gwc.getRoles().size()));
            return;
        }
        PlayerState victim = PlayerState.KEY.get(target);
        victim.setDoused();

        victim.scheduleGasolineHint(DelayedAbility.randomDelayTicks(user.getRandom()));

        setCd(ability, baseCooldownTicks(gwc.getRoles().size()));

        target.getWorld().playSound(null, target.getBlockPos(), Sounds.POUR_GASOLINE,
                net.minecraft.sound.SoundCategory.PLAYERS, 1.0F, 1.0F);


        if (allPassengersDoused(user, gwc)) {
            win(user, gwc);
        }
    }


    public static void tickGasoline(ServerPlayerEntity player, PlayerState pc) {
        if (pc.gasolineHintTicks <= 0) return;
        if (--pc.gasolineHintTicks > 0) return;
        if (!GameFunctions.isPlayerAliveAndSurvival(player)) return;

        player.sendMessage(Text.translatable("btt.action.arsonist.smell")
                .withColor(Roles.ARSONIST.color()), true);
    }


    private static boolean allPassengersDoused(ServerPlayerEntity user, GameWorldComponent gwc) {
        for (UUID uuid : gwc.getRoles().keySet()) {
            if (uuid.equals(user.getUuid())) continue;
            if (!(user.getServerWorld().getPlayerByUuid(uuid) instanceof ServerPlayerEntity p)) continue;
            if (!GameFunctions.isPlayerAliveAndSurvival(p)) continue;
            if (!Roles.isPassengerCampFor(gwc, p)) continue;
            if (!PlayerState.KEY.get(p).isDoused()) return false;
        }
        return true;
    }





    private static void win(ServerPlayerEntity user, GameWorldComponent gwc) {
        dev.hae.btt.victory.SoloWin.request(user.getServerWorld(), Endings.Ending.ARSONIST_WIN, user);
    }

    private static void setCd(AbilityState ability, int ticks) {
        ability.setCooldown(ticks);
        ability.sync();
    }
}
