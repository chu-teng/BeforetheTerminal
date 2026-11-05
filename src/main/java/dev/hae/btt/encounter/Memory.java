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
        if (dev.doctor4t.wathe.game.GameFunctions.isPlayerSpectatingOrCreative(player)
                && !dev.hae.btt.role.Voodoo.isRemnant(player)) return false;
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
        int clues = (Roles.kindOf(guessed) == Roles.kindOf(selectedRole) ? 1 : 0)
                + (dev.hae.btt.game.Scripts.ALL.stream().anyMatch(script -> script.allows(guessed) && script.allows(selectedRole)) ? 1 : 0);
        String result = guessed == selectedRole ? "btt.memory.perfect"
                : clues == 2 ? "btt.memory.close" : clues == 1 ? "btt.memory.related" : "btt.memory.unrelated";
        dev.hae.btt.advancement.BttAdvancements.guess(player, guessed == selectedRole);
        if (guessed == selectedRole) dev.hae.btt.advancement.BttAdvancements.fact(player, "memory_perfect");
        if (guessed == selectedRole) {
            state.memorySolved = true;
            player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.GLOWING, 100, 0, false, false));
            dev.hae.btt.component.LetterState.KEY.get(player).markSelf(selectedRole.identifier().getPath());
        }
        state.sync();
        if (state.memorySolved) dev.hae.btt.role.RoleTransitions.reannounce(player,
                GameWorldComponent.KEY.get(player.getWorld()).getRole(player));
        showResult(player, Text.translatable(result));
    }
    public static boolean isGuessFeedback(Text text) {
        return text.getContent() instanceof net.minecraft.text.TranslatableTextContent content
                && java.util.Set.of("btt.memory.perfect", "btt.memory.related", "btt.memory.close", "btt.memory.unrelated", "btt.memory.cooldown").contains(content.getKey());
    }

    public static Text feedback(Text message) {
        if (isGuessFeedback(message)) return message.copy().setStyle(net.minecraft.text.Style.EMPTY.withColor(0xFFFFFF));
        if (!(message.getContent() instanceof net.minecraft.text.TranslatableTextContent content)) return null;
        String key = content.getKey();
        String answer = switch (key) {
            case "btt.action.hunter.success", "btt.action.judge.success", "btt.action.snake_charmer.swap",
                    "btt.action.prophet.same", "btt.action.prophet.found", "btt.action.detective.killed",
                    "btt.action.detective.deduction.success", "btt.action.assassin.hit" -> "btt.memory.yes";
            case "btt.action.hunter.miss", "btt.action.judge.rejected", "btt.action.snake_charmer.wrong",
                    "btt.action.prophet.different", "btt.action.prophet.none", "btt.action.detective.clean",
                    "btt.action.detective.deduction.failed", "btt.action.assassin.fail", "btt.action.assassin.miss",
                    "btt.action.sculptor.present", "btt.action.muyuubyou.interrupted" -> "btt.memory.no";
            default -> null;
        };
        if (answer != null) return Text.translatable(answer).withColor(0xFFFFFF);
        if (!java.util.Set.of("btt.action.investigator.result", "btt.action.engineer.camera.report",
                "btt.action.cannibal.learn", "btt.action.philosopher.become", "btt.action.sculptor.changed",
                "btt.action.gambler.progress", "btt.action.executioner.executed", "btt.action.swapper.swapped",
                "btt.action.warden.cage", "btt.action.harpy.shroud", "btt.action.bomb.countdown",
                "btt.action.folklorist.target", "btt.action.conductor.direction", "btt.action.attendant.direction").contains(key)) return null;
        var out = Text.empty();
        for (Object argument : content.getArgs()) {
            if (!out.getSiblings().isEmpty()) out.append("、");
            out.append(argument instanceof Text text ? text.copy().setStyle(net.minecraft.text.Style.EMPTY) : Text.literal(String.valueOf(argument)));
        }
        return out.withColor(0xFFFFFF);
    }

    private static void showResult(ServerPlayerEntity player, Text text) {
        player.sendMessage(text, true);
    }
}
