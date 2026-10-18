package dev.hae.btt.role;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import dev.hae.btt.component.AbilityState;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.victory.Endings;
import dev.hae.btt.mixin.game.KillHookMixin;
import dev.hae.btt.network.ActionReceiver;
import dev.hae.btt.component.ExecutionerState;





public final class Executioner {
    private Executioner() {}


    static final double RANGE = 6.0;

    static final int NEEDED = 2;


    public static void mark(ServerPlayerEntity user, ServerPlayerEntity target) {
        AbilityState ability = AbilityState.KEY.get(user);
        if (ability.cooldown > 0) return;
        if (user == target) return;
        if (user.distanceTo(target) > RANGE) return;
        PlayerState pc = PlayerState.KEY.get(user);
        if (!pc.executionerTarget.isEmpty()) {

            return;
        }
        if (!dev.hae.btt.mechanic.DelayedAbility.schedule(user, dev.hae.btt.mechanic.DelayedAbility.EXECUTIONER_NOTICE, target)) return;
        pc.executionerTarget = target.getUuid().toString();
        pc.sync();
        ability.setCooldown(RoleDefs.CD_1MIN);
        ability.sync();

    }





    public static void notifyTarget(ServerPlayerEntity caster, ServerPlayerEntity target) {
        var game = GameWorldComponent.KEY.get(caster.getWorld());
        if (!Roles.hasCurrentOrPriorRole(game, caster, Roles.EXECUTIONER)
                || !target.getUuidAsString().equals(PlayerState.KEY.get(caster).executionerTarget)) return;
        target.sendMessage(Text.translatable("btt.action.executioner.targeted")
                .formatted(net.minecraft.util.Formatting.GRAY), true);
    }

    public static void onTargetDeath(PlayerEntity victim, PlayerEntity killer, Identifier reason) {
        if (!(victim.getWorld() instanceof ServerWorld world)) return;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(world);
        if (!gwc.isRunning()) return;
        String victimId = victim.getUuid().toString();

        boolean executed = dev.hae.btt.trait.TraitRules.execution(victim, killer, reason);
        for (ServerPlayerEntity p : world.getPlayers()) {
            if (!Roles.hasCurrentOrPriorRole(gwc, p, Roles.EXECUTIONER)) continue;
            PlayerState pc = PlayerState.KEY.get(p);
            if (!victimId.equals(pc.executionerTarget)) continue;
            pc.executionerTarget = "";
            if (executed) pc.executionerExecuted++;
            pc.sync();


            dev.hae.btt.record.GameRecordManager.recordTalentUse(p, "executioner_target", null);
            if (pc.executionerExecuted >= NEEDED) {
                win(p, gwc, world);
                return;
            }

            p.sendMessage(Text.translatable(executed
                            ? "btt.action.executioner.executed"
                            : "btt.action.executioner.lost",
                    victim.getName().getString(), pc.executionerExecuted)
                    .withColor(Roles.EXECUTIONER.color()), true);
        }
    }





    private static void win(ServerPlayerEntity user, GameWorldComponent gwc, ServerWorld world) {
        dev.hae.btt.victory.SoloWin.request(world, Endings.Ending.EXECUTIONER_WIN, user);
    }
}
