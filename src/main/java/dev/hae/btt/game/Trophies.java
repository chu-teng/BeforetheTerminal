package dev.hae.btt.game;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.hae.btt.api.event.BttEvents;
import dev.hae.btt.component.GameState;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.role.Roles;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.*;


public final class Trophies {
    private Trophies() {}
    private static final String TAG = "bttWreath";
    private static final Map<UUID, List<String>> PENDING = new HashMap<>();

    public static void register() { BttEvents.GameEnded.EVENT.register(Trophies::snapshot); }


    public static void clearPendingAwards() { PENDING.clear(); }

    private static void snapshot(ServerWorld world, String ending) {
        var gwc = GameWorldComponent.KEY.get(world);
        Set<String> winners = new HashSet<>(Arrays.asList(GameState.KEY.get(world).winners.split(",")));
        for (ServerPlayerEntity player : world.getPlayers()) {
            PENDING.remove(player.getUuid());
            if (gwc.getRole(player) == null || Roles.campOf(gwc, player) == null) continue;
            List<String> ids = new ArrayList<>();
            PlayerState state = PlayerState.KEY.get(player);

            if (state.correctExecutions >= 2) ids.add("lily");
            if (state.killedPrincipal) ids.add("gladiolus");
            if (state.killedOutsider) ids.add("artichoke");
            if (winners.contains(player.getUuidAsString())) {
                boolean relationship = "LOVERS_WIN".equals(ending) || "ARCHENEMY_WIN".equals(ending);
                ids.add(relationship || state.soloObjectiveComplete ? "iris" : switch (Roles.campOf(gwc, player)) {
                    case PASSENGER -> "laurel";
                    case KILLER -> "spider_lily";
                    default -> "iris";
                });
            }
            for (String id : ids) dev.hae.btt.advancement.BttAdvancements.wreath(player, id);
            if (!ids.isEmpty()) PENDING.put(player.getUuid(), ids);
        }
    }

    public static void grantAll(ServerWorld world) {
        for (ServerPlayerEntity player : world.getPlayers()) {
            List<String> awards = PENDING.remove(player.getUuid());
            if (awards == null || awards.isEmpty()) continue;
            ItemStack previous = player.getEquippedStack(EquipmentSlot.HEAD);
            player.equipStack(EquipmentSlot.HEAD, stack(awards.getFirst()));
            if (!previous.isEmpty()) player.giveItemStack(previous);
            for (int i = 1; i < awards.size(); i++) player.giveItemStack(stack(awards.get(i)));
            player.currentScreenHandler.syncState();
        }
    }

    public static void clearAll(ServerWorld world) {
        for (ServerPlayerEntity player : world.getPlayers()) {
            PENDING.remove(player.getUuid());
            removeAll(player);
        }

        for (var item : world.getEntitiesByType(net.minecraft.entity.EntityType.ITEM, item -> isWreath(item.getStack()))) item.discard();
    }

    public static void removeAll(ServerPlayerEntity player) {
        for (int slot = 0; slot < player.getInventory().size(); slot++)
            if (isWreath(player.getInventory().getStack(slot))) player.getInventory().setStack(slot, ItemStack.EMPTY);
        player.currentScreenHandler.syncState();
    }

    public static ItemStack stack(String id) {
        int color = switch (id) {
            case "laurel" -> 0x9ACD32;
            case "spider_lily" -> 0xFF0000;
            case "iris" -> 0x0000FF;
            case "lily" -> 0xFFFFFF;
            case "gladiolus" -> 0xFFA500;
            case "artichoke" -> 0xFF00FF;
            default -> throw new IllegalArgumentException("Unknown wreath: " + id);
        };
        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
        stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(color, false));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("btt.wreath." + id).styled(style -> style.withItalic(false)));
        stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(Text.translatable("btt.wreath." + id + ".desc"))));
        NbtCompound tag = new NbtCompound();
        tag.putString(TAG, id);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(tag));
        return stack;
    }

    private static boolean isWreath(ItemStack stack) {
        return stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().contains(TAG);
    }
}
