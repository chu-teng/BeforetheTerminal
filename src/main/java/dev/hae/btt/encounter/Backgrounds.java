package dev.hae.btt.encounter;

import dev.hae.btt.game.ServerSettings;

import dev.hae.btt.game.BeforeTheTerminalGameMode;

import dev.hae.btt.game.GameModes;

import dev.hae.btt.game.SeatAssignment;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.hae.btt.component.GameState;
import dev.hae.btt.registry.BttItems;
import dev.hae.btt.role.RoleAnnouncements;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;










public final class Backgrounds {
    private Backgrounds() {}


    public static final String ZHISHAN = "zhishan";





    public static final String INSIGHT = "insight";

    public static final String EXPRESS = "express";
    public static final String RESONANCE = "resonance";
    public static final String WHIRLPOOL = "whirlpool";
    public static final String LEADS = "leads";
    public static final String LIFE = "life";
    public static final String DREAM = "dream";
    public static final String INTRIGUE = "intrigue";
    public static final String STORM = "storm";
    public static final String REBIRTH = "rebirth";
    public static final String DUPLICATES = "duplicates";

    public static final String SKIN = "skin";

    public static final String COMPLICITY = "complicity";
    public static final List<String> IMPLEMENTED = List.of(DREAM, STORM, LIFE, SKIN, COMPLICITY, REBIRTH, DUPLICATES, ZHISHAN);


    private static final int PER_LINE = 6;




    private static int orderIndex(Role role) {
        return RoleAnnouncements.orderOf(RoleAnnouncements.get(role.identifier().getPath()));
    }


    public static boolean isActive(net.minecraft.world.World world, String id) {
        return world != null && id != null && IMPLEMENTED.contains(id) && id.equals(GameState.KEY.get(world).background);
    }







    public static java.util.Set<String> opposedRoles(String id) {
        if (id == null || id.isEmpty()) return java.util.Set.of();
        return switch (id) {

            case ZHISHAN -> java.util.Set.of("heretic", "black_death");
            case EXPRESS -> java.util.Set.of("attendant");
            case LIFE -> java.util.Set.of("monk");
            case INTRIGUE -> java.util.Set.of("ex_undercover", "ex_traitor");
            default -> java.util.Set.of();
        };
    }


    public static String draw(ServerWorld world) {
        return draw(world, java.util.List.of());
    }

    public static String draw(ServerWorld world, java.util.Collection<Role> fixedRoles) {
        return draw(world, fixedRoles, GameModes.modeOf(GameWorldComponent.KEY.get(world)));
    }

    public static String draw(ServerWorld world, java.util.Collection<Role> fixedRoles, BeforeTheTerminalGameMode.Mode mode) {
        if (world.getRandom().nextBoolean()) return "";
        java.util.Set<String> fixed = new java.util.HashSet<>();
        for (Role role : fixedRoles) if (role != null) fixed.add(role.identifier().getPath());
        for (Role role : org.agmas.harpymodloader.Harpymodloader.FORCED_MODDED_ROLE_FLIP.values())
            if (role != null) fixed.add(role.identifier().getPath());
        java.util.List<String> eligible = IMPLEMENTED.stream()
                .filter(id -> ServerSettings.modeAllows(mode, id))
                .filter(id -> java.util.Collections.disjoint(opposedRoles(id), fixed))
                .filter(id -> !INTRIGUE.equals(id) || world.getPlayers().stream().filter(p -> dev.doctor4t.wathe.cca.MapVariablesWorldComponent.KEY.get(world).getReadyArea().contains(p.getPos())).count() >= 12)
                .filter(id -> !WHIRLPOOL.equals(id) || dev.hae.btt.map.Rooms.activeFor(world).stream()
                        .filter(room -> !room.spawnPoints().isEmpty()).count() >= 2).toList();
        return eligible.isEmpty() ? "" : eligible.get(world.getRandom().nextInt(eligible.size()));
    }


    public static void set(ServerWorld world, String id) {
        GameState state = GameState.KEY.get(world);
        state.background = id != null && IMPLEMENTED.contains(id) ? id : "";
        state.sync();
    }


    public static void apply(ServerWorld world, List<ServerPlayerEntity> players) {
        String id = GameState.KEY.get(world).background;
        applyMode(world, players);
        if (!IMPLEMENTED.contains(id)) return;
        if (ZHISHAN.equals(id)) zhishan(world, players);
        else if (SKIN.equals(id)) skin(world, players);
        else if (EXPRESS.equals(id)) dev.doctor4t.wathe.cca.GameTimeComponent.KEY.get(world).setTime(4 * 60 * 20);

    }

    private static void applyMode(ServerWorld world, List<ServerPlayerEntity> players) {
        var game = GameWorldComponent.KEY.get(world);
        var mode = GameModes.modeOf(game);
        if (mode == BeforeTheTerminalGameMode.Mode.MEMORY) players.forEach(Memory::restoreSlip);

    }










    private static void skin(ServerWorld world, List<ServerPlayerEntity> players) {
        GameWorldComponent gwc = GameWorldComponent.KEY.get(world);
        Set<String> present = new java.util.HashSet<>();
        for (ServerPlayerEntity player : players) {
            Role role = gwc.getRole(player);
            if (role != null) present.add(role.identifier().toString());
        }
        List<Role> absent = new ArrayList<>();
        for (Role role : dev.hae.btt.role.Roles.allRoles()) {
            if (!dev.hae.btt.role.Roles.isImplemented(role)) continue;
            if (dev.hae.btt.role.Roles.kindOf(role) != dev.hae.btt.role.Roles.Kind.CIVILIAN
                    && dev.hae.btt.role.Roles.kindOf(role) != dev.hae.btt.role.Roles.Kind.ENFORCER) continue;
            if (present.contains(role.identifier().toString())) continue;
            absent.add(role);
        }
        if (absent.isEmpty()) return;
        for (ServerPlayerEntity player : players) {
            if (dev.hae.btt.role.Roles.kindOfReal(gwc, player) != dev.hae.btt.role.Roles.Kind.PRINCIPAL) continue;
            java.util.Collections.shuffle(absent, new java.util.Random(player.getRandom().nextLong()));
            List<String> names = new ArrayList<>();
            for (int i = 0; i < Math.min(3, absent.size()); i++)
                names.add(SeatAssignment.displayName(absent.get(i)).getString());
            player.sendMessage(Text.translatable("btt.background.skin.roles", String.join("、", names)), true);
        }
    }

    private static void zhishan(ServerWorld world, List<ServerPlayerEntity> players) {
        GameWorldComponent gwc = GameWorldComponent.KEY.get(world);
        Set<Role> assigned = new LinkedHashSet<>();
        for (ServerPlayerEntity player : players) {
            Role role = gwc.getRole(player);
            if (role != null) assigned.add(role);
        }
        if (assigned.isEmpty()) return;
        List<Role> roles = new ArrayList<>(assigned);
        roles.sort(java.util.Comparator.comparingInt(Backgrounds::orderIndex));

        List<Text> lines = new ArrayList<>();
        for (int i = 0; i < roles.size(); i += PER_LINE) {
            MutableText line = Text.empty();
            for (int k = i; k < Math.min(i + PER_LINE, roles.size()); k++) {
                if (k > i) line.append(Text.literal(" "));
                Role role = roles.get(k);

                line.append(SeatAssignment.displayName(role).copy()
                        .setStyle(Style.EMPTY.withItalic(false).withColor(role.color())));
            }
            lines.add(line);
        }

        for (ServerPlayerEntity player : players) {
            ItemStack slip = new ItemStack(BttItems.SLIP);
            slip.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("btt.background.zhishan.slip")
                    .setStyle(Style.EMPTY.withItalic(false)));
            slip.set(DataComponentTypes.LORE, new LoreComponent(lines));
            player.giveItemStack(slip);
        }
    }
}
