package dev.hae.btt;

import dev.doctor4t.wathe.api.Role;
import dev.hae.btt.role.Roles;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

import java.util.List;











public class BeforeTheTerminal implements ModInitializer {

    public static String MOD_ID = "btt";


    public static final Identifier WATHE_OVERRIDE_PACK = Identifier.of(MOD_ID, "wathe_override");

    @Override
    public void onInitialize() {
        registerWatheOverridePack();
        dev.hae.btt.registry.BttItems.init();
        dev.hae.btt.registry.BttBlocks.init();
        dev.hae.btt.registry.BttEntities.init();
        dev.hae.btt.mechanic.TrapFall.register();
        dev.hae.btt.record.replay.ReplayPlayer.register();
        dev.hae.btt.config.BttConfig.HANDLER.load();
        dev.hae.btt.game.ServerSettings.register();
        dev.hae.btt.network.RolePackets.register();
        dev.hae.btt.game.GameModes.register();

        dev.hae.btt.trait.Traits.init();

        dev.hae.btt.trait.relationship.Relationships.init();


        dev.hae.btt.map.BttMaps.register();
        dev.hae.btt.map.MapEnhancements.register();
        dev.hae.btt.network.MapVoteC2SPacket.register();
        dev.hae.btt.command.BttCommands.register();

        dev.hae.btt.advancement.BttAdvancements.register();
        dev.hae.btt.record.BttRecordHooks.register();
    }













    private static void registerWatheOverridePack() {
        boolean ok = net.fabricmc.fabric.api.resource.ResourceManagerHelper.registerBuiltinResourcePack(
                WATHE_OVERRIDE_PACK,
                net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow(),
                net.fabricmc.fabric.api.resource.ResourcePackActivationType.ALWAYS_ENABLED);
        if (!ok) {
            dev.hae.btt.game.BeforeTheTerminalGameMode.LOGGER.error(
                    "[BTT] builtin pack {} 注册失败——wathe 命名空间的语言/标签覆盖不会生效", WATHE_OVERRIDE_PACK);
        } else {
            dev.hae.btt.game.BeforeTheTerminalGameMode.LOGGER.info(
                    "[BTT] builtin pack registered: {} (resourcepacks/wathe_override, ALWAYS_ENABLED)", WATHE_OVERRIDE_PACK);
        }
    }

}
