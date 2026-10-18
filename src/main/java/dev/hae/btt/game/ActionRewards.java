package dev.hae.btt.game;

import dev.doctor4t.wathe.index.WatheItems;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.registry.BttItems;
import dev.hae.btt.role.Roles;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;


public final class ActionRewards {
    private ActionRewards() {}


    private static final int NEEDS_PER_REWARD = 6;


    public static void register() {
        dev.hae.btt.api.event.BttEvents.TaskCompleted.EVENT.register((player, task) -> onTaskCompleted(player));
    }

    private static void onTaskCompleted(ServerPlayerEntity player) {
        if (player == null) return;
        var gwc = dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(player.getWorld());
        if (!gwc.isRunning()) return;


        if (!Roles.isPassengerCampFor(gwc, player)) return;
        PlayerState pc = PlayerState.KEY.get(player);
        if (pc.actionTaskProgress >= NEEDS_PER_REWARD) return;
        pc.actionTaskProgress++;
        pc.sync();
        if (pc.actionTaskProgress != NEEDS_PER_REWARD) return;
        java.util.List<String> locations = new java.util.ArrayList<>();
        locations.add("RECOVER_ROOM");
        var world = player.getServerWorld();
        var area = dev.doctor4t.wathe.cca.MapVariablesWorldComponent.KEY.get(world).getPlayArea();
        boolean console = false, chair = false;
        for (int x = net.minecraft.util.math.MathHelper.floor(area.minX) >> 4; x <= net.minecraft.util.math.MathHelper.floor(area.maxX) >> 4; x++) {
            for (int z = net.minecraft.util.math.MathHelper.floor(area.minZ) >> 4; z <= net.minecraft.util.math.MathHelper.floor(area.maxZ) >> 4; z++) {
                if (!world.isChunkLoaded(x, z)) continue;
                var chunk = world.getChunk(x, z);
                var sections = chunk.getSectionArray();
                for (int index = 0; index < sections.length; index++) {
                    var section = sections[index];
                    if (section == null || section.isEmpty() || !section.hasAny(state ->
                            state.isOf(dev.hae.btt.registry.BttBlocks.CONTROL_CONSOLE)
                                    || state.isOf(dev.hae.btt.registry.BttBlocks.LOOKOUT_CHAIR))) continue;
                    int baseY = world.sectionIndexToCoord(index) << 4;
                    for (int dx = 0; dx < 16; dx++) for (int dy = 0; dy < 16; dy++) for (int dz = 0; dz < 16; dz++) {
                        if (!area.contains((x << 4) + dx + 0.5, baseY + dy + 0.5, (z << 4) + dz + 0.5)) continue;
                        var state = section.getBlockState(dx, dy, dz);
                        console |= state.isOf(dev.hae.btt.registry.BttBlocks.CONTROL_CONSOLE);
                        chair |= state.isOf(dev.hae.btt.registry.BttBlocks.LOOKOUT_CHAIR);
                    }
                }
            }
        }
        if (console) locations.add("RECOVER_CONSOLE");
        if (chair) locations.add("RECOVER_LOOKOUT");
        Needs.addRecovery(player, locations.get(player.getRandom().nextInt(locations.size())));
    }

    public static void grantRecoveredItem(ServerPlayerEntity player) {
        Item reward = switch (player.getRandom().nextInt(3)) {
            case 0 -> WatheItems.LOCKPICK;
            case 1 -> BttItems.REMOTE;
            default -> BttItems.WALKIE_TALKIE;
        };
        dev.hae.btt.advancement.BttAdvancements.fact(player, "recovery");
        player.giveItemStack(new ItemStack(reward));
        player.sendMessage(Text.translatable("btt.action.reward.task",
                        Text.translatable(reward.getTranslationKey()))
                .withColor(BeforeTheTerminalGameMode.FACTION_PASSENGER_COLOR), true);
    }
}
