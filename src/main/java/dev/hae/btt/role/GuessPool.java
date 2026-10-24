package dev.hae.btt.role;

import dev.doctor4t.wathe.api.Role;
import dev.hae.btt.game.Scripts;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;






















public final class GuessPool {
    private GuessPool() {}


    public static boolean isPickRole(@Nullable Role ability) {
        return ability == Roles.NOVELIST || ability == Roles.ASSASSIN || ability == Roles.MESSIAH
                || ability == Roles.SCULPTOR || ability == Roles.PHILOSOPHER

                || ability == Roles.BARTENDER || ability == Roles.GAMBLER;
    }


    public static Scripts.Script scriptOf(@Nullable World world) {
        return world == null ? null : Scripts.byId(dev.hae.btt.component.GameState.KEY.get(world).script);
    }


    public static List<Role> candidates(Role ability) {
        return candidates(ability, null);
    }

    public static List<Role> candidates(Role ability, @Nullable Scripts.Script script) {
        if (!isPickRole(ability)) return List.of();
        List<Role> out = new ArrayList<>();
        for (Role role : Roles.allRoles()) {
            if (((ability == Roles.GAMBLER || ability == Roles.PHILOSOPHER) ? Roles.isImplemented(role) : canAppear(role, script)) && matchesAbility(ability, role)) out.add(role);
        }
        return List.copyOf(out);
    }







    public static List<Role> catalogue(@Nullable Scripts.Script script) {
        List<Role> out = new ArrayList<>();
        for (Role role : Roles.allRoles()) {
            if (canAppear(role, script)) out.add(role);
        }
        return List.copyOf(out);
    }


    public static boolean isAllowed(Role ability, @Nullable Role guess) {
        return isAllowed(ability, guess, null);
    }


    public static boolean isAllowed(Role ability, @Nullable Role guess, @Nullable Scripts.Script script) {
        return guess != null && isPickRole(ability)
                && (ability == Roles.PHILOSOPHER ? Roles.isImplemented(guess) : canAppear(guess, script)) && matchesAbility(ability, guess);
    }


    private static boolean canAppear(Role role, @Nullable Scripts.Script script) {
        return script == null || script.allows(role);
    }


    private static boolean matchesAbility(Role ability, Role role) {
        if (ability == Roles.ASSASSIN) {

            return !Roles.isKillerCamp(role);
        }
        if (ability == Roles.SCULPTOR) {

            return Roles.isPassengerCamp(role) || Roles.isKillerCamp(role);
        }
        if (ability == Roles.PHILOSOPHER) {

            return Roles.isPassengerCamp(role);
        }

        return true;
    }
}
