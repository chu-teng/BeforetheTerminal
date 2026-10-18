package dev.hae.btt.registry;

import dev.hae.btt.BeforeTheTerminal;
import dev.hae.btt.entity.FlyingSwordEntity;
import dev.hae.btt.entity.PoisonGasBombEntity;
import dev.hae.btt.entity.PoisonGasCloudEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;








public final class BttEntities {
    private BttEntities() {}


    public static final EntityType<PoisonGasBombEntity> POISON_GAS_BOMB = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(BeforeTheTerminal.MOD_ID, "poison_gas_bomb"),
            EntityType.Builder.create(PoisonGasBombEntity::new, SpawnGroup.MISC)
                    .dimensions(0.25f, 0.25f).build("poison_gas_bomb")
    );

    public static final EntityType<PoisonGasCloudEntity> POISON_GAS_CLOUD = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(BeforeTheTerminal.MOD_ID, "poison_gas_cloud"),
            EntityType.Builder.create(PoisonGasCloudEntity::new, SpawnGroup.MISC)
                    .dimensions(0.1f, 0.1f).build("poison_gas_cloud")
    );

    public static final EntityType<FlyingSwordEntity> FLYING_SWORD = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(BeforeTheTerminal.MOD_ID, "flying_sword"),
            EntityType.Builder.<FlyingSwordEntity>create(FlyingSwordEntity::new, SpawnGroup.MISC)
                    .dimensions(0.5f, 0.5f).trackingTickInterval(10).build("flying_sword")
    );


    public static final EntityType<dev.hae.btt.entity.BloomEntity> BLOOM = Registry.register(
            Registries.ENTITY_TYPE, Identifier.of(BeforeTheTerminal.MOD_ID, "bloom"),
            EntityType.Builder.create(dev.hae.btt.entity.BloomEntity::new, SpawnGroup.CREATURE)
                    .dimensions(0.6f, 1.95f).build("bloom"));

    public static void init() {
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(BLOOM,
                net.minecraft.entity.mob.ZombieEntity.createZombieAttributes()
                        .add(net.minecraft.entity.attribute.EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.16));
    }
}
