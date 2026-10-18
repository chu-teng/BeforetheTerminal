package dev.hae.btt.block_entity;

import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheSounds;
import dev.hae.btt.block.BttTrapBlock;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.registry.BttBlocks;
import dev.hae.btt.mechanic.TrapFall;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;













public class BttTrapBlockEntity extends BlockEntity {


    public static final int ARM_TICKS = GameConstants.getInTicks(0, 5);

    private int armedTicks = 0;
    private java.util.UUID armedBy;

    public BttTrapBlockEntity(BlockPos pos, BlockState state) {
        super(BttBlocks.TRAP_BLOCK_ENTITY, pos, state);
    }

    public void arm(ServerPlayerEntity user) {
        this.armedBy = user.getUuid();
        this.armedTicks = ARM_TICKS;
        markDirty();
    }

    public boolean isArmed() {
        return armedTicks > 0;
    }

    public int getArmedTicks() {
        return armedTicks;
    }


    public String translationKey() {
        return getCachedState().getBlock().getTranslationKey();
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, BttTrapBlockEntity trap) {
        if (trap.armedTicks <= 0) return;
        if (!(state.getBlock() instanceof BttTrapBlock block)) return;
        if (!(world instanceof ServerWorld serverWorld)) return;
        if (!SeatAssignment.isBttMode(world)) {
            trap.armedTicks = 0;
            return;
        }

        if (trap.armedTicks == ARM_TICKS) {


            world.playSound(null, pos, block.kind() == BttTrapBlock.Kind.LANTERN
                            ? dev.hae.btt.registry.Sounds.TRAP_LANTERN
                            : dev.hae.btt.registry.Sounds.TRAP_WIRE,
                    SoundCategory.BLOCKS, 1.2f, 1f);
        }
        trap.armedTicks--;

        if (block.kind() == BttTrapBlock.Kind.WIRE) {
            Box box = new Box(pos).expand(0.75, 0.75, 0.75);
            boolean hit = false;
            for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                if (!box.contains(player.getPos())) continue;
                if (!GameFunctions.isPlayerAliveAndSurvival(player)) continue;
                hit = true;
                dev.hae.btt.advancement.BttAdvancements.trapDamage(player, trap.armedBy == null ? null : serverWorld.getPlayerByUuid(trap.armedBy), "wire");
            }
            if (hit) {
                serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 40, 0.6, 0.6, 0.6, 0.1);
                trap.armedTicks = 0;
            }
        } else {


            Box below = new Box(
                    pos.getX() + 0.5 - 1.25, pos.getY() - 5.0, pos.getZ() + 0.5 - 1.25,
                    pos.getX() + 0.5 + 1.25, pos.getY() + 0.5, pos.getZ() + 0.5 + 1.25);
            for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                if (!below.contains(player.getPos())) continue;
                if (!GameFunctions.isPlayerAliveAndSurvival(player)) continue;


                world.setBlockState(pos, net.minecraft.block.Blocks.AIR.getDefaultState());
                TrapFall.start(serverWorld, pos, state, trap.armedBy);
                trap.armedTicks = 0;
                break;
            }
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putInt("armedTicks", this.armedTicks);
        if (armedBy != null) nbt.putUuid("armedBy", armedBy);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        this.armedTicks = nbt.getInt("armedTicks");
        this.armedBy = nbt.containsUuid("armedBy") ? nbt.getUuid("armedBy") : null;
    }
}
