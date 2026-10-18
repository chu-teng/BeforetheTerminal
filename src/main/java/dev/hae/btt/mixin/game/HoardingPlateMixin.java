package dev.hae.btt.mixin.game;
import dev.doctor4t.wathe.block.FoodPlatterBlock;
import dev.doctor4t.wathe.block_entity.BeveragePlateBlockEntity;
import dev.hae.btt.trait.Traits;
import dev.hae.btt.trait.TraitRules;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;


@Mixin(FoodPlatterBlock.class)
public abstract class HoardingPlateMixin {
    @ModifyVariable(method = "onUse", at = @At("LOAD"), ordinal = 0)
    private boolean btt$hoarding(boolean blocked, BlockState state, World world, BlockPos pos,
                                  PlayerEntity player, BlockHitResult hit) {
        if (!blocked || !TraitRules.active(player) || !Traits.has(player, Traits.HOARDING)
                || !(world.getBlockEntity(pos) instanceof BeveragePlateBlockEntity plate)) return blocked;
        var items = plate.getStoredItems().stream().map(net.minecraft.item.ItemStack::getItem)
                .collect(java.util.stream.Collectors.toSet());
        int carried = 0;
        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            var stack = player.getInventory().getStack(slot);
            if (items.contains(stack.getItem())) carried += stack.getCount();
        }
        return carried >= 2;
    }
}
