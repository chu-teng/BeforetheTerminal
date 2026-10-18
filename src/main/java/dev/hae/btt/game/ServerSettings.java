package dev.hae.btt.game;

import dev.hae.btt.encounter.Memory;

import dev.hae.btt.encounter.Backgrounds;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.hae.btt.config.BttConfig;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.*;


public final class ServerSettings {
    private ServerSettings() {}
    private static final Set<UUID> ADMINS = new LinkedHashSet<>();
    private static final Map<ServerWorld, RoundSettings> PENDING = new HashMap<>();
    private static final List<String> CATEGORIES = List.of("mode", "script", "encounter");
    public record RoundSettings(String script, String encounter) {}

    public static BeforeTheTerminalGameMode.Mode configuredMode() {
        try { return BeforeTheTerminalGameMode.Mode.valueOf(BttConfig.HANDLER.instance().serverMode); }
        catch (RuntimeException ex) { return BeforeTheTerminalGameMode.Mode.SCRIPT; }
    }
    public static boolean isAdmin(UUID uuid) { return ADMINS.contains(uuid); }

    public static void register() {
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(
                dev.hae.btt.network.AdminMenuS2CPacket.ID, dev.hae.btt.network.AdminMenuS2CPacket.CODEC);
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playC2S().register(
                dev.hae.btt.network.AdminSettingC2SPacket.ID, dev.hae.btt.network.AdminSettingC2SPacket.CODEC);
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(
                dev.hae.btt.network.AdminSettingC2SPacket.ID, (packet, context) -> choose(context.player(), packet.category(), packet.value()));
        net.fabricmc.fabric.api.event.player.UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            if (AdminEquipment.category(stack).isEmpty()) return net.minecraft.util.TypedActionResult.pass(stack);
            if (player instanceof ServerPlayerEntity serverPlayer) useAdminControl(serverPlayer, stack);
            return net.minecraft.util.TypedActionResult.success(stack);
        });
        ServerLifecycleEvents.SERVER_STARTING.register(server -> { ADMINS.clear(); PENDING.clear(); });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> { ADMINS.clear(); PENDING.clear(); });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            rebalance(server, null);
            AdminEquipment.refresh(handler.getPlayer());
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID departed = handler.getPlayer().getUuid();
            ADMINS.remove(departed);
            rebalance(server, departed);
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % 20 != 0) return;
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                AdminEquipment.refresh(player);
                Memory.restoreSlip(player);
            }
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, environment) ->
                dispatcher.register(CommandManager.literal("admin")
                        .requires(source -> source.hasPermissionLevel(2) || source.getEntity() instanceof ServerPlayerEntity p && isAdmin(p.getUuid()))
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                            player.sendMessage(Text.empty().append(label("mode")).append(" · ")
                                    .append(label("script")).append(" · ").append(label("encounter")), true);
                            return 1;
                        })
                        .then(CommandManager.literal("transfer")
                                .then(CommandManager.argument("from", EntityArgumentType.player()).executes(ctx -> {
                                    boolean ok = transfer(ctx.getSource().getPlayerOrThrow(), EntityArgumentType.getPlayer(ctx, "from"));
                                    ctx.getSource().sendFeedback(() -> Text.translatable(ok ? "btt.admin.transferred" : "btt.admin.transfer_failed"), false);
                                    return ok ? 1 : 0;
                                })
                                        .then(CommandManager.argument("to", EntityArgumentType.player()).requires(source -> source.hasPermissionLevel(2)).executes(ctx -> {
                                            boolean ok = transfer(EntityArgumentType.getPlayer(ctx, "from"), EntityArgumentType.getPlayer(ctx, "to"));
                                            ctx.getSource().sendFeedback(() -> Text.translatable(ok ? "btt.admin.transferred" : "btt.admin.transfer_failed"), false);
                                            return ok ? 1 : 0;
                                        }))))));
    }

    private static void rebalance(MinecraftServer server, UUID excluded) {
        List<ServerPlayerEntity> online = server.getPlayerManager().getPlayerList().stream()
                .filter(p -> !p.getUuid().equals(excluded)).toList();
        Set<UUID> ids = new HashSet<>();
        for (ServerPlayerEntity player : online) ids.add(player.getUuid());
        ADMINS.retainAll(ids);
        List<ServerPlayerEntity> candidates = new ArrayList<>(online.stream().filter(p -> !isAdmin(p.getUuid())).toList());
        Collections.shuffle(candidates);
        for (ServerPlayerEntity player : candidates) {
            if (ADMINS.size() >= Math.min(3, online.size())) break;
            ADMINS.add(player.getUuid());
            player.sendMessage(Text.translatable("btt.admin.granted"), true);
        }
    }

    private static boolean transfer(ServerPlayerEntity from, ServerPlayerEntity to) {
        if (!isAdmin(from.getUuid()) || isAdmin(to.getUuid()) || from == to
                || to.getServer().getPlayerManager().getPlayer(to.getUuid()) != to) return false;
        ADMINS.remove(from.getUuid());
        ADMINS.add(to.getUuid());
        from.closeHandledScreen();
        AdminEquipment.refresh(from);
        AdminEquipment.refresh(to);
        to.sendMessage(Text.translatable("btt.admin.granted"), true);
        return true;
    }

    private static Text label(String category) {
        BttConfig config = BttConfig.HANDLER.instance();
        Text value = switch (category) {
            case "mode" -> Text.translatable("btt.mode." + configuredMode().name().toLowerCase(Locale.ROOT));
            case "script" -> !configuredMode().usesScript()
                    ? Text.translatable("btt.admin.no_script") : scriptName(config.serverScript);
            case "encounter" -> encounterName(config.serverEncounter);
            default -> throw new IllegalArgumentException(category);
        };
        return Text.translatable("btt.admin.setting", Text.translatable("btt.admin." + category), value);
    }
    private static Text scriptName(String id) { return "random".equals(id) ? Text.translatable("btt.admin.random") : Text.translatable("btt.script." + id); }
    private static Text encounterName(String id) {
        return switch (id) {
            case "random" -> Text.translatable("btt.admin.random");
            case "none" -> Text.translatable("btt.admin.none");
            default -> Text.translatable("btt.background." + id);
        };
    }

    public static boolean forbidDrop(net.minecraft.entity.player.PlayerEntity player) {
        return !GameWorldComponent.KEY.get(player.getWorld()).isRunning()
                && !player.isCreative() && !player.isSpectator() && !player.getAbilities().allowModifyWorld;
    }

    public static boolean useAdminControl(ServerPlayerEntity player, ItemStack stack) {
        String category = AdminEquipment.category(stack);
        if (category.isEmpty()) return false;
        if (isAdmin(player.getUuid()) && lobby(player) && CATEGORIES.contains(category)) open(player, category);
        else player.sendMessage(Text.translatable("btt.admin.denied"), true);
        return true;
    }

    private static void save(ServerPlayerEntity player, String category, String value) {
        if (!isAdmin(player.getUuid()) || !lobby(player)) return;
        BttConfig config = BttConfig.HANDLER.instance();
        switch (category) {
            case "mode" -> {
                config.serverMode = value;
                if (!modeAllows(configuredMode(), config.serverEncounter)) config.serverEncounter = "random";
            }
            case "script" -> { if (configuredMode().usesScript()) config.serverScript = value; }
            case "encounter" -> { if (modeAllows(configuredMode(), value)) config.serverEncounter = value; }
            default -> { return; }
        }
        BttConfig.HANDLER.save();
        player.sendMessage(Text.translatable("btt.admin.saved"), true);
        player.closeHandledScreen();
        for (ServerPlayerEntity admin : player.getServer().getPlayerManager().getPlayerList()) AdminEquipment.refresh(admin);
    }

    static boolean lobby(ServerPlayerEntity player) {
        var game = GameWorldComponent.KEY.get(player.getWorld());
        return game.getGameStatus() == GameWorldComponent.GameStatus.INACTIVE
                && !dev.hae.btt.draft.DraftManager.isActiveFor(player.getServerWorld());
    }

    private static void open(ServerPlayerEntity player, String category) {
        var config = BttConfig.HANDLER.instance();
        String selected = switch (category) {
            case "mode" -> configuredMode().name();
            case "script" -> configuredMode().usesScript() ? config.serverScript : "none";
            case "encounter" -> config.serverEncounter;
            default -> "";
        };
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player,
                new dev.hae.btt.network.AdminMenuS2CPacket(category, selected, configuredMode().name()));
    }

    private static void choose(ServerPlayerEntity player, String category, String value) {
        if (!isAdmin(player.getUuid()) || !lobby(player) || !CATEGORIES.contains(category)) return;
        if (value.isEmpty()) { open(player, category); return; }
        List<String> choices = new ArrayList<>();
        switch (category) {
            case "mode" -> {
                for (var mode : BeforeTheTerminalGameMode.Mode.displayOrder()) choices.add(mode.name());
            }
            case "script" -> {
                if (!configuredMode().usesScript()) return;
                choices.add("random");
                for (var script : Scripts.ALL) choices.add(script.id());
            }
            case "encounter" -> {
                choices.addAll(List.of("random", "none"));
                Backgrounds.IMPLEMENTED.stream().filter(id -> modeAllows(configuredMode(), id))
                        .sorted().forEach(choices::add);
            }
            default -> { return; }
        }
        if (!choices.contains(value)) return;
        save(player, category, value);
        open(player, category);
    }

    public static boolean modeAllows(BeforeTheTerminalGameMode.Mode mode, String encounter) {
        return encounter != null && (encounter.isEmpty() || Set.of("random", "none").contains(encounter)
                || Backgrounds.IMPLEMENTED.contains(encounter));
    }


    public static boolean prepare(ServerWorld world, BeforeTheTerminalGameMode mode) {
        if (PENDING.containsKey(world)) return true;
        BttConfig config = BttConfig.HANDLER.instance();
        String configured = config.serverEncounter;
        if (Set.of("memory", "opposition", "admiration").contains(configured)) {
            configured = "none";
            config.serverEncounter = configured;
            BttConfig.HANDLER.save();
        }
        boolean valid = "random".equals(configured) || "none".equals(configured) || Backgrounds.IMPLEMENTED.contains(configured);
        if (!valid || !modeAllows(mode.mode, configured)
                || mode.mode.usesScript()
                && !"random".equals(config.serverScript) && Scripts.byId(config.serverScript) == null) return refuse(world);
        List<Role> fixed = new ArrayList<>(org.agmas.harpymodloader.Harpymodloader.FORCED_MODDED_ROLE_FLIP.values());
        String encounter = "random".equals(configured) ? Backgrounds.draw(world, fixed, mode.mode)
                : "none".equals(configured) ? "" : configured;
        if (!modeAllows(mode.mode, encounter)) encounter = "";
        if (Backgrounds.DUPLICATES.equals(encounter)) {
            var counts = new HashMap<Role, Integer>();
            fixed.stream().filter(Objects::nonNull).forEach(role -> counts.merge(role, 1, Integer::sum));
            if (counts.values().stream().anyMatch(count -> count > 2)) return refuse(world);
        }
        Set<String> opposed = Backgrounds.opposedRoles(encounter);
        if (fixed.stream().filter(Objects::nonNull).anyMatch(role -> opposed.contains(role.identifier().getPath()))) return refuse(world);
        if (Backgrounds.WHIRLPOOL.equals(encounter) && dev.hae.btt.map.Rooms.activeFor(world).stream().filter(room -> !room.spawnPoints().isEmpty()).count() < 2) return refuse(world);
        String script = !mode.mode.usesScript() ? "" : "random".equals(config.serverScript)
                ? Scripts.draw(world.getRandom()).id() : config.serverScript;
        PENDING.put(world, new RoundSettings(script, encounter));
        SeatAssignment.setOpposed(opposed);
        SeatAssignment.setDuplicates(Backgrounds.DUPLICATES.equals(encounter));
        return true;
    }
    private static boolean refuse(ServerWorld world) {
        for (ServerPlayerEntity player : world.getPlayers()) player.sendMessage(Text.translatable("btt.admin.start_conflict"), true);
        return false;
    }
    public static RoundSettings consume(ServerWorld world) { return PENDING.remove(world); }
    public static void cancel(ServerWorld world) { PENDING.remove(world); }
}
