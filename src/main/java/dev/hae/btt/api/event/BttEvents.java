package dev.hae.btt.api.event;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.util.ShopEntry;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
















public final class BttEvents {
    private BttEvents() {}


    public interface Death {
        void onDeath(ServerPlayerEntity victim, @Nullable ServerPlayerEntity killer, Identifier reason);

        Event<Death> EVENT = EventFactory.createArrayBacked(Death.class, listeners -> (victim, killer, reason) -> {
            for (Death l : listeners) l.onDeath(victim, killer, reason);
        });
    }


    public interface DeathReaction extends Death {
        Event<DeathReaction> EVENT = EventFactory.createArrayBacked(DeathReaction.class, listeners -> (victim, killer, reason) -> {
            for (DeathReaction listener : listeners) listener.onDeath(victim, killer, reason);
        });
    }


    public interface SkillUsed {
        void onSkillUsed(ServerPlayerEntity user, @Nullable ServerPlayerEntity target, Identifier skill);

        Event<SkillUsed> EVENT = EventFactory.createArrayBacked(SkillUsed.class, listeners -> (user, target, skill) -> {
            for (SkillUsed l : listeners) l.onSkillUsed(user, target, skill);
        });
    }


    public interface ShopPurchase {
        void onPurchase(ServerPlayerEntity player, ShopEntry entry, int index, int pricePaid);

        Event<ShopPurchase> EVENT = EventFactory.createArrayBacked(ShopPurchase.class,
                listeners -> (player, entry, index, pricePaid) -> {
                    for (ShopPurchase l : listeners) l.onPurchase(player, entry, index, pricePaid);
                });
    }













    public interface BuildShopEntries {
        void onBuild(net.minecraft.world.World world, @Nullable net.minecraft.entity.player.PlayerEntity buyer,
                     dev.hae.btt.ability.shop.ShopBuilder builder);

        Event<BuildShopEntries> EVENT = EventFactory.createArrayBacked(BuildShopEntries.class,
                listeners -> (world, buyer, builder) -> {
                    for (BuildShopEntries l : listeners) l.onBuild(world, buyer, builder);
                });
    }

    public interface TaskCompleted {
        void onTaskCompleted(ServerPlayerEntity player, String task);

        Event<TaskCompleted> EVENT = EventFactory.createArrayBacked(TaskCompleted.class, listeners -> (player, task) -> {
            for (TaskCompleted l : listeners) l.onTaskCompleted(player, task);
        });
    }


    public interface RoleAssigned {
        void onRoleAssigned(ServerPlayerEntity player, Role role);

        Event<RoleAssigned> EVENT = EventFactory.createArrayBacked(RoleAssigned.class, listeners -> (player, role) -> {
            for (RoleAssigned l : listeners) l.onRoleAssigned(player, role);
        });
    }


    public interface GameStarted {
        void onGameStarted(ServerWorld world);

        Event<GameStarted> EVENT = EventFactory.createArrayBacked(GameStarted.class, listeners -> world -> {
            for (GameStarted l : listeners) l.onGameStarted(world);
        });
    }

    public interface GameEnded {
        void onGameEnded(ServerWorld world, String ending);

        Event<GameEnded> EVENT = EventFactory.createArrayBacked(GameEnded.class, listeners -> (world, ending) -> {
            for (GameEnded l : listeners) l.onGameEnded(world, ending);
        });
    }


    public interface DraftPick {
        void onDraftPick(ServerWorld world, UUID player, int ordinal, Role role, boolean hidden);

        Event<DraftPick> EVENT = EventFactory.createArrayBacked(DraftPick.class,
                listeners -> (world, player, ordinal, role, hidden) -> {
                    for (DraftPick l : listeners) l.onDraftPick(world, player, ordinal, role, hidden);
                });
    }





    public interface EndingRewrite {
        dev.hae.btt.victory.Endings.Ending rewrite(ServerWorld world, dev.hae.btt.victory.Endings.Ending decided);

        Event<EndingRewrite> EVENT = EventFactory.createArrayBacked(EndingRewrite.class, listeners -> (world, decided) -> {
            for (EndingRewrite l : listeners) {
                dev.hae.btt.victory.Endings.Ending out = l.rewrite(world, decided);
                if (out != null) return out;
            }
            return null;
        });
    }





    public interface EndingVeto {
        boolean allow(ServerWorld world, dev.hae.btt.victory.Endings.Ending decided);

        Event<EndingVeto> EVENT = EventFactory.createArrayBacked(EndingVeto.class, listeners -> (world, decided) -> {
            for (EndingVeto l : listeners) {
                if (!l.allow(world, decided)) return false;
            }
            return true;
        });
    }
}
