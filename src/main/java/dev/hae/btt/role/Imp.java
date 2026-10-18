package dev.hae.btt.role;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import dev.hae.btt.component.AbilityState;

import java.util.UUID;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.game.GameEvents;
import dev.hae.btt.role.RoleTransitions;















public final class Imp {
    private Imp() {}


    public static final int COOLDOWN_TICKS = GameConstants.getInTicks(1, 0);

    private static final double RANGE = 6.0;


    public static void mark(ServerPlayerEntity user, ServerPlayerEntity target) {
        AbilityState ability = AbilityState.KEY.get(user);
        if (ability.cooldown > 0) return;
        if (user.distanceTo(target) > RANGE) return;
        ability.setCooldown(COOLDOWN_TICKS);
        ability.sync();
        PlayerState.KEY.get(user).impMark = target.getUuidAsString();



    }





    public static boolean isNeutral(Role role) {
        if (role == null) return false;
        if (Roles.SOLO_NEUTRALS.contains(role)) return true;
        Roles.Kind kind = Roles.kindOf(role);
        return kind != null && kind.isNeutralKind();
    }


    public static boolean isLoneOrOutsider(Role role) {
        return isNeutral(role);
    }


    public static void succession(ServerPlayerEntity deadImp) {
        PlayerState pc = PlayerState.KEY.get(deadImp);
        if (pc.impMark.isEmpty()) return;
        ServerPlayerEntity successor;
        try {
            successor = deadImp.getServerWorld().getPlayerByUuid(UUID.fromString(pc.impMark)) instanceof ServerPlayerEntity s ? s : null;
        } catch (IllegalArgumentException e) {
            successor = null;
        }
        if (successor == null || !GameFunctions.isPlayerAliveAndSurvival(successor)) return;
        Role role = GameWorldComponent.KEY.get(deadImp.getWorld()).getRole(successor);
        if (!isNeutral(role)) return;
        RoleTransitions.takeOver(successor, Roles.IMP);
        PlayerState.KEY.get(successor).impMark = "";
        successor.sendMessage(Text.translatable("btt.action.imp.succession")
                .withColor(Roles.IMP.color()), true);
    }
}
