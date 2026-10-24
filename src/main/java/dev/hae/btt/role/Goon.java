package dev.hae.btt.role;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.component.PlayerState;
import net.minecraft.server.network.ServerPlayerEntity;

public final class Goon {
    private Goon() {}
    public static boolean intercept(ServerPlayerEntity target, ServerPlayerEntity caster) {
        if (!GameWorldComponent.KEY.get(target.getWorld()).isRunning()
                || !GameFunctions.isPlayerAliveAndSurvival(caster)
                || GameWorldComponent.KEY.get(target.getWorld()).getRole(caster) == null
                || target == caster || Drunk.isDrunk(target) || Drunk.isDrunk(caster)
                || !GameFunctions.isPlayerAliveAndSurvival(target)) return false;
        var state = PlayerState.KEY.get(target);
        long now = target.getServerWorld().getTime();
        boolean ready = now >= state.goonNextChange;
        PlayerState.KEY.get(caster).applyDrunk(ready ? 60 * 20 : 20);
        if (ready) {
            dev.hae.btt.faction.Camps.joinCamp(target, caster);
            state.goonNextChange = now + 60 * 20;
            state.sync();
        }
        return true;
    }
    public static boolean interceptTarget(ServerPlayerEntity caster, ServerPlayerEntity target) {
        if (target == null || target.getWorld() != caster.getWorld()) return false;
        if (Voodoo.isRemnant(target) && GameWorldComponent.KEY.get(caster.getWorld()).getRole(caster) != Roles.VOODOO) return true;
        if (dev.hae.btt.trait.TraitRules.active(target) && target != caster
                && GameFunctions.isPlayerAliveAndSurvival(target)
                && dev.hae.btt.trait.Traits.has(target, dev.hae.btt.trait.Traits.ALLERGIC)
                && !RoleDefs.isNearbySkill(Roles.effectiveRole(GameWorldComponent.KEY.get(caster.getWorld()), caster)))
            target.sendMessage(net.minecraft.text.Text.translatable("btt.trait.allergic.notice").withColor(dev.hae.btt.trait.Traits.ALLERGIC.color()), true);
        var def = RoleDefs.get(Roles.effectiveRole(GameWorldComponent.KEY.get(target.getWorld()), target));
        return def != null && def.intercept(target, caster) || dev.hae.btt.trait.TraitRules.intercept(caster, target);
    }
}
