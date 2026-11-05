package dev.hae.btt.game;

import dev.doctor4t.wathe.api.WatheGameModes;
import dev.hae.btt.component.GameState;
import dev.hae.btt.role.RoleAnnouncements;
import dev.hae.btt.role.RoleDefs;
import dev.hae.btt.role.Roles;
import dev.hae.btt.BeforeTheTerminal;

import java.util.Set;







public final class GameModes {
    private GameModes() {}

    public static final BeforeTheTerminalGameMode BEFORE_THE_TERMINAL = new BeforeTheTerminalGameMode();

    public static final BeforeTheTerminalGameMode DRAFT =
            new BeforeTheTerminalGameMode(BeforeTheTerminalGameMode.DRAFT_ID, BeforeTheTerminalGameMode.Mode.DRAFT);
    public static final BeforeTheTerminalGameMode MEMORY =
            new BeforeTheTerminalGameMode(BeforeTheTerminalGameMode.MEMORY_ID, BeforeTheTerminalGameMode.Mode.MEMORY);

    public static final BeforeTheTerminalGameMode CUSTOM =
            new BeforeTheTerminalGameMode(BeforeTheTerminalGameMode.CUSTOM_ID, BeforeTheTerminalGameMode.Mode.CUSTOM);

    public static final BeforeTheTerminalGameMode MOB =
            new BeforeTheTerminalGameMode(BeforeTheTerminalGameMode.MOB_ID, BeforeTheTerminalGameMode.Mode.MOB);


    public static BeforeTheTerminalGameMode.Mode modeOf(dev.doctor4t.wathe.cca.GameWorldComponent gwc) {
        if (gwc == null) return null;
        return gwc.getGameMode() instanceof BeforeTheTerminalGameMode btt ? btt.mode : null;
    }


    public static BeforeTheTerminalGameMode of(BeforeTheTerminalGameMode.Mode mode) {
        if (mode == BeforeTheTerminalGameMode.Mode.DRAFT) return DRAFT;
        if (mode == BeforeTheTerminalGameMode.Mode.MEMORY) return MEMORY;
        if (mode == BeforeTheTerminalGameMode.Mode.CUSTOM) return CUSTOM;
        if (mode == BeforeTheTerminalGameMode.Mode.MOB) return MOB;
        return BEFORE_THE_TERMINAL;
    }





    public static Set<Rule> rules(net.minecraft.world.World world) {
        if (world == null) return Set.of();
        BeforeTheTerminalGameMode.Mode mode = modeOf(dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(world));
        if (mode == null) return Set.of();
        GameState state = GameState.KEY.get(world);
        if (state.rules == null || state.rules.isBlank()) return mode.rules();
        return Rule.normalize(Rule.parse(state.rules));
    }


    public static boolean hasRule(net.minecraft.world.World world, Rule rule) {
        return rule != null && rules(world).contains(rule);
    }


    public static boolean isDebate(net.minecraft.world.World world) {
        return hasRule(world, Rule.DEBATE);
    }




    public static BeforeTheTerminalGameMode modeForStart(dev.doctor4t.wathe.cca.GameWorldComponent gwc) {
        return of(ServerSettings.configuredMode());
    }


    public static void register() {
        Roles.register();
        RoleDefs.init();
        RoleAnnouncements.init();
        dev.hae.btt.role.Engineer.register();
        WatheGameModes.registerGameMode(BeforeTheTerminalGameMode.DRAFT_ID, DRAFT);
        WatheGameModes.registerGameMode(BeforeTheTerminalGameMode.ID, BEFORE_THE_TERMINAL);
        WatheGameModes.registerGameMode(BeforeTheTerminalGameMode.MEMORY_ID, MEMORY);
        WatheGameModes.registerGameMode(BeforeTheTerminalGameMode.CUSTOM_ID, CUSTOM);
        WatheGameModes.registerGameMode(BeforeTheTerminalGameMode.MOB_ID, MOB);
        GameEvents.init();
        RoundResults.register();
        GameTicker.register();
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            RoundCleanup.clearServerSessions();
        });
        BeforeTheTerminalGameMode.LOGGER.info("[BTT] game modes registered: {} + {} + {} + {} + {} ({} roles, {} defs)",
                BeforeTheTerminalGameMode.DRAFT_ID, BeforeTheTerminalGameMode.ID, BeforeTheTerminalGameMode.MEMORY_ID,
                BeforeTheTerminalGameMode.CUSTOM_ID, BeforeTheTerminalGameMode.MOB_ID,
                Roles.registeredRoleIds().size(), dev.hae.btt.role.RoleDefs.defCount());
    }
}
