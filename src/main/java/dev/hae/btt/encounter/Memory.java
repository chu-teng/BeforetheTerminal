package dev.hae.btt.encounter;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.registry.BttItems;
import dev.hae.btt.role.Roles;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;


public final class Memory {
    private Memory() {}
    public static boolean isBlind(PlayerEntity player) {
        if (player == null || dev.hae.btt.game.GameModes.modeOf(GameWorldComponent.KEY.get(player.getWorld())) != dev.hae.btt.game.BeforeTheTerminalGameMode.Mode.MEMORY) return false;
        var status = GameWorldComponent.KEY.get(player.getWorld()).getGameStatus();
        return (status == GameWorldComponent.GameStatus.ACTIVE || status == GameWorldComponent.GameStatus.STARTING)
                && !PlayerState.KEY.get(player).memorySolved;
    }
    public static boolean isSlip(ItemStack stack) {
        NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
        return stack.isOf(BttItems.SLIP) && data != null && data.copyNbt().getBoolean("btt_memory");
    }
    public static void restoreSlip(ServerPlayerEntity player) {
        if (!isBlind(player) || !dev.doctor4t.wathe.game.GameFunctions.isPlayerAliveAndSurvival(player)) return;
        for (int slot = 0; slot < player.getInventory().size(); slot++) if (isSlip(player.getInventory().getStack(slot))) return;
        ItemStack stack = new ItemStack(BttItems.SLIP);
        NbtCompound data = new NbtCompound();
        data.putBoolean("btt_memory", true);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(data));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("btt.memory.slip").styled(style -> style.withItalic(false)));
        stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(Text.translatable("btt.memory.goal"))));
        player.getInventory().insertStack(stack);
    }
    public static boolean useSlip(ServerPlayerEntity player, ItemStack stack) {
        if (!isSlip(stack)) return false;
        if (!isBlind(player)) return true;
        var state = PlayerState.KEY.get(player);
        long wait = state.memoryNextGuess - player.getServerWorld().getTime();
        if (wait > 0) {
            showResult(player, Text.translatable("btt.memory.cooldown", (wait + 19) / 20));
            return true;
        }
        return true;
    }
    public static void guess(ServerPlayerEntity player, String roleId) {
        Role guessed = Roles.resolve(roleId);
        if (guessed == null || !dev.doctor4t.wathe.game.GameFunctions.isPlayerAliveAndSurvival(player)
                || (!isSlip(player.getMainHandStack()) && !isSlip(player.getOffHandStack()))) return;
        var state = PlayerState.KEY.get(player);
        if (!isBlind(player) || player.getServerWorld().getTime() < state.memoryNextGuess) return;
        Role selectedRole = Roles.realRole(GameWorldComponent.KEY.get(player.getWorld()), player);
        if (selectedRole == null) return;
        state.memoryNextGuess = player.getServerWorld().getTime() + dev.hae.btt.trait.TraitRules.itemCooldown(player, 30 * 20);
        player.getItemCooldownManager().set(BttItems.SLIP, 30 * 20);
        String result = guessed == selectedRole ? "btt.memory.perfect"
                : Roles.kindOf(guessed) == Roles.kindOf(selectedRole) ? "btt.memory.close" : "btt.memory.unrelated";
        dev.hae.btt.advancement.BttAdvancements.guess(player, guessed == selectedRole);
        if (guessed == selectedRole) dev.hae.btt.advancement.BttAdvancements.fact(player, "memory_perfect");
        if (guessed == selectedRole) {
            state.memorySolved = true;
            dev.hae.btt.component.LetterState.KEY.get(player).markSelf(selectedRole.identifier().getPath());
        }
        state.sync();
        if (state.memorySolved) dev.hae.btt.role.RoleTransitions.reannounce(player,
                GameWorldComponent.KEY.get(player.getWorld()).getRole(player));
        showResult(player, Text.translatable(result));
    }
    public static boolean isGuessFeedback(Text text) {
        return text.getContent() instanceof net.minecraft.text.TranslatableTextContent content
                && java.util.Set.of("btt.memory.perfect", "btt.memory.close", "btt.memory.unrelated", "btt.memory.cooldown").contains(content.getKey());
    }
    private static void showResult(ServerPlayerEntity player, Text text) {
        player.sendMessage(text, true);
    }
}
