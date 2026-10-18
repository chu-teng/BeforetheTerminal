package dev.hae.btt.game;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.LoreComponent;
import java.util.*;


final class AdminEquipment {
    private AdminEquipment() {}
    private static final String CONTROL_TAG = "btt_admin";
    static String category(ItemStack stack) {
        NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
        return data == null ? "" : data.copyNbt().getString(CONTROL_TAG);
    }

    static ItemStack controlCompass(String category, Text name) {
        ItemStack stack = new ItemStack(Items.COMPASS);
        NbtCompound data = new NbtCompound();
        data.putString(CONTROL_TAG, category);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(data));
        stack.set(DataComponentTypes.CUSTOM_NAME, name.copy().styled(style -> style.withItalic(false)));
        stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(Text.translatable("btt.admin.next_round"))));
        return stack;
    }

    static void refresh(ServerPlayerEntity player) {
        boolean eligible = ServerSettings.isAdmin(player.getUuid()) && ServerSettings.lobby(player);
        Set<String> present = new HashSet<>();
        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            ItemStack stack = player.getInventory().getStack(slot);
            NbtComponent itemData = stack.get(DataComponentTypes.CUSTOM_DATA);
            if (itemData != null && itemData.copyNbt().getBoolean("btt_admin_armor") && (!eligible || slot != 38)) {
                player.getInventory().setStack(slot, ItemStack.EMPTY); continue;
            }
            String category = category(stack);
            if (category.isEmpty()) continue;
            if (!eligible || !category.equals("mode") || !present.add(category)) {
                player.getInventory().setStack(slot, ItemStack.EMPTY);
            } else player.getInventory().setStack(slot, controlCompass(category, Text.translatable("btt.admin.title")));
        }
        if (eligible && !present.contains("mode")) player.getInventory().insertStack(controlCompass("mode", Text.translatable("btt.admin.title")));
        ItemStack chest = player.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST);
        boolean marked = chest.get(DataComponentTypes.CUSTOM_DATA) != null
                && chest.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("btt_admin_armor");
        if (eligible && !marked) {
            ItemStack armor = new ItemStack(Items.IRON_CHESTPLATE);
            NbtCompound tag = new NbtCompound(); tag.putBoolean("btt_admin_armor", true);
            armor.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(tag));
            player.equipStack(net.minecraft.entity.EquipmentSlot.CHEST, armor);
            if (!chest.isEmpty()) player.getInventory().insertStack(chest);
        } else if (!eligible && marked) player.equipStack(net.minecraft.entity.EquipmentSlot.CHEST, ItemStack.EMPTY);
    }
}
