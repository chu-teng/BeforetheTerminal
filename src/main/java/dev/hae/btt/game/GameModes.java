package dev.hae.btt.game;

import dev.doctor4t.wathe.api.WatheGameModes;
import dev.hae.btt.role.RoleAnnouncements;
import dev.hae.btt.role.RoleDefs;
import dev.hae.btt.role.Roles;
import dev.hae.btt.BeforeTheTerminal;




public final class GameModes {
    private GameModes() {}

    public static final BeforeTheTerminalGameMode BEFORE_THE_TERMINAL = new BeforeTheTerminalGameMode();

    public static final BeforeTheTerminalGameMode DRAFT =
            new BeforeTheTerminalGameMode(BeforeTheTerminalGameMode.DRAFT_ID, BeforeTheTerminalGameMode.Mode.DRAFT);

    public static final BeforeTheTerminalGameMode DEBATE =
            new BeforeTheTerminalGameMode(BeforeTheTerminalGameMode.DEBATE_ID, BeforeTheTerminalGameMode.Mode.DEBATE);

    public static final BeforeTheTerminalGameMode DUEL = new BeforeTheTerminalGameMode(BeforeTheTerminalGameMode.DUEL_ID, BeforeTheTerminalGameMode.Mode.DUEL);
    public static final BeforeTheTerminalGameMode MEMORY = new BeforeTheTerminalGameMode(BeforeTheTerminalGameMode.MEMORY_ID, BeforeTheTerminalGameMode.Mode.MEMORY);


    public static BeforeTheTerminalGameMode.Mode modeOf(dev.doctor4t.wathe.cca.GameWorldComponent gwc) {
        if (gwc == null) return null;
        return gwc.getGameMode() instanceof BeforeTheTerminalGameMode btt ? btt.mode : null;
    }


    public static BeforeTheTerminalGameMode of(BeforeTheTerminalGameMode.Mode mode) {
        if (mode == BeforeTheTerminalGameMode.Mode.DRAFT) return DRAFT;
        if (mode == BeforeTheTerminalGameMode.Mode.DEBATE) return DEBATE;
        if (mode == BeforeTheTerminalGameMode.Mode.DUEL) return DUEL;
        if (mode == BeforeTheTerminalGameMode.Mode.MEMORY) return MEMORY;
        return BEFORE_THE_TERMINAL;
    }


    public static boolean isDebate(net.minecraft.world.World world) {
        var mode = world == null ? null : modeOf(dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(world));
        return mode != null && mode.usesDebate();
    }




    public static BeforeTheTerminalGameMode modeForStart(dev.doctor4t.wathe.cca.GameWorldComponent gwc) {
        return of(ServerSettings.configuredMode());
    }


    public static void register() {
        Roles.register();
        RoleDefs.init();
        RoleAnnouncements.init();
        dev.hae.btt.role.Engineer.register();
        WatheGameModes.registerGameMode(BeforeTheTerminalGameMode.ID, BEFORE_THE_TERMINAL);
        WatheGameModes.registerGameMode(BeforeTheTerminalGameMode.DRAFT_ID, DRAFT);
        WatheGameModes.registerGameMode(BeforeTheTerminalGameMode.DEBATE_ID, DEBATE);
        WatheGameModes.registerGameMode(BeforeTheTerminalGameMode.DUEL_ID, DUEL);
        WatheGameModes.registerGameMode(BeforeTheTerminalGameMode.MEMORY_ID, MEMORY);
        GameEvents.init();
        GameTicker.register();
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            RoundCleanup.clearServerSessions();
        });
        BeforeTheTerminalGameMode.LOGGER.info("[BTT] game modes registered: {} + {} + {} + {} + {} ({} roles, {} defs)",
                BeforeTheTerminalGameMode.ID, BeforeTheTerminalGameMode.DRAFT_ID, BeforeTheTerminalGameMode.DEBATE_ID, BeforeTheTerminalGameMode.DUEL_ID, BeforeTheTerminalGameMode.MEMORY_ID,
                Roles.newRoleIds().size(), dev.hae.btt.role.RoleDefs.defCount());
    }
}
