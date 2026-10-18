package dev.hae.btt.entity;

import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;









public class PoisonGasCloudEntity extends Entity {

    private static final int MAX_LIFETIME = 600;

    private static final int SPREAD_INTERVAL = 5;

    private static final int MAX_GAS_BLOCKS = 400;

    private static final double MAX_RADIUS = 12.0;

    private static final int EXPOSURE_THRESHOLD = 100;

    private static final int POISON_TICKS = 800;
    private static final DustParticleEffect GAS_PARTICLE =
            new DustParticleEffect(new Vector3f(0.72F, 0.16F, 0.14F), 1.5F);

    private final Set<BlockPos> gasBlocks = new HashSet<>();
    private final Map<UUID, Integer> exposure = new HashMap<>();
    private Set<BlockPos> frontier = new HashSet<>();
    private UUID ownerUuid;
    private int age;

    public PoisonGasCloudEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    public void setOwnerUuid(UUID uuid) {
        this.ownerUuid = uuid;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.getWorld() instanceof ServerWorld world)) return;
        this.age++;
        if (this.age > MAX_LIFETIME) {
            this.discard();
            return;
        }
        if (this.age == 1) {
            BlockPos origin = this.getBlockPos();
            this.gasBlocks.add(origin);
            this.frontier.add(origin);
        }
        if (this.age % SPREAD_INTERVAL == 0) this.spread(world);
        if (this.age % 3 == 0) this.spawnCloudParticles(world);
        this.applyPoison(world);
    }


    private void spread(ServerWorld world) {
        if (this.frontier.isEmpty() || this.gasBlocks.size() >= MAX_GAS_BLOCKS) return;
        BlockPos origin = this.getBlockPos();
        Set<BlockPos> next = new HashSet<>();
        for (BlockPos pos : this.frontier) {
            if (this.gasBlocks.size() >= MAX_GAS_BLOCKS) break;
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = pos.offset(direction);
                if (this.gasBlocks.contains(neighbor)) continue;
                if (neighbor.getSquaredDistance(origin) > MAX_RADIUS * MAX_RADIUS) continue;
                BlockState state = world.getBlockState(neighbor);
                if (!state.getCollisionShape(world, neighbor).isEmpty()) continue;
                this.gasBlocks.add(neighbor);
                next.add(neighbor);
            }
        }
        this.frontier = next;
    }


    private void spawnCloudParticles(ServerWorld world) {
        if (this.gasBlocks.isEmpty()) return;
        List<BlockPos> blocks = new ArrayList<>(this.gasBlocks);
        int samples = Math.min(6, blocks.size());
        for (int i = 0; i < samples; i++) {
            BlockPos pos = blocks.get(world.getRandom().nextInt(blocks.size()));
            world.spawnParticles(GAS_PARTICLE, pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5,
                    1, 0.3, 0.3, 0.3, 0.0);
        }
    }


    private void applyPoison(ServerWorld world) {
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (!GameFunctions.isPlayerAliveAndSurvival(player)) continue;


            if (dev.hae.btt.role.Roles.hasCurrentRole(
                    dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(world),
                    player, dev.hae.btt.role.Roles.ALCHEMIST)) {
                this.exposure.remove(player.getUuid());
                continue;
            }
            BlockPos feet = player.getBlockPos();
            BlockPos eye = BlockPos.ofFloored(player.getX(), player.getEyeY(), player.getZ());
            if (!this.gasBlocks.contains(feet) && !this.gasBlocks.contains(eye)) {
                this.exposure.remove(player.getUuid());
                continue;
            }
            int ticks = this.exposure.merge(player.getUuid(), 1, Integer::sum);
            if (ticks != EXPOSURE_THRESHOLD) continue;
            PlayerPoisonComponent poison = PlayerPoisonComponent.KEY.get(player);
            int poisonTicks = poison.poisonTicks > 0
                    ? Math.max(1, poison.poisonTicks - POISON_TICKS)
                    : POISON_TICKS;
            poison.setPoisonTicks(poisonTicks, this.ownerUuid);

            dev.doctor4t.wathe.api.Role ownerRole = this.ownerUuid == null ? null
                    : dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(world).getRole(this.ownerUuid);
        }
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.age = nbt.getInt("Age");
        if (nbt.containsUuid("Owner")) this.ownerUuid = nbt.getUuid("Owner");
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("Age", this.age);
        if (this.ownerUuid != null) nbt.putUuid("Owner", this.ownerUuid);
    }
}
