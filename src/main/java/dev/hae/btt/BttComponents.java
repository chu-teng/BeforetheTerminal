package dev.hae.btt;

import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.hae.btt.component.AbilityState;
import dev.hae.btt.component.BodyState;
import dev.hae.btt.component.ConfigState;
import dev.hae.btt.component.CorpseState;
import dev.hae.btt.component.CurseState;
import dev.hae.btt.component.DisguiseState;
import dev.hae.btt.component.ExecutionerState;
import dev.hae.btt.component.GameState;
import dev.hae.btt.component.MapVoteState;
import dev.hae.btt.component.RotationState;
import dev.hae.btt.component.DraftState;
import dev.hae.btt.component.PlayerState;
import dev.hae.btt.component.ScavengerState;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import org.ladysnake.cca.api.v3.world.WorldComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.world.WorldComponentInitializer;







public class BttComponents implements EntityComponentInitializer, WorldComponentInitializer {
    public BttComponents() {
    }

    @Override
    public void registerEntityComponentFactories(@NotNull EntityComponentFactoryRegistry registry) {
        registry.beginRegistration(PlayerEntity.class, AbilityState.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(AbilityState::new);
        registry.beginRegistration(PlayerEntity.class, DisguiseState.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(DisguiseState::new);
        registry.beginRegistration(PlayerEntity.class, CurseState.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(CurseState::new);
        registry.beginRegistration(PlayerBodyEntity.class, CorpseState.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(CorpseState::new);
        registry.beginRegistration(PlayerEntity.class, ExecutionerState.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(ExecutionerState::new);
        registry.beginRegistration(PlayerEntity.class, ScavengerState.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(ScavengerState::new);
        registry.beginRegistration(PlayerEntity.class, PlayerState.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(PlayerState::new);
        registry.beginRegistration(PlayerBodyEntity.class, BodyState.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(BodyState::new);
        registry.beginRegistration(PlayerEntity.class, dev.hae.btt.component.NeedsState.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(dev.hae.btt.component.NeedsState::new);

        registry.beginRegistration(PlayerEntity.class, dev.hae.btt.component.LetterState.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(dev.hae.btt.component.LetterState::new);
    }

    @Override
    public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry) {
        registry.register(GameState.KEY, GameState::new);
        registry.register(ConfigState.KEY, ConfigState::new);
        registry.register(MapVoteState.KEY, MapVoteState::new);
        registry.register(RotationState.KEY, RotationState::new);
        registry.register(DraftState.KEY, DraftState::new);
        registry.register(dev.hae.btt.component.ShopState.KEY, dev.hae.btt.component.ShopState::new);

        registry.register(dev.hae.btt.component.DebateState.KEY, dev.hae.btt.component.DebateState::new);
    }
}
