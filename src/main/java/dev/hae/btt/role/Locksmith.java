package dev.hae.btt.role;

import dev.doctor4t.wathe.block.SmallDoorBlock;
import dev.doctor4t.wathe.block_entity.DoorBlockEntity;
import dev.doctor4t.wathe.block_entity.SmallDoorBlockEntity;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheSounds;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.network.DoorHighlightS2CPacket;








public final class Locksmith {
    private Locksmith() {}


    private static final double RANGE = 4.0;


    public static void onDoorTampered(DoorBlockEntity door) {
        if (!(door.getWorld() instanceof ServerWorld world)) return;
        if (!SeatAssignment.isBttMode(world)) return;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(world);
        if (!gwc.isRunning()) return;
        boolean blasted = door.isBlasted();
        var pos = door.getPos();
        for (ServerPlayerEntity locksmith : world.getPlayers()) {
            if (!gwc.isRole(locksmith, Roles.LOCKSMITH)) continue;
            if (!GameFunctions.isPlayerAliveAndSurvival(locksmith)) continue;

            locksmith.sendMessage(Text.translatable(blasted
                            ? "btt.action.locksmith.blasted" : "btt.action.locksmith.jammed")
                    .withColor(Roles.LOCKSMITH.color()), true);
            ServerPlayNetworking.send(locksmith, new DoorHighlightS2CPacket(pos, blasted));
        }
    }





    public static boolean repair(ServerPlayerEntity user) {
        if (!(user.getWorld() instanceof ServerWorld world)) return false;
        if (!(user.raycast(RANGE, 1.0F, false) instanceof BlockHitResult hit)) return false;
        DoorBlockEntity door = Architect.doorAt(world, hit.getBlockPos());
        if (door == null) return false;
        if (door.isBlasted() || door.isJammed()) {
            restop(world, door);

            BlockState state = world.getBlockState(door.getPos());
            if (door instanceof SmallDoorBlockEntity && state.getBlock() instanceof SmallDoorBlock) {
                SmallDoorBlockEntity neighbor = SmallDoorBlock.getNeighborDoorEntity(state, world, door.getPos());
                if (neighbor != null && (neighbor.isBlasted() || neighbor.isJammed())) {
                    restop(world, neighbor);
                }
            }
            world.playSound(null, door.getPos(), WatheSounds.BLOCK_DOOR_TOGGLE, SoundCategory.BLOCKS, 1f, 1.2f);
            return true;
        }

        door.setJammed(GameConstants.JAMMED_DOOR_TIME);
        door.sync();
        world.playSound(null, door.getPos(), WatheSounds.BLOCK_DOOR_TOGGLE, SoundCategory.BLOCKS, 1f, 0.6f);
        return true;
    }


    private static void restop(ServerWorld world, DoorBlockEntity door) {
        if (door.isBlasted()) {
            door.setBlasted(false);
            if (door.isOpen()) door.toggle(false);
        }
        if (door.isJammed()) {
            door.setJammed(0);
        }
        door.sync();
    }
}