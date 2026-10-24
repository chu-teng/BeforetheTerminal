package dev.hae.btt.mixin.game;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameRoundEndComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.role.RoleAnnouncements;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;








@Mixin(GameRoundEndComponent.class)
public abstract class RoundEndRoleMixin {

    @Shadow @Final private World world;
    @Shadow @Final @Mutable private List<GameRoundEndComponent.RoundEndData> players;

    @Inject(method = "setRoundEndData", at = @At("TAIL"))
    private void bttPerRoleData(List<ServerPlayerEntity> playersArg, GameFunctions.WinStatus winStatus, CallbackInfo ci) {
        if (!SeatAssignment.isBttMode(world)) {
            dev.hae.btt.component.GameState.KEY.get(world).roundResultBtt = false;
            dev.hae.btt.component.GameState.KEY.get(world).sync();
            return;
        }
        List<GameRoundEndComponent.RoundEndData> rebuilt = new ArrayList<>();
        boolean changed = false;
        for (GameRoundEndComponent.RoundEndData d : this.players) {


            Role display = btt$initialRole(d.player().getId());
            var entry = display == null ? null : RoleAnnouncements.get(display.identifier().getPath());
            if (entry != null && entry != d.role()) {
                rebuilt.add(new GameRoundEndComponent.RoundEndData(d.player(), entry, d.wasDead()));
                changed = true;
            } else {
                rebuilt.add(d);
            }
        }
        if (changed) {
            this.players.clear();
            this.players.addAll(rebuilt);
            ((GameRoundEndComponent) (Object) this).sync();
        }
    }


    private Role btt$initialRole(java.util.UUID uuid) {
        Role current = GameWorldComponent.KEY.get(world).getRole(uuid);
        net.minecraft.entity.player.PlayerEntity p = world.getPlayerByUuid(uuid);
        if (p != null) {
            dev.hae.btt.component.PlayerState pc = dev.hae.btt.component.PlayerState.KEY.get(p);
            if (pc != null && pc.initialRole != null && !pc.initialRole.isEmpty()) {
                Role first = dev.hae.btt.role.Roles.byId(pc.initialRole);
                if (first != null) return first;
            }
        }
        return current;
    }

    @Inject(method = "didWin(Ljava/util/UUID;)Z", at = @At("HEAD"), cancellable = true)
    private void bttDidWin(UUID uuid, CallbackInfoReturnable<Boolean> cir) {
        if (!SeatAssignment.isBttMode(world) && !dev.hae.btt.component.GameState.KEY.get(world).roundResultBtt) return;
        String winners = dev.hae.btt.component.GameState.KEY.get(world).winners;
        cir.setReturnValue(Arrays.stream(winners.split(",")).anyMatch(uuid.toString()::equals));
    }
}
