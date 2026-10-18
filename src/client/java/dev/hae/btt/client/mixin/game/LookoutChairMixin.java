package dev.hae.btt.client.mixin.game;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.block.entity.SeatEntity;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.hae.btt.client.SightGate;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.registry.BttBlocks;
import dev.hae.btt.role.Drunk;
import dev.hae.btt.role.Roles;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;




















@Mixin(value = WatheClient.class, priority = 1500)
public abstract class LookoutChairMixin {


    private static final int LOOKOUT_GREEN = 0x4EDD35;

    @Inject(method = "getInstinctHighlight", at = @At("HEAD"), cancellable = true)
    private static void btt$lookoutChairReveal(Entity target, CallbackInfoReturnable<Integer> cir) {
        if (!(target instanceof PlayerEntity p) || p.isSpectator()) return;
        ClientPlayerEntity viewer = MinecraftClient.getInstance().player;
        if (viewer == null || viewer.getWorld() == null) return;
        if (!SeatAssignment.isBttMode(viewer.getWorld())) return;
        if (!btt$seatedOnLookoutChair(viewer)) return;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(viewer.getWorld());
        if (!gwc.isRunning()) return;
        if (!SightGate.canSee(p)) return;
        if (!dev.hae.btt.component.PlayerState.KEY.get(p).observedLookout) return;
        cir.setReturnValue(LOOKOUT_GREEN);
        cir.cancel();
    }


    private static boolean btt$seatedOnLookoutChair(ClientPlayerEntity viewer) {
        if (!(viewer.getVehicle() instanceof SeatEntity seat)) return false;
        if (viewer.getWorld() == null) return false;
        return viewer.getWorld().getBlockState(seat.getBlockPos()).getBlock() == BttBlocks.LOOKOUT_CHAIR;
    }
}
