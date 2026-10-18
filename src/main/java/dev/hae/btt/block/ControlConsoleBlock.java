package dev.hae.btt.block;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.api.ClientBridge;
import dev.hae.btt.game.SeatAssignment;
import dev.hae.btt.component.PlayerState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;








public class ControlConsoleBlock extends Block {

    public ControlConsoleBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!SeatAssignment.isBttMode(world)) return ActionResult.PASS;
        if (!GameWorldComponent.KEY.get(world).isRunning()) return ActionResult.PASS;
        if (!GameFunctions.isPlayerAliveAndSurvival(player)) return ActionResult.PASS;
        if (world.isClient()) {
            ClientBridge.consoleOpener.run();
        } else {
            PlayerState.KEY.get(player).openConsole();
            if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer)
                dev.hae.btt.game.Needs.recoverAtConsole(serverPlayer);
        }
        return ActionResult.SUCCESS;
    }
}
