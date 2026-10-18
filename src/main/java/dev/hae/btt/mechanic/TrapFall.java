package dev.hae.btt.mechanic;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;











public final class TrapFall {

    private TrapFall() {}


    private static final class Fall {
        final ServerWorld world;
        final BlockPos origin;
        final BlockState state;
        final FallingBlockEntity entity;
        int ticks;
        final java.util.UUID owner;

        net.minecraft.util.math.Vec3d lastPos;

        Fall(ServerWorld world, BlockPos origin, BlockState state, FallingBlockEntity entity, java.util.UUID owner) {
            this.owner = owner;
            this.world = world;
            this.origin = origin;
            this.state = state;
            this.entity = entity;
            this.lastPos = entity.getPos();
        }
    }

    private static final List<Fall> ACTIVE = new ArrayList<>();

    private static final int MAX_TICKS = 100;

    private static final Box HIT = new Box(-0.6, 0.0, -0.6, 0.6, 1.0, 0.6);

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> tick());
    }

    public static void start(ServerWorld world, BlockPos origin, BlockState state, java.util.UUID owner) {
        FallingBlockEntity falling = FallingBlockEntity.spawnFromBlock(world, origin, state);
        falling.dropItem = false;
        ACTIVE.add(new Fall(world, origin.toImmutable(), state, falling, owner));
    }

    private static void tick() {
        if (ACTIVE.isEmpty()) return;
        ACTIVE.removeIf(fall -> {
            fall.ticks += 1;


            var now = fall.entity.getPos();
            Box box = new Box(fall.lastPos, now).expand(0.6, 0.0, 0.6);
            fall.lastPos = now;
            for (var player : fall.world.getPlayers()) {
                if (!dev.doctor4t.wathe.game.GameFunctions.isPlayerAliveAndSurvival(player)) continue;
                if (!box.contains(player.getPos())) continue;
                dev.hae.btt.advancement.BttAdvancements.trapDamage(player, fall.owner == null ? null : fall.world.getPlayerByUuid(fall.owner), "lantern");
            }
            boolean done = fall.ticks > MAX_TICKS || fall.entity.isRemoved() || fall.entity.isOnGround();
            if (done) {


                BlockPos landed = fall.entity.getBlockPos();
                if (fall.world.getBlockState(landed).isOf(fall.state.getBlock())) {
                    fall.world.setBlockState(landed, net.minecraft.block.Blocks.AIR.getDefaultState());
                }
                if (!fall.entity.isRemoved()) fall.entity.discard();

                if (fall.world.getBlockState(fall.origin).isAir()) {
                    fall.world.setBlockState(fall.origin, fall.state);
                }
            }
            return done;
        });
    }
}
