package dev.hae.btt.map;

import dev.hae.btt.component.MapVoteState;
import dev.hae.btt.game.BeforeTheTerminalGameMode;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.List;














public final class Rooms {
    private Rooms() {}


    public static List<BttRoom> activeFor(ServerWorld world) {
        MapVoteState st = MapVoteState.KEY.get(world);
        if (!st.rooms.isEmpty()) return st.rooms;
        ServerWorld overworld = world.getServer() == null ? null : world.getServer().getOverworld();
        if (overworld != null && overworld != world) {
            List<BttRoom> main = MapVoteState.KEY.get(overworld).rooms;
            if (!main.isEmpty()) return main;
        }
        return List.of();
    }







    public static boolean distribute(ServerWorld world, List<ServerPlayerEntity> players,
                                     java.util.Map<java.util.UUID, dev.doctor4t.wathe.api.Role> seats) {
        if (players.isEmpty()) return false;
        List<BttRoom> rooms = activeFor(world);
        if (rooms.isEmpty()) {

            assignLetterNumbers(world, players, java.util.Map.of());
            return false;
        }


        assignLetterNumbers(world, players, java.util.Map.of());
        java.util.List<BttRoom> available = new java.util.ArrayList<>();
        for (BttRoom room : rooms) if (!room.spawnPoints().isEmpty()) available.add(room);
        if (available.isEmpty()) return false;
        java.util.Collections.shuffle(available);
        java.util.List<ServerPlayerEntity> shuffled = new java.util.ArrayList<>(players);
        java.util.Collections.shuffle(shuffled);
        java.util.Map<java.util.UUID, Integer> roomOf = new java.util.HashMap<>();
        boolean[] killerSeats = new boolean[shuffled.size()];
        for (int i = 0; i < shuffled.size(); i++)
            killerSeats[i] = dev.hae.btt.role.Roles.isKillerSeat(seats.get(shuffled.get(i).getUuid()));
        int[] assigned = planRoomIndices(available, killerSeats,
                false);
        int[] occupancy = new int[available.size()];
        for (int i = 0; i < shuffled.size(); i++) {
            BttRoom room = available.get(assigned[i]);
            teleport(shuffled.get(i), world, room.spawnPoints().get(occupancy[assigned[i]]++ % room.spawnPoints().size()));
            roomOf.put(shuffled.get(i).getUuid(), rooms.indexOf(room));
        }

        BeforeTheTerminalGameMode.LOGGER.info("[BTT] rooms: distributed {} player(s) across {} room(s)",
                players.size(), rooms.size());



        for (ServerPlayerEntity p : players) {
            Integer idx = roomOf.get(p.getUuid());
            if (idx != null) grantRoomKey(p, rooms.get(idx));
        }
        return true;
    }


    public static int[] planRoomIndices(List<BttRoom> available, boolean[] killerSeats, boolean whirlpool) {
        if (available.isEmpty()) throw new IllegalArgumentException("No rooms with spawn points");
        int[] assigned = new int[killerSeats.length];
        java.util.Arrays.fill(assigned, -1);
        int[] occupancy = new int[available.size()];
        if (whirlpool && available.size() >= 2) {
            int killers = 0;
            for (boolean killer : killerSeats) if (killer) killers++;
            int limit = killers == killerSeats.length ? available.size() : available.size() - 1;
            int index = 0;
            for (int i = 0; i < killerSeats.length; i++) {
                if (!killerSeats[i]) continue;
                while (index < limit - 1 && occupancy[index] >= available.get(index).capacity()) index++;
                assigned[i] = index;
                occupancy[index]++;
            }
        }
        boolean[] reserved = new boolean[available.size()];
        for (int i = 0; i < reserved.length; i++) reserved[i] = occupancy[i] > 0;
        for (int player = 0; player < assigned.length; player++) {
            if (assigned[player] >= 0) continue;
            int selected = -1;
            for (int i = 0; i < available.size(); i++) {
                if (reserved[i] || occupancy[i] >= available.get(i).capacity()) continue;
                if (selected < 0 || occupancy[i] < occupancy[selected]) selected = i;
            }

            if (selected < 0) {
                for (int i = 0; i < available.size(); i++)
                    if (!reserved[i] && (selected < 0 || occupancy[i] < occupancy[selected])) selected = i;
            }
            assigned[player] = selected;
            occupancy[selected]++;
        }
        return assigned;
    }







    private static void assignLetterNumbers(ServerWorld world, List<ServerPlayerEntity> players,
                                            java.util.Map<java.util.UUID, Integer> roomOf) {
        java.util.List<ServerPlayerEntity> ordered = new java.util.ArrayList<>(players);
        ordered.sort(java.util.Comparator
                .comparingInt((ServerPlayerEntity p) -> roomOf.getOrDefault(p.getUuid(), Integer.MAX_VALUE))
                .thenComparing(p -> p.getGameProfile().getName(), String.CASE_INSENSITIVE_ORDER));
        StringBuilder csv = new StringBuilder();
        for (int i = 0; i < ordered.size(); i++) {
            if (i > 0) csv.append(',');
            csv.append(ordered.get(i).getUuidAsString()).append(':').append(i + 1);
        }
        dev.hae.btt.component.GameState state = dev.hae.btt.component.GameState.KEY.get(world);
        state.letterNumbers = csv.toString();
        state.sync();
        BeforeTheTerminalGameMode.LOGGER.info("[BTT] letter numbers assigned for {} player(s)", ordered.size());
    }









    private static void grantRoomKey(ServerPlayerEntity player, BttRoom room) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            if (player.getInventory().getStack(i).isOf(dev.doctor4t.wathe.index.WatheItems.KEY)) {
                player.getInventory().setStack(i, net.minecraft.item.ItemStack.EMPTY);
            }
        }
        net.minecraft.item.ItemStack key =
                new net.minecraft.item.ItemStack(dev.doctor4t.wathe.index.WatheItems.KEY);
        key.apply(net.minecraft.component.DataComponentTypes.LORE,
                net.minecraft.component.type.LoreComponent.DEFAULT,
                c -> new net.minecraft.component.type.LoreComponent(
                        net.minecraft.text.Text.literal(room.name())
                                .getWithStyle(net.minecraft.text.Style.EMPTY
                                        .withItalic(false).withColor(0xFF8C00))));
        player.giveItemStack(key);
    }


    public static void teleport(ServerPlayerEntity player, BttRoom.Spawn spawn) {
        teleport(player, player.getServerWorld(), spawn);
    }


    public static void teleport(ServerPlayerEntity player, ServerWorld target, BttRoom.Spawn spawn) {
        player.teleport(target, spawn.x(), spawn.y(), spawn.z(), spawn.yaw(), spawn.pitch());
    }
}
