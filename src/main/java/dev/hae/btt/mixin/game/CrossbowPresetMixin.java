package dev.hae.btt.mixin.game;

import dev.hae.btt.game.SeatAssignment;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Unit;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;











@Mixin(Item.class)
public abstract class CrossbowPresetMixin {

    @Inject(method = "inventoryTick", at = @At("TAIL"))
    private void btt$crossbowPreset(ItemStack stack, World world, Entity entity, int slot, boolean selected, CallbackInfo ci) {
        if (!((Object) this instanceof CrossbowItem)) return;
        if (!SeatAssignment.isBttMode(world)) return;
        if (stack.contains(DataComponentTypes.LORE)) return;
        var registry = world.getRegistryManager().get(RegistryKeys.ENCHANTMENT);
        if (registry != null) {
            registry.getEntry(Enchantments.MULTISHOT).ifPresent(entry -> stack.addEnchantment(entry, 1));
            registry.getEntry(Enchantments.PIERCING).ifPresent(entry -> stack.addEnchantment(entry, 4));
        }
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, false);
        stack.set(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.translatable("item.btt.crossbow.desc1").setStyle(Style.EMPTY.withItalic(false).withColor(0xAAAAAA)),
                Text.translatable("item.btt.crossbow.desc2").setStyle(Style.EMPTY.withItalic(false).withColor(0xAAAAAA)))));
    }
}