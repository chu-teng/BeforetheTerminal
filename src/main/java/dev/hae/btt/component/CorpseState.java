package dev.hae.btt.component;

import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import dev.hae.btt.BeforeTheTerminal;
import dev.hae.btt.config.BttConfig;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class CorpseState implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<CorpseState> KEY = ComponentRegistry.getOrCreate(Identifier.of(BeforeTheTerminal.MOD_ID, "corpse"), CorpseState.class);
    public Identifier deathReason = GameConstants.DeathReasons.GENERIC;
    public Identifier technicalRole = WatheRoles.CIVILIAN.identifier();




    public Identifier initialRole = WatheRoles.CIVILIAN.identifier();
    public boolean scavenged = false;
    public boolean passengerAtDeath;
    public boolean neutralAtDeath;
    public transient boolean observedAutopsy;
    public transient boolean observedPassengerKnown;
    public transient boolean observedPassenger;
    public transient int observedAgeSeconds;
    public PlayerBodyEntity playerBodyEntity;
    private final java.util.Map<java.util.UUID, NbtCompound> sentViews = new java.util.HashMap<>();

    public void reset() {
        this.sync();
    }

    public CorpseState(PlayerBodyEntity playerBodyEntity) {
        this.playerBodyEntity = playerBodyEntity;
    }

    public void sync() {
        KEY.sync(this.playerBodyEntity);
    }

    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putString("deathReason", deathReason.toString());
        tag.putString("technicalRole", technicalRole.toString());
        tag.putString("initialRole", initialRole.toString());
        tag.putBoolean("scavenged", scavenged);
        tag.putBoolean("passengerAtDeath", passengerAtDeath);
        tag.putBoolean("neutralAtDeath", neutralAtDeath);
    }

    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.deathReason = Identifier.of(tag.getString("deathReason"));
        this.technicalRole = Identifier.of(tag.getString("technicalRole"));
        this.initialRole = tag.contains("initialRole") ? Identifier.of(tag.getString("initialRole")) : this.technicalRole;
        this.scavenged = tag.getBoolean("scavenged");
        passengerAtDeath = tag.getBoolean("passengerAtDeath");
        neutralAtDeath = tag.getBoolean("neutralAtDeath");
        observedAutopsy = tag.getBoolean("observedAutopsy");
        observedPassengerKnown = tag.getBoolean("observedPassengerKnown");
        observedPassenger = tag.getBoolean("observedPassenger");
        observedAgeSeconds = tag.getInt("observedAgeSeconds");
    }

    @Override
    public void writeSyncPacket(net.minecraft.network.RegistryByteBuf buf,
                                net.minecraft.server.network.ServerPlayerEntity recipient) {
        NbtCompound view = viewFor(recipient);
        buf.writeNbt(view);
        sentViews.put(recipient.getUuid(), view);
    }

    private NbtCompound viewFor(net.minecraft.server.network.ServerPlayerEntity recipient) {
        if (dev.hae.btt.game.SeatAssignment.isBttMode(playerBodyEntity.getWorld())) {
            return dev.hae.btt.perception.CorpseView.forPlayer(this, recipient);
        } else {
            NbtCompound saved = new NbtCompound();
            writeToNbt(saved, playerBodyEntity.getWorld().getRegistryManager());
            var game = dev.doctor4t.wathe.cca.GameWorldComponent.KEY.get(playerBodyEntity.getWorld());
            saved.putBoolean("observedAutopsy", game.isRole(recipient, dev.hae.btt.role.Roles.DOCTOR)
                    || game.isRole(recipient, dev.hae.btt.role.Roles.DUCE)
                    || game.isRole(recipient, dev.hae.btt.role.Roles.THIEF)
                    || dev.hae.btt.trait.Traits.has(recipient, dev.hae.btt.trait.Traits.ANATOMY)
                    || GameFunctions.isPlayerSpectatingOrCreative(recipient));
            saved.putBoolean("observedPassengerKnown", game.isRole(recipient, dev.hae.btt.role.Roles.MORTICIAN));
            var role = dev.hae.btt.role.Roles.byId(technicalRole.toString());
            saved.putBoolean("observedPassenger", role != null && dev.hae.btt.role.Roles.isPassengerCamp(role));
            saved.putInt("observedAgeSeconds", playerBodyEntity.age / 20);
            return saved;
        }
    }

    @Override
    public void serverTick() {
        var provider = (org.ladysnake.cca.api.v3.component.ComponentProvider) playerBodyEntity;
        var recipients = new java.util.HashSet<java.util.UUID>();
        for (var recipient : provider.getRecipientsForComponentSync()) {
            recipients.add(recipient.getUuid());
            NbtCompound view = viewFor(recipient);
            if (view.equals(sentViews.get(recipient.getUuid()))) continue;
            KEY.syncWith(recipient, provider, (buf, receiver) -> buf.writeNbt(view), this);
            sentViews.put(recipient.getUuid(), view);
        }
        sentViews.keySet().retainAll(recipients);
    }
}
