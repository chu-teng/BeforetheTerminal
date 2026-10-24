package dev.hae.btt.role;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import dev.doctor4t.wathe.index.WatheItems;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.util.List;

public final class Damsel {
    private Damsel() {}
    private static final ThreadLocal<Boolean> HUNT_SHOT = ThreadLocal.withInitial(() -> false);

    public static void opening(ServerPlayerEntity damsel, List<Role> assigned) {
        if (Drunk.isDrunk(damsel)) return;
        var game = GameWorldComponent.KEY.get(damsel.getWorld());
        for (var player : damsel.getServerWorld().getPlayers()) {
            if (Roles.kindOfReal(game, player) != Roles.Kind.ACCOMPLICE) continue;
            var gun = new ItemStack(WatheItems.DERRINGER);
            var data = new NbtCompound();
            data.putBoolean("btt_damsel_gun", true);
            gun.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(data));
            gun.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("item.btt.damsel_gun").styled(style -> style.withItalic(false)));
            gun.set(DataComponentTypes.LORE, new net.minecraft.component.type.LoreComponent(
                    List.of(Text.translatable("item.btt.damsel_gun.desc").formatted(net.minecraft.util.Formatting.GRAY))));
            player.giveItemStack(gun);
        }
    }

    public static boolean specialGun(ItemStack stack) {
        var data = stack.get(DataComponentTypes.CUSTOM_DATA);
        return stack.isOf(WatheItems.DERRINGER) && data != null && data.copyNbt().getBoolean("btt_damsel_gun");
    }

    public static void fire(net.minecraft.entity.player.PlayerEntity victim,
                            net.minecraft.entity.player.PlayerEntity killer, Runnable original) {
        if (!(killer instanceof ServerPlayerEntity shooter) || !specialGun(shooter.getMainHandStack())) {
            original.run();
            return;
        }
        var game = GameWorldComponent.KEY.get(victim.getWorld());
        if (game.getRole(victim) != Roles.DAMSEL) return;
        hit(true, original);
    }

    private static void hit(boolean hunting, Runnable kill) {
        boolean previous = HUNT_SHOT.get();
        HUNT_SHOT.set(hunting);
        try { kill.run(); } finally {
            if (previous) HUNT_SHOT.set(true); else HUNT_SHOT.remove();
        }
    }

    public static void died(ServerPlayerEntity victim, ServerPlayerEntity killer, Identifier reason) {
        if (!HUNT_SHOT.get() || killer == null || !dev.doctor4t.wathe.game.GameConstants.DeathReasons.GUN.equals(reason)
                || Drunk.isDrunk(victim)) return;
        var game = GameWorldComponent.KEY.get(victim.getWorld());
        if (Roles.kindOfReal(game, killer) == Roles.Kind.ACCOMPLICE) {
            dev.hae.btt.advancement.BttAdvancements.fact(killer, "damsel_found");
            dev.hae.btt.victory.EndingPipeline.factionLoss(victim);
        }
    }
}
