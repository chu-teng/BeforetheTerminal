package dev.hae.btt.mixin.game;

import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.entity.BttArrowMarker;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.combat.DeathReasons;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;



















@Mixin(PersistentProjectileEntity.class)
public abstract class BttArrowMixin implements BttArrowMarker {


    @Unique
    private boolean bttArrow = false;

    @Unique
    private boolean bttFromCrossbow = false;


    @Unique
    private int bttPierce = 0;

    @Override
    public void btt$markArrow(boolean fromCrossbow, int pierce) {
        this.bttArrow = true;
        this.bttFromCrossbow = fromCrossbow;
        this.bttPierce = pierce;
    }

    @Inject(method = "onEntityHit", at = @At("HEAD"), cancellable = true)
    private void btt$arrowHitEntity(EntityHitResult hitResult, CallbackInfo ci) {
        if (!this.bttArrow) return;
        PersistentProjectileEntity self = (PersistentProjectileEntity) (Object) this;
        if (!(hitResult.getEntity() instanceof ServerPlayerEntity target)) return;
        World world = self.getWorld();
        if (!SeatAssignment.isBttMode(world)) return;
        if (!GameFunctions.isPlayerAliveAndSurvival(target)) return;
        Entity owner = self.getOwner();
        if (owner != null && target.getUuid().equals(owner.getUuid())) return;

        ServerPlayerEntity shooter = owner instanceof ServerPlayerEntity sp ? sp : null;
        if (!this.bttFromCrossbow && shooter != null && !shooter.isCreative()) {
            shooter.getItemCooldownManager().set(net.minecraft.item.Items.BOW, 1200);
        }
        GameFunctions.killPlayer(target, true, shooter, DeathReasons.ARROW);
        self.playSound(SoundEvents.ITEM_TRIDENT_HIT, 1.0F, 1.4F);
        if (this.bttFromCrossbow && shooter != null) btt$consumeCrossbow(shooter);
        if (this.bttPierce > 0) {
            this.bttPierce--;
        } else {
            self.discard();
        }
        ci.cancel();
    }

    @Inject(method = "onBlockHit", at = @At("HEAD"), cancellable = true)
    private void btt$arrowHitBlock(BlockHitResult blockHitResult, CallbackInfo ci) {
        if (!this.bttArrow) return;
        PersistentProjectileEntity self = (PersistentProjectileEntity) (Object) this;

        self.playSound(SoundEvents.ITEM_TRIDENT_HIT_GROUND, 0.8F, 1.4F);
        self.discard();
        ci.cancel();
    }


    @Unique
    private static void btt$consumeCrossbow(ServerPlayerEntity shooter) {
        var inventory = shooter.getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.getStack(i).getItem() instanceof CrossbowItem) {
                inventory.getStack(i).decrement(1);
                shooter.currentScreenHandler.syncState();
                return;
            }
        }
    }
}
