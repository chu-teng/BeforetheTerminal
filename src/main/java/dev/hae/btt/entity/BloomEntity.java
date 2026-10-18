package dev.hae.btt.entity;

import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.registry.Sounds;
import dev.hae.btt.combat.DeathReasons;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import java.util.UUID;


public final class BloomEntity extends ZombieEntity {
    private UUID gardener;
    public BloomEntity(EntityType<? extends ZombieEntity> type, World world) {
        super(type, world);
        setBaby(true);
        setPersistent();
        equipStack(EquipmentSlot.HEAD, new ItemStack(Items.LILY_OF_THE_VALLEY));
        setSilent(true);
    }
    public void setGardener(UUID owner) { gardener = owner; }
    @Override protected void initGoals() {
        goalSelector.add(1, new MeleeAttackGoal(this, 0.6, false));
        targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, 10, true, false,
                target -> target instanceof ServerPlayerEntity player && !player.getUuid().equals(gardener)
                        && GameFunctions.isPlayerAliveAndSurvival(player)));
    }
    @Override public boolean tryAttack(Entity target) {
        if (!(target instanceof ServerPlayerEntity player) || player.getUuid().equals(gardener)
                || !GameFunctions.isPlayerAliveAndSurvival(player) || !SeatAssignment.isBttMode(getWorld())) return false;
        var owner = gardener == null ? null : getWorld().getPlayerByUuid(gardener);
        getWorld().playSound(null, getBlockPos(), Sounds.FLOWER_BLOOM, net.minecraft.sound.SoundCategory.BLOCKS, 1, 1);
        GameFunctions.killPlayer(player, true, owner, DeathReasons.BLOOM);
        return true;
    }
    @Override public boolean damage(DamageSource source, float amount) {
        if (getWorld().isClient || !(source.getAttacker() instanceof PlayerEntity)) return false;
        discard(); return true;
    }
    @Override protected boolean burnsInDaylight() { return false; }
    @Override protected boolean canConvertInWater() { return false; }
    @Override protected boolean shouldDropLoot() { return false; }
    @Override public void writeCustomDataToNbt(NbtCompound tag) {
        super.writeCustomDataToNbt(tag);
        if (gardener != null) tag.putUuid("Gardener", gardener);
    }
    @Override public void readCustomDataFromNbt(NbtCompound tag) {
        super.readCustomDataFromNbt(tag);
        gardener = tag.containsUuid("Gardener") ? tag.getUuid("Gardener") : null;
    }
}
