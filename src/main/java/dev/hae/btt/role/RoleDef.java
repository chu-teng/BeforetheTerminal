package dev.hae.btt.role;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import dev.hae.btt.game.GameEvents;
import dev.hae.btt.mixin.game.KillHookMixin;












public final class RoleDef {

    public interface Kit {
        void give(ServerPlayerEntity player);
    }


    public interface OnKill {
        void accept(ServerPlayerEntity shooter, PlayerEntity victim, Identifier reason, GameWorldComponent gwc);
    }


    public interface OnTick {
        void tick(ServerPlayerEntity player, ServerWorld world, GameWorldComponent gwc);
    }

    private java.util.function.BiConsumer<ServerPlayerEntity, java.util.List<Role>> opening;
    RoleDef opening(java.util.function.BiConsumer<ServerPlayerEntity, java.util.List<Role>> handler) { opening = handler; return this; }
    public void openRound(ServerPlayerEntity player, java.util.List<Role> assigned) {
        if (opening != null) opening.accept(player, assigned);
    }

    public final Role role;
    private Kit kit;
    private java.util.function.Consumer<ServerPlayerEntity> initialize;
    private dev.hae.btt.api.event.BttEvents.Death anyDeath;
    RoleDef anyDeath(dev.hae.btt.api.event.BttEvents.Death handler) { anyDeath = handler; return this; }
    public void observeDeath(ServerPlayerEntity victim, ServerPlayerEntity killer, Identifier reason) {
        if (anyDeath != null) anyDeath.onDeath(victim, killer, reason);
    }

    public interface Targeted {
        void accept(ServerPlayerEntity caster, ServerPlayerEntity target, String guess);
    }
    private Targeted targeted;
    RoleDef targeted(java.util.function.BiConsumer<ServerPlayerEntity, ServerPlayerEntity> handler) {
        return targeted((caster, target, guess) -> handler.accept(caster, target));
    }
    RoleDef targeted(Targeted handler) { targeted = handler; return this; }
    public boolean hasTargeted() { return targeted != null; }
    public void useTargeted(ServerPlayerEntity caster, ServerPlayerEntity target) { useTargeted(caster, target, ""); }
    public void useTargeted(ServerPlayerEntity caster, ServerPlayerEntity target, String guess) { targeted.accept(caster, target, guess); }


    public interface MultiTargeted {
        void accept(ServerPlayerEntity caster, GameWorldComponent game, java.util.List<java.util.UUID> picks);
    }
    private java.util.function.BiPredicate<ServerPlayerEntity, java.util.List<java.util.UUID>> multiInput;
    private MultiTargeted multiTargeted;
    RoleDef multiTargeted(java.util.function.BiPredicate<ServerPlayerEntity, java.util.List<java.util.UUID>> input,
                          MultiTargeted handler) {
        multiInput = input;
        multiTargeted = handler;
        return this;
    }
    public boolean validMultiTargets(ServerPlayerEntity caster, java.util.List<java.util.UUID> picks) {
        return multiInput != null && multiInput.test(caster, picks);
    }
    public boolean hasMultiTargeted() { return multiTargeted != null; }
    public void useMultiTargeted(ServerPlayerEntity caster, GameWorldComponent game, java.util.List<java.util.UUID> picks) {
        if (multiTargeted != null) multiTargeted.accept(caster, game, picks);
    }
    static java.util.function.BiPredicate<ServerPlayerEntity, java.util.List<java.util.UUID>> distinctTargets(int min, int max) {
        return (caster, picks) -> picks.size() >= min && picks.size() <= max
                && new java.util.HashSet<>(picks).size() == picks.size();
    }

    public record OutsiderHost(String type, dev.hae.btt.victory.Endings.Ending ending, int priority, net.minecraft.sound.SoundEvent music) {}
    private OutsiderHost outsiderHost;
    public OutsiderHost outsiderHost() { return outsiderHost; }
    RoleDef outsider(String type, dev.hae.btt.victory.Endings.Ending ending, int priority) {
        return outsider(type, ending, priority, null);
    }
    RoleDef outsider(String type, dev.hae.btt.victory.Endings.Ending ending, int priority, net.minecraft.sound.SoundEvent music) {
        outsiderHost = new OutsiderHost(type, ending, priority, music); return this;
    }
    private java.util.function.BiPredicate<ServerPlayerEntity, ServerPlayerEntity> poisonRecruit;
    RoleDef poisonRecruit(java.util.function.BiPredicate<ServerPlayerEntity, ServerPlayerEntity> handler) {
        poisonRecruit = handler; return this;
    }
    public boolean recruitPoisoned(ServerPlayerEntity source, ServerPlayerEntity victim) {
        return poisonRecruit != null && poisonRecruit.test(source, victim);
    }

    private dev.hae.btt.api.event.BttEvents.Death onDeath;
    private LethalHandler lethal;
    private java.util.function.BiPredicate<ServerPlayerEntity, ServerPlayerEntity> protects;
    private ProtectionHandler protection;
    public interface LethalHandler {
        boolean allow(ServerPlayerEntity victim, PlayerEntity killer, Identifier reason);
    }
    public interface ProtectionHandler {
        void redirect(ServerPlayerEntity guardian, ServerPlayerEntity victim, PlayerEntity killer, Identifier reason);
    }
    RoleDef onDeath(dev.hae.btt.api.event.BttEvents.Death handler) { onDeath = handler; return this; }
    public void dispatchDeath(ServerPlayerEntity victim, ServerPlayerEntity killer, Identifier reason) {
        if (onDeath != null) onDeath.onDeath(victim, killer, reason);
    }
    RoleDef lethal(LethalHandler handler) { lethal = handler; return this; }
    public boolean allowLethal(ServerPlayerEntity victim, PlayerEntity killer, Identifier reason) {
        return lethal == null || lethal.allow(victim, killer, reason);
    }
    RoleDef protection(java.util.function.BiPredicate<ServerPlayerEntity, ServerPlayerEntity> test, ProtectionHandler handler) {
        protects = test; protection = handler; return this;
    }
    public boolean protects(ServerPlayerEntity guardian, ServerPlayerEntity victim) { return protects != null && protects.test(guardian, victim); }
    public void redirectLethal(ServerPlayerEntity guardian, ServerPlayerEntity victim, PlayerEntity killer, Identifier reason) {
        protection.redirect(guardian, victim, killer, reason);
    }
    private java.util.function.Consumer<ServerPlayerEntity> self;
    public boolean hasSelf() { return self != null; }
    public void useSelf(ServerPlayerEntity player) { self.accept(player); }
    RoleDef self(java.util.function.Consumer<ServerPlayerEntity> handler) { self = handler; return this; }

    private java.util.function.BiConsumer<ServerPlayerEntity, String> untargeted;
    public boolean hasUntargeted() { return untargeted != null; }
    public void useUntargeted(ServerPlayerEntity player, String choice) { untargeted.accept(player, choice); }
    RoleDef untargeted(java.util.function.Consumer<ServerPlayerEntity> handler) {
        return untargeted((player, choice) -> handler.accept(player));
    }
    RoleDef untargeted(java.util.function.BiConsumer<ServerPlayerEntity, String> handler) {
        untargeted = handler;
        return this;
    }
    private java.util.function.Predicate<ServerPlayerEntity> killerVictory;
    RoleDef killerVictory(java.util.function.Predicate<ServerPlayerEntity> predicate) {
        killerVictory = predicate;
        return this;
    }
    public boolean killerVictory(ServerPlayerEntity player) {
        return killerVictory != null && killerVictory.test(player);
    }
    private OnKill onKill;
    private OnTick onTick;
    private java.util.function.BiConsumer<ServerPlayerEntity, String> roleChoice;
    private java.util.function.BiPredicate<ServerPlayerEntity, ServerPlayerEntity> beforeTargeted;
    private java.util.function.BiConsumer<ServerPlayerEntity, ServerPlayerEntity> nearby;
    private java.util.function.Predicate<ServerPlayerEntity> nearbyReady;

    public boolean hasNearby() { return nearby != null; }
    public boolean nearbyReady(ServerPlayerEntity player) { return nearbyReady != null && nearbyReady.test(player); }
    public void useNearby(ServerPlayerEntity player, ServerPlayerEntity target) { nearby.accept(player, target); }
    RoleDef nearby(java.util.function.Predicate<ServerPlayerEntity> ready,
                   java.util.function.BiConsumer<ServerPlayerEntity, ServerPlayerEntity> handler) {
        nearbyReady = ready;
        nearby = handler;
        return this;
    }

    public boolean hasRoleChoice() { return roleChoice != null; }
    public boolean chooseRole(ServerPlayerEntity player, String choice) {
        if (roleChoice == null) return false;
        roleChoice.accept(player, choice);
        return true;
    }
    public boolean intercept(ServerPlayerEntity target, ServerPlayerEntity caster) {
        return beforeTargeted != null && beforeTargeted.test(target, caster);
    }
    RoleDef roleChoice(java.util.function.BiConsumer<ServerPlayerEntity, String> handler) {
        roleChoice = handler;
        return this;
    }
    RoleDef beforeTargeted(java.util.function.BiPredicate<ServerPlayerEntity, ServerPlayerEntity> handler) {
        beforeTargeted = handler;
        return this;
    }

    RoleDef(Role role) {
        this.role = role;
    }

    public void dispatchKit(ServerPlayerEntity player) {
        dispatchInitialization(player);
        if (!RoleTransitions.isConverting(player)) dispatchEquipment(player);
    }

    public void dispatchInitialization(ServerPlayerEntity player) {
        if (initialize != null) initialize.accept(player);
    }

    public void dispatchEquipment(ServerPlayerEntity player) {
        if (kit != null) kit.give(player);
    }

    RoleDef initialize(java.util.function.Consumer<ServerPlayerEntity> initialize) {
        this.initialize = initialize;
        return this;
    }

    public void dispatchKill(ServerPlayerEntity shooter, PlayerEntity victim, Identifier reason, GameWorldComponent gwc) {
        if (onKill != null) onKill.accept(shooter, victim, reason, gwc);
    }

    public void dispatchTick(ServerPlayerEntity player, ServerWorld world, GameWorldComponent gwc) {
        if (onTick != null) onTick.tick(player, world, gwc);
    }



    static RoleDef of(Role role) {
        return new RoleDef(role);
    }

    RoleDef kit(Kit kit) {
        this.kit = kit;
        return this;
    }

    RoleDef onKill(OnKill onKill) {
        this.onKill = onKill;
        return this;
    }

    RoleDef onTick(OnTick onTick) {
        this.onTick = onTick;
        return this;
    }
}
