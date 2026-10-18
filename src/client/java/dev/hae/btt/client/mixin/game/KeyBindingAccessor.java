package dev.hae.btt.client.mixin.game;

import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;








@Mixin(KeyBinding.class)
public interface KeyBindingAccessor {
    @Accessor("boundKey")
    net.minecraft.client.util.InputUtil.Key btt$boundKey();
    @Accessor("pressed")
    boolean btt$isPressedRaw();
}
