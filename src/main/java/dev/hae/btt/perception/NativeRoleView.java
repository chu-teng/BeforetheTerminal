package dev.hae.btt.perception;

import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.role.Voodoo;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.UUID;


public final class NativeRoleView {
    private NativeRoleView() {}

    public static boolean fullObserver(ServerPlayerEntity viewer) {
        return GameFunctions.isPlayerSpectatingOrCreative(viewer) && !Voodoo.isRemnant(viewer);
    }

    public static NbtCompound forPlayer(NbtCompound saved, GameWorldComponent game, ServerPlayerEntity viewer) {
        NbtCompound view = saved.copy();
        if (game.getGameMode() instanceof dev.hae.btt.game.BeforeTheTerminalGameMode) {
            long audible = viewer.getServerWorld().getPlayers().stream().filter(p ->
                    !dev.hae.btt.trait.Traits.has(p, dev.hae.btt.trait.Traits.STEALTH)
                    && dev.doctor4t.wathe.cca.PlayerPsychoComponent.KEY.get(p).getPsychoTicks() > 0).count();
            view.putInt("PsychosActive", (int) audible);
        }
        if (!(game.getGameMode() instanceof dev.hae.btt.game.BeforeTheTerminalGameMode) || fullObserver(viewer)
                || game.getGameStatus() == GameWorldComponent.GameStatus.INACTIVE) return view;
        var own = PlayerState.KEY.get(viewer);
        boolean initializing = game.getGameStatus() == GameWorldComponent.GameStatus.STARTING;
        boolean knowsPartner = "ARCHENEMY".equals(own.relType) || "TWINS".equals(own.relType);
        boolean dream = game.isRole(viewer, dev.hae.btt.role.Roles.ASTRAL);
        NbtList unknown = new NbtList();
        for (var role : WatheRoles.ROLES) {
            NbtList permitted = new NbtList();
            for (var element : saved.getList(role.identifier().toString(), NbtElement.INT_ARRAY_TYPE)) {
                UUID uuid = NbtHelper.toUuid(element);
                boolean knownDream = dream && (uuid.toString().equals(own.astralPassenger)
                        || uuid.toString().equals(own.astralPassenger2) || uuid.toString().equals(own.astralKiller));
                if (!initializing && (uuid.equals(viewer.getUuid()) || knowsPartner && uuid.toString().equals(own.partner) || knownDream))
                    permitted.add(element.copy());
                else unknown.add(element.copy());
            }
            view.put(role.identifier().toString(), permitted);
        }
        var civilians = view.getList(WatheRoles.CIVILIAN.identifier().toString(), NbtElement.INT_ARRAY_TYPE);
        civilians.addAll(unknown);
        view.put(WatheRoles.CIVILIAN.identifier().toString(), civilians);
        return view;
    }
}
