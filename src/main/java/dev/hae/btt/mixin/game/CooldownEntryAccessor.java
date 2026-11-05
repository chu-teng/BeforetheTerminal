package dev.hae.btt.mixin.game;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(targets = "net.minecraft.entity.player.ItemCooldownManager$Entry")
public interface CooldownEntryAccessor {
    @Accessor("startTick") int btt$start();
    @Accessor("endTick") int btt$end();
    @Mutable @Accessor("startTick") void btt$start(int value);
    @Mutable @Accessor("endTick") void btt$end(int value);
}
