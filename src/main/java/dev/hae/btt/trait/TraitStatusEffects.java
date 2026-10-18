package dev.hae.btt.trait;

import dev.hae.btt.component.PlayerState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;


public final class TraitStatusEffects {
    private TraitStatusEffects() {}
    private static final ThreadLocal<Boolean> GRANTING = ThreadLocal.withInitial(() -> false);

    public static void maintain(ServerPlayerEntity player, RegistryEntry<StatusEffect> type, boolean wanted) {
        var state = PlayerState.KEY.get(player);
        boolean owned = owned(state, type);
        if (!wanted) {
            if (owned) player.removeStatusEffect(type);
            setOwned(state, type, false);
            return;
        }
        if (player.hasStatusEffect(type)) return;
        setOwned(state, type, false);
        boolean previous = GRANTING.get();
        GRANTING.set(true);
        try {
            boolean night = type.equals(StatusEffects.NIGHT_VISION);
            boolean applied = player.addStatusEffect(new StatusEffectInstance(type,
                    StatusEffectInstance.INFINITE, 0, !night, false, night));
            setOwned(state, type, applied);
        } finally {
            if (previous) GRANTING.set(true); else GRANTING.remove();
        }
    }

    public static void externalApplication(ServerPlayerEntity player, StatusEffectInstance effect, boolean applied) {
        if (GRANTING.get() || !(applied || effect.isInfinite())) return;
        setOwned(PlayerState.KEY.get(player), effect.getEffectType(), false);
    }


    public static void beforeExternalApplication(ServerPlayerEntity player, StatusEffectInstance effect) {
        if (GRANTING.get()) return;
        var type = effect.getEffectType();
        var state = PlayerState.KEY.get(player);
        var current = player.getStatusEffect(type);
        if (owned(state, type) && current != null && effect.getAmplifier() > current.getAmplifier()) {
            player.removeStatusEffect(type);
            setOwned(state, type, false);
        }
    }

    public static void clear(PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
        maintain(serverPlayer, StatusEffects.SLOW_FALLING, false);
        maintain(serverPlayer, StatusEffects.NIGHT_VISION, false);
    }

    private static boolean owned(PlayerState state, RegistryEntry<StatusEffect> type) {
        return type.equals(StatusEffects.SLOW_FALLING) ? state.bttSlowFalling
                : type.equals(StatusEffects.NIGHT_VISION) && state.bttNightVision;
    }
    private static void setOwned(PlayerState state, RegistryEntry<StatusEffect> type, boolean owned) {
        if (type.equals(StatusEffects.SLOW_FALLING)) state.bttSlowFalling = owned;
        if (type.equals(StatusEffects.NIGHT_VISION)) state.bttNightVision = owned;
    }
}
