package dev.hae.btt.entity;

import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.registry.BttItems;
import dev.hae.btt.combat.DeathReasons;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;











public class FlyingSwordEntity extends PersistentProjectileEntity {

    private static final TrackedData<Byte> DATA_HIT_DIRECTION =
            DataTracker.registerData(FlyingSwordEntity.class, TrackedDataHandlerRegistry.BYTE);


    private static final int MAX_LIFETIME = 20 * 120;

    @Nullable
    private BlockPos stuckBlockPos = null;
    @Nullable
    private Direction stuckDirection = null;
    private int ticksAlive = 0;
    private boolean isStuck = false;

    private final Set<Integer> hitEntities = new HashSet<>();

    public FlyingSwordEntity(EntityType<? extends FlyingSwordEntity> entityType, World world) {
        super(entityType, world);
        this.pickupType = PickupPermission.DISALLOWED;
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        return new ItemStack(BttItems.SWORD);
    }


    public void setSwordStack(ItemStack stack) {
        this.setStack(stack);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(DATA_HIT_DIRECTION, (byte) 0);
    }

    @Override
    public void tick() {
        this.ticksAlive++;
        if (this.ticksAlive > MAX_LIFETIME) {
            this.discard();
            return;
        }


        if (this.isStuck) {
            if (!this.getWorld().isClient && this.stuckBlockPos != null
                    && this.getWorld().getBlockState(this.stuckBlockPos).isAir()) {
                this.discard();
            }
            return;
        }


        if (!this.getWorld().isClient && SeatAssignment.isBttMode(this.getWorld())) {
            Vec3d currentPos = this.getPos();
            Vec3d velocity = this.getVelocity();
            Vec3d nextPos = currentPos.add(velocity);

            Box searchBox = this.getBoundingBox().stretch(velocity).expand(1.0);
            for (Entity entity : this.getWorld().getOtherEntities(this, searchBox)) {
                if (!(entity instanceof ServerPlayerEntity target)) continue;
                if (!GameFunctions.isPlayerAliveAndSurvival(target)) continue;
                if (this.hitEntities.contains(target.getId())) continue;
                if (target.getUuid().equals(this.getOwner() != null ? this.getOwner().getUuid() : null)) continue;

                Box entityBox = target.getBoundingBox().expand(target.getTargetingMargin());
                if (entityBox.raycast(currentPos, nextPos).isEmpty()) continue;

                this.hitEntities.add(target.getId());
                this.onEntityHit(new EntityHitResult(target));
            }
        }

        super.tick();
    }


    @Nullable
    @Override
    protected EntityHitResult getEntityCollision(Vec3d currentPosition, Vec3d nextPosition) {
        return null;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        if (!(entityHitResult.getEntity() instanceof ServerPlayerEntity target)) return;
        if (!SeatAssignment.isBttMode(this.getWorld())) return;
        if (!GameFunctions.isPlayerAliveAndSurvival(target)) return;
        Entity owner = this.getOwner();
        if (owner != null && target.getUuid().equals(owner.getUuid())) return;

        ServerPlayerEntity killer = owner instanceof ServerPlayerEntity sp ? sp : null;
        GameFunctions.killPlayer(target, true, killer, DeathReasons.FLYING_SWORD);

        this.playSound(SoundEvents.ITEM_TRIDENT_HIT, 1.0F, 1.0F);
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        this.stuckBlockPos = blockHitResult.getBlockPos();
        this.stuckDirection = blockHitResult.getSide();
        this.dataTracker.set(DATA_HIT_DIRECTION, (byte) this.stuckDirection.getId());

        Vec3d hitLocation = blockHitResult.getPos();
        this.setPosition(hitLocation.x, hitLocation.y, hitLocation.z);
        this.setVelocity(Vec3d.ZERO);
        this.isStuck = true;
        this.playSound(SoundEvents.ITEM_TRIDENT_HIT_GROUND, 1.0F, 1.0F);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Age", this.ticksAlive);
        nbt.putBoolean("IsStuck", this.isStuck);
        if (this.stuckDirection != null) nbt.putByte("HitDirection", (byte) this.stuckDirection.getId());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.ticksAlive = nbt.getInt("Age");
        this.isStuck = nbt.getBoolean("IsStuck");
        if (nbt.contains("HitDirection")) {
            this.stuckDirection = Direction.byId(nbt.getByte("HitDirection"));
            this.dataTracker.set(DATA_HIT_DIRECTION, nbt.getByte("HitDirection"));
        }
    }

    @Override
    public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
        return true;
    }

    public int getTicksAlive() {
        return this.ticksAlive;
    }

    public boolean isStuckInBlock() {
        return this.isStuck || this.stuckBlockPos != null;
    }

    public Direction getHitDirection() {
        if (this.stuckDirection != null) return this.stuckDirection;
        return Direction.byId(this.dataTracker.get(DATA_HIT_DIRECTION));
    }

    @Override
    protected boolean tryPickup(PlayerEntity player) {
        return false;
    }

    @Override
    public void onPlayerCollision(PlayerEntity player) {

    }
}
