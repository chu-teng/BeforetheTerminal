package dev.hae.btt.ability;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.role.Drunk;
import dev.hae.btt.role.Roles;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.sound.SoundCategory;

import dev.doctor4t.wathe.game.GameConstants;
import net.minecraft.server.network.ServerPlayerEntity;
import dev.hae.btt.component.AbilityState;
import dev.hae.btt.role.Flowers;
import dev.hae.btt.role.Locksmith;


public final class UntargetedAbilities {
    private UntargetedAbilities() {}



    public static void engineer(ServerPlayerEntity user) {
        dev.hae.btt.role.Engineer.use(user, AbilityRules.roleColor(user));
    }



    public static void locksmith(ServerPlayerEntity user) {
        AbilityState ability = AbilityState.KEY.get(user);
        if (ability.cooldown > 0) return;
        if (!Locksmith.repair(user)) return;
        AbilityRules.setCooldown(ability, GameConstants.getInTicks(0, 30));
    }








    public static void professorVial(ServerPlayerEntity user) {
        AbilityState ability = AbilityState.KEY.get(user);
        if (ability.cooldown > 0) return;
        var hit = user.raycast(4.0, 0.0F, false);
        if (!(hit instanceof net.minecraft.util.hit.BlockHitResult bhr)) return;
        if (!(user.getServerWorld().getBlockEntity(bhr.getBlockPos())
                instanceof dev.doctor4t.wathe.block_entity.BeveragePlateBlockEntity plate)) {
            return;
        }
        plate.setPoisoner(user.getUuidAsString());
        user.playSoundToPlayer(net.minecraft.sound.SoundEvents.BLOCK_BREWING_STAND_BREW,
                net.minecraft.sound.SoundCategory.BLOCKS, 0.5F, 1.0F);
        AbilityRules.setCooldown(ability, GameConstants.getInTicks(2, 0));

    }



    public static void gardener(ServerPlayerEntity user) {
        AbilityState ability = AbilityState.KEY.get(user);
        if (ability.cooldown > 0) return;
        boolean planted = Flowers.plant(user);

        if (!planted) return;
        AbilityRules.setCooldown(ability, GameConstants.getInTicks(0, 30));

    }

    public static void stowaway(ServerPlayerEntity user) {
        if (!dev.hae.btt.ability.AbilityRules.canAct(user)) return;
        if (!SeatAssignment.isBttMode(user.getWorld())) return;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(user.getWorld());
        AbilityState ability = AbilityState.KEY.get(user);
        if (!Roles.hasCurrentRole(gwc, user, Roles.STOWAWAY) || ability.cooldown > 0) return;
        ability.setCooldown(GameConstants.getInTicks(2, 0));
        ability.sync();

        if (Drunk.isDrunk(user)) return;
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 30 * 20, 0, true, false, true));
        AbilityRules.recordUse(user, null, Roles.STOWAWAY);
    }

}
