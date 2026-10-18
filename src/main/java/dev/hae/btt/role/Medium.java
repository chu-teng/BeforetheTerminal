package dev.hae.btt.role;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.role.RoleTransitions;
import dev.hae.btt.mechanic.Attachment;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

import java.util.UUID;
















public final class Medium {
    private Medium() {}






    public static boolean retain(ServerPlayerEntity user, ServerPlayerEntity target) {
        if (target == null || target == user) return false;
        if (!GameFunctions.isPlayerAliveAndSurvival(target)) return false;
        PlayerState mine = PlayerState.KEY.get(user);
        if (!mine.mediumRetain.isEmpty()) {
            ServerPlayerEntity old = resolve(user, mine.mediumRetain);

            if (old != null && GameFunctions.isPlayerAliveAndSurvival(old)) return false;
            if (old != null) Attachment.detach(old);
        }
        mine.mediumRetain = target.getUuidAsString();
        mine.sync();
        return true;
    }





    public static void onDeath(ServerPlayerEntity victim, ServerWorld world) {
        GameWorldComponent gwc = GameWorldComponent.KEY.get(world);

        if (!gwc.isRole(victim, Roles.MEDIUM)) {
            for (ServerPlayerEntity medium : world.getPlayers()) {
                if (medium == victim || !gwc.isRole(medium, Roles.MEDIUM)) continue;
                PlayerState mp = PlayerState.KEY.get(medium);
                if (!victim.getUuidAsString().equals(mp.mediumRetain)) continue;

                if (!GameFunctions.isPlayerAliveAndSurvival(medium)) return;
                Attachment.attach(victim, medium, Attachment.KIND_MEDIUM);
                return;
            }
            return;
        }

        PlayerState pc = PlayerState.KEY.get(victim);
        ServerPlayerEntity soul = resolve(victim, pc.mediumRetain);
        pc.mediumRetain = "";
        pc.sync();
        if (soul == null) return;
        if (!Attachment.isAttached(soul) || !Attachment.KIND_MEDIUM.equals(Attachment.kindOf(soul))) return;
        reviveInPlace(soul, victim);
    }


    private static void reviveInPlace(ServerPlayerEntity soul, ServerPlayerEntity medium) {
        Attachment.detach(soul);
        if (!GameFunctions.isPlayerAliveAndSurvival(soul)) {
            soul.changeGameMode(GameMode.ADVENTURE);
        }
        soul.teleport(medium.getServerWorld(), medium.getX(), medium.getY(), medium.getZ(),
                soul.getYaw(), soul.getPitch());
        inheritInventory(soul, medium);

        RoleTransitions.replace(soul, Roles.MEDIUM, 0, Roles.CAMP_PASSENGER, false);
        PlayerState sp = PlayerState.KEY.get(soul);
        sp.applyPermanentDrunk();
        sp.sync();

        dev.doctor4t.wathe.cca.PlayerMoodComponent.KEY.get(soul).setMood(1.0f);
        soul.sendMessage(Text.translatable("btt.action.medium.revive").withColor(Roles.MEDIUM.color()), true);
    }


    private static void inheritInventory(ServerPlayerEntity soul, ServerPlayerEntity medium) {
        for (int i = 0; i < medium.getInventory().size(); i++) {
            ItemStack stack = medium.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            soul.getInventory().setStack(i, stack.copy());
            medium.getInventory().setStack(i, ItemStack.EMPTY);
        }
    }


    private static ServerPlayerEntity resolve(ServerPlayerEntity owner, String uuid) {
        if (uuid == null || uuid.isEmpty()) return null;
        try {
            return owner.getServerWorld().getPlayerByUuid(UUID.fromString(uuid)) instanceof ServerPlayerEntity s ? s : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
