package dev.hae.btt.role;

import dev.doctor4t.wathe.block_entity.DoorBlockEntity;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import dev.hae.btt.component.AbilityState;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.network.ActionReceiver;
import dev.hae.btt.network.DoorHighlightS2CPacket;











public final class Architect {
    private Architect() {}


    public static final int PASSAGE_CHARGE_TICKS = GameConstants.getInTicks(0, 30);


    public static void register() {
        PayloadTypeRegistry.playS2C().register(DoorHighlightS2CPacket.ID, DoorHighlightS2CPacket.CODEC);
    }


    public static void open(ServerPlayerEntity user) {
        if (!(user.getWorld() instanceof ServerWorld world)) return;
        GameWorldComponent gwc = GameWorldComponent.KEY.get(world);
        if (gwc == null || !gwc.isRunning()) return;
        AbilityState ability = AbilityState.KEY.get(user);
        if (ability.cooldown > 0) return;
        PlayerState pc = PlayerState.KEY.get(user);
        if (pc.passageDim.isEmpty()) {
            pc.passageDim = world.getRegistryKey().getValue().toString();
            pc.passageX = user.getBlockPos().getX();
            pc.passageY = user.getBlockPos().getY();
            pc.passageZ = user.getBlockPos().getZ();
            pc.passageTicks = PASSAGE_CHARGE_TICKS;
            pc.sync();
            return;
        }
        if (pc.passageTicks > 0) return;
        ServerWorld target = worldOf(user, pc.passageDim);
        if (target == null) {
            pc.passageDim = "";
            pc.passageTicks = 0;
            pc.sync();
            return;
        }
        user.teleport(target, pc.passageX + 0.5, pc.passageY, pc.passageZ + 0.5, user.getYaw(), user.getPitch());
        ability.setCooldown(RoleDefs.CD_1MIN);
        ability.sync();

        pc.passageDim = "";
        pc.passageTicks = 0;
        pc.sync();
    }


    public static void tick(ServerPlayerEntity user) {
        PlayerState pc = PlayerState.KEY.get(user);
        if (pc.passageTicks <= 0) return;
        if (--pc.passageTicks > 0) return;
        pc.sync();
    }


    public static DoorBlockEntity doorAt(ServerWorld world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof DoorBlockEntity door) return door;
        if (world.getBlockEntity(pos.up()) instanceof DoorBlockEntity door) return door;
        if (world.getBlockEntity(pos.down()) instanceof DoorBlockEntity door) return door;
        return null;
    }


    private static ServerWorld worldOf(ServerPlayerEntity user, String dim) {
        if (dim == null || dim.isEmpty() || user.getServer() == null) return null;
        Identifier id = Identifier.tryParse(dim);
        if (id == null) return null;
        return user.getServer().getWorld(RegistryKey.of(RegistryKeys.WORLD, id));
    }
}
