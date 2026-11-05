package dev.hae.btt.mixin.game;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.Map;
@Mixin(ItemCooldownManager.class)
public interface CooldownSnapshotAccessor {
    @Accessor("entries") Map<Item, ?> btt$entries();
    @Accessor("tick") int btt$tick();
}
