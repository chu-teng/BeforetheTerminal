package dev.hae.btt.client.util;

import dev.hae.btt.BeforeTheTerminal;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.MinecraftClient;
import dev.doctor4t.wathe.game.GameConstants;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;










public final class BttItemTooltips {

    private BttItemTooltips() {}

    private static final int COOLDOWN_COLOR = 0xC90000;

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;
            Identifier id = Registries.ITEM.getId(stack.getItem());
            boolean bttItem = BeforeTheTerminal.MOD_ID.equals(id.getNamespace());

            boolean bttBow = stack.isOf(net.minecraft.item.Items.BOW);
            if (!bttItem && !bttBow) return;
            if (bttBow) lines.add(Text.translatable("item.btt.bow.tooltip").withColor(0x808080));





            int regularFrom = lines.isEmpty() ? 0 : 1;
            ItemCooldownManager cooldowns = client.player.getItemCooldownManager();
            if (cooldowns.isCoolingDown(stack.getItem())) {


                float remaining = cooldowns.getCooldownProgress(stack.getItem(), 0f);
                int total = GameConstants.ITEM_COOLDOWNS.getOrDefault(stack.getItem(), GameConstants.getInTicks(1, 0));
                int timeLeft = Math.max(0, (int) (remaining * total));
                if (timeLeft > 0) {
                    int minutes = Math.floorDiv(timeLeft, 1200);
                    int seconds = Math.floorDiv(timeLeft - minutes * 1200, 20);
                    String countdown = (minutes > 0 ? minutes + "m" : "") + (seconds > 0 ? seconds + "s" : "");
                    lines.add(regularFrom, Text.translatable("tip.cooldown", countdown).withColor(COOLDOWN_COLOR));
                    regularFrom++;
                }
            }
            for (int i = regularFrom; i < lines.size(); i++) {
                Text line = lines.get(i);
                var color = line.getStyle().getColor();
                if (color != null && color.getRgb() == COOLDOWN_COLOR) continue;
                lines.set(i, Text.empty().append(line).withColor(0x808080));
            }
        });
    }
}
