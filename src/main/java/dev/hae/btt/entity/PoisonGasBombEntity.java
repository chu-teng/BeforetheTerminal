package dev.hae.btt.entity;

import dev.doctor4t.wathe.index.WatheSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import dev.hae.btt.registry.BttEntities;
import dev.hae.btt.registry.BttItems;





public class PoisonGasBombEntity extends ThrownItemEntity {
    public PoisonGasBombEntity(EntityType<?> type, World world) {
        super(BttEntities.POISON_GAS_BOMB, world);
    }

    @Override
    protected Item getDefaultItem() {
        return BttItems.POISON_GAS_GRENADE;
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (!(this.getWorld() instanceof ServerWorld world)) return;
        PoisonGasCloudEntity cloud = new PoisonGasCloudEntity(BttEntities.POISON_GAS_CLOUD, world);
        cloud.setPos(this.getX(), this.getY(), this.getZ());
        if (this.getOwner() instanceof PlayerEntity owner) cloud.setOwnerUuid(owner.getUuid());
        world.spawnEntity(cloud);

        this.discard();
    }
}
