package dev.hae.btt.game;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import dev.hae.btt.component.LetterState;
import dev.hae.btt.network.LetterC2SPacket;
import dev.hae.btt.role.Roles;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;








public final class LetterNotes {
    private LetterNotes() {}

    public static void apply(ServerPlayerEntity player, LetterC2SPacket payload) {
        if (player == null || payload == null) return;
        ServerWorld world = player.getServerWorld();
        if (world == null || world.getServer() == null) return;
        if (!SeatAssignment.isBttMode(world)) return;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(world);
        if (!gwc.isRunning()) return;
        if (!GameFunctions.isPlayerAliveAndSurvival(player)) return;
        if (!holdsLetter(player)) return;

        if (!gwc.getRoles().containsKey(payload.target())) return;

        if (payload.status() == -2 && payload.target().equals(player.getUuid()) && payload.value().isEmpty()) {
            dev.hae.btt.advancement.BttAdvancements.fact(player, "tickets"); return;
        }
        LetterState state = LetterState.KEY.get(player);
        String uuid = payload.target().toString();


        if (payload.status() >= 0) {
            state.setStatus(uuid, payload.status());
        }

        String value = payload.value();
        if (value == null || value.isEmpty()) return;
        if (LetterC2SPacket.CLEAR.equals(value)) {
            state.setMark(uuid, "");
            return;
        }
        if (Roles.isCampValue(value)) {
            dev.hae.btt.advancement.BttAdvancements.fact(player, "diary");
            state.setMark(uuid, value);
            return;
        }
        Role role = Roles.resolve(value);
        if (role == null) return;
        dev.hae.btt.advancement.BttAdvancements.fact(player, "diary");
        state.setMark(uuid, role.identifier().getPath());
    }


    public static boolean holdsLetter(ServerPlayerEntity player) {
        ItemStack main = player.getMainHandStack();
        if (!main.isEmpty() && main.isOf(WatheItems.LETTER)) return true;
        ItemStack off = player.getOffHandStack();
        return !off.isEmpty() && off.isOf(WatheItems.LETTER);
    }
}
