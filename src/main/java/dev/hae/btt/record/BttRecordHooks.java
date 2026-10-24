package dev.hae.btt.record;

import dev.doctor4t.wathe.game.GameFunctions;
import dev.hae.btt.api.event.BttEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;








public final class BttRecordHooks {
    private BttRecordHooks() {}

    public static void register() {
        BttEvents.Death.EVENT.register((victim, killer, reason) ->
                GameRecordManager.recordDeath(victim, killer, reason));

        BttEvents.ShopPurchase.EVENT.register((player, entry, index, pricePaid) ->
                GameRecordManager.recordShopPurchase(player, entry, index, pricePaid));

        BttEvents.TaskCompleted.EVENT.register((player, task) ->
                GameRecordManager.recordTaskComplete(player, task));

        BttEvents.RoleAssigned.EVENT.register((player, role) ->
                GameRecordManager.recordRoleSnapshot((ServerWorld) player.getWorld(),
                        dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(player.getWorld())));

        BttEvents.GameStarted.EVENT.register(world -> {
            var gwc = dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(world);
            GameRecordManager.startMatch(world, gwc);
            GameRecordManager.recordMatchStart(world, gwc);
        });

        BttEvents.DraftPick.EVENT.register((world, uuid, ordinal, role, hidden) ->
                GameRecordManager.event(GameRecordTypes.DRAFT_PICK)
                        .world(world)
                        .putInt("ordinal", ordinal)
                        .putBool("hidden", hidden)
                        .put("role", role == null ? "" : role.identifier().toString())
                        .putUuid("actor", uuid)
                        .record());

        BttEvents.GameEnded.EVENT.register((world, ending) -> GameRecordManager.endMatch(world));




        BttEvents.SkillUsed.EVENT.register((user, target, skill) -> {
            net.minecraft.nbt.NbtCompound extra = new net.minecraft.nbt.NbtCompound();
            extra.putBoolean("drunk", dev.hae.btt.component.PlayerState.KEY.get(user).isDrunk());
            SkillUseRecording.capture(user, () -> GameRecordManager.recordSkillUse(user, skill, target, extra));
        });
    }
}
